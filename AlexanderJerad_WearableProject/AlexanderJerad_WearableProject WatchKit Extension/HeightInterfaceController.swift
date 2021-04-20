//
//  HeightInterfaceController.swift
//  AlexanderJerad_WearableProject WatchKit Extension
//
//  Created by Jerad Alexander on 4/21/20.
//  Copyright © 2020 Jerad Alexander. All rights reserved.
//

import WatchKit
import Foundation
import HealthKit


class HeightInterfaceController: WKInterfaceController {
    //varialbes and outlets
    @IBOutlet var heightPicker: WKInterfacePicker!
    @IBOutlet var selecedLabel: WKInterfaceLabel!
    var isAuthorized : Bool = false
    public let healthStore = HKHealthStore()
    var bodyHeightType : HKQuantityType?
    var heightValue = 1
    private final let maxHeight = 100
    var heightOptions = [WKPickerItem]()
    
    
    override func awake(withContext context: Any?) {
        super.awake(withContext: context)
        //getting picker options
        for i in 1...maxHeight {
            let item = WKPickerItem()
            item.title = String(i)
            heightOptions.append(item)
        }
        //setting picker items
        heightPicker.setItems(heightOptions)
        
        //if available
        guard HKHealthStore.isHealthDataAvailable() else {
            return
        }
        //type
        guard let quantityType = HKObjectType.quantityType(forIdentifier: .height) else {
            
            return
        }
        bodyHeightType = quantityType
        
        //requestiong auth
        HKHealthStore().requestAuthorization(toShare: [quantityType], read: [quantityType]) {
            result, error in
            
            if error != nil {
                print(error ?? "")
                return
            }
            
            if !result {
                print("User did not authorize healthkit data")
                return
            }
            
            self.isAuthorized = result
            //loading last helper
            self.loadLastHeight()
        }
    }
    
    override func willActivate() {
        // This method is called when watch view controller is about to be visible to user
        super.willActivate()
    }
    
    override func didDeactivate() {
        // This method is called when watch view controller is no longer visible
        super.didDeactivate()
    }
    //loading last helper
    func loadLastHeight() {
        let mostRecentPredicate = HKQuery.predicateForSamples(withStart: Date.distantPast,
                                                              end: Date(),
                                                              options: [])
        let sortDescriptor = NSSortDescriptor(key: HKSampleSortIdentifierStartDate, ascending: false)
        
        let heightType = HKSampleType.quantityType(forIdentifier: .height)!
        
        let bodyHeight = HKSampleQuery.init(sampleType: heightType, predicate: mostRecentPredicate, limit: 1,sortDescriptors: [sortDescriptor], resultsHandler: { (query, results, error) in
            DispatchQueue.main.async(execute: {
                
                if (results?.count)! > 0 {
                    let mostRecent = results?.first as? HKQuantitySample
                    let height = mostRecent?.quantity.doubleValue(for: HKUnit.inch())
                    //setting values for picker and label
                    self.heightValue = Int(height!)
                    self.selecedLabel.setText(String(self.heightValue))
                    
                    self.heightPicker.setSelectedItemIndex(self.heightValue - 1)
                }
            })
        })
        //executing query
        self.healthStore.execute(bodyHeight)
    }
    //if picker is changed
    @IBAction func heightChanged(_ value: Int) {
        heightValue = value + 1
        self.selecedLabel.setText(String(heightValue))
    }
    
    //
    @IBAction func savePressed() {
        let height = Double(heightValue)
        //if authorized
        if isAuthorized {
            
            let quantity = HKQuantity(unit: HKUnit.inch(), doubleValue: height)
            //setting quantity
            let bodyheight = HKQuantitySample(
                type: bodyHeightType!,
                quantity: quantity,
                start: Date(),
                end: Date()
            )
            //saving height
            HKHealthStore().save(bodyheight) {
                result, error in
                
                if error != nil {
                    
                    return
                }
                
                //alerting user
                DispatchQueue.main.async(execute: {
                    let action1 = WKAlertAction.init(title: "Saved", style:.default) {
                        print("Saved action")
                    }
                    let alertString = String(self.heightValue) + " was saved"
                    
                    self.presentAlert(withTitle: "Saved", message: alertString, preferredStyle:.actionSheet, actions: [action1])
                    
                })
            }
        }
    }
}
