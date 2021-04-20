//
//  BPInterfaceController.swift
//  AlexanderJerad_WearableProject WatchKit Extension
//
//  Created by Jerad Alexander on 4/21/20.
//  Copyright © 2020 Jerad Alexander. All rights reserved.
//

import WatchKit
import Foundation
import HealthKit


class BPInterfaceController: WKInterfaceController {
    
    //variables and outlets
    var isAuthorized : Bool = false
    var systolicType : HKQuantityType?
    var diastolicType : HKQuantityType?
    public let healthStore = HKHealthStore()
    @IBOutlet var systolicPicker: WKInterfacePicker!
    @IBOutlet var diastolicPicker: WKInterfacePicker!
    @IBOutlet var bpLabel : WKInterfaceLabel!
    var systolicValue = 1
    var diastolicValue = 1
    var bpOptions = [WKPickerItem]()
    private final let maxBP = 370
    
    override func awake(withContext context: Any?) {
        super.awake(withContext: context)
        
        //setting up pickers
        for i in 1...maxBP {
            let item = WKPickerItem()
            item.title = String(i)
            bpOptions.append(item)
        }
        systolicPicker.setItems(bpOptions)
        diastolicPicker.setItems(bpOptions)
        
        //is data available
        guard HKHealthStore.isHealthDataAvailable() else {
            return
        }
        
        //getting types
        guard let systolicTypeLoad = HKObjectType.quantityType(forIdentifier: .bloodPressureSystolic) else {
            return
        }
        self.systolicType = systolicTypeLoad
        
        guard let diastolicTypeLoad = HKObjectType.quantityType(forIdentifier: .bloodPressureDiastolic) else {
            
            return
        }
        self.diastolicType = diastolicTypeLoad
        
        //requesting auth
        HKHealthStore().requestAuthorization(toShare: [systolicType!, diastolicType!], read: [systolicType!, diastolicType!]) {
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
            self.loadLastBP()
        }
    }
    func loadLastBP() {
        let mostRecentPredicate = HKQuery.predicateForSamples(withStart: Date.distantPast,
                                                              end: Date(),
                                                              options: [])
        let sortDescriptor = NSSortDescriptor(key: HKSampleSortIdentifierStartDate, ascending: false)
        
        let bpType = HKSampleType.correlationType(forIdentifier: .bloodPressure)!
        
        
        let bp = HKSampleQuery.init(sampleType: bpType, predicate: mostRecentPredicate, limit: 1,sortDescriptors: [sortDescriptor], resultsHandler: { (query, results, error) in
            DispatchQueue.main.async(execute: {
                
                //getting and setting values
                if (results?.count)! > 0 {
                    let mostRecent = results?.first as? HKCorrelation
                    let sys = mostRecent?.objects(for: self.systolicType!).first as? HKQuantitySample
                    let dias = mostRecent?.objects(for: self.diastolicType!).first as?HKQuantitySample
                    self.systolicValue = Int(sys!.quantity.doubleValue(for: HKUnit.millimeterOfMercury()))
                    self.diastolicValue = Int(dias!.quantity.doubleValue(for: HKUnit.millimeterOfMercury()))
                    
                    self.systolicPicker.setSelectedItemIndex(self.systolicValue - 1)
                    self.diastolicPicker.setSelectedItemIndex(self.diastolicValue - 1)
                }
            })
        })
        self.healthStore.execute(bp)
    }
    
    override func willActivate() {
        // This method is called when watch view controller is about to be visible to user
        super.willActivate()
    }
    
    override func didDeactivate() {
        // This method is called when watch view controller is no longer visible
        super.didDeactivate()
    }
    //picker changes
    @IBAction func systolicDidChange(_ value: Int) {
        systolicValue = Int(bpOptions[value].title!)!
        bpLabel.setText(String(self.systolicValue) + "/" + String(self.diastolicValue))
    }
    
    @IBAction func diastolicDidChange(_ value: Int) {
        diastolicValue = Int(bpOptions[value].title!)!
        bpLabel.setText(String(self.systolicValue) + "/" + String(self.diastolicValue))
    }
    
    //save function
    @IBAction func save() {
        if isAuthorized {
            let systolicQuantity = HKQuantity(unit: HKUnit.millimeterOfMercury(), doubleValue: Double(systolicValue))
            let diastolicQuantity = HKQuantity(unit: HKUnit.millimeterOfMercury(), doubleValue: Double(diastolicValue))
            
            let systolicPressure = HKQuantitySample(type: systolicType!, quantity: systolicQuantity, start: Date(), end: Date())
            let diastolicPressure = HKQuantitySample(type: diastolicType!, quantity: diastolicQuantity, start: Date(), end: Date())
            
            let bpCorrelationType = HKCorrelationType.correlationType(forIdentifier: .bloodPressure)
            let bpCorrelation = Set(arrayLiteral: systolicPressure, diastolicPressure)
            let bloodPressure = HKCorrelation(type: bpCorrelationType!, start: Date(), end: Date(), objects: bpCorrelation)
            //saving blood pressure
            HKHealthStore().save(bloodPressure) {
                result, error in
                
                if error != nil {
                    
                    return
                }
                DispatchQueue.main.async(execute: {
                    //alerting the user
                                   let action1 = WKAlertAction.init(title: "Saved", style:.default) {
                                       print("Saved action")
                                   }
                                   let alertString = String(self.systolicValue) + "/" + String(self.diastolicValue) + " was saved"
                                   
                                   self.presentAlert(withTitle: "Saved", message: alertString, preferredStyle:.actionSheet, actions: [action1])
                })
            }
        }
    }
}
