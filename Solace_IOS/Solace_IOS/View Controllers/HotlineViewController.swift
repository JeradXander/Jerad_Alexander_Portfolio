//
//  HotlineViewController.swift
//  Solace_IOS
//
//  Created by Zakarias McDanal on 9/14/20.
//  Copyright © 2020 Jerad Alexander. All rights reserved.
//

import UIKit

class HotlineViewController: UIViewController {

    @IBOutlet weak var hotlineImgBtn: UIImageView!
    @IBOutlet weak var goBackBtn: UIButton!
    @IBOutlet weak var callingInLbl: UILabel!
    @IBOutlet weak var countdownLbl: UILabel!
    
    var tapped = false
    var timer: Timer?
    var count = 6
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.addBackground()
        // Do any additional setup after loading the view.
        
        
        // Hotline button tap gesture assigned
        let tap = UITapGestureRecognizer(target: self, action: #selector(HotlineViewController.imageTapped))
        hotlineImgBtn.addGestureRecognizer(tap)
        hotlineImgBtn.isUserInteractionEnabled = true
    }
    
    // Hotline button was clicked logic
    @objc func imageTapped() {
        // if the tapped view is a UIImageView then set it to imageview
       
            if (tapped) {
                tapped = false
                callingInLbl.isHidden = true
                countdownLbl.isHidden = true
                timer!.invalidate()
                count = 6;
                print(count)
            } else {
                fireTimer()
                tapped = true
                callingInLbl.isHidden = false
                countdownLbl.isHidden = false
                print(count)
            }
            //Here you can initiate your new ViewController

        
    }
    
    // Countdown Timer logic
    @objc func fireTimer() {
        timer = Timer.scheduledTimer(timeInterval: 1.0, target: self, selector: #selector(fireTimer), userInfo: nil, repeats: false)
        if(count > 0) {
            count -= 1
            countdownLbl.text = String(count)
        } else {
            dialNumber(number: "18002738255")
            timer!.invalidate()
        }
    }
    
    // Dials number passed in from fireTimer()
    func dialNumber(number : String) {

     if let phoneCallURL = URL(string: "tel://\(number)") {

       let application:UIApplication = UIApplication.shared
       if (application.canOpenURL(phoneCallURL)) {
           application.open(phoneCallURL, options: [:], completionHandler: nil)
       }
     }
    }
    

    // Logic to work the "go back" button
    @IBAction func goBackPressed(_ sender: UIButton) {
        self.dismiss(animated: true, completion: nil)
    }
    

}
