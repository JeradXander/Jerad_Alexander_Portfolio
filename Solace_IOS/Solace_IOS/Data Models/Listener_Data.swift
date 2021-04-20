//
//  Listener_Data.swift
//  Solace_IOS
//
//  Created by Jerad Alexander on 9/24/20.
//  Copyright © 2020 Jerad Alexander. All rights reserved.
//

import Foundation

class Listener_Data {
    
    //variables
    var uid: String!
    var matchedUser: String!
    var hardship: String!
    var searching : Bool!
    var matched : Bool!
    
    //constructor
    init(_uid : String, _matchedUser: String, _hardship: String, _searching: Bool,_matched: Bool) {
        self.uid = _uid
        self.hardship = _hardship
        self.searching = _searching
        self.matched = _matched
        self.matchedUser = _matchedUser
    }
}
