//
//  CustomResource.swift
//  Solace_IOS
//
//  Created by Zakarias McDanal on 9/17/20.
//  Copyright © 2020 Jerad Alexander. All rights reserved.
//

import Foundation
import UIKit

class CustomResource: UILabel {
    
    var name : String = ""

    convenience init(name: String) {
        self.init()
        self.name = name
    }
}
