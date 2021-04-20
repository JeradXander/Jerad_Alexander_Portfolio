//
//  WeightInterfaceController.swift
//  AlexanderJerad_WearableProject WatchKit Extension
//
//  Created by Jerad Alexander on 4/21/20.
//  Copyright © 2020 Jerad Alexander. All rights reserved.
//

import WatchKit
import Foundation
import HealthKit


class WeightInterfaceController: WKInterfaceController {
    
    //variables and outlets
    @IBOutlet var weightPicker: WKInterfacePicker!
    @IBOutlet var selecedLabel: WKInterfaceLabel!
    var isAuthorized : Bool = false
    public let healthStore = HKHealthStore()
    var bodyWeightType : HKQuantityType?
    var weightValue = 1
    private final let maxWeight = 500
    var wightOption = [WKPickerItem]()
    
    
    override func awake(withContext context: Any?) {
        super.awake(withContext: context)
        //setting up picker 
        for i in 1...maxWeight {
            let item = WKPickerItem()
            item.title = String(i)
            wightOption.append(item)
        }
        weightPicker.setItems(wightOption)
        
        guard HKHealthStore.isHealthDataAvailable() else {
            return
        }
        
        guard let quantityType = HKObjectType.quantityType(forIdentifier: .bodyMass) else {
            
            return
        }
        bodyWeightType = quantityType
        
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
            //loading weight
            self.loadLastWeight()
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
    
    func loadLastWeight() {
        let mostRecentPredicate = HKQuery.predicateForSamples(withStart: Date.distantPast,
                                                              end: Date(),
                                                              options: [])
        let sortDescriptor = NSSortDescriptor(key: HKSampleSortIdentifierStartDate, ascending: false)
        
        let weightType = HKSampleType.quantityType(forIdentifier: .bodyMass)!
        
        //query
        let bodyWeight = HKSampleQuery.init(sampleType: weightType, predicate: mostRecentPredicate, limit: 1,sortDescriptors: [sortDescriptor], resultsHandler: { (query, results, error) in
            DispatchQueue.main.async(execute: {
                
                if (results?.count)! > 0 {
                    let mostRecent = results?.first as? HKQuantitySample
                    let weight = mostRecent?.quantity.doubleValue(for: HKUnit.pound())
                    
                    //setting values
                    self.weightValue = Int(weight!)
                    self.selecedLabel.setText(String(self.weightValue))
                    self.weightPicker.setSelectedItemIndex(self.weightValue - 1)
                }
            })
        })
        self.healthStore.execute(bodyWeight)
    }
    //picker value changed
    @IBAction func weightChanged(_ value: Int) {
        weightValue = value + 1
        self.selecedLabel.setText(String(weightValue))
    }
    
    //saving weight
    @IBAction func savePressed() {
        let weight = Double(weightValue)
        //if auth
        if isAuthorized {
            
            let quantity = HKQuantity(unit: HKUnit.pound(), doubleValue: weight)
            //quanity
            let bodyweight = HKQuantitySample(
                type: bodyWeightType!,
                quantity: quantity,
                start: Date(),
                end: Date()
            )
            //saving weight
            HKHealthStore().save(bodyweight) {
                result, error in
                
                if error != nil {
                    
                    return
                }
                //alerting user
                DispatchQueue.main.async(execute: {
                    
                    let action1 = WKAlertAction.init(title: "Saved", style:.default) {
                        print("Saved action")
                    }
                    let alertString = String(self.weightValue) + " was saved"
                    self.presentAlert(withTitle: "Saved", message: alertString, preferredStyle:.actionSheet, actions: [action1])
                })
            }
        }
    }
}
