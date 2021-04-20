//
//  Chat_Data.swift
//  Solace_IOS
//
//  Created by Jerad Alexander on 9/20/20.
//  Copyright © 2020 Jerad Alexander. All rights reserved.
//

import Foundation

class Chat_Data{
    
    //variables
    var message : String
    var receiver : String
    var sender : String
    var timeStamp : String
    var isSeen : Bool
    
    //constructor
    init(_message : String, _receiver : String , _timeStamp : String , _isSeen: Bool,_sender : String) {
        self.message = _message
        self.receiver = _receiver
        self.timeStamp = _timeStamp
        self.isSeen = _isSeen
        self.sender = _sender
    }
}
