//
//  Chatter_Data.swift
//  Solace_IOS
//
//  Created by Zakarias McDanal on 9/21/20.
//  Copyright © 2020 Jerad Alexander. All rights reserved.
//

import Foundation

class Chatter_Data {
    
    //variables
    var uid: String!
    var username: String!
    var profileUrl: String!
    var hardship: String!
    var karma: String!
    var lastMessage: String!
    
    //constructor
    init(Uid: String, Username: String, ProfileUrl: String, Hardship: String, Karma: String, lastMessage: String) {
        self.uid = Uid
        self.username = Username
        self.profileUrl = ProfileUrl
        self.hardship = Hardship
        self.karma = Karma
        self.lastMessage = lastMessage
    }
}
