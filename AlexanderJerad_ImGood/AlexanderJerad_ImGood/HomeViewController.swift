//
//  HomeViewController.swift
//  AlexanderJerad_ImGood
//
//  Created by Jerad Alexander on 12/8/19.
//  Copyright © 2019 Jerad Alexander. All rights reserved.
//

import UIKit
import Firebase
import FirebaseDatabase
import FirebaseUI
import CoreLocation

class HomeViewController: UIViewController , FUIAuthDelegate, CLLocationManagerDelegate, UITextFieldDelegate{
    
    //MARK: - controllers Outlets
    @IBOutlet weak var navBar: UINavigationItem!
    @IBOutlet weak var ImGoodStatus: UIImageView!
    @IBOutlet weak var backGroundView: UIView!
    @IBOutlet weak var mapImage: UIImageView!
    @IBOutlet weak var friendsImage: UIImageView!
    @IBOutlet weak var textView: UITextView!
    @IBOutlet weak var onboardingView: UIView!
    @IBOutlet weak var textField: UITextField!
    @IBOutlet weak var phoneLabel: UILabel!
    
    //location manager variable
    let locationManager = CLLocationManager()
    
    //Date formatter
    let dateFormatter = DateFormatter()
    
    //basic variables to hold data
    var userName = ""
    var id = ""
    var email = ""
    var isOk : Bool = false
    var dateNow = Date()
    let rootRef = Database.database().reference()
    let uid = Auth.auth().currentUser?.uid
    var greenBackColor = UIColor()
    var redBackColor = UIColor()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        //set up done button for numbers pad
        donenumberTextPad()
        
        //datetime for right now
        dateNow = Date()
        
        //tap gestures for imageviews
        let tapGestureRecognizerMap = UITapGestureRecognizer(target: self, action: #selector(imageTapped(tapGestureRecognizer:)))
        mapImage.isUserInteractionEnabled = true
        mapImage.addGestureRecognizer(tapGestureRecognizerMap)
        
        let tapGestureRecognizerFriends = UITapGestureRecognizer(target: self, action: #selector(imageTapped2(tapGestureRecognizer:)))
        friendsImage.isUserInteractionEnabled = true
        friendsImage.addGestureRecognizer(tapGestureRecognizerFriends)
        
        //MARK: - Start of user log-in
        DispatchQueue.main.async {
            self.checkIfUserIsLoggedIn()
        }
        
        // Ask for Authorisation from the User.
        self.locationManager.requestAlwaysAuthorization()
        
        // For use in foreground
        self.locationManager.requestWhenInUseAuthorization()
        if CLLocationManager.locationServicesEnabled() {
            locationManager.delegate = self
            locationManager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters
            locationManager.startUpdatingLocation()
        }
    }
    //MARK: - login function
    func checkIfUserIsLoggedIn()
    {
        //formatting date formatter
        dateFormatter.dateFormat = "yyyy-MM-dd HH:mm:ssZ"
        dateFormatter.locale = Locale(identifier: "en_US_POSIX")
        
        //condition to get if user is signed in with firebase auth
        if Auth.auth().currentUser?.uid == nil
        {
            //signout alert
            let alert = UIAlertController(title: "Error No One is Signed In", message: "Sign- In to Continue", preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "OK", style: .default , handler: { _ in
                //log out method
                self.HandleLogOut()
            }))
            self.present(alert, animated: true, completion: nil)
        }
        else
        {
            //setting nav title
            navBar.title = "Home"
            
            //getting current users uid
            let uid = Auth.auth().currentUser?.uid
           
            //MARK: - start of firebase snapshot
        Database.database().reference().child("Users").child(uid!).observeSingleEvent(of: .value, with: { (snapshot) in
                //getting dictionary
                if let dict = snapshot.value as? [String: Any]
                {
                    //username
                    self.userName = dict["fullName"] as! String
                    //unwrapping good status
                    if let ok : Bool = dict["GoodStatus"] as? Bool
                    {
                        self.isOk = ok
                    }
                    //optional for location and onboarding
                    if dict["LastCheckInLoc"] as? String == nil
                    {
                        //hiding onboarding
                        self.textView.isHidden = false
                        self.textField.isHidden = false
                        self.onboardingView.isHidden = false
                    }
                    else
                    {
                        self.textView.isHidden = true
                    }
                    
                    //conditional if good status is true or false
                    if self.isOk == false
                    {
                        //setting view for check-in status
                        self.ImGoodStatus.image = UIImage(named: "I'mGoodFalse")
                        self.backGroundView.backgroundColor = self.redBackColor
                    }
                    else
                    {
                        //conditional for 12 hours since last check in
                        if let lastCheck : String = dict["checkTime"] as? String
                        {
                            //unwrapping
                            if let lasttimeCheck = self.dateFormatter.date(from: lastCheck)
                            {
                                //if 12 hours less or more
                                if lasttimeCheck.timeIntervalSince(self.dateNow) > -43200
                                {
                                    print("less than 12 hours")
                                    self.ImGoodStatus.image = UIImage(named: "I'mGoodTrue")
                                    self.backGroundView.backgroundColor = self.greenBackColor
                                }
                                else{
                                    print("more than 12 hours")
                                    //telling user its time to check in
                                    let alert = UIAlertController(title: "Time To Check-in ", message: "Pressed the check-in button to be good", preferredStyle: .alert)
                                    
                                    alert.addAction(UIAlertAction(title: "OK", style: .default , handler: { _ in
                                        
                                        //setting good status to false
                                        self.isOk = false
                                        //calling good status function
                                        self.GoodStatus((Any).self)
                                    }))
                                    self.present(alert, animated: true, completion: nil)
                                }
                            }
                        }
                    }
                }
            }, withCancel: nil)
        }
    }
    //MARK: - sign out action
    @IBAction func SignoutTapped(_ sender: UIBarButtonItem) {
        //alert
        let alert = UIAlertController(title: "Are You Sure", message: "Yes to complete Sign-Out", preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
        alert.addAction(UIAlertAction(title: "Yes", style: .default , handler: { _ in
            //logout function
            self.HandleLogOut()
        }))
        self.present(alert, animated: true, completion: nil)
    }
    
    //MARK: - log out function
    func HandleLogOut()
    {
        do
        {
            //auth sign out
            try Auth.auth().signOut()
        }
        catch let logouterror
        {
            print(logouterror)
        }
        //segue to sign in controller
        performSegue(withIdentifier: "goToSignIn", sender: self)
    }
    
    //MARK:- GOOD Status ACTION
    @IBAction func GoodStatus(_ sender: Any)
    {
        //date time for now
        dateNow = Date()
        //conditional for good status
        if isOk == false
        {
            //removing onboarding
            if textView.isHidden == false
            {
                textView.isHidden = true
            }
            //changing good status
            isOk = true
            //getting current location
            let location = locationManager.location?.coordinate
           
   //updating firebase realtime database
            Database.database().reference().child("Users").child(uid!).updateChildValues(["GoodStatus": true])
            Database.database().reference().child("Users").child(uid!).updateChildValues(["LastCheckInLoc": "\(String(describing: location!.latitude.description)),\(String(describing: location!.longitude.description))"])
            
            Database.database().reference().child("Users").child(uid!).updateChildValues(["checkTime": dateNow.description])
            
            //changing ui
            ImGoodStatus.image = UIImage(named: "I'mGoodTrue")
            backGroundView.backgroundColor = greenBackColor
        }
        else
        {
            //changing ui and status to check-in
            isOk = false
            Database.database().reference().child("Users").child(uid!).updateChildValues(["GoodStatus": false])
            ImGoodStatus.image = UIImage(named: "I'mGoodFalse")
            backGroundView.backgroundColor = redBackColor
        }
        
    }
    
    //location manager did update
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation])
    {

    }
    
    //MARK: - perpare for segue
    override func prepare(for segue: UIStoryboardSegue, sender: Any?)
    {
        //conditional for what segue to run
        if segue.identifier == "friends"
        {
            //sending data to destination controller
            if let AllView = segue.destination as? FriendsViewController
            {
                AllView.backgroundColor = backGroundView.backgroundColor!
                AllView.redColor = redBackColor
                AllView.greenColor = greenBackColor
            }
        }
        else if segue.identifier == "toMap"
        {
            if let AllView = segue.destination as? MapViewController
            {
                AllView.backGroundColor = backGroundView.backgroundColor!
                AllView.redColor = redBackColor
                AllView.greenColor = greenBackColor
            }
        }
    }
    
    // image tapped function
    @objc func imageTapped(tapGestureRecognizer: UITapGestureRecognizer)
    {
        _ = tapGestureRecognizer.view as! UIImageView
        performSegue(withIdentifier: "toMap", sender: (Any).self)
    }
    // image tapped function
    @objc func imageTapped2(tapGestureRecognizer: UITapGestureRecognizer)
    {
        _ = tapGestureRecognizer.view as! UIImageView
        performSegue(withIdentifier: "friends", sender: (Any).self)
    }
    
    //Numberpad function for done button
    func donenumberTextPad()
    {
        //numberpad toolbar
        let toolbar = UIToolbar(frame: CGRect(origin: .zero, size: .init(width: view.frame.size.width
            , height: 30)))
        //space
        let space = UIBarButtonItem(barButtonSystemItem: .flexibleSpace, target: nil, action: nil)
            //button
        let done = UIBarButtonItem(title: "Done", style: .done, target: self, action: #selector(doneAction))
        
        toolbar.setItems([space, done], animated: true)
        toolbar.sizeToFit()
        
        textField.inputAccessoryView = toolbar
    }
    //action fro done button
    @objc func doneAction()
    {
        //conditional for if the number is valid
        if textField.text!.count < 10 || textField.text!.count > 10
        {
            //error alert
            let alert = UIAlertController(title: "Phone Number Error", message: "Phone number must be 10 digits", preferredStyle: .alert)
            
            alert.addAction(UIAlertAction(title: "OK", style: .default , handler: { _ in
            }))
            self.present(alert, animated: true, completion: nil)
        }
        else
        {
            let alert = UIAlertController(title: "Are You Sure", message: "Is \(textField.text!) your number", preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
            alert.addAction(UIAlertAction(title: "Yes", style: .default , handler: { _ in
                
                //adding phone number to user
                self.textView.text = "Great\nNow let's check-in for the first time.\nPlease press the check-in button."
                Database.database().reference().child("Users").child(self.uid!).updateChildValues(["phoneNumber": self.textField.text!])
                self.view.endEditing(true)
                self.phoneLabel.isHidden = true
                self.textField.isHidden = true
                self.onboardingView.isHidden = true
            }))
            self.present(alert, animated: true, completion: nil)
            self.view.endEditing(true)
        }
    }
}

