//
//  PastConversationCell.swift
//  Solace_IOS
//
//  Created by Zakarias McDanal on 9/21/20.
//  Copyright © 2020 Jerad Alexander. All rights reserved.
//

import UIKit

class PastConversationCell: UITableViewCell {
    
    //delagate
    var cellDelegate: PastConvoDelegate?
    
    //outlets
    @IBOutlet weak var profilePic: UIImageView!
    @IBOutlet weak var usernameLbl: UILabel!
    @IBOutlet weak var lastMessageLbl: UILabel!
    @IBOutlet weak var hamburgerMenuBtn: UIButton!
    @IBOutlet var chatView: UIView!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }
    
    @IBAction func hamburgerClicked(_ sender: UIButton) {
        cellDelegate?.didPressButton(sender.tag)
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)
        
        // Configure the view for the selected state
    }
}
