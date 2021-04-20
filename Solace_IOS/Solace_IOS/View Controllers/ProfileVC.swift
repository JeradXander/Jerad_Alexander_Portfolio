//
//  ProfileVCViewController.swift
//  Solace_IOS
//
//  Created by Zakarias McDanal on 9/10/20.
//  Copyright © 2020 Jerad Alexander. All rights reserved.
//

import UIKit
import Firebase
import FirebaseAuth
import Alamofire
import AlamofireImage

class ProfileVC: UIViewController, PastConvoDelegate, UITableViewDelegate, UITableViewDataSource, UIPickerViewDataSource, UIPickerViewDelegate,UIAdaptivePresentationControllerDelegate{
    
    //variables
    var chatterUids: [String] = [String]()
    var chatters: [Chatter_Data] = [Chatter_Data]()
    var selecteduidForChat : String = ""
    let ref = Database.database().reference()
    let userID = Auth.auth().currentUser!.uid
    var username = ""
    var imageUri = ""
    var avatarImage : UIImage!
    var lastMessage: String!
    var hardshipData : [String] = [String]()
    var hardship = "Relationships"
    var karma = 0
    var hardshipIndex = 0
    var numberofRowsForMultiplier: Int = 0
    var lastMessageKey = ""
    let group = DispatchGroup()
    var lastMessage_Data: [LastMessage_Data] = [LastMessage_Data]()
    
    //outlets
    @IBOutlet var chatlistTableView: UITableView!
    @IBOutlet var contentHeight: NSLayoutConstraint!
    @IBOutlet var menuButton: UIButton!
    @IBOutlet weak var hotlineBtn: UIImageView!
    @IBOutlet weak var profileImg: UIImageView!
    @IBOutlet weak var usernameLbl: UILabel!
    @IBOutlet weak var hardshipPicker: UIPickerView!
    @IBOutlet weak var displayView: UIView!
    @IBOutlet var KarmaBar: UIProgressView!
    @IBOutlet var karmaLabel: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.addBackground()
        
        //ui setup
        displayView.layer.cornerRadius = 20
        hardshipPicker.layer.cornerRadius = 25
        hardshipPicker.layer.borderWidth = 1.5
        hardshipPicker.layer.borderColor = UIColor(named: "SolaceYellow")?.cgColor
        
        //pulling data from database
        pullDataFromFirebase()
        pullChatterUidsFromFirebase()
        
        //tableview setup
        chatlistTableView.delegate = self
        chatlistTableView.dataSource = self
        
        //picker data
        hardshipData = ["Relationships","Family","Death", "Job", "Natural Disaster", "Other"]
        
        //picker setup
        self.hardshipPicker.delegate = self
        self.hardshipPicker.dataSource = self
        // Do any additional setup after loading the view.
        
        //reload data
        chatlistTableView.reloadData()
        
        //ui setup for how many chatters there are
        numberofRowsForMultiplier =   chatlistTableView.numberOfRows(inSection: 0)
        if numberofRowsForMultiplier < 6 {
            contentHeight =  MyConstraint.changeMultiplier(contentHeight, multiplier:  getNewMultiplier(numberOfRowsInt: numberofRowsForMultiplier))
        }
        
        //tap gesture
        let tap = UITapGestureRecognizer(target: self, action: #selector(ProfileVC.openPage))
        hotlineBtn.addGestureRecognizer(tap)
        hotlineBtn.isUserInteractionEnabled = true
    }
    
    //opening page logic
    @objc func openPage(sender:UITapGestureRecognizer) {
        switch sender.view!.tag {
        case 0:
            performSegue(withIdentifier: "toHotlineFromProfile", sender: nil)
        default:
            print("openPage on ProfileVC: Error - Default was called")
        }
    }
    
    // Button action for past conversation cell hamburger menu
    func didPressButton(_ tag: Int) {
        let alert = UIAlertController(title: "Select Action", message: nil, preferredStyle: .actionSheet)
        
        alert.addAction(UIAlertAction(title: "Block/Report", style: .default, handler: { action in
            //edit profile logic here
            let alert2 = UIAlertController(title: "Are you sure you wish to permanently delete \(self.chatters[tag].username!)?", message: nil, preferredStyle: .actionSheet)
            
            alert2.addAction(UIAlertAction(title: "Yes", style: .default, handler: { action in
                //edit profile logic here
                let uid = self.chatters[tag].uid
                if (!self.chatterUids.isEmpty) {
                    self.chatters.removeAll()
                    self.chatterUids.removeAll()
                }
                //blocked reference
                self.ref.child("Users").child(self.userID).child("ChatList").child(uid!).updateChildValues(["blocked": true])
                
                let databaseRef = self.ref.child("Reports").child(uid!).child("timesReported")
                
                //single event
                databaseRef.observeSingleEvent(of: .value, with: { snapshot in
                    let valString = snapshot.value
                    if let value = valString as? NSInteger {
                        let finalValue = value + 1
                        databaseRef.setValue(finalValue)
                    } else {
                        databaseRef.setValue(1)
                    }
                })
                //pulling chatter info
                self.pullChatterUidsFromFirebase()
            }))
            
            //alert to load chatters and messges
            alert2.addAction(UIAlertAction(title: "Nevermind", style: .cancel, handler: nil))
            
            self.present(alert2, animated: true)
            
        }))
        
        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
        self.present(alert, animated: true)
    }
    
    //tableview delagates
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        print("Chatters \(chatterUids.count)")
        
        return chatters.count
        
    }
    
    // Past Conversations Table View
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "cellreuse", for: indexPath) as! PastConversationCell
        
        print("Chatters data: \(chatters.count)")
        
        if (chatterUids.isEmpty) {
            return cell
        } else {
            
            //cell setup
            cell.chatView.layer.cornerRadius = 20
            cell.cellDelegate = self
            cell.usernameLbl.text = chatters[indexPath.row].username
            let url = URL(string: chatters[indexPath.row].profileUrl)!
            
            do {
                let data =  try Data(contentsOf: url)
                //image from data
                self.avatarImage = UIImage(data: data, scale: UIScreen.main.scale)!
                //setting image as circle
                cell.profilePic.maskCircle(anyImage: self.avatarImage)
            } catch {
                cell.profilePic.maskCircle(anyImage: UIImage(named: "addphoto")!)
            }
            
            cell.lastMessageLbl.text = chatters[indexPath.row].lastMessage
            cell.hamburgerMenuBtn.tag = indexPath.row
            return cell
        }
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        
        self.selecteduidForChat = chatterUids[indexPath.row]
        self.performSegue(withIdentifier: "toChat", sender: nil)
        print(self.selecteduidForChat)
    }
    
    //logic to make content view bigger for more chats
    func getNewMultiplier(numberOfRowsInt: Int)-> CGFloat{
        
        switch numberOfRowsInt {
        case 5:
            return 1.0011
        case 4:
            return 1.0013
        case 3:
            return 1.00141
        case 2:
            return 1.001
        default:
            return 1.0016
        }
    }
    
    // Hamburger menu was pressed
    @IBAction func MenuPressed(){
        let alert = UIAlertController(title: "Select Action", message: nil, preferredStyle: .actionSheet)
        
        // Action to show Edit Profile
        alert.addAction(UIAlertAction(title: "Edit Profile", style: .default, handler: { action in
            self.performSegue(withIdentifier: "toEdit", sender: nil)
        }))
        // Signs out of account via firebase
        alert.addAction(UIAlertAction(title: "Sign-Out", style: .default, handler: {action in
            
            let firebaseAuth = Auth.auth()
            do {
                try firebaseAuth.signOut()
                self.performSegue(withIdentifier: "splash", sender: nil)
                
            } catch let signOutError as NSError {
                print ("Error signing out: %@", signOutError)
            }
        }))
        
        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
        self.present(alert, animated: true)
    }
    
    // Pulls all previous conversation chatter data from Firebase
    func pullChatterUidsFromFirebase() {
        if (!chatterUids.isEmpty) {
            self.chatters.removeAll()
            self.chatterUids.removeAll()
            
        }
        ref.child("Users").child(userID).child("ChatList").observeSingleEvent(of: .value ) {(snapshot) in
            let enumerator = snapshot.children
            while let snap = enumerator.nextObject() as? DataSnapshot {
                //message info
                if let sp = snap.value as? [String: Any] {
                    
                    guard let id = sp["id"] as? String,
                          let blocked = sp["blocked"] as? Bool else {
                        print("ProfileVC - pullChatterUidsFromFirebase(): Failed to pull chatters")
                        return
                    }
                    if !blocked {
                        self.chatterUids.append(id)
                    }
                }
            }
            
            if (!self.chatterUids.isEmpty) {
                for uid in self.chatterUids {
                    self.ref.child("Users").child(uid).observeSingleEvent(of: .value ) {(snapshot) in
                        if let user = snapshot.value as? [String: Any] {
                            
                            guard let chatterUsername = user["username"] as? String else {
                                print("Error with username pull")
                                return
                            }
                            guard let chatterProfilePic = user["image"] as? String else {
                                print("Error with image pull")
                                return
                            }
                            guard let chatterKarma = user["karma"] as? Int else {
                                print("Error with karma")
                                return
                            }
                            
                            let stringKarma = "\(chatterKarma)"
                            
                            guard let chatterHardship = user["hardship"] as? String else {
                                print("Error with hardship")
                                return
                            }
                            
                            self.getLastMessage(chatterUid: uid, Username: chatterUsername, ProfileUrl: chatterProfilePic, Hardship: chatterHardship, Karma: stringKarma)
                            
                        }
                    }
                }
            }
        }
    }
    
    // Pulls user's data to populate page ui with
    func pullDataFromFirebase() {
        ref.child("Users").child(userID).observe(.value) { (snapshot) in
            // print(snapshot)
            if let p = snapshot.value as? [String: Any] {
                
                // Username pull and application
                guard let username = p["username"] as? String else {
                    print("Error with listName")
                    return
                }
                self.usernameLbl.text = username
                
                // Hardship pull and application
                guard let hardshipPull = p["hardship"] as? String else {
                    print("Error with listName")
                    return
                }
                self.checkRow(hardshipPull: hardshipPull)
                self.hardshipPicker.selectRow(self.hardshipIndex, inComponent: 0, animated: true)
                
                // Hardship pull and application
                guard let karmaPull = p["karma"] as? Int else {
                    print("Error with listName")
                    return
                }
                
                self.setupKarmaBar(level: karmaPull)
                
                // Image pull and application
                guard let profilePic = p["image"] as? String else {
                    print("Error with listName")
                    return
                }
                let url = URL(string: profilePic)!
                
                do {
                    let data =  try Data(contentsOf: url)
                    //image from data
                    self.avatarImage = UIImage(data: data, scale: UIScreen.main.scale)!
                    //setting image as circle
                    self.profileImg.maskCircle(anyImage: self.avatarImage)
                } catch {
                    self.profileImg.maskCircle(anyImage: UIImage(named: "addphoto")!)
                }
            }
            
        }
    }
    
    // Hardship picker setup - start
    func checkRow(hardshipPull: String) {
        switch hardshipPull {
        case "Relationships":
            hardshipIndex = 0
        case "Family":
            hardshipIndex = 1
        case "Death":
            hardshipIndex = 2
        case "Job":
            hardshipIndex = 3
        case "Natural Disaster":
            hardshipIndex = 4
        default:
            hardshipIndex = 5
        }
    }
    //pickerview delagates
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return hardshipData.count
    }
    
    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        let row = hardshipData[row]
        hardshipPicker.setValue(UIColor.white, forKeyPath: "textColor")
        return row
    }
    
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        hardship = hardshipData[row]
        Database.database().reference().root.child("Users").child(userID).updateChildValues(["hardship": hardship])
    }
    // Hardship picker setup - end
    
    
    // Grabs the last message between users to populate the custom cell of the past conversations table view .... gasp
    func getLastMessage(chatterUid: String, Username: String, ProfileUrl: String, Hardship: String, Karma: String){
        
        group.enter()
        DispatchQueue.global(qos: .default).async {
            self.ref.child("Users").child(self.userID).child("Chats").observe(.value) { (snapshot) in
                
                let enumerator = snapshot.children
                while let snap = enumerator.nextObject() as? DataSnapshot {
                    //message info
                    
                    
                    if let sp = snap.value as? [String: Any] {
                        //print(sp)
                        // Username pull and application
                        guard let message = sp["message"] as? String,
                              let receiver = sp["receiver"] as? String, let timeStamp = sp["timestamp"] as? String, let sender = sp["sender"] as? String,
                              let isSeen = sp["isSeen"] as? Bool else {
                            print("Error with listName")
                            return
                        }
                        
                        let chatter : Chat_Data = Chat_Data(_message: message, _receiver: receiver, _timeStamp: timeStamp, _isSeen: isSeen, _sender: sender)
                        
                        if chatter.sender.elementsEqual(self.userID) && chatter.receiver.elementsEqual(chatterUid) || chatter.sender.elementsEqual(chatterUid) && chatter.receiver.elementsEqual(self.userID){
                            
                            var newCount = 0
                            
                            
                            for chatMess in self.lastMessage_Data {
                                if (chatMess.fromUid == chatterUid) {
                                    let total = chatMess.count
                                    newCount = total + 1
                                }
                            }
                            
                            let data : LastMessage_Data = LastMessage_Data(LastMessage: message, FromUid: chatterUid, Count: newCount)
                            
                            if (self.lastMessage_Data.isEmpty) {
                                self.lastMessage_Data.append(data)
                            } else {
                                for (index, value) in self.lastMessage_Data.enumerated() {
                                    if value.fromUid == chatterUid {
                                        self.lastMessage_Data.remove(at: index)
                                    }
                                }
                                self.lastMessage_Data.append(data)
                            }
                        }
                    }
                    
                }
                
                var messageToShow = ""
                
                for message in self.lastMessage_Data {
                    if (message.fromUid == chatterUid) {
                        messageToShow = message.lastMessage
                    }
                }
                
                print(self.lastMessage_Data.count)
                
                self.group.notify(queue: .main){
                    let chatter = Chatter_Data(Uid: chatterUid, Username: Username, ProfileUrl: ProfileUrl, Hardship: Hardship, Karma: Karma, lastMessage: messageToShow)
                    
                    for (index, value) in self.chatters.enumerated() {
                        if value.uid == chatterUid {
                            self.chatters.remove(at: index)
                        }
                    }
                    self.chatters.append(chatter)
                    self.chatlistTableView.reloadData()
                    self.numberofRowsForMultiplier =   self.chatlistTableView.numberOfRows(inSection: 0)
                    if self.numberofRowsForMultiplier < 6 {
                        self.contentHeight =  MyConstraint.changeMultiplier(self.contentHeight, multiplier:  self.getNewMultiplier(numberOfRowsInt: self.numberofRowsForMultiplier))
                    }
                    print("tableViewRows: " + self.chatlistTableView.numberOfRows(inSection: 0).description)
                }
                
            }
            self.group.leave()
        }
    }
    
    // For all segues, this is where the luggage to take with is packed
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "toChat"{
            if let destinationVC = segue.destination as? ChatViewController{
                
                destinationVC.selectedUid = selecteduidForChat
                destinationVC.presentationController?.delegate = self;
            }
        }
        if segue.identifier == "toEdit"{
            if let destinationVC = segue.destination as? EditProfileViewController{
                
                destinationVC.userID = userID
                destinationVC.avatarImg = avatarImage
                destinationVC.userNameValue = username
            }
        }
    }
    
    //when user closes chat sets them to offline
    func presentationControllerDidDismiss(_ presentationController: UIPresentationController) {
        print("chat is closed")
        if let uid = Auth.auth().currentUser?.uid{
            ref.child("Users").child(uid).updateChildValues(["onlineStatus": "offline"])
        }
    }
    
    //setting up karma bar
    func setupKarmaBar(level : Int){
        
        let karmaLevel = level / 1000
        karmaLabel.text = "Karma Level: " + karmaLevel.description
        KarmaBar.transform.scaledBy(x: 1, y: 10)
        KarmaBar.layer.cornerRadius = 20
        KarmaBar.layer.borderWidth = 15.0
        KarmaBar.layer.borderColor = UIColor(named: "SolaceLightGreen")?.cgColor
        KarmaBar.clipsToBounds = true
        KarmaBar.layer.sublayers![1].cornerRadius = 10
        KarmaBar.subviews[1].clipsToBounds = true
        let karmaPercentage : Double = Double(level - (1000 * karmaLevel))
        KarmaBar.progress = Float(karmaPercentage/1000)
    }
    
}

//new constraint struct
struct MyConstraint {
    static func changeMultiplier(_ constraint: NSLayoutConstraint, multiplier: CGFloat) -> NSLayoutConstraint {
        let newConstraint = NSLayoutConstraint(
            item: constraint.firstItem as Any,
            attribute: constraint.firstAttribute,
            relatedBy: constraint.relation,
            toItem: constraint.secondItem,
            attribute: constraint.secondAttribute,
            multiplier: multiplier,
            constant: constraint.constant)
        
        newConstraint.priority = constraint.priority
        NSLayoutConstraint.deactivate([constraint])
        NSLayoutConstraint.activate([newConstraint])
        
        return newConstraint
    }
}
