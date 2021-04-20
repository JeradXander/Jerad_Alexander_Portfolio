//
//  InterfaceController.swift
//  AlexanderJerad_WearableProject WatchKit Extension
//
//  Created by Jerad Alexander on 4/21/20.
//  Copyright © 2020 Jerad Alexander. All rights reserved.
//

import WatchKit
import Foundation
import HealthKit


class InterfaceController: WKInterfaceController {
    
    //values and outlests
    var isAuthorized : Bool!
    var quantityTypes = [HKQuantityType]()
    @IBOutlet var heightButton: WKInterfaceButton!
    @IBOutlet var WeightButton: WKInterfaceButton!
    @IBOutlet var BPButton: WKInterfaceButton!
    
    override func awake(withContext context: Any?) {
        super.awake(withContext: context)
        //is data available
        guard HKHealthStore.isHealthDataAvailable() else {
            print("this device does not have a health kit")
            return
        }
        //
        guard let massType = HKObjectType.quantityType(forIdentifier: .bodyMass), let heightType = HKObjectType.quantityType(forIdentifier: .height), let hrType = HKObjectType.quantityType(forIdentifier: .restingHeartRate), let bpsType = HKObjectType.quantityType(forIdentifier: .bloodPressureSystolic), let bpdType = HKObjectType.quantityType(forIdentifier: .bloodPressureDiastolic)else {
            return
        }
        
        quantityTypes = [massType, heightType, hrType,bpsType,bpdType]
        
        //requestion authorization
        HKHealthStore().requestAuthorization(toShare: [massType,heightType,hrType,bpdType,bpsType], read: [massType,heightType,hrType,bpdType,bpsType]) {
            result, error in
            
            if error != nil {
                print(error ?? "error")
                return
            }
            
            if !result {
                print("User did not authorize healthkit data")
                return
            }
            print(result)
            self.isAuthorized = result
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
    //button presses to push to correct controller
    @IBAction func heightPressed() {
        pushController(withName: "heightController", context: nil)
    }
    //button presses to push to correct controller
    @IBAction func wieghtPressed() {
        pushController(withName: "weightController", context: nil)
    }
    //button presses to push to correct controller
    @IBAction func bpPressed() {
        pushController(withName: "BPController", context: nil)
    }
    //button presses to push to correct controller
    @IBAction func restingPressed() {
        pushController(withName: "RestingController", context: nil)
    }
}
