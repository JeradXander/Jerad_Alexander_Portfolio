//
//  ImageUtility.swift
//  Solace_IOS
//
//  Created by Jerad Alexander on 9/12/20.
//  Copyright © 2020 Jerad Alexander. All rights reserved.
//

import Foundation
import UIKit

extension UIImageView {
  public func maskCircle(anyImage: UIImage) {
    self.contentMode = UIView.ContentMode.scaleAspectFill
    self.layer.cornerRadius = self.frame.height / 2
    self.layer.masksToBounds = false
    self.clipsToBounds = true

   // make square(* must to make circle),
   // resize(reduce the kilobyte) and
   // fix rotation.
   self.image = anyImage
  }
}
