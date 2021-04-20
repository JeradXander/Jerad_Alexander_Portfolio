//
//  UserTableViewCell.swift
//
//
//  Created by Jerad Alexander on 12/14/19.
//

import UIKit

class UserTableViewCell: UITableViewCell {
    
    //Outlets
    @IBOutlet weak var name: UIView!
    @IBOutlet weak var nameLabel: UILabel!
    @IBOutlet weak var requestName: UILabel!
    @IBOutlet weak var accept: UIButton!
    @IBOutlet weak var sendRequestButton: UIButton!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
        //setting up cell
        if sendRequestButton != nil{
            sendRequestButton.backgroundColor = .clear
            sendRequestButton.layer.cornerRadius = 5
            sendRequestButton.layer.borderWidth = 1
            sendRequestButton.layer.borderColor = UIColor.black.cgColor
        }
        else if accept != nil{
            accept.backgroundColor = .clear
            accept.layer.cornerRadius = 5
            accept.layer.borderWidth = 1
            accept.layer.borderColor = UIColor.black.cgColor
        }
    }
    
    var buttonFunction: (() -> (Void))!
    //action
    @IBAction func sendRequestPressed(_ sender: UIButton)
    {
        buttonFunction()
    }
    //action function
    func setFunction(_ function: @escaping () -> Void) {
        self.buttonFunction = function
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        
        // Configure the view for the selected state
    }
    
}
