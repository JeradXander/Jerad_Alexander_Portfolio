//
//  Api_Data.swift
//  Solace_IOS
//
//  Created by Zakarias McDanal on 9/13/20.
//  Copyright © 2020 Jerad Alexander. All rights reserved.
//

import Foundation
import UIKit

class Api_Data {
    
    // Quote
    var quote: String
    
    // Book
    var bookTitle: String
    var bookDesc: String
    var bookThumb: String
    var bookLink: String
    
    //constructor
    init(quote: String, bookTitle: String, bookDesc: String, bookThumb: String, bookLink: String) {
        self.quote = quote
        self.bookTitle = bookTitle
        self.bookDesc = bookDesc
        self.bookThumb = bookThumb
        self.bookLink = bookLink
    }
}
