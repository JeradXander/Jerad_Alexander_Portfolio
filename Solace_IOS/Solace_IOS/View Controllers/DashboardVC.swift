//
//  ViewController.swift
//  Solace_IOS
//
//  Created by Jerad Alexander on 9/7/20.
//  Copyright © 2020 Jerad Alexander. All rights reserved.
//

import UIKit
import WebKit
import Firebase
import Alamofire
import AlamofireImage

class DashboardVC: UIViewController, UIAdaptivePresentationControllerDelegate {
    
    //outlets
    @IBOutlet var chatViews: [UIView]!
    @IBOutlet var findListnerButton: UIButton!
    @IBOutlet var listenButton: UIButton!
    @IBOutlet var resourceViews: [UIView]!
    @IBOutlet var resourceLabels: [UILabel]!
    @IBOutlet weak var bookOfTheWeekView: UIView!
    @IBOutlet weak var dashboardView: UIView!
    @IBOutlet weak var quoteLbl: UILabel!
    @IBOutlet weak var bookImg: UIImageView!
    @IBOutlet weak var bookTitleLbl: UILabel!
    @IBOutlet var bookDescLbl: UITextView!
    @IBOutlet weak var hotlineImg: UIImageView!
    @IBOutlet weak var imageView: UIImageView!
    
    //variables
    var date = Date()
    var DAY_IN_MS = 1000 * 60 * 60 * 24;
    var url: String!
    var searchStatus = ""
    let ref = Database.database().reference()
    var selecteduidForChat = ""
    var bookOfWeek: BOW_Data!
    // Object to populate the screen with quote and book of the week
    var api_data: Api_Data!
    // Stock screan data that populates if quote api or book of the week api doesnt work out
    var quote = "I always advice people - Don't wait ! Do something when you are young, " +
        "when you have no responsibilities. Invest time in yourself to have great Experiences" +
        " that are going to enrich you, then you can't possibly lose."
    var bTitle = "The No-Self Help Book"
    var bDescription = "It’s time to get over your self! Written by a clinical psychologist and student of Eastern philosophy, this handy little guide offers a radical solution to anyone struggling with self-doubt, self-esteem, and self-defeating thoughts: “no-self help.” By breaking free of your own self-limiting beliefs, you’ll discover your infinite potential. There is an insidious, global identity theft occurring that has robbed people of their very recognition of their true selves. The culprit—indeed the mastermind of this crisis—has committed the inside job of creating and promoting the idea that we are all a separate self, which is the chief source of our daily distress and dissatisfaction. No more than a narrative of personhood pieced together from disparate neural activations, the self we believe ourselves to be in our own minds—although quite capable of being affirming, inspiring, and constructive—often spews forth a distressing flow of worry and second-guessing, blaming and shaming, regret and guilt. This book offers an antidote to this epidemic of stolen identity, isolation, and self-deprecation: no-self (a concept known in Buddhist philosophy as anatta or anatman). The No-Self Help Book turns the idea of self-improvement on its head, arguing that the key to well-being lies not in the relentless pursuit of bettering one’s self but in the recognition of the self as a false identity born in the mind. Rather than identifying with a small, relative sense of self, this book encourages you to embrace a liberating alternative—an expansive awareness that is flexible and open to experiencing life as an ongoing and ever-changing process, without attachment to personal outcomes or storylines. To help you make this leap from self to no-self, the book provides forty bite-sized chapters full of clever and inspiring insights based in positive psychology and non-duality—a philosophy that asserts there is no real separation between any of us. So, if you’re tired of “self-help” and you’re ready to explore who you are beyond the self, let The No-Self Help Book be your guide."
    var bLink = "https://play.google.com/store/books/details?id=zeFiDwAAQBAJ"
    var bThumbnail = "http://books.google.com/books/content?id=zeFiDwAAQBAJ&printsec=frontcover&img=1&zoom=5&edge=curl&source=gbs_api"
    
    override func viewDidAppear(_ animated: Bool) {
        //seting label propertiess
        bookTitleLbl.backgroundColor = .white
        bookTitleLbl.textColor = .black
    }
    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.addBackground()
        
        // New date declared for book of the week user defaults
        date = Date()
        // UI setup
        for view in chatViews {
            view.layer.cornerRadius = 10
            let gold = UIColor(hex: "#ffe700ff")
            view.layer.backgroundColor = gold?.cgColor
        }
        
        //label and button setup
        bookTitleLbl.adjustsFontSizeToFitWidth = false
        bookTitleLbl.lineBreakMode = .byTruncatingTail
        
        findListnerButton.layer.masksToBounds = true
        findListnerButton.layer.cornerRadius = 10
        listenButton.layer.masksToBounds = true
        listenButton.layer.cornerRadius = 10
        bookOfTheWeekView.layer.cornerRadius = 10
        
        //corner radius
        for view in resourceViews {
            view.layer.cornerRadius = 20
        }
        
        // Json Pulls
        downloadJSON(atURL: "https://type.fit/api/quotes")
        
        //tap gestures
        let tap = UITapGestureRecognizer(target: self, action: #selector(DashboardVC.openPage))
        bookImg.addGestureRecognizer(tap)
        bookImg.isUserInteractionEnabled = true
        
        let tap2 = UITapGestureRecognizer(target: self, action: #selector(DashboardVC.openPage))
        hotlineImg.addGestureRecognizer(tap2)
        hotlineImg.isUserInteractionEnabled = true
        
        //gesture for labels
        for label in resourceLabels {
            let tap = UITapGestureRecognizer(target: self, action: #selector(DashboardVC.openPage))
            label.addGestureRecognizer(tap)
            label.isUserInteractionEnabled = true
        }
    }
    
    // All UITapGestures on the dashboard output here
    @objc func openPage(sender:UITapGestureRecognizer) {
        // switch for resources and buttons
        switch sender.view!.tag {
        case 0:
            url = "http://www.adaa.org/living-with-anxiety/ask-and-learn/resources"
            performSegue(withIdentifier: "toWebView", sender: nil)
        case 1:
            url = "http://www.autismspeaks.org/"
            performSegue(withIdentifier: "toWebView", sender: nil)
        case 2:
            url = "http://www.dbsalliance.org/"
            performSegue(withIdentifier: "toWebView", sender: nil)
        case 3:
            url = "http://www.glbtnationalhelpcenter.org/"
            performSegue(withIdentifier: "toWebView", sender: nil)
        case 4:
            url = "https://www.nationaleatingdisorders.org/"
            performSegue(withIdentifier: "toWebView", sender: nil)
        case 5:
            url = "http://www.samhsa.gov/"
            performSegue(withIdentifier: "toWebView", sender: nil)
        case 6:
            url = "http://www.mayoclinic.org/diseases-conditions/mental-illness/basics/definition/con-20033813"
            performSegue(withIdentifier: "toWebView", sender: nil)
        case 7:
            url = "https://www.nami.org/Find-Support/Veterans-and-Active-Duty"
            performSegue(withIdentifier: "toWebView", sender: nil)
        case 8:
            if let phoneCallURL = URL(string: "tel://18007997233") {
                
                //opens phone
                let application:UIApplication = UIApplication.shared
                if (application.canOpenURL(phoneCallURL)) {
                    application.open(phoneCallURL, options: [:], completionHandler: nil)
                }
            }
        case 9:
            //starts phone
            if let phoneCallURL = URL(string: "tel://18002738255") {
                
                let application:UIApplication = UIApplication.shared
                if (application.canOpenURL(phoneCallURL)) {
                    application.open(phoneCallURL, options: [:], completionHandler: nil)
                }
            }
        case 10:
            //opens web view
            url = api_data.bookLink
            performSegue(withIdentifier: "toWebView", sender: nil)
        case 11:
            //to hotline
            performSegue(withIdentifier: "toHotlineFromDash", sender: nil)
        default:
            print("Error with openWebPage() in DashboardVC")
        }
        
    }
    
    // For all segues, this is where the luggage to take with is packed
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        
        if segue.identifier == "toSolace"{
            if let destination = segue.destination as? SolaceViewController {
                destination.searchStatus = searchStatus
                destination.delegate = self
                destination.presentationController?.delegate = self;
            }
        }else if segue.identifier == "toChat"{
            if let destinationVC = segue.destination as? ChatViewController{
                
                destinationVC.selectedUid = selecteduidForChat
                destinationVC.presentationController?.delegate = self;
            }
        }else{
            if let destination = segue.destination as? WebViewController {
                destination.urlToOpen = url
            }
        }
    }
    
    //My json pull which obtains everything to go inside my object
    func downloadJSON(atURL urlString: String) {
        //Create a default configuration
        let config = URLSessionConfiguration.default
        
        //Create a session
        let session = URLSession(configuration: config)
        
        //Validate the URL to ensure it is not broken link
        if let validURL = URL(string: urlString) {
            
            //Create a task to send the request and download whatever is found at validURL
            //Make sure that your are choosing the correct dataTask with URLRequest and not URL
            let task = session.dataTask(with: validURL, completionHandler: { (data, response, error) in
                
                //Bail Out on error
                if error != nil { assertionFailure(); return }
                
                //Check the response, statusCode, and data
                guard let response = response as? HTTPURLResponse,
                      response.statusCode == 200,
                      let data = data
                else { assertionFailure(); return }
                
                do {
                    //De-Serialize data object
                    if let json = try JSONSerialization.jsonObject(with: data, options: .mutableContainers) as? [[String: Any]] {
                        
                        
                        let randomItem = json.randomElement()
                        guard let text = randomItem!["text"] as? String
                        else { assertionFailure(); return }
                        
                        var fullText = ""
                        
                        if let author = randomItem!["author"] as? String {
                            fullText = "\"\(text)\" - \(author)"
                        } else {
                            fullText = "\"\(text)\" - Unknown"
                        }
                        
                        // Quote is received and now we wait on the book of the week
                        self.quote = fullText
                        
                        // Pulls book of the week from User Defaults
                        if let data = UserDefaults.standard.data(forKey: "bow") {
                            if let decoded = try? JSONDecoder().decode(BOW_Data.self, from: data) {
                                self.bookOfWeek = decoded
                                if (self.isWithinWeek(lastBook: self.bookOfWeek.timestamp)) {
                                    DispatchQueue.main.async {
                                        self.api_data = Api_Data(quote: self.quote, bookTitle: self.bookOfWeek.bookTitle, bookDesc: self.bookOfWeek.bookDesc, bookThumb: self.bookOfWeek.bookThumb, bookLink: self.bookOfWeek.bookLink)
                                        self.quoteLbl.text = self.api_data.quote
                                        self.bookTitleLbl.text = self.api_data.bookTitle
                                        self.bookDescLbl.text = self.api_data.bookDesc
                                        
                                        let url = URL(string: self.api_data.bookThumb)!
                                        self.bookImg.af.setImage(withURL: url)
                                        
                                    }
                                }
                                // If past the date of a full week, new pull is made
                                else {
                                    if let validURL = URL(string: "https://www.googleapis.com/books/v1/volumes?q=self%20help") {
                                        
                                        let task = session.dataTask(with: validURL, completionHandler: { (data, response, error) in
                                            
                                            if error != nil { assertionFailure(); return }
                                            var dataToPass = Data()
                                            if let response = response as? HTTPURLResponse {
                                                if (response.statusCode == 200) {
                                                    dataToPass = data!
                                                    
                                                    
                                                    do {
                                                        if let json = try JSONSerialization.jsonObject(with: dataToPass, options: .mutableContainers) as? [String: Any] {
                                                            
                                                            guard let outerData = json["items"] as? [[String: Any]]
                                                            else { assertionFailure(); return }
                                                            
                                                            // Random pull from api
                                                            let randomItem = outerData.randomElement()
                                                            
                                                            if let vol = randomItem!["volumeInfo"] as? [String: Any] {
                                                                
                                                                
                                                                if let titl = vol["title"] as? String {
                                                                    self.bTitle = titl
                                                                }
                                                                if let desc = vol["description"] as? String {
                                                                    self.bDescription = desc
                                                                }
                                                                if let lin = vol["canonicalVolumeLink"] as? String {
                                                                    self.bLink = lin
                                                                }
                                                                if let thumbObject = vol["imageLinks"] as? [String: Any] {
                                                                    if let thumb = thumbObject["smallThumbnail"] as? String {
                                                                        self.bThumbnail = thumb
                                                                    }
                                                                }
                                                                // new save to user defaults
                                                                let currentDate = Date()
                                                                let bowToSave = BOW_Data(BookTitle: self.bTitle, BookDesc: self.bDescription, BookThumb: self.bThumbnail, BookLink: self.bLink, Timestamp: self.currentTimeInMiliseconds(thisDate: currentDate))
                                                                if let encoded = try? JSONEncoder().encode(bowToSave) {
                                                                    UserDefaults.standard.set(encoded, forKey: "bow")
                                                                }
                                                                
                                                            }
                                                        }
                                                    }
                                                    catch {
                                                        print(error.localizedDescription)
                                                        assertionFailure()
                                                    }
                                                } else {
                                                    print("NOT OKAYYYYYYY")
                                                }
                                            }
                                            //Do UI Stuff
                                            DispatchQueue.main.async {
                                                self.api_data = Api_Data(quote: self.quote, bookTitle: self.bTitle, bookDesc: self.bDescription, bookThumb: self.bThumbnail, bookLink: self.bLink)
                                                self.quoteLbl.text = self.api_data.quote
                                                self.bookTitleLbl.text = self.api_data.bookTitle
                                                self.bookDescLbl.text = self.api_data.bookDesc
                                                
                                                let url = URL(string: self.api_data.bookThumb)!
                                                self.bookImg.af.setImage(withURL: url)
                                                
                                            }
                                        })
                                        task.resume()
                                    }
                                    
                                }
                            }
                        } else {
                            
                            // Usually ran only on first ever use of app
                            if let validURL = URL(string: "https://www.googleapis.com/books/v1/volumes?q=self%20help") {
                                
                                let task = session.dataTask(with: validURL, completionHandler: { (data, response, error) in
                                    
                                    if error != nil { assertionFailure(); return }
                                    var dataToPass = Data()
                                    if let response = response as? HTTPURLResponse {
                                        if (response.statusCode == 200) {
                                            dataToPass = data!
                                            
                                            do {
                                                if let json = try JSONSerialization.jsonObject(with: dataToPass, options: .mutableContainers) as? [String: Any] {
                                                    
                                                    guard let outerData = json["items"] as? [[String: Any]]
                                                    else { assertionFailure(); return }
                                                    
                                                    let randomItem = outerData.randomElement()
                                                    
                                                    if let vol = randomItem!["volumeInfo"] as? [String: Any] {
                                                        
                                                        if let titl = vol["title"] as? String {
                                                            self.bTitle = titl
                                                        }
                                                        if let desc = vol["description"] as? String {
                                                            self.bDescription = desc
                                                        }
                                                        if let lin = vol["canonicalVolumeLink"] as? String {
                                                            self.bLink = lin
                                                        }
                                                        if let thumbObject = vol["imageLinks"] as? [String: Any] {
                                                            if let thumb = thumbObject["smallThumbnail"] as? String {
                                                                self.bThumbnail = thumb
                                                            }
                                                        }
                                                        // Save to User Defaults
                                                        let currentDate = Date()
                                                        let bowToSave = BOW_Data(BookTitle: self.bTitle, BookDesc: self.bDescription, BookThumb: self.bThumbnail, BookLink: self.bLink, Timestamp: self.currentTimeInMiliseconds(thisDate: currentDate))
                                                        if let encoded = try? JSONEncoder().encode(bowToSave) {
                                                            UserDefaults.standard.set(encoded, forKey: "bow")
                                                        }
                                                        
                                                    }
                                                }
                                            }
                                            catch {
                                                print(error.localizedDescription)
                                                assertionFailure()
                                            }
                                        } else {
                                            print("NOT OKAYYYYYYY")
                                        }
                                    }
                                    //Do UI Stuff
                                    DispatchQueue.main.async {
                                        // A little screen action with our new data
                                        self.api_data = Api_Data(quote: self.quote, bookTitle: self.bTitle, bookDesc: self.bDescription, bookThumb: self.bThumbnail, bookLink: self.bLink)
                                        self.quoteLbl.text = self.api_data.quote
                                        self.bookTitleLbl.text = self.api_data.bookTitle
                                        self.bookDescLbl.text = self.api_data.bookDesc
                                        
                                        let url = URL(string: self.api_data.bookThumb)!
                                        self.bookImg.af.setImage(withURL: url)
                                        
                                    }
                                })
                                task.resume()
                            }
                            
                        }
                        
                    }
                }
                catch {
                    print(error.localizedDescription)
                    assertionFailure()
                }
                
            })
            //ALWAYS HAVE TO START THE TASK
            task.resume()
        }
    }
    
    @IBAction func solacePressed(_ sender: UIButton) {
        //logic for what listener was pressed and what is being passed through
        if sender.tag == 5{
            
            searchStatus = "findPerson"
            performSegue(withIdentifier: "toSolace", sender: nil)
        }else{
            searchStatus = "listening"
            performSegue(withIdentifier: "toSolace", sender: nil)
        }
    }
    
    func presentationControllerDidDismiss(_ presentationController: UIPresentationController) {
        //if listener what closed by swiping
        let searchingDict : Dictionary = ["searching": false,
                                          "matched": false,
                                          "matched_user": "",
                                          "hardship":"",
                                          "uid":Auth.auth().currentUser!.uid] as [String : Any]
        
        //updating database
        ref.child("Searching").child(Auth.auth().currentUser!.uid).setValue(searchingDict)
    }
    
    // Checks to see if User Default timestamp int is still greater than the current date minus .... just look at it
    func isWithinWeek(lastBook: Int) -> Bool {
        let newDate = Date()
        let dateInFull = currentTimeInMiliseconds(thisDate: newDate) - (7 * DAY_IN_MS)
        if (lastBook > dateInFull){
            return true;
        }else{
            return false;
        }
    }
    // Converts the date from isWithinWeek() into milliseconds
    func currentTimeInMiliseconds(thisDate: Date) -> Int {
        let since1970 = thisDate.timeIntervalSince1970
        return Int(since1970 * 1000)
    }
}

//delagate for solace
extension DashboardVC: SolaceDelegate {
    func openChat(withId: String) {
        selecteduidForChat = withId
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            //opening chat with what user is found
            self.performSegue(withIdentifier: "toChat", sender: nil)
        }
    }
}
