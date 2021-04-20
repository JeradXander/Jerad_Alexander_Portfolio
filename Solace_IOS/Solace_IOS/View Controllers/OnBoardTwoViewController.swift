//
//  OnBoardTwoViewController.swift
//  Solace_IOS
//
//  Created by Jerad Alexander on 9/12/20.
//  Copyright © 2020 Jerad Alexander. All rights reserved.
//

import UIKit
import Firebase
import AVKit


class OnBoardTwoViewController: UIViewController, UIPickerViewDataSource, UIPickerViewDelegate, UIImagePickerControllerDelegate & UINavigationControllerDelegate, UITextFieldDelegate {
    
    
    //service
    let alertService = AlertService()
    //variables
    private let photoOutput = AVCapturePhotoOutput()
    var imageCaptured : UIImage? = nil
    var imageUrl = ""
    var email: String = ""
    var password: String = ""
    var avatarNotNil = false
    var hardship = "Relationships"
    var uid: String? = ""
    var userDict = [String: Any]()
    var hardshipData : [String] = [String]()
    
    //outlets
    @IBOutlet var usernameField: UITextField!
    @IBOutlet var firstNameField: UITextField!
    @IBOutlet var lastNameField: UITextField!
    @IBOutlet var avatarImageView: UIImageView!
    @IBOutlet var harsdshipPicker: UIPickerView!
    @IBOutlet var SignUp: UIButton!
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        //delagates for textfields
        self.usernameField.delegate = self
        self.firstNameField.delegate = self
        self.lastNameField.delegate = self
        
        //Looks for single or multiple taps.
        let tap: UITapGestureRecognizer = UITapGestureRecognizer(target: self, action: #selector(UIInputViewController.dismissKeyboard))
        
        let avatartapGestureRecognizer = UITapGestureRecognizer(target: self, action: #selector(imageTapped(tapGestureRecognizer:)))
        avatarImageView.isUserInteractionEnabled = true
        avatarImageView.addGestureRecognizer(avatartapGestureRecognizer)
        
        tap.cancelsTouchesInView = false
        
        //gesture setup
        view.addGestureRecognizer(tap)
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow), name: UIResponder.keyboardWillShowNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillHide), name: UIResponder.keyboardWillHideNotification, object: nil)
        
        //adding background
        self.view.addBackground()
        self.avatarImageView.maskCircle(anyImage: UIImage(named: "addphoto")!)
        
        hardshipData = ["Relationships","Family","Death", "Job", "Natural Disaster", "Other"]
        
        //picker setup
        self.harsdshipPicker.delegate = self
        self.harsdshipPicker.dataSource = self
        
        //buttonsetup
        SignUp.layer.cornerRadius = 15.0
        SignUp.layer.borderWidth = 3.0
        SignUp.layer.borderColor = UIColor(named: "SolaceYellow")?.cgColor
        SignUp.titleEdgeInsets = UIEdgeInsets(top: 5, left: 5, bottom: 5, right: 5)
        
        
        //feild corner radius
        usernameField.layer.cornerRadius = 15.0
        firstNameField.layer.cornerRadius = 15.0
        lastNameField.layer.cornerRadius = 15.0
        //border colors
        usernameField.layer.borderColor = UIColor(named: "SolaceYellow")?.cgColor
        firstNameField.layer.borderColor = UIColor(named: "SolaceYellow")?.cgColor
        lastNameField.layer.borderColor = UIColor(named: "SolaceYellow")?.cgColor
        
        //border setup fro feilds
        usernameField.layer.borderWidth = 3.0
        usernameField.layer.masksToBounds = true
        usernameField.layer.shadowRadius = 3.0
        usernameField.layer.shadowColor = UIColor(named: "SolaceYellow")?.cgColor
        usernameField.layer.shadowOpacity = 1.0
        
        firstNameField.layer.borderWidth = 3.0
        firstNameField.layer.shadowOpacity = 1.0
        firstNameField.layer.masksToBounds = true
        firstNameField.layer.shadowRadius = 3.0
        firstNameField.layer.shadowColor = UIColor(named: "SolaceYellow")?.cgColor
        
        lastNameField.layer.borderWidth = 3.0
        lastNameField.layer.masksToBounds = true
        lastNameField.layer.shadowRadius = 3.0
        lastNameField.layer.shadowColor = UIColor(named: "SolaceYellow")?.cgColor
        lastNameField.layer.shadowOpacity = 1.0
        // Do any additional setup after loading the view.
        // Do any additional setup after loading the view.
    }
    
    
    @IBAction func SignUpPressed(_ sender: Any) {
        
        //feild data
        let userName : String = usernameField.text!
        let firstName : String = firstNameField.text!
        let lastName : String = lastNameField.text!
        
        //input validation
        if userName.elementsEqual("") || firstName.elementsEqual("") || lastName.elementsEqual(""){
            Toast.show(message: "fields are empty cannot be empty", controller: self)
        }else if !avatarNotNil{
            Toast.show(message: "You need to add a photo for your profile picture", controller: self)
        }
        else {
            let alertvs = alertService.alertLoading()
            present(alertvs, animated: true)
            
            let group = DispatchGroup()
            
            //creating user
            Auth.auth().createUser(withEmail: email, password: password) { authResult, error in
                
                group.enter()
                DispatchQueue.global(qos: .default).async {
                    if let data = self.imageCaptured?.jpegData(compressionQuality: 0.5){
                        
                        //uplaod photo
                        let f = userName + Date().description  + ".jpg"
                        let ref = Storage.storage().reference().child("Users_Profile_Cover_Imgs/").child(f)
                        
                        let md = StorageMetadata()
                        md.contentType = "image/png"
                        // Get a reference to the storage service using the default Firebase App
                        
                        ref.putData(data, metadata: md) { (metadata, error) in
                            if error == nil {
                                ref.downloadURL(completion: { (url, error) in
                                    print("task 1  Done, url is \(String(describing: url))")
                                    
                                    self.imageUrl = url!.absoluteString
                                    group.leave()
                                })
                            }else{
                                print("error \(String(describing: error))")
                                group.leave()
                            }
                        }
                    }
                    
                }
                
                group.notify(queue: .main){
                    if authResult?.user.uid != nil{
                        
                        //creating user
                        if let uidString: String = authResult?.user.uid{
                            self.userDict = [
                                "email": self.email,
                                "first_name": firstName,
                                "hardship": self.hardship,
                                "image":  self.imageUrl,
                                "is_available": true,
                                "karma": 0,
                                "lastName": lastName,
                                "onlineStatus": "online",
                                "typingTo": "noOne",
                                "uid": uidString,
                                "username": userName
                            ]
                            
                            //updating database with new user
                            var ref: DatabaseReference!
                            ref = Database.database().reference()
                            ref.child("Users").child(uidString).setValue(self.userDict)
                        }}
                    alertvs.dismiss(animated: false, completion: {
                        //to dashboard
                        if let tabViewController = self.storyboard?.instantiateViewController(withIdentifier: "tabBar") as? TabBarVC {
                            self.present(tabViewController, animated: true, completion: nil)
                        }
                    })
                }
            }
        }
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
    
    //picker delagates
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return hardshipData.count
    }
    
    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        let row = hardshipData[row]
        return row
    }
    
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        hardship = hardshipData[row]
        
    }
    
    //return key pressed
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        //switching to next responder
        self.switchBasedNextTextField(textField)
        return true
    }
    
    //going to next feilds
    private func switchBasedNextTextField(_ textField: UITextField) {
        switch textField {
        
        case self.usernameField:
            self.firstNameField.becomeFirstResponder()
        case self.firstNameField:
            self.lastNameField.becomeFirstResponder()
        case self.lastNameField:
            self.lastNameField.resignFirstResponder()
        default:
            self.usernameField.resignFirstResponder()
        }
    }
    
    //image tapped
    @objc func imageTapped(tapGestureRecognizer: UITapGestureRecognizer){
        
        // create the alert
        let alert = UIAlertController(title: "Add Photo", message: "Please select how you want to add a photo", preferredStyle: UIAlertController.Style.alert)
        
        // add the actions (buttons)
        alert.addAction(UIAlertAction(title: "Photos", style: UIAlertAction.Style.default, handler:{action in  self.importImage()}))
        alert.addAction(UIAlertAction(title: "Take Picture", style: UIAlertAction.Style.default, handler: {action in  self.openCamera()}))
        alert.addAction(UIAlertAction(title: "Cancel", style: UIAlertAction.Style.cancel, handler: nil))
        
        // show the alert
        self.present(alert, animated: true, completion: nil)
    }
    
    func importImage(){
        let picker = UIImagePickerController()
        picker.allowsEditing = true
        picker.delegate = self
        present(picker, animated: true)
    }
    
    //image picker
    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
        guard let image = info[.editedImage] as? UIImage else { return }
        
        dismiss(animated: true)
        
        //setting image from picker
        avatarImageView.maskCircle(anyImage: image)
        if avatarImageView.image != UIImage(named: "addphoto"){
            avatarNotNil = true
            imageCaptured = image
        }
    }
    
    //opening camera
    func openCamera(){
        let controller = CustomCameraViewController()
        self.present(controller, animated: true, completion: nil)
    }
    
    
    //Calls this function when the tap is recognized.
    @objc func dismissKeyboard() {
        //Causes the view (or one of its embedded text fields) to resign the first responder status.
        view.endEditing(true)
    }
}
