//
//  OnBoardOneViewController.swift
//  Solace_IOS
//
//  Created by Jerad Alexander on 9/12/20.
//  Copyright © 2020 Jerad Alexander. All rights reserved.
//

import UIKit

class OnBoardOneViewController: UIViewController, UITextFieldDelegate {
    
    //variables
    var email = ""
    var password = ""
    
    //Oulets
    @IBOutlet var ContinueButton: UIButton!
    @IBOutlet var emailField: UITextField!
    @IBOutlet var passField: UITextField!
    @IBOutlet var conPassField: UITextField!
    @IBOutlet var toLoginButton: UIButton!
    
    //service
    let alertService = AlertService()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        //text delegates
        self.emailField.delegate = self
        self.passField.delegate = self
        self.conPassField.delegate = self
        
        //Looks for single or multiple taps.
        let tap: UITapGestureRecognizer = UITapGestureRecognizer(target: self, action: #selector(UIInputViewController.dismissKeyboard))
        
        //Uncomment the line below if you want the tap not not interfere and cancel other interactions.
        tap.cancelsTouchesInView = false
        
        view.addGestureRecognizer(tap)
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow), name: UIResponder.keyboardWillShowNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillHide), name: UIResponder.keyboardWillHideNotification, object: nil)
        
        //adding background
        self.view.addBackground()
        
        //button setuop
        ContinueButton.layer.cornerRadius = 15.0
        ContinueButton.layer.borderWidth = 3.0
        ContinueButton.layer.borderColor = UIColor(named: "SolaceYellow")?.cgColor
        ContinueButton.titleEdgeInsets = UIEdgeInsets(top: 5, left: 5, bottom: 5, right: 5)
        
        
        //feild corner radius
        emailField.layer.cornerRadius = 15.0
        passField.layer.cornerRadius = 15.0
        conPassField.layer.cornerRadius = 15.0
        //border colors
        emailField.layer.borderColor = UIColor(named: "SolaceYellow")?.cgColor
        passField.layer.borderColor = UIColor(named: "SolaceYellow")?.cgColor
        conPassField.layer.borderColor = UIColor(named: "SolaceYellow")?.cgColor
        
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
        
        conPassField.layer.borderWidth = 3.0
        conPassField.layer.borderWidth = 3.0
        conPassField.layer.masksToBounds = true
        conPassField.layer.shadowRadius = 3.0
        conPassField.layer.shadowColor = UIColor(named: "SolaceYellow")?.cgColor
        conPassField.layer.shadowOpacity = 1.0
        // Do any additional setup after loading the view.
        // Do any additional setup after loading the view.
    }
    
    
    
    @IBAction func ContinuePressed(_ sender: UIButton) {
        //continue pressed getting input validation
        email  = emailField.text!
        password = passField.text!
        let conPass : String = conPassField.text!
        
        if email.elementsEqual("") || password.elementsEqual(""){
            Toast.show(message: "Email or password fields are empty", controller: self)
        }
        else if !isValidEmail(email) {
            Toast.show(message: "Must be a valid email", controller: self)
        }
        else if password != conPass {
            Toast.show(message: "Password Fields Do Not Match", controller: self)
        }
        else {
            //going to next onboard controller
            performSegue(withIdentifier: "toOnBoard2", sender: nil)
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
                self.view.frame.origin.y -= 120
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
    
    //return key pressed
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        //switching to next responder
        self.switchBasedNextTextField(textField)
        return true
    }
    
    //next feild delagate
    private func switchBasedNextTextField(_ textField: UITextField) {
        switch textField {
        
        case self.emailField:
            self.passField.becomeFirstResponder()
        case self.passField:
            self.conPassField.becomeFirstResponder()
        case self.conPassField:
            self.conPassField.resignFirstResponder()
        default:
            self.emailField.resignFirstResponder()
        }
    }
    
    //prepare for segue
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        
        if segue.identifier == "toLogin"{
            
        }else{
            let onBoard2 : OnBoardTwoViewController = segue.destination as! OnBoardTwoViewController
            //sending values to second view
            onBoard2.email = email
            onBoard2.password = password
        }
    }
}
