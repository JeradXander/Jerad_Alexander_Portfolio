//
//  MapViewController.swift
//  AlexanderJerad_ImGood
//
//  Created by Jerad Alexander on 12/14/19.
//  Copyright © 2019 Jerad Alexander. All rights reserved.
//

import UIKit
import MapKit
import CoreLocation
import Firebase

class MapViewController: UIViewController, CLLocationManagerDelegate,MKMapViewDelegate, UITableViewDelegate, UITableViewDataSource
{
    // variables for map and view
    let locationManager = CLLocationManager()
    let annotation = MKPointAnnotation()
    var backGroundColor = UIColor()
    var redColor = UIColor()
    var greenColor = UIColor()
    var memberSelected = User()
    
    //outlets
    @IBOutlet weak var mapView: MKMapView!
    @IBOutlet weak var familyTableView: UITableView!
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        //setting nav title and view color
        navigationItem.title = "Maps"
        view.backgroundColor = backGroundColor
        //tableview delagate and source
        familyTableView.delegate = self
        familyTableView.dataSource = self
        //current user
        FriendsClass.friendsClass.getCurrentUser
            {
                (user) in
                self.navigationController?.title = user.fullName
        }
        
        //observer for friends list
        FriendsClass.friendsClass.addFriendObserver {
            self.familyTableView.reloadData()
        }
        
        //calling current location
        getcurrentLocation()
    }
    //MARK: - getting current location
    func getcurrentLocation()
    {
        self.locationManager.requestWhenInUseAuthorization()
        if CLLocationManager.locationServicesEnabled()
        {
            locationManager.delegate = self
            locationManager.desiredAccuracy = kCLLocationAccuracyBest
            locationManager.startUpdatingLocation()
        }
        //setting up mapview
        mapView.delegate = self
        mapView.mapType = .standard
        mapView.isZoomEnabled = true
        mapView.isScrollEnabled = true
        
        //more mapview set up
        if let coordinates = locationManager.location?.coordinate
        {
            //span for mapview
            let span =  MKCoordinateSpan(latitudeDelta: 0.05, longitudeDelta: 0.05)
            let region = MKCoordinateRegion(center: coordinates, span: span)
            
            mapView.setRegion(region, animated: true)
            annotation.coordinate = coordinates
                //annotation information
            FriendsClass.friendsClass.getCurrentUser({ (user) in
                self.annotation.title = user.fullName
            })
            
            annotation.subtitle = "current location"
            //centering mapview on annotation
            mapView.setCenter(annotation.coordinate, animated: true)
            mapView.addAnnotation(annotation)
        }
    }
    //sign out action
    @IBAction func SignoutTapped(_ sender: UIBarButtonItem) {
        
        let alert = UIAlertController(title: "Are You Sure", message: "Yes to complete Sign-Out", preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
        alert.addAction(UIAlertAction(title: "Yes", style: .default , handler: { _ in
            //logging out
            self.HandleLogOut()
        }))
        self.present(alert, animated: true, completion: nil)
    }
    
    //sign out function
    func HandleLogOut()
    {
        do
        {
            try Auth.auth().signOut()
        }
        catch let logouterror
        {
            print(logouterror)
        }
        //segue to sign in controller
        performSegue(withIdentifier: "goToSignIn", sender: self)
    }
}

//MARK: - Extension

extension MapViewController
{
    //MARK: - header title
    func tableView(_ tableView: UITableView, titleForHeaderInSection section: Int) -> String?
    {
        return "Family"
    }
    //MARK: - number of rows in section
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int
    {
        return FriendsClass.friendsClass.friendList.count
    }
    //MARK: - cell for for at
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        //default cell to return
        var cellToReturn = UITableViewCell()
        //dequeue reusable cell for memory
        let cell = tableView.dequeueReusableCell(withIdentifier: "UserCell", for: indexPath) 
        
        // Configure the cell...
        let currentUser = FriendsClass.friendsClass.friendList[indexPath.row]
        
        //setting up cell based on good status
        if currentUser.goodStatus == true
        {
            //cell setup
            cell.detailTextLabel?.text = "Status: Good"
            cell.backgroundColor = greenColor
            cell.textLabel?.text = currentUser.fullName
            //setting cell to return
            cellToReturn = cell
        }
        else if currentUser.goodStatus == false
        {
            //cell setup
            cell.detailTextLabel?.text = "Status: Check In"
            cell.backgroundColor = redColor
            cell.textLabel?.text = currentUser.fullName
                //cell to return
            cellToReturn = cell
        }
        return cellToReturn
    }
    // height for rows
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 50
    }
    
    //MARK: - if cell is selected
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        //getting member selected
        memberSelected =  FriendsClass.friendsClass.friendList[indexPath.row]
        
        //unwrapping coordinated
        if let coors : String = memberSelected.coordinates
        {
            //converting string to array
            let coorArray : [String] = coors.components(separatedBy: ",")
            
            if let lat = Double(coorArray[0]), let long = Double(coorArray[1])
            {
                //getting 2d location
                let coordinates = CLLocationCoordinate2DMake(lat , long )
                
                //setting up new annotation
                annotation.coordinate = coordinates
                annotation.title = memberSelected.fullName
                annotation.subtitle = "Last location"
                //centering on member selected coordinated
                mapView.setCenter(coordinates, animated: true)
                mapView.addAnnotation(annotation)
            }
        }
    }
}

