//
//  LoadingAertViewController.swift
//  Solace_IOS
//
//  Created by Jerad Alexander on 9/12/20.
//  Copyright © 2020 Jerad Alexander. All rights reserved.
//

import UIKit
import Lottie

class LoadingAlertViewController: UIViewController {
    
    //outlets
    @IBOutlet var splashAnim: AnimationView!
    @IBOutlet var alertLabel: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        //animation setup
        splashAnim.loopMode = .repeat(2)
        splashAnim.animationSpeed = 0.8
        splashAnim.play()
    }
}
