//
//  ViewController.swift
//  AlexanderJerad_ImGood
//
//  Created by Jerad Alexander on 12/7/19.
//  Copyright © 2019 Jerad Alexander. All rights reserved.
//

import UIKit
import FirebaseUI
import FirebaseDatabase

class ViewController: UIViewController {
    
    //MARK: - outlets and variables
    @IBOutlet weak var logButton: UIButton!
    var name = ""
    var id = ""
    var email = ""
    //colors to send in prepare
    var greenColor = UIColor(red: 52/255, green: 199/255, blue: 89/255, alpha: 0.60)
    var redColor  = UIColor(red: 255/255, green: 59/255, blue: 48/255, alpha: 0.70)
    
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        logButton.layer.cornerRadius = 5
    }
    //MARK: - log in action
    @IBAction func LogInTapped(_ sender: UIButton)
    {
        //calling default authorization ui from firebase
        let authUI = FUIAuth.defaultAuthUI()
        
        guard authUI != nil
            else
        {
            return
        }
        //setting delegate
        authUI?.delegate = self
        //list of ways the user can sign in/up
        authUI?.providers = [FUIFacebookAuth(), FUIGoogleAuth(),FUIEmailAuth()] 
        //custom controller
        let authViewController = SignInViewController(authUI: authUI!)
        
        let navSign = UINavigationController(rootViewController: authViewController)
        
        
        //presenting login controller
        present(navSign, animated: true, completion: nil)
    }
    
    //MARK: - return to sign in action for sign out segue
    @IBAction func returnToSignIn(segue: UIStoryboardSegue)
    {
    }
}
//MARK: - Extension
extension ViewController: FUIAuthDelegate
{
    //MARK: - funcion for did sign in
    func authUI(_ authUI: FUIAuth, didSignInWith authDataResult: AuthDataResult?, error: Error?) {
        
        //checking again error
        
        guard error == nil
            else
        {
            print(error.debugDescription)
            return
        }
        
        //getting user information
        name = (authDataResult?.user.displayName)!
        id = (authDataResult?.user.uid)!
        email = (authDataResult?.user.email)!
        
        //setting initial user
        let initialUser = ["fullName": name, "email" : email]
        Database.database().reference().child("Users").child(id).updateChildValues(initialUser)
        
        DispatchQueue.main.async
            {
                //performing segue to home controller
                self.performSegue(withIdentifier: "Welcome", sender: self)
        }
    }
    
    //MARK: - prepare for segue Function
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        //conditional for segue identifier
        if segue.identifier == "Welcome"
        {
            
            let destinationNavigationController = segue.destination as! UINavigationController
            
            //sending data to destination controller
            if let homeView = destinationNavigationController.topViewController as? HomeViewController
            {
                homeView.userName = name
                homeView.id = id
                homeView.email = email
                homeView.greenBackColor = greenColor
                homeView.redBackColor = redColor
            }
        }
    }
}



