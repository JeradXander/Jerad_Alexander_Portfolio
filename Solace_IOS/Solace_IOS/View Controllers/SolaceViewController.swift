//
//  SolaceViewController.swift
//  Solace_IOS
//
//  Created by Jerad Alexander on 9/23/20.
//  Copyright © 2020 Jerad Alexander. All rights reserved.
//

import UIKit
import Lottie
import Firebase

protocol SolaceDelegate: class {
    func openChat(withId: String)
}

class SolaceViewController: UIViewController ,UIAdaptivePresentationControllerDelegate{
    
    //outlets
    @IBOutlet var splashAnim: AnimationView!
    @IBOutlet var searchingLabel: UILabel!
    @IBOutlet var countdownLabel: UILabel!
    @IBOutlet var backView: UIView!
    @IBOutlet var buttonView: UIButton!
    
    //variables
    var searchStatus = ""
    var timer:Timer?
    var timeLeft = 30
    var mUser = Auth.auth().currentUser
    var listenerList: [Listener_Data] = [Listener_Data]()
    var randomList: [Listener_Data] = [Listener_Data]()
    var blockedUids: [String] = [String]()
    var listenerUid = ""
    var talkerUid = ""
    var hardship = ""
    var matched = false
    var searching = false
    var randomGenerator : RandomNumberGenerator!
    let ref = Database.database().reference()
    let group = DispatchGroup()
    weak var delegate: SolaceDelegate?
    
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        //animation
        splashAnim.loopMode = .loop
        splashAnim.animationSpeed = 1.0
        splashAnim.play()
        
        //ui setup
        backView.layer.cornerRadius = 15.0
        backView.layer.borderColor = UIColor(named: "SolaceYellow")?.cgColor
        backView.layer.borderWidth = 3.0
        backView.layer.borderWidth = 3.0
        backView.layer.masksToBounds = true
        backView.layer.shadowRadius = 3.0
        backView.layer.shadowColor = UIColor(named: "SolaceYellow")?.cgColor
        
        buttonView.layer.cornerRadius = 15.0
        buttonView.layer.borderColor = UIColor(named: "SolaceYellow")?.cgColor
        buttonView.layer.borderWidth = 3.0
        buttonView.layer.borderWidth = 3.0
        buttonView.layer.masksToBounds = true
        buttonView.layer.shadowRadius = 3.0
        buttonView.layer.shadowColor = UIColor(named: "SolaceYellow")?.cgColor
        
        //loading data and listener
        loadUserHardship()
        loadBlockedList()
        listenForMatch()
        
        group.notify(queue: DispatchQueue.main) {
            //start search logic
            if self.searchStatus == "findPerson"{
                self.searchingLabel.text = "Searching for person to talk to"
                self.startFindPersonMethod()
            }else{
                self.searchingLabel.text = "Searching for Person in need"
                self.startListeningMethod()
            }
        }
        
    }
    
    //loading hardship
    func loadUserHardship(){
        group.enter()
        DispatchQueue.global(qos: .default).async { [self] in
            self.ref.child("Users").child(self.mUser!.uid).observeSingleEvent(of: .value) { (snapshot) in
                // print(snapshot)
                if let p = snapshot.value as? [String: Any] {
                    // Hardship pull and application
                    guard let hardshipPull = p["hardship"] as? String else {
                        print("Error with listName")
                        return
                    }
                    print(hardshipPull)
                    self.hardship = hardshipPull
                    self.group.leave()
                }
            }
        }
    }
    
    //block logic
    func loadBlockedList(){
        
        ref.child("Users").child(mUser!.uid).child("ChatList").observe(.value) { (snapshot) in
            self.blockedUids.removeAll()
            
            let enumerator = snapshot.children
            while let snap = enumerator.nextObject() as? DataSnapshot {
                //message info
                if let sp = snap.value as? [String: Any] {
                    
                    guard let id = sp["id"] as? String,
                          let blocked = sp["blocked"] as? Bool else {
                        print("ProfileVC - pullChatterUidsFromFirebase(): Failed to pull chatters")
                        return
                    }
                    if blocked {
                        self.blockedUids.append(id)
                    }
                }
            }
        }
    }
    
    //cancelled logic
    @IBAction func cancelPressed(_ sender: Any) {
        
        searchingAsHelper(isSearching: false)
        dismiss(animated: true, completion: nil)
    }
    
    //starting methods
    func startFindPersonMethod(){
        timer = Timer.scheduledTimer(timeInterval: 1.0, target: self, selector: #selector(onTimerFires), userInfo: nil, repeats: true)
        searchingForHelper()
    }
    
    func startListeningMethod(){
        searchingAsHelper(isSearching: true)
        timer = Timer.scheduledTimer(timeInterval: 1.0, target: self, selector: #selector(onTimerFires), userInfo: nil, repeats: true)
    }
    
    //searching methods
    func searchingAsHelper(isSearching: Bool){
        searching = true;
        print("helper \(self.hardship)")
        let searchingDict : Dictionary = ["searching": isSearching,
                                          "matched": false,
                                          "matched_user": "",
                                          "hardship":self.hardship,
                                          "uid":mUser!.uid] as [String : Any]
        
        ref.child("Searching").child(mUser!.uid).setValue(searchingDict)
    }
    
    func searchingForHelper(){
        Database.database().reference().child("Searching").queryOrdered(byChild: "searching").queryEqual(toValue: true).queryLimited(toLast: 1000).observeSingleEvent(of: .value) { (snapshot) in
            
            let enumerator = snapshot.children
            while let snap = enumerator.nextObject() as? DataSnapshot {
                
                print(snap)
                self.listenerList.removeAll()
                self.randomList.removeAll()
                
                if let sp = snap.value as? [String: Any] {
                    
                    guard let uid = sp["uid"] as? String,
                          let matched = sp["matched"] as? Bool, let matchedUser = sp["matched_user"] as? String, let hardship = sp["hardship"] as? String, let searching = sp["searching"] as? Bool else {
                        print("error getting searching Listener data")
                        return
                    }
                    
                    let modelListener  =  Listener_Data(_uid: uid, _matchedUser: matchedUser, _hardship: hardship, _searching: searching, _matched: matched)
                    
                    if !self.blockedUids.isEmpty && self.blockedUids.contains(modelListener.uid){
                        print("Blocked User: " + modelListener.uid)
                    }
                    else{
                        if modelListener.searching && modelListener.hardship.elementsEqual(self.hardship){
                            self.listenerList.append(modelListener)
                        }else if modelListener.searching{
                            self.randomList.append(modelListener)
                        }
                    }
                }
                
            }
            
            if self.listenerList.count > 0 {
                print("found listener")
                if let listener = self.listenerList.randomElement(){
                    //setting dictionary to update firebase
                    let foundDict : Dictionary = ["matched": true,
                                                  "matched_user": self.mUser!.uid,
                                                  "searching":listener.searching!,
                                                  "uid":listener.uid!, "hardship": listener.hardship!] as [String : Any]
                    
                    Database.database().reference().child("Searching").child(listener.uid!).setValue(foundDict)
                    
                    //setting listeneruid
                    self.listenerUid = listener.uid
                    
                    self.searchingLabel.text = "Matched"
                    self.buttonView.isHidden = true
                    self.timer?.invalidate()
                    self.timeLeft = 5
                    self.countdownLabel.text = "\(self.timeLeft) seconds left"
                    
                    
                    self.timer = Timer.scheduledTimer(timeInterval: 1.0, target: self, selector: #selector(self.foundListnerTimerFires), userInfo: nil, repeats: true)
                }
            }
            else if self.randomList.count > 0{
                print("found listener")
                if let listener = self.randomList.randomElement(){
                    //setting dictionary to update firebase
                    let foundDict : Dictionary = [
                        "matched": true,
                        "matched_user": self.mUser!.uid,
                        "searching":listener.searching!,
                        "uid":listener.uid!, "hardship": listener.hardship!] as [String : Any]
                    
                    Database.database().reference().child("Searching").child(listener.uid!).setValue(foundDict)
                    
                    //setting listeneruid
                    self.listenerUid = listener.uid
                    
                    self.searchingLabel.text = "Matched"
                    self.buttonView.isHidden = true
                    self.timer?.invalidate()
                    self.timeLeft = 5
                    self.countdownLabel.text = "\(self.timeLeft) seconds left"
                    
                    
                    self.timer = Timer.scheduledTimer(timeInterval: 1.0, target: self, selector: #selector(self.foundListnerTimerFires), userInfo: nil, repeats: true)
                }
            }else{
                print("none found. run cancel timer")
                self.searchingLabel.text = "We are sorry. All of our listeners are matched. please try again"
                self.buttonView.isHidden = true
                
                self.timer?.invalidate()
                self.timeLeft = 5
                self.countdownLabel.text = "\(self.timeLeft) seconds left"
                
                
                self.timer = Timer.scheduledTimer(timeInterval: 1.0, target: self, selector: #selector(self.noListnerTimerFires), userInfo: nil, repeats: true)
            }
            
            
        }
    }
    
    //match listener
    func listenForMatch(){
        ref.child("Searching").child(mUser!.uid).observe(.value) { (snapshot) in
            
            if self.searching {
                if let p = snapshot.value as? [String: Any] {
                    
                    if let matched = p["matched"] as? Bool, let matchedUid = p["matched_user"] as? String{
                        
                        if matched{
                            self.searchingLabel.text = "Matched"
                            
                            self.timer?.invalidate()
                            self.timeLeft = 5
                            self.countdownLabel.text = "\(self.timeLeft) seconds left"
                            
                            self.talkerUid = matchedUid
                            
                            self.timer = Timer.scheduledTimer(timeInterval: 1.0, target: self, selector: #selector(self.matchedTimerFires), userInfo: nil, repeats: true)
                        }
                    }
                }
            }
        }
    }
    
    //timer methods
    @objc func onTimerFires()
    {
        timeLeft -= 1
        countdownLabel.text = "\(timeLeft) seconds left"
        
        if timeLeft <= 0 {
            searchingAsHelper(isSearching: false)
            timer?.invalidate()
            timer = nil
            dismiss(animated: true, completion: nil)
        }
    }
    
    //timer methods
    @objc func noListnerTimerFires()
    {
        buttonView.isHidden = true
        timeLeft -= 1
        countdownLabel.text = "\(timeLeft) seconds left"
        
        if timeLeft <= 0 {
            
            
            timer?.invalidate()
            timer = nil
            dismiss(animated: true, completion: nil)
        }
    }
    
    //timer methods
    @objc func foundListnerTimerFires()
    {
        timeLeft -= 1
        countdownLabel.text = "\(timeLeft) seconds left"
        
        if timeLeft <= 0 {
            
            timer?.invalidate()
            timer = nil
            
            self.delegate?.openChat(withId: listenerUid)
            dismiss(animated: true, completion: nil)
            
            listenerUid = ""
        }
    }
    
    //timer methods
    @objc func matchedTimerFires()
    {
        timeLeft -= 1
        countdownLabel.text = "\(timeLeft) seconds left"
        
        if timeLeft <= 0 {
            
            timer?.invalidate()
            timer = nil
            
            //setting dictionary to update firebase
            let foundDict : Dictionary = [
                "matched": false,
                "matched_user": "",
                "hardship": ""] as [String : Any]
            
            Database.database().reference().child("Searching").child(mUser!.uid).setValue(foundDict)
            
            
            searchingAsHelper(isSearching: false)
            print("Presenting Chat");
            
            self.delegate?.openChat(withId: talkerUid)
            dismiss(animated: true, completion: nil)
            
            talkerUid = "";
            matched = false;
            
            timer?.invalidate()
            timer = nil
        }
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "toChat"{
            if let destinationVC = segue.destination as? ChatViewController{
                
                if !listenerUid.elementsEqual(""){
                    destinationVC.selectedUid = listenerUid
                    destinationVC.presentationController?.delegate = self;
                }else if !talkerUid.elementsEqual(""){
                    destinationVC.selectedUid = talkerUid
                    destinationVC.presentationController?.delegate = self;
                }
            }
        }
    }
}
