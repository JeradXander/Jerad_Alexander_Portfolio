//
//  HealthViewController.swift
//  AlexanderJerad_WearableProject
//
//  Created by Jerad Alexander on 4/21/20.
//  Copyright © 2020 Jerad Alexander. All rights reserved.
//

import UIKit
import HealthKit

class HealthViewController: UIViewController {
    
    //variables and outlets
    public let healthStore = HKHealthStore()
    @IBOutlet var heightValue: UILabel!
    @IBOutlet var weightValue: UILabel!
    @IBOutlet var restinghrValue: UILabel!
    @IBOutlet var bpValue: UILabel!
    
    var isAuthorized : Bool!
    var quantityTypes = [HKQuantityType]()
    var systolicType : HKQuantityType?
    var diastolicType : HKQuantityType?
    var systolicValue = 1
    var diastolicValue = 1
    
    override func viewDidLoad() {
        super.viewDidLoad()
        //is data available
        guard HKHealthStore.isHealthDataAvailable() else {
            print("this device does not have a health kit")
            return
        }
        //getting all the quantitytypes for my healthkit reading
        guard let massType = HKObjectType.quantityType(forIdentifier: .bodyMass), let heightType = HKObjectType.quantityType(forIdentifier: .height), let hrType = HKObjectType.quantityType(forIdentifier: .heartRate), let bpsType = HKObjectType.quantityType(forIdentifier: .bloodPressureSystolic), let bpdType = HKObjectType.quantityType(forIdentifier: .bloodPressureDiastolic)else {
            return
        }
        //saving types to array
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
            //displaying health methods
            self.displayhealth()
        }
        
        
    }
    //helper method for displaying health data
    func displayhealth(){
        
        if self.isAuthorized == true{
            
            //predicate
            let mostRecentPredicate = HKQuery.predicateForSamples(withStart: Date.distantPast,
                                                                  end: Date(),
                                                                  options: [])
            //sorting descriptor
            let sortDescriptor = NSSortDescriptor(key: HKSampleSortIdentifierStartDate, ascending: false)
            
            //query for body weight
            let bodyweight = HKSampleQuery.init(sampleType: quantityTypes[0], predicate: mostRecentPredicate, limit: 1,sortDescriptors: [sortDescriptor], resultsHandler: { (query, results, error) in
                DispatchQueue.main.async(execute: {
                    
                    if (results?.count)! > 0 {
                        let mostRecent = results?.first as? HKQuantitySample
                        let weight = mostRecent?.quantity.doubleValue(for: HKUnit.pound())
                        self.weightValue.text = "\(String(weight!)) Lbs."
                    }
                })
            })
            //query for body height
            let bodyHeight = HKSampleQuery.init(sampleType: quantityTypes[1], predicate: mostRecentPredicate, limit: 1,sortDescriptors: [sortDescriptor], resultsHandler: { (query, results, error) in
                DispatchQueue.main.async(execute: {
                    
                    if (results?.count)! > 0 {
                        let mostRecent = results?.first as? HKQuantitySample
                        let height = mostRecent?.quantity.doubleValue(for: HKUnit.inch())
                        self.heightValue.text = "\(String(height!.rounded())) In."
                    }
                })
            })
            //query for heart rate
            let HeartRate = HKSampleQuery.init(sampleType: quantityTypes[2], predicate: mostRecentPredicate, limit: 1,sortDescriptors: [sortDescriptor], resultsHandler: { (query, results, error) in
                DispatchQueue.main.async(execute: {
                    
                    if (results?.count)! > 0 {
                        let mostRecent = results?.first as? HKQuantitySample
                        let hr = mostRecent?.quantity.doubleValue(for: HKUnit(from: "count/min"))
                        self.restinghrValue.text = "\(String(Int(hr!))) BPM"
                    }
                })
            })
            //executing queries
            loadLastBP()
            self.healthStore.execute(bodyHeight)
            self.healthStore.execute(bodyweight)
            self.healthStore.execute(HeartRate)
        }
    }
    
    func loadLastBP() {
        
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
                        
                        let dias = mostRecent?.objects(for: self.diastolicType!).first as? HKQuantitySample
                        self.systolicValue = Int(sys!.quantity.doubleValue(for: HKUnit.millimeterOfMercury()))
                        self.diastolicValue = Int(dias!.quantity.doubleValue(for: HKUnit.millimeterOfMercury()))
                        
                        self.bpValue.text = "\(self.systolicValue)/\(self.diastolicValue)"
                        
                    }
                })
            })
            self.healthStore.execute(bp)
        }
    }
}
