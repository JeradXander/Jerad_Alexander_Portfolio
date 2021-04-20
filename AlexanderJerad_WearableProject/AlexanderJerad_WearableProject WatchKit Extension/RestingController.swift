//
//  RestingController.swift
//  AlexanderJerad_WearableProject WatchKit Extension
//
//  Created by Jerad Alexander on 4/21/20.
//  Copyright © 2020 Jerad Alexander. All rights reserved.
//

import WatchKit
import Foundation
import HealthKit


class RestingController: WKInterfaceController {
    //variables and outlets
    var isAuthorized : Bool = false
    var hrType : HKQuantityType?
    var diastolicType : HKQuantityType?
    var heartRateQuery : HKObserverQuery?
    public let healthStore = HKHealthStore()
    @IBOutlet var hrLabel : WKInterfaceLabel!
    @IBOutlet var manEdit: WKInterfaceTextField!
    
    override func awake(withContext context: Any?) {
        super.awake(withContext: context)
        
        //setting content type for edit filed
        manEdit.setTextContentType(.postalCode)
        
        //is data available
        guard HKHealthStore.isHealthDataAvailable() else {
            return
        }
        
        //getting type
        guard let hrTypeLoad = HKObjectType.quantityType(forIdentifier: .heartRate) else {
            return
        }
        self.hrType = hrTypeLoad
        
        //requesting auth for type
        HKHealthStore().requestAuthorization(toShare: [hrType!], read: [hrType!]) {
            result, error in
            
            if error != nil {
                print(error ?? "")
                return
            }
            
            if !result {
                print("User did not authorize healthkit data")
                return
            }
            
            self.isAuthorized = true
            //subribing to heartbeat changes
            self.subscribeToHeartBeatChanges()
        }
        
        // Configure interface objects here.
    }
    
    override func willActivate() {
        // This method is called when watch view controller is about to be visible to user
        super.willActivate()
    }
    
    override func didDeactivate() {
        // This method is called when watch view controller is no longer visible
        super.didDeactivate()
    }
    public func subscribeToHeartBeatChanges() {
        
        // Creating the sample for the heart rate
        guard let sampleType: HKSampleType =
            HKObjectType.quantityType(forIdentifier: .heartRate) else {
                return
        }
        
        // Creating an observer, so updates are received whenever HealthKit’s
        // heart rate data changes.
        self.heartRateQuery = HKObserverQuery.init(
            sampleType: sampleType,
            predicate: nil) { [weak self] _, _, error in
                guard error == nil else {
                    print("error")
                    return
                }
                
                // When the completion is called, an other query is executed
                // to fetch the latest heart rate
                self?.fetchLatestHeartRateSample(completion: { sample in
                    guard let sample = sample else {
                        return
                    }
                    
                    // The completion in called on a background thread, but we
                    // need to update the UI on the main.
                    DispatchQueue.main.async {
                        
                        // Converting the heart rate to bpm
                        let heartRateUnit = HKUnit(from: "count/min")
                        let heartRate = sample
                            .quantity
                            .doubleValue(for: heartRateUnit)
                        
                        // Updating the UI with the retrieved value
                        self?.hrLabel.setText("\(Int(heartRate))")
                    }
                })
        }
        self.healthStore.execute(heartRateQuery!)
    }
    
    public func fetchLatestHeartRateSample(
        completion: @escaping (_ sample: HKQuantitySample?) -> Void) {
        
        //Create sample type for the heart rate
        guard let sampleType = HKObjectType
            .quantityType(forIdentifier: .heartRate) else {
                completion(nil)
                return
        }
        
        // Predicate for specifiying start and end dates for the query
        let predicate = HKQuery
            .predicateForSamples(
                withStart: Date.distantPast,
                end: Date(),
                options: .strictEndDate)
        
        // Set sorting by date.
        let sortDescriptor = NSSortDescriptor(
            key: HKSampleSortIdentifierStartDate,
            ascending: false)
        
        //Create the query
        let query = HKSampleQuery(
            sampleType: sampleType,
            predicate: predicate,
            limit: Int(HKObjectQueryNoLimit),
            sortDescriptors: [sortDescriptor]) { (_, results, error) in
                
                guard error == nil else {
                    print("Error: \(error!.localizedDescription)")
                    return
                }
                if results!.count > 0{
                completion(results?[0] as? HKQuantitySample)
                }
        }
        //executing query
        self.healthStore.execute(query)
    }
    //for manual press
    @IBAction func manualPressed(_ value: NSString?) {
        //check if user entered a valid num,ber if used the draw to text method
        if value == nil{
        }else{
            
            let isANumber = (value)!.isNumber
            print(isANumber)
            
            if !isANumber{
                //letting user know there is a error
                //alerting user
                DispatchQueue.main.async(execute: {
                    let action1 = WKAlertAction.init(title: "Uh-OH", style:.default) {
                        print("error action")
                        self.manEdit.setText("0")
                    }
                    
                    let alertString = "Heart rate much be a number"
                    
                    self.presentAlert(withTitle: "Error", message: alertString, preferredStyle:.actionSheet, actions: [action1])})
                
            }else{
                //if is authorized and value is number
                if isAuthorized {
                    //converting to double
                    let bpmstring = value! as String
                    let bpmInt = Int(bpmstring)!
                    let bpmDouble = Double(bpmInt)
                    let quantity = HKQuantity(unit: HKUnit(from: "count/min") , doubleValue: bpmDouble)
                    
                    //saving hearrate
                    let heartRate = HKQuantitySample(
                        type: hrType!,
                        quantity: quantity,
                        start: Date(),
                        end: Date()
                    )
                    //healthsotre saving method
                    HKHealthStore().save(heartRate) {
                        result, error in
                        
                        if error != nil {
                            
                            return
                        }
                        
                        //alerting user
                        DispatchQueue.main.async(execute: {
                            let action1 = WKAlertAction.init(title: "Saved", style:.default) {
                                print("Saved action")
                            }
                            let alertString = String(bpmDouble) + " was saved"
                            
                            self.presentAlert(withTitle: "Saved", message: alertString, preferredStyle:.actionSheet, actions: [action1])
                        })
                    }
                }
            }
        }
    }
}

//extension for checking if value is actually a number
extension NSString  {
    var isNumber: Bool {
        return length > 0 && rangeOfCharacter(from: CharacterSet.decimalDigits.inverted).location == NSNotFound
    }
}
