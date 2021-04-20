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

class FriendsViewController: UIViewController, UITableViewDelegate, UITableViewDataSource {
    
    var backgroundColor = UIColor()
    @IBOutlet weak var requestTableView: UITableView!
    @IBOutlet weak var noRequest: UILabel!
    @IBOutlet weak var familyTableView: UITableView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        requestTableView.dataSource = self
        requestTableView.delegate = self
        familyTableView.dataSource = self
        familyTableView.delegate = self
        
        
        FriendsClass.friendsClass.getCurrentUser
            {
                (user) in
                self.navigationController?.title = user.fullName
        }
        
        FriendsClass.friendsClass.addRequestObserver
            {
                if FriendsClass.friendsClass.requestList.count == 0
                {
                    self.noRequest.isHidden = false
                    print(FriendsClass.friendsClass.requestList)
                }
                else{
                    self.noRequest.isHidden = true
                    print(FriendsClass.friendsClass.requestList)
                self.requestTableView.reloadData()
                }
        }
        
        FriendsClass.friendsClass.addFriendObserver {
            self.familyTableView.reloadData()
            print(FriendsClass.friendsClass.friendList.count)
        }
        
    }
    
    @IBAction func SignoutTapped(_ sender: UIBarButtonItem) {
        
        let alert = UIAlertController(title: "Are You Sure", message: "Yes to complete Sign-Out", preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
        alert.addAction(UIAlertAction(title: "Yes", style: .default , handler: { _ in
            self.HandleLogOut()
        }))
        self.present(alert, animated: true, completion: nil)
    }
    
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
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        var cellToReturn = UITableViewCell()
        
        if tableView == requestTableView
        {
        let cell = tableView.dequeueReusableCell(withIdentifier: "UserCell", for: indexPath) as! UserTableViewCell
        
        
        // Configure the cell...
        let currentUser = FriendsClass.friendsClass.requestList[indexPath.row]
        
        cell.backgroundColor = backgroundColor
        cell.requestName.text = currentUser.fullName
        
        cell.setFunction {
            
            let alert = UIAlertController(title: "Add Friend", message: "Add \(currentUser.fullName) to the family.", preferredStyle: .alert)
            
            
            alert.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
            
            
            alert.addAction(UIAlertAction(title: "Yes", style: .default , handler: { _ in
                
                let id = currentUser.id
                FriendsClass.friendsClass.acceptFriendRequest(id)
                
            }))
            self.present(alert, animated: true, completion: nil)
            
            
        }
            cellToReturn = cell
             return cellToReturn
        }
        else if tableView == familyTableView
        {
            return cellToReturn
        }
        
        return cellToReturn
        
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 50
    }
    
    
    
}
