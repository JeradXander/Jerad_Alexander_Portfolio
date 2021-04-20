//
//  FriendClass.swift
//  AlexanderJerad_ImGood
//
//  Created by Jerad Alexander on 12/14/19.
//  Copyright © 2019 Jerad Alexander. All rights reserved.
//

import Foundation
import FirebaseDatabase
import Firebase
import FirebaseUI


class FriendsClass
{
    static let friendsClass = FriendsClass()
    
    let usersReference = Database.database().reference().child("Users")
    // The list of all users
    var userList = [User]()
    // list of friends
    var friendList = [User]()
    var requestList = [User]()
    
    var getCurrentUserRef : DatabaseReference
    {
        let uid = Auth.auth().currentUser!.uid
        return usersReference.child("\(uid)")
    }
    
  // The Firebase reference to the current user's friend tree
       var usersFriendRef: DatabaseReference
       {
           return getCurrentUserRef.child("friends")
       }
       
    //  The Firebase reference to the current user's friend request tree
       var getCurrentUserRequestRef : DatabaseReference
       {
           return getCurrentUserRef.child("requests")
       }
       
   // The current user's id
       var currentUserUid: String
       {
           let uid = Auth.auth().currentUser!.uid
           return uid
       }
    
   //Gets the current User object for the specified user id
       func getCurrentUser(_ completion: @escaping (User) -> Void)
       {
           getCurrentUserRef.observeSingleEvent(of: DataEventType.value, with: { (snapshot) in
            let name = snapshot.childSnapshot(forPath: "fullName").value as! String
            let email = snapshot.childSnapshot(forPath: "email").value as! String
            let id = snapshot.key
            let phone = snapshot.childSnapshot(forPath: "phoneNumber").value as! String
            let coor = snapshot.childSnapshot(forPath: "LastCheckInLoc").value as! String
            let goodStatus = snapshot.childSnapshot(forPath: "GoodStatus").value as! Bool
                          
                       completion(User(userEmail: email, userID: id, fn: name, coor: coor, goodStat: goodStatus,phoneNum: phone))
           })
       }
    // Gets the User object for the specified user id */
       func getUser(_ userID: String, completion: @escaping (User) -> Void)
       {
           usersReference.child(userID).observeSingleEvent(of: DataEventType.value, with: { (snapshot) in
              let name = snapshot.childSnapshot(forPath: "fullName").value as! String
               let email = snapshot.childSnapshot(forPath: "email").value as! String
               let id = snapshot.key
             let phone = snapshot.childSnapshot(forPath: "phoneNumber").value as! String
               let coor = snapshot.childSnapshot(forPath: "LastCheckInLoc").value as! String
              let goodStatus = snapshot.childSnapshot(forPath: "GoodStatus").value as! Bool
               
            completion(User(userEmail: email, userID: id, fn: name, coor: coor, goodStat: goodStatus, phoneNum: phone))
           })
       }
    
    //MARK: - functions to get all users
   
    
   // Adds a user observer. The completion function will run every time this list changes, allowing you to update your UI.
    func addUserObserver(_ update: @escaping () -> Void)
    {
        FriendsClass.friendsClass.usersReference.observe(DataEventType.value, with: { (snapshot) in
            
            self.userList.removeAll()
            
            for child in snapshot.children.allObjects as! [DataSnapshot] {
                let email = child.childSnapshot(forPath: "email").value as! String
                 let name = child.childSnapshot(forPath: "fullName").value as! String
                 let phone = child.childSnapshot(forPath: "phoneNumber").value as! String
                
                var isGood = false
                if let goodStatus = child.childSnapshot(forPath: "GoodStatus").value as? Bool
                {
                    isGood = goodStatus
                }
                
                var coor = ""
                 if let coors = child.childSnapshot(forPath: "LastCheckInLoc").value as? String
                 {
                    coor = coors
                 }
                
                
                if email != Auth.auth().currentUser?.email! {
                    
                 
                    self.userList.append(User(userEmail: email, userID: child.key, fn: name, coor: coor, goodStat: isGood, phoneNum: phone))
                }
            }
            update()
        })
    }
    //Removes the user observer when done with view
    func removeUserObserver()
    {
        usersReference.removeAllObservers()
    }
    
    //MARK: - functions to get all Friends
    
    
    // MARK: - All friends

    // Adds a friend observer.
     
    func addFriendObserver(_ update: @escaping () -> Void)
    {
        usersFriendRef.observe(DataEventType.value, with: { (snapshot) in
            
            self.friendList.removeAll()
            
            for child in snapshot.children.allObjects as! [DataSnapshot] {
                let id = child.key
                self.getUser(id, completion: { (user) in
                    
                
                    self.friendList.append(user)
                    update()
                })
            }
            // If there are no children, run completion here instead
            if snapshot.childrenCount == 0 {
                update()
            }
        })
    }
    // Removes the friend observer.
     
    func removeFriendObserver()
    {
        getCurrentUserRef.removeAllObservers()
    }
    
       
    //MARK: - Functions for friend requests
    // Sends a friend request to the user with the specified id */
       func sendRequestToUser(_ userID: String)
       {
           usersReference.child(userID).child("requests").child(currentUserUid).setValue(true)
       }
       
      // Unfriends the user with the specified id
       func removeFriend(_ userID: String)
       {
           getCurrentUserRef.child("friends").child(userID).removeValue()
           usersReference.child(userID).child("friends").child(currentUserUid).removeValue()
       }
       
       // Accepts a friend request from the user with the specified id */
       func acceptFriendRequest(_ userID: String)
       {
           getCurrentUserRef.child("requests").child(userID).removeValue()
           getCurrentUserRef.child("friends").child(userID).setValue(true)
           usersReference.child(userID).child("friends").child(currentUserUid).setValue(true)
           usersReference.child(userID).child("requests").child(currentUserUid).removeValue()
       }
    
    //MARK: - Functions for request
    func addRequestObserver(_ update: @escaping () -> Void)
    {
    getCurrentUserRequestRef.observe(DataEventType.value, with: { (snapshot) in
        self.requestList.removeAll()
        for child in snapshot.children.allObjects as! [DataSnapshot]
        {
            let id = child.key
            
            self.getUser(id, completion: { (user) in
                self.requestList.append(user)
                update()
            })
        }
        // If there are no children, run completion here instead
        if snapshot.childrenCount == 0 {
            update()
        }
    })
}
    
    // Removes the friend request observer.
    func removeRequestObserver()
    {
       getCurrentUserRef.removeAllObservers()
    }
}
