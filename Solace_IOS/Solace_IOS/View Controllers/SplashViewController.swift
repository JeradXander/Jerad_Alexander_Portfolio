//
//  SplashViewController.swift
//  Solace_IOS
//
//  Created by Jerad Alexander on 9/7/20.
//  Copyright © 2020 Jerad Alexander. All rights reserved.
//

import UIKit
import FirebaseAuth
import Lottie

class SplashViewController: UIViewController {
    
    //variables
    var handle: AuthStateDidChangeListenerHandle?
    var firebaseAuth: Auth!
    
    //outlets
    @IBOutlet var splashAnimation: AnimationView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        //background and animation setup
        self.view.addBackground()
        splashAnimation.loopMode = .playOnce
        splashAnimation.animationSpeed = 1.0
        splashAnimation.play()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        //logic to open next conteoller based on login status
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.75) {
            if Auth.auth().currentUser != nil
            {
                //logged in
                self.performSegue(withIdentifier: "toDashboard", sender: nil)
            }
            else{
                //not logged in
                self.performSegue(withIdentifier: "toLogin", sender: nil)
            }
        }
    }
    
    override func viewWillAppear(_ animated: Bool) {
        handle = Auth.auth().addStateDidChangeListener { (auth, user) in
        }
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        Auth.auth().removeStateDidChangeListener(handle!)
    }
}
