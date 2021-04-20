//
//  FriendRequestViewController.swift
//  AlexanderJerad_ImGood
//
//  Created by Jerad Alexander on 12/15/19.
//  Copyright © 2019 Jerad Alexander. All rights reserved.
//

import UIKit
import FirebaseDatabase
import Firebase
import MessageUI


class FriendsViewController: UIViewController, UITableViewDelegate, UITableViewDataSource, MFMessageComposeViewControllerDelegate{
    
    //variables
    var backgroundColor = UIColor()
    var redColor = UIColor()
    var greenColor = UIColor()
    var phoneString = ""
    
    //outlets
    @IBOutlet weak var requestTableView: UITableView!
    @IBOutlet weak var familyTableView: UITableView!
    @IBOutlet weak var addFriendButton: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        //setting title and background colot
        navigationItem.title = "Friends"
        view.backgroundColor = backgroundColor
        
        
        //setting delegates and date sources for both tableviews
        requestTableView.dataSource = self
        requestTableView.delegate = self
        requestTableView.backgroundColor = backgroundColor
        
        familyTableView.dataSource = self
        familyTableView.delegate = self
        familyTableView.backgroundColor = backgroundColor
        
        FriendsClass.friendsClass.getCurrentUser
            {
                (user) in
        }
        
        //observer for request
        FriendsClass.friendsClass.addRequestObserver
            {
                if FriendsClass.friendsClass.requestList.count == 0
                {
                    
                }
                else{
                    //reloading tableviews
                    self.requestTableView.reloadData()
                    self.familyTableView.reloadData()
                }
        }
        //observer for adding friends
        FriendsClass.friendsClass.addFriendObserver {
            self.familyTableView.reloadData()
            self.requestTableView.reloadData()
            print(FriendsClass.friendsClass.friendList.count)
        }
    }
    
    //action for add friends button
    @IBAction func addPushed(_ sender: Any)
    {
        performSegue(withIdentifier: "allFriends", sender: UIButton.self)
    }
    
    //log out action
    @IBAction func SignoutTapped(_ sender: UIBarButtonItem) {
        
        let alert = UIAlertController(title: "Are You Sure", message: "Yes to complete Sign-Out", preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
        alert.addAction(UIAlertAction(title: "Yes", style: .default , handler: { _ in
            self.HandleLogOut()
        }))
        self.present(alert, animated: true, completion: nil)
    }
    
    // log out function
    func HandleLogOut()
    {
        do
        {
            try Auth.auth().signOut()
        }
        catch let logouterror
        {
            print(logouterror)
        }
        performSegue(withIdentifier: "goToSignIn", sender: self)
    }
}

//MARK: - Extension
extension FriendsViewController
{
    // titles for tableviews
    func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) -> String? {
        
        let error = "error"
        if tableView == requestTableView
        {
            return "Requests"
        }
        else if tableView == familyTableView
        {
            return "Family"
        }
        
        return error
    }
    // Number of rows for tableviews
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        
        if tableView == requestTableView
        {
            return FriendsClass.friendsClass.requestList.count
        }
        else if tableView == familyTableView
        {
            return FriendsClass.friendsClass.friendList.count
        }
        return 0
    }
    //MARK: - cell setup for tableviews
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cellToReturn = UITableViewCell()
        
        if tableView == requestTableView
        {
            let cell = tableView.dequeueReusableCell(withIdentifier: "UserCell", for: indexPath) as! UserTableViewCell
            
            
            // Configure the cell...
            let currentUser = FriendsClass.friendsClass.requestList[indexPath.row]
            
            cell.backgroundColor = backgroundColor
            cell.requestName.text = currentUser.fullName
            
            //button function adding friend
            cell.setFunction {
                
                let alert = UIAlertController(title: "Add Friend", message: "Add \(currentUser.fullName) to the family.", preferredStyle: .alert)
                
                alert.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
                alert.addAction(UIAlertAction(title: "Yes", style: .default , handler: { _ in
                    
                    let id = currentUser.id
                    //adding friend function
                    FriendsClass.friendsClass.acceptFriendRequest(id)
                    self.requestTableView.reloadData()
                    
                }))
                self.present(alert, animated: true, completion: nil)
                
                
            }
            cellToReturn = cell
            
        }
            //for family tableview
        else if tableView == familyTableView
        {
            let cell = tableView.dequeueReusableCell(withIdentifier: "UserCell", for: indexPath) as! familyMemberTableViewCell
            // Configure the cell...
            let currentUser = FriendsClass.friendsClass.friendList[indexPath.row]
            
            //conditional for view
            if currentUser.goodStatus == true
            {
                //setting up cell
                cell.button.titleLabel?.text = "Message"
                cell.isGoodLabel.text = "Status: Good"
                cell.backgroundColor = greenColor
                cell.name.text = currentUser.fullName
                
                //button function
                cell.setFunction {
                    self.phoneString = "1\(currentUser.phoneNum)"
                    self.displayMessage()
                }
                cellToReturn = cell
            }
            else if currentUser.goodStatus == false
            {
                //setting up cell
                cell.button.titleLabel?.text = "Message"
                cell.name.text = currentUser.fullName
                cell.isGoodLabel.text = "Status: Should check-in"
                cell.backgroundColor = redColor
                cell.setFunction {
                    self.phoneString = "1\(currentUser.phoneNum)"
                    //calling display message controller
                    self.displayMessage()
                }
                cellToReturn = cell
            }
        }
        //cell to return
        return cellToReturn
    }
    
    //height for row
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 50
    }
    
    //MARK: - view for footer
    func tableView(_ tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
        var footer = UIView()
        //setting footer view to color of background
        if tableView == familyTableView
        {
            let footerView = UIView(frame: CGRect(x: 0, y: 0, width: tableView.bounds.size.width, height: 50))
            footerView.backgroundColor = backgroundColor
            footer = footerView
        }
        else if tableView == requestTableView{
            let footerView = UIView(frame: CGRect(x: 0, y: 0, width: tableView.bounds.size.width, height: 50))
            footerView.backgroundColor = backgroundColor
            footer = footerView
        }
        return footer
    }
    
    
    //MARK: - segue prepare
    override func prepare(for segue: UIStoryboardSegue, sender: Any?)
    {
        if segue.identifier == "allFriends"
        {
            if let AllView = segue.destination as? AllUserTableViewController
            {
                AllView.backgroundColor = backgroundColor
            }
        }
    }
    
    //MARK: - compose message controller
    func messageComposeViewController(_ controller: MFMessageComposeViewController, didFinishWith result: MessageComposeResult) {
        //dismissing for cancel or send function
        dismiss(animated: true, completion: nil)
        
    }
    //function for displaying messages
    func displayMessage()
    {
        //setting up compse controller
        let messageVc = MFMessageComposeViewController()
        messageVc.messageComposeDelegate = self
        //user selected is recipient
        messageVc.recipients = [phoneString]
        messageVc.body = "Hello, is everything good?"
        
        //conditional for sending text
        if MFMessageComposeViewController.canSendText()
        {
            self.present(messageVc, animated: true, completion: nil)
        }
        else{
            print("Cant text")
            self.present(messageVc, animated: true, completion: nil)
        }
    }
}
