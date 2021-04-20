//
//  DisplayProfileViewController.swift
//  Solace_IOS
//
//  Created by Zakarias McDanal on 9/21/20.
//  Copyright © 2020 Jerad Alexander. All rights reserved.
//

import UIKit
import Firebase

class DisplayProfileViewController: UIViewController {
    
    //outlets
    @IBOutlet var allViews: [UIView]!
    @IBOutlet weak var profilePic: UIImageView!
    @IBOutlet weak var usernameLbl: UILabel!
    @IBOutlet weak var karmaLbl: UILabel!
    @IBOutlet weak var hardshipLbl: UILabel!
    @IBOutlet var mainView: UIView!
    
    //variables
    var image: UIImage!
    var username: String = ""
    var hardship: String!
    var displayuid: String!
    

    override func viewDidLoad() {
        super.viewDidLoad()

        //ui setup
        view?.backgroundColor = UIColor(white: 1, alpha: 0.1)
        mainView.layer.cornerRadius = 10.0
        mainView.layer.borderWidth = 3.0
        mainView.layer.borderColor = UIColor(named: "SolaceYellow")?.cgColor
        
        // Do any additional setup after loading the view.
        
        for view in allViews {
            view.layer.cornerRadius = 10
        }

        userInfoListener()
        
    }
    
// Dismiss the dialog box if close button was clicked
    @IBAction func closeBtnClicked(_ sender: UIButton) {
        self.dismiss(animated: true, completion: nil)
    }
    
    func userInfoListener(){
        //setting image as circle
        self.profilePic.maskCircle(anyImage: self.image)
        
        //username
        usernameLbl.text = self.username
        
        //hardship
        hardshipLbl.text = hardship
        
        
        Database.database().reference(withPath: "Users").child(displayuid).observeSingleEvent(of: .value) { (snapshot) in
            // print(snapshot)
            if let p = snapshot.value as? [String: Any] {
                
                var karma = "Karma Level: 0"
                
                if let kar = p["karma"] as? Int {
                    karma = kar.description
                }
                
                self.karmaLbl.text = "Karma Level: " + karma.description
              
            }
        }
    }
}
