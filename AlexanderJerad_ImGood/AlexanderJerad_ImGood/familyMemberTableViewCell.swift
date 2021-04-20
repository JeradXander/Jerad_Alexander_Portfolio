//
//  familyMemberTableViewCell.swift
//  AlexanderJerad_ImGood
//
//  Created by Jerad Alexander on 12/15/19.
//  Copyright © 2019 Jerad Alexander. All rights reserved.
//

import UIKit

class familyMemberTableViewCell: UITableViewCell {
    
    //Outlets
    @IBOutlet weak var name: UILabel!
    @IBOutlet weak var isGoodLabel: UILabel!
    @IBOutlet weak var button: UIButton!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
        button.backgroundColor = .clear
        button.layer.cornerRadius = 5
        button.layer.borderWidth = 1
        button.layer.borderColor = UIColor.black.cgColor
    }
    //button function
    var buttonFunction: (() -> (Void))!
    
    //action for button
    @IBAction func sendRequestPressed(_ sender: UIButton)
    {
        buttonFunction()
    }
    //function for action
    func setFunction(_ function: @escaping () -> Void) {
        
        self.buttonFunction = function
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        
        // Configure the view for the selected state
    }
    
}
