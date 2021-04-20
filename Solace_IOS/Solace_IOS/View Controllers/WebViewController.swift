//
//  WebViewController.swift
//  Solace_IOS
//
//  Created by Zakarias McDanal on 9/17/20.
//  Copyright © 2020 Jerad Alexander. All rights reserved.
//

import UIKit
import WebKit

class WebViewController: UIViewController, WKNavigationDelegate  {

     var urlToOpen: String!
       
    @IBOutlet var webView: WKWebView!
    
    override func viewDidLoad() {
           super.viewDidLoad()
           webView = WKWebView()
           webView.navigationDelegate = self
           view = webView
           
           let url = URL(string: urlToOpen)!
           webView.load(URLRequest(url: url))
           webView.allowsBackForwardNavigationGestures = true
       }

}
