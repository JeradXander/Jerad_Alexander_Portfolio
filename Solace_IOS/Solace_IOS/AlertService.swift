//
//  AlertService.swift
//  Solace_IOS
//
//  Created by Jerad Alexander on 9/11/20.
//  Copyright © 2020 Jerad Alexander. All rights reserved.
//

import Foundation
import UIKit

class AlertService{
  
    func alertLogging()-> LoggingInAlertViewController{
        let storyboard = UIStoryboard(name: "Alerts", bundle: .main)
        
        let alertVC = storyboard.instantiateViewController(withIdentifier: "Logging") as! LoggingInAlertViewController
        
        return alertVC
    }
    
    func alertLoading()-> LoadingAlertViewController{
         let storyboard = UIStoryboard(name: "Alerts", bundle: .main)
         
         let alertVC = storyboard.instantiateViewController(withIdentifier: "Loading") as! LoadingAlertViewController
         
         return alertVC
     }
}
