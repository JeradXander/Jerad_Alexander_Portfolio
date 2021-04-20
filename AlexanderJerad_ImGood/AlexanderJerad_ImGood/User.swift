//
//  User.swift
//  FirebaseFriendRequest
//
//  Created by Kiran Kunigiri on 7/12/16.
//  Copyright © 2016 Kiran. All rights reserved.
//

import Foundation

class User {
    //properties
    var email: String
    var id: String
    var fullName : String
    var coordinates : String?
    var goodStatus : Bool
    var phoneNum : String
    
    //initializer
    init(userEmail: String, userID: String, fn: String, coor: String?, goodStat: Bool , phoneNum : String)
    {
        self.email = userEmail
        self.id = userID
        self.fullName = fn
        self.coordinates = coor
        self.goodStatus = goodStat
        self.phoneNum = phoneNum
    }
    
    //default initilizer
    init()
    {
        self.email = "userEmail"
        self.id = "userID"
        self.fullName = "fn"
        self.coordinates = "coor"
        self.goodStatus = true
        self.phoneNum = "7071010100"
    }
}
