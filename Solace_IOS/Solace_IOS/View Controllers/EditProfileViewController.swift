//
//  EditProfileViewController.swift
//  Solace_IOS
//
//  Created by Zakarias McDanal on 9/19/20.
//  Copyright © 2020 Jerad Alexander. All rights reserved.
//

import UIKit
import Firebase
import AVKit

class EditProfileViewController: UIViewController, UINavigationControllerDelegate, UIImagePickerControllerDelegate , UITextFieldDelegate{
    
    //variables
    let ref = Database.database().reference()
    var userID = Auth.auth().currentUser!.uid
    var imageUrl = ""
    var avatarImg : UIImage? = nil
    var avatarNotNil = false
    var imageCaptured : UIImage? = nil
    var userNameValue = ""
    
    //outlets
    @IBOutlet var profileImg: UIImageView!
    @IBOutlet var userName: UILabel!
    @IBOutlet var usernameTF: UITextField!
    @IBOutlet weak var greenBaseView: UIView!
    @IBOutlet weak var saveView: UIView!
    @IBOutlet weak var saveBtn: UIButton!
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        
        
        // Do any additional setup after loading the view.
        //border setup fro feilds
        //border colors
        usernameTF.layer.cornerRadius = 15.0
        usernameTF.layer.borderColor = UIColor(named: "SolaceYellow")?.cgColor
        usernameTF.layer.borderWidth = 3.0
        usernameTF.layer.borderWidth = 3.0
        usernameTF.layer.masksToBounds = true
        usernameTF.layer.shadowRadius = 3.0
        usernameTF.layer.shadowColor = UIColor(named: "SolaceYellow")?.cgColor
        usernameTF.delegate = self
        
        greenBaseView.layer.cornerRadius = 10.0
        greenBaseView.layer.borderWidth = 3.0
        greenBaseView.layer.borderColor = UIColor(named: "SolaceYellow")?.cgColor
        
        saveView.layer.cornerRadius = 10.0
        saveBtn.layer.cornerRadius = 10.0
        
        //Looks for single or multiple taps.
        let tap: UITapGestureRecognizer = UITapGestureRecognizer(target: self, action: #selector(UIInputViewController.dismissKeyboard))
        
        //Uncomment the line below if you want the tap not not interfere and cancel other interactions.
        tap.cancelsTouchesInView = true
        
        view.addGestureRecognizer(tap)
        
        
        let avatartapGestureRecognizer = UITapGestureRecognizer(target: self, action: #selector(imageTapped(tapGestureRecognizer:)))
        profileImg.isUserInteractionEnabled = true
        profileImg.addGestureRecognizer(avatartapGestureRecognizer)
        
        
        self.profileImg.maskCircle(anyImage: self.avatarImg!)
        
        
    }
    
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
    
    //importing image
    func importImage(){
        let picker = UIImagePickerController()
        picker.allowsEditing = true
        picker.delegate = self
        present(picker, animated: true)
    }
    
    //getting image from picker
    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
        guard let image = info[.editedImage] as? UIImage else { return }
        
        dismiss(animated: true)
        
        profileImg.maskCircle(anyImage: image)
        
        if profileImg.image != UIImage(named: "addphoto"){
            avatarNotNil = true
            imageCaptured = image
        }
    }
    
    //opening camera
    func openCamera(){
        let controller = CustomCameraViewController()
        self.present(controller, animated: true, completion: nil)
    }
    
    // Update the firebase if the save button was clicked
    @IBAction func saveBtnClicked(_ sender: UIButton) {
        if usernameTF.text == ""  && avatarNotNil == false{
            
            dismiss(animated: true, completion: nil)
        }else if usernameTF.text != ""{
            
            if !avatarNotNil {
                ref.child("Users").child(userID).updateChildValues(["username": usernameTF.text!])
                dismiss(animated: true, completion: nil)
            }else{
                ref.child("Users").child(userID).updateChildValues(["username": usernameTF.text!])
                
                updateProfileImage()
            }
        } else{
            updateProfileImage()
        }
        
    }
    
    //oplaoding image
    func updateProfileImage(){
        let group = DispatchGroup()
        group.enter()
        DispatchQueue.global(qos: .default).async {
            if let data = self.imageCaptured?.jpegData(compressionQuality: 0.5){
                
                
                let f = self.userID + Date().description  + ".jpg"
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
            print(self.imageUrl)
            self.ref.child("Users").child(self.userID).updateChildValues(["image": self.imageUrl])
            
            self.dismiss(animated: true, completion: nil)
        }
    }
    
    //return pressed
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        
        dismissKeyboard()
        return true
    }
    
    //Calls this function when the tap is recognized.
    @objc func dismissKeyboard() {
        //Causes the view (or one of its embedded text fields) to resign the first responder status.
        view.endEditing(true)
        
        func switchBasedNextTextField(_ textField: UITextField) {
            self.usernameTF.resignFirstResponder()
        }
    }
}
