//
//  LastMessage_Data.swift
//  Solace_IOS
//
//  Created by Zakarias McDanal on 9/24/20.
//  Copyright © 2020 Jerad Alexander. All rights reserved.
//

import Foundation

class LastMessage_Data {
    
    //variables
    var lastMessage = ""
    var fromUid = ""
    var count = 0
    
    //constructor
    init(LastMessage: String, FromUid: String, Count: Int) {
        self.lastMessage = LastMessage
        self.fromUid = FromUid
        self.count = Count
    }
}
