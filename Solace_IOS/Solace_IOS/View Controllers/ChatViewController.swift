//
//  ChatViewController.swift
//  Solace_IOS
//
//  Created by Jerad Alexander on 9/19/20.
//  Copyright © 2020 Jerad Alexander. All rights reserved.
//

import UIKit
import Firebase
import Alamofire

class ChatViewController: UIViewController ,UINavigationControllerDelegate, UITextFieldDelegate, UITableViewDelegate,UIAdaptivePresentationControllerDelegate, UITableViewDataSource  {
    
    //variables
    var userUid: String = Auth.auth().currentUser!.uid
    var selectedUid : String = "ptrQgfuq0tgMucxRNjCZQfVOyTs2"
    var avatarImageString : String = ""
    var username = ""
    var hardship = ""
    var chatList : Array<Chat_Data> = Array()
    var message = ""
    var userkarma : Int!
    var selectedKarma : Int!
    var lastMessageKey: String = ""
    let ref = Database.database().reference()
    var image : UIImage = UIImage(named: "addphoto")!
    
    //outlets
    @IBOutlet var chatTableView: UITableView!
    @IBOutlet var viewForKeyboard: UIView!
    @IBOutlet weak var toolbarView: UIView!
    @IBOutlet var nameLabel: UILabel!
    @IBOutlet var onlineStatus: UILabel!
    @IBOutlet var avatarImage: UIImageView!
    @IBOutlet var messageField: UITextField!
    @IBOutlet var isSeenLbl: UILabel!
    @IBOutlet var heightFromBottom: NSLayoutConstraint!
    
    override func viewDidAppear(_ animated: Bool) {
        //logic to show display and scroll to bottom
        if !chatList.isEmpty{
            
            self.scrollToBottom()
        }
        else{
            
            performSegue(withIdentifier: "toDisplay", sender: Any?.self)
        }
    }
    override func viewDidLoad() {
        super.viewDidLoad()
        view.addBackground()
        
        // keyboard notifications
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow), name: UIResponder.keyboardWillShowNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillHide), name: UIResponder.keyboardWillHideNotification, object: nil)
        
        //textfeild delagate
        messageField.delegate = self
        
        //tap gesture for diplay
        let tap: UITapGestureRecognizer = UITapGestureRecognizer(target: self, action: #selector(UIInputViewController.dismissKeyboard))
        
        tap.cancelsTouchesInView = false
        
        //setting up tablebiew
        chatTableView.addGestureRecognizer(tap)
        chatTableView.register(receiverTableViewCell.nib(), forCellReuseIdentifier: "receiverCell")
        chatTableView.register(SenderTableViewCell.nib(), forCellReuseIdentifier: "senderCell")
        chatTableView.separatorStyle = UITableViewCell.SeparatorStyle.none
        chatTableView.delegate = self
        chatTableView.dataSource = self
        
        //gesture
        let toolBartapGestureRecognizer = UITapGestureRecognizer(target: self, action: #selector(imageTapped(tapGestureRecognizer:)))
        toolbarView.isUserInteractionEnabled = true
        toolbarView.addGestureRecognizer(toolBartapGestureRecognizer)
        self.avatarImage.maskCircle(anyImage: UIImage(named: "addphoto")!)
        // Do any additional setup after loading the view.
        //loading chats and listener
        loadChats()
        //loading user info
        userInfoListener()
        
        //changing online status
        if let uid = Auth.auth().currentUser?.uid{
            ref.child("Users").child(uid).updateChildValues(["onlineStatus": "online"])
        }
        
    }
    
    //loadinf user info
    func userInfoListener(){
        
        Database.database().reference(withPath: "Users").child(selectedUid).observeSingleEvent(of: .value) { [self] (snapshot) in
            // print(snapshot)
            if let p = snapshot.value as? [String: Any] {
                
                
                // Username pull and application
                guard let user = p["username"] as? String else {
                    print("Error with listName")
                    return
                }
                self.nameLabel.text = user
                
                self.username = user
                
                guard let karmaval = p["karma"] as? Int else {
                    print("Error with listName")
                    return
                }
                
                self.selectedKarma = karmaval
                
                //hardship
                guard let hard = p["hardship"] as? String else {
                    print("Error with listName")
                    return
                }
                
                self.hardship = hard
                
                // Image pull and application
                guard let profilePic = p["image"] as? String else {
                    print("Error with listName")
                    return
                }
                let url = URL(string: profilePic)!
                //data pull
                
                
                do {
                    let data =  try Data(contentsOf: url)
                    //image from data
                    self.image = UIImage(data: data, scale: UIScreen.main.scale)!
                    //setting image as circle
                    self.avatarImage.maskCircle(anyImage: self.image)
                } catch {
                    self.avatarImage.maskCircle(anyImage: UIImage(named: "addphoto")!)
                }
                
            }
        }
        
        
        //sending message listern
        let databaseReferenceforSender =
            Database.database().reference(withPath: "Users").child(userUid)
        
        databaseReferenceforSender.observeSingleEvent(of: .value) { (snapshot) in
            // print(snapshot)
            if let p = snapshot.value as? [String: Any] {
                
                self.userkarma = (p["karma"] as? Int)!
            }
        }
    }
    
    //loading all of chats
    func loadChats(){
        
        Database.database().reference(withPath: "Users").child(self.userUid).child("Chats").observe(.value) { (snapshot) in
            self.chatList.removeAll()
            
            let enumerator = snapshot.children
            while let snap = enumerator.nextObject() as? DataSnapshot {
                //message info
                
                
                if let sp = snap.value as? [String: Any] {
                    
                    // Username pull and application
                    guard let message = sp["message"] as? String,
                          let receiver = sp["receiver"] as? String, let timeStamp = sp["timestamp"] as? String, let sender = sp["sender"] as? String,
                          let isSeen = sp["isSeen"] as? Bool else {
                        print("Error with listName")
                        return
                    }
                    
                    let chat : Chat_Data = Chat_Data(_message: message, _receiver: receiver, _timeStamp: timeStamp, _isSeen: isSeen, _sender: sender)
                    
                    if chat.sender.elementsEqual(self.userUid) && chat.receiver.elementsEqual(self.selectedUid) || chat.sender.elementsEqual(self.selectedUid) && chat.receiver.elementsEqual(self.userUid){
                        
                        self.chatList.append(chat)
                        self.addChatToEachUsersChatList(sentOrReceived: "receiver");
                    }
                }
            }
            if !self.chatList.isEmpty{
                if self.chatList[self.chatList.count - 1].isSeen   {
                    
                    self.isSeenLbl.text = "Seen"
                }else{
                    
                }
                
                if self.chatList[self.chatList.count - 1].sender == self.selectedUid{
                    
                    self.isSeen()
                }
                // when background job finished, do something in main thread
                self.chatTableView.reloadData()
                self.scrollToBottom()
            }
            
            //sending message listern
            let databaseReferenceforSender =
                Database.database().reference(withPath: "Users").child(self.userUid)
            
            databaseReferenceforSender.observeSingleEvent(of: .value) { (snapshot) in
                // print(snapshot)
                if let p = snapshot.value as? [String: Any] {
                    
                    self.userkarma = (p["karma"] as? Int)!
                }
            }
            Database.database().reference(withPath: "Users").child(self.selectedUid).observeSingleEvent(of: .value) { (snapshot) in
                // print(snapshot)
                if let p = snapshot.value as? [String: Any] {
                    
                    guard let karmaval = p["karma"] as? Int else {
                        print("Error with listName")
                        return
                    }
                    self.selectedKarma = karmaval
                }
            }
        }
    }
    
    //sending message logoc
    func sendMessage(){
        if messageField.text != ""{
            let currentTime = Date()
            
            _ = Date(timeIntervalSince1970: TimeInterval(currentTime.timeIntervalSince1970) * 1000)
            
            let mesdate : Int = Int((Date().timeIntervalSince1970 * 1000).rounded())
            
            //sending message listern
            let databaseReferenceforSender =
                Database.database().reference(withPath: "Users").child(userUid)
            
            //sending message listern
            let databaseReferenceforReceiver =
                Database.database().reference(withPath: "Users").child(selectedUid)
            
            let message = [
                "sender": self.userUid,
                "receiver": self.selectedUid,
                "message": messageField.text!,
                "timestamp":  mesdate.description,
                "isSeen": false,
            ] as [String : Any]
            
            //updating database
            databaseReferenceforSender.child("Chats").childByAutoId().setValue(message)
            databaseReferenceforReceiver.child("Chats").childByAutoId().setValue(message)
            databaseReferenceforSender.child("karma").setValue(userkarma + 20)
            databaseReferenceforReceiver.child("karma").setValue(selectedKarma + 20)
            
            messageField.text = ""
            isSeenLbl.text = "Delivered"
            
            //adding user to chatlist
            addChatToEachUsersChatList(sentOrReceived: "sent");
        }else{
        }
    }
    
    //adding user to chat list
    func addChatToEachUsersChatList(sentOrReceived: String){
        
        //conditional for sent or received
        if sentOrReceived == "sent"{
            let chatRef = ref.child("Users").child(userUid).child("ChatList").child(selectedUid)
            
            chatRef.observe(.value){ (snapshot) in
                // print(snapshot)
                
                if !snapshot.isEqual(self.selectedUid){
                    
                    if !snapshot.exists(){
                        
                        chatRef.child("id").setValue(self.selectedUid)
                        chatRef.child("blocked").setValue(false)
                    }
                }
            }
        }else if sentOrReceived == "receiver"{
            let chatRef = ref.child("Users").child(userUid).child("ChatList").child(selectedUid)
            
            chatRef.observe(.value){ (snapshot) in
                // print(snapshot)
                
                if !snapshot.isEqual(self.selectedUid){
                    
                    if !snapshot.exists(){
                        
                        chatRef.child("id").setValue(self.selectedUid)
                        chatRef.child("blocked").setValue(false)
                    }
                }
            }
        }
    }
    
    //logic for if the last message was seen
    func isSeen(){
        
        Database.database().reference(withPath: "Users").child(self.selectedUid).child("Chats").queryLimited(toLast: 1).observeSingleEvent(of: .value) { (snapshot) in
            
            let enumerator = snapshot.children
            while let snap = enumerator.nextObject() as? DataSnapshot {
                //message info
                
                self.lastMessageKey = snap.key
                
                
                let ref = Database.database().reference(withPath: "Users").child(self.selectedUid).child("Chats").child(self.lastMessageKey)
                
                ref.updateChildValues(["isSeen": true])
            }
        }
        // when background job finished, do something in main thread
        self.chatTableView.reloadData()
        self.scrollToBottom()
    }
    
    //method to scroll to bottom
    func scrollToBottom()  {
        if !chatList.isEmpty{
            let indexPath = NSIndexPath(row: self.chatList.count-1, section: 0)
            self.chatTableView.scrollToRow(at: indexPath as IndexPath, at: .bottom, animated: true)
        }
    }
    
    //tableview delagates
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        self.chatList.count
    }
    
    //cell setup
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        if chatList[indexPath.row].sender.elementsEqual(selectedUid){
            let cell = tableView.dequeueReusableCell(withIdentifier: "senderCell", for: indexPath) as! SenderTableViewCell
            
            if let time : Double = Double(chatList[indexPath.row].timeStamp) {
                
                let dateString = getTimeForMessage(time: time)
                
                cell.dateLabel.text = dateString
                
            }else{
                cell.dateLabel.text = chatList[indexPath.row].timeStamp
            }
            
            
            cell.TextFeild.text = chatList[indexPath.row].message
            cell.TextFeild.backgroundColor = UIColor.white
            cell.TextFeild.textContainerInset = UIEdgeInsets(top: 10, left: 0, bottom: 10, right: 10)
            
            return cell
        }else{
            let cell = tableView.dequeueReusableCell(withIdentifier: "receiverCell", for: indexPath) as! receiverTableViewCell
            
            if let time : Double = Double(chatList[indexPath.row].timeStamp) {
                
                let dateString = getTimeForMessage(time: time)
                
                cell.dateLabel.text = dateString
                
            }else{
                cell.dateLabel.text = chatList[indexPath.row].timeStamp
            }
            
            cell.TextFeild.text = chatList[indexPath.row].message
            cell.TextFeild.textContainerInset = UIEdgeInsets(top: 10, left: 10, bottom: 10, right: 10)
            
            return cell
        }
    }
    
    //getting time for messages
    func getTimeForMessage(time: Double)->String{
        let messageDate = Date(timeIntervalSince1970: TimeInterval(time) / 1000)
        
        // 1) Create a DateFormatter() object.
        let format = DateFormatter()
        
        format.dateFormat = "h:mm a"
        format.timeZone = .current
        
        let dateString = format.string(from: messageDate)
        return dateString
    }
    
    //send button pressed
    @IBAction func sendMessagePressed(_ sender: Any) {
        sendMessage()
    }
    
    //tap gesture method
    @objc func imageTapped(tapGestureRecognizer: UITapGestureRecognizer){
        performSegue(withIdentifier: "toDisplay", sender: Any?.self)
    }
    
    //moving feilds up with keyboard
    @objc func keyboardWillShow(notification: NSNotification) {
        if let keyboardSize = (notification.userInfo?[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue)?.cgRectValue {
            
            heightFromBottom = BottomConstraint.changeBottomConstraint(heightFromBottom, constent: keyboardSize.height)
            
        }
    }
    
    //hide keyboard
    @objc func keyboardWillHide(notification: NSNotification) {
        heightFromBottom = BottomConstraint.changeBottomConstraint(heightFromBottom, constent: 0)
    }
    
    //return key pressed
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        
        //textField code
        messageField.resignFirstResponder()
        sendMessage()
        return true
    }
    
    
    
    //Calls this function when the tap is recognized.
    @objc func dismissKeyboard() {
        //Causes the view (or one of its embedded text fields) to resign the first responder status.
        view.endEditing(true)
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "toDisplay"{
            
            if let displayVC = segue.destination as? DisplayProfileViewController{
                
                //sending display data
                displayVC.displayuid = selectedUid
                displayVC.username = username
                displayVC.image = image
                displayVC.username = username
                displayVC.hardship = hardship
            }
        }
    }
    
    func presentationControllerDidDismiss(_ presentationController: UIPresentationController) {
        print("dismissed")
    }
}

//constraint strunct
struct BottomConstraint {
    static func changeBottomConstraint(_ constraint: NSLayoutConstraint, constent: CGFloat) -> NSLayoutConstraint {
        let newConstraint = NSLayoutConstraint(
            item: constraint.firstItem as Any,
            attribute: constraint.firstAttribute,
            relatedBy: constraint.relation,
            toItem: constraint.secondItem,
            attribute: constraint.secondAttribute,
            multiplier: constraint.multiplier,
            constant: constent)
        
        newConstraint.priority = constraint.priority
        
        NSLayoutConstraint.deactivate([constraint])
        NSLayoutConstraint.activate([newConstraint])
        
        return newConstraint
    }
}
