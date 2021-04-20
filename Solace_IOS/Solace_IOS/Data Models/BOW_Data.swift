//
//  BOW_Data.swift
//  Solace_IOS
//
//  Created by Zakarias McDanal on 9/26/20.
//  Copyright © 2020 Jerad Alexander. All rights reserved.
//

import Foundation

class BOW_Data : Encodable, Decodable {
    
    //variable
    var bookTitle: String
    var bookDesc: String
    var bookThumb: String
    var bookLink: String
    var timestamp: Int
    
    //constructor
    init(BookTitle: String, BookDesc: String, BookThumb: String, BookLink: String, Timestamp: Int) {
        self.bookTitle = BookTitle
        self.bookDesc = BookDesc
        self.bookThumb = BookThumb
        self.bookLink = BookLink
        self.timestamp = Timestamp
    }
}
