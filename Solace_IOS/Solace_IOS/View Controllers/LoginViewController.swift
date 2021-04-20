//
//  LoginViewController.swift
//  Solace_IOS
//
//  Created by Jerad Alexander on 9/10/20.
//  Copyright © 2020 Jerad Alexander. All rights reserved.
//

import UIKit
import Foundation
import FirebaseAuth



class LoginViewController: UIViewController , UITextFieldDelegate{
    
    //outlets
    @IBOutlet var emailField: UITextField!
    @IBOutlet var passField: UITextField!
    @IBOutlet var LoginButton: UIButton!
    @IBOutlet var registerButton: UIButton!
    
    //service
    let alertService = AlertService()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        //delagates
        self.emailField.delegate = self
        self.passField.delegate = self
        
        //Looks for single or multiple taps.
        let tap: UITapGestureRecognizer = UITapGestureRecognizer(target: self, action: #selector(UIInputViewController.dismissKeyboard))
        
        //Uncomment the line below if you want the tap not not interfere and cancel other interactions.
        tap.cancelsTouchesInView = true
        
        view.addGestureRecognizer(tap)
        //keyboard notifiers
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow), name: UIResponder.keyboardWillShowNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillHide), name: UIResponder.keyboardWillHideNotification, object: nil)
        
        //ui setup
        self.view.addBackground()
        LoginButton.layer.cornerRadius = 15.0
        LoginButton.layer.borderWidth = 3.0
        LoginButton.layer.borderColor = UIColor(named: "SolaceYellow")?.cgColor
        LoginButton.titleEdgeInsets = UIEdgeInsets(top: 5, left: 5, bottom: 5, right: 5)
        
        
        //feild corner radius
        emailField.layer.cornerRadius = 15.0
        passField.layer.cornerRadius = 15.0
        //border colors
        emailField.layer.borderColor = UIColor(named: "SolaceYellow")?.cgColor
        passField.layer.borderColor = UIColor(named: "SolaceYellow")?.cgColor
        
        //border setup fro feilds
        emailField.layer.borderWidth = 3.0
        passField.layer.borderWidth = 3.0
        emailField.layer.masksToBounds = true
        emailField.layer.shadowRadius = 3.0
        emailField.layer.shadowColor = UIColor(named: "SolaceYellow")?.cgColor
        emailField.layer.shadowOpacity = 1.0
        passField.layer.masksToBounds = true
        passField.layer.shadowRadius = 3.0
        passField.layer.shadowColor = UIColor(named: "SolaceYellow")?.cgColor
        passField.layer.shadowOpacity = 1.0
        // Do any additional setup after loading the view.
    }
    
    
    
    @IBAction func buttonpress(_ sender: Any) {
        // what button or label is pressed
        switch (sender as AnyObject).tag {
        case 0:
            //password reset logic
            let alert = UIAlertController(title: "What Email should we send your reset?", message: nil, preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
            
            alert.addTextField(configurationHandler: { textField in
                textField.placeholder = "email"
                textField.textContentType = .emailAddress
            })
            
            alert.addAction(UIAlertAction(title: "OK", style: .default, handler: { action in
                //sending password reset
                if let email = alert.textFields?.first?.text {
                    Auth.auth().sendPasswordReset(withEmail: email) { error in
                        // Your code here
                        if error != nil {
                            Toast.show(message: "Error sending email please try again", controller: self)
                        } else {
                            Toast.show(message: "Email Sent to " + email, controller: self)
                        }
                    }
                    
                }
            }))
            
            alert.view.backgroundColor = UIColor(named: "SolaceDarkGreen")
            self.present(alert, animated: true)
            
            break
        case 1:
            //login selected
            let email : String = emailField.text!
            let password : String = passField.text!
            
            //user input validation
            if email.elementsEqual("") || password.elementsEqual(""){
                Toast.show(message: "Email or password fields are empty", controller: self)
            }
            else if !isValidEmail(email) {
                Toast.show(message: "Must be a valid email", controller: self)
            }
            else {
                //loggin in alert
                let alertvs = alertService.alertLogging()
                present(alertvs, animated: true)
                
                //signing in
                Auth.auth().signIn(withEmail: email, password: password) { (authResult, error) in
                    if let error = error as NSError? {
                        switch AuthErrorCode(rawValue: error.code) {
                        case .operationNotAllowed:
                            Toast.show(message: error.description, controller: self)
                            alertvs.dismiss(animated: true, completion: nil)
                        case .userDisabled:
                            // Error: The user account has been disabled by an administrator.
                            Toast.show(message: "Account Disabled by Admin", controller: self)
                            alertvs.dismiss(animated: true, completion: nil)
                        case .wrongPassword:
                            // Error: The password is invalid or the user does not have a password.
                            Toast.show(message: "Wrong Password", controller: self)
                            alertvs.dismiss(animated: true, completion: nil)
                        case .invalidEmail:
                            // Error: Indicates the email address is malformed.
                            Toast.show(message: "Invalid Email", controller: self)
                            alertvs.dismiss(animated: true, completion: nil)
                        default:
                            print("Error: \(error.localizedDescription)")
                            alertvs.dismiss(animated: true, completion: nil)
                        }
                    } else {
                        print("User signs in successfully")
                        alertvs.dismiss(animated: false, completion: {
                            //signed in success and going to dashboard
                            if let tabViewController = self.storyboard?.instantiateViewController(withIdentifier: "tabBar") as? TabBarVC {
                                self.present(tabViewController, animated: true, completion: nil)
                            }
                        })
                    }
                }
            }
            break
        case 2:
            
            break
        default:
            print("Shouldn't happen")
            break
        }
    }
    
    //email validation
    func isValidEmail(_ email: String) -> Bool {
        let emailRegEx = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}"
        
        let emailPred = NSPredicate(format:"SELF MATCHES %@", emailRegEx)
        return emailPred.evaluate(with: email)
    }
    
    
    //moving feilds up with keyboard
    @objc func keyboardWillShow(notification: NSNotification) {
        if ((notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue)?.cgRectValue) != nil {
            if self.view.frame.origin.y == 0 {
                self.view.frame.origin.y -= 200
            }
        }
    }
    
    //hiding keyboard
    @objc func keyboardWillHide(notification: NSNotification) {
        if self.view.frame.origin.y != 0 {
            self.view.frame.origin.y = 0
        }
    }
    
    //Calls this function when the tap is recognized.
    @objc func dismissKeyboard() {
        //Causes the view (or one of its embedded text fields) to resign the first responder status.
        view.endEditing(true)
    }
    
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        //switching to next responder
        self.switchBasedNextTextField(textField)
        return true
    }
    
    //to next feild delagate
    private func switchBasedNextTextField(_ textField: UITextField) {
        
        switch textField {
        case self.emailField:
            self.passField.becomeFirstResponder()
        case self.passField:
            self.passField.resignFirstResponder()
        default:
            self.emailField.resignFirstResponder()
        }
    }
}
