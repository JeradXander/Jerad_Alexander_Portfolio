//
//  SignInViewController.swift
//  AlexanderJerad_ImGood
//
//  Created by Jerad Alexander on 12/8/19.
//  Copyright © 2019 Jerad Alexander. All rights reserved.
//

import UIKit
import FirebaseUI

class SignInViewController: FUIAuthPickerViewController{
//MARK: - some customization to default firebase auth controller
    override func viewDidLoad() {
        super.viewDidLoad()
        //set up for back image
        let width = UIScreen.main.bounds.size.width
        let height = UIScreen.main.bounds.size.height / 2.6
        let backImage = UIImageView(frame: CGRect(x: 0, y: 0, width: width, height: height))
        backImage.center.y = self.view.center.y // for vertical
        backImage.image = UIImage(named: "I'mGoodIcon")
        backImage.contentMode = UIView.ContentMode.scaleAspectFit
        backImage.backgroundColor = .red
        view.insertSubview(backImage, at: 1)
    }
}
