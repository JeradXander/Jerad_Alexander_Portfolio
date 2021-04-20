//
//  SenderTableViewCell.swift
//  Solace_IOS
//
//  Created by Jerad Alexander on 9/20/20.
//  Copyright © 2020 Jerad Alexander. All rights reserved.
//

import UIKit

class SenderTableViewCell: UITableViewCell {
    
    //identifier
    static let identifeir = "senderCell"
    
    //outlets
    @IBOutlet weak var TextFeild : UITextView!
    @IBOutlet var dateLabel: UILabel!
    
    static func nib()-> UINib{
        return UINib(nibName: "SenderTableViewCell", bundle: nil)
        
    }
    
    @IBAction func didTapOption(){
    }
    
    func configure(with title: String){
    }
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
        TextFeild.text = "Lorem ipsum dolor sit er elit lamet, consectetaur cillium adipisicing pecu, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. Nam liber te conscient to factor tum poen legum odioque civiuda."
    }
}
