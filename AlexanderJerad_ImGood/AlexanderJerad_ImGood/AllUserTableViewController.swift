//
//  AllUserTableViewController.swift
//  AlexanderJerad_ImGood
//
//  Created by Jerad Alexander on 12/14/19.
//  Copyright © 2019 Jerad Alexander. All rights reserved.
//

import UIKit
import Firebase
import FirebaseDatabase

class AllUserTableViewController: UITableViewController , UISearchBarDelegate, UISearchResultsUpdating, UISearchControllerDelegate{
    
    //variables
    var backgroundColor = UIColor()
    var UserList = [User]()
    var allUsers = FriendsClass.friendsClass.userList
    var filteredArray = [User]()
    
    //passing nil tells the search controllers
    var searchController = UISearchController(searchResultsController: nil)
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        //setting delegate and datasource to adhere to tableview requirements
        tableView.delegate = self
        tableView.dataSource = self
        tableView.backgroundColor = backgroundColor
        navigationItem.title = "Add Users"
        
        //current user function
        FriendsClass.friendsClass.getCurrentUser
            {
                (user) in
                self.navigationController?.title = user.fullName
        }
        //user added observer
        FriendsClass.friendsClass.addUserObserver
            { () in
                //filtered array for search
                self.filteredArray = FriendsClass.friendsClass.userList
                //loading data for tableview
                self.tableView.reloadData()
        }
        
        //set up search controller
        searchController.dimsBackgroundDuringPresentation = false
        searchController.definesPresentationContext = true
        //to recieve updates to searches here in this table view
        searchController.searchResultsUpdater = self
        searchController.searchBar.scopeButtonTitles = ["All"]
        searchController.searchBar.delegate = self
        navigationItem.searchController = searchController
        //setting filtered array
    }
    
    // MARK: - Table view data source
    
    //number of sections
    override func numberOfSections(in tableView: UITableView) -> Int {
        // #warning Incomplete implementation, return the number of sections
        return 1
    }
    
    //number of rows
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        filteredArray.count
    }
    //height for rows
    override func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 50
    }
    //view for footer
    override func tableView(_ tableView: UITableView, viewForFooterInSection section: Int) -> UIView? {
        let footerView = UIView(frame: CGRect(x: 0, y: 0, width: tableView.bounds.size.width, height: 50))
        footerView.backgroundColor = backgroundColor
        
        return footerView
    }
    
    //setting up cells
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "UserCell", for: indexPath) as! UserTableViewCell
        
        // Configure the cell...
        let currentUser = filteredArray[indexPath.row]
        
        cell.nameLabel.text = currentUser.fullName
        cell.backgroundColor = backgroundColor
        
        //button function
        cell.setFunction {
            
            //alert to add friend
            let alert = UIAlertController(title: "Send Friend Request", message: "Request \(currentUser.fullName) to be friends.", preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
            alert.addAction(UIAlertAction(title: "Yes", style: .default , handler: { _ in
                
                let id = currentUser.id
                FriendsClass.friendsClass.sendRequestToUser(id)
                
            }))
            self.present(alert, animated: true, completion: nil)
        }
        return cell
    }
    
    //MARK: - updating search results
    func updateSearchResults(for searchController: UISearchController)
    {
        let searchText = searchController.searchBar.text
        
        //filter the data
        //dump full data set into the array we use to filter
        filteredArray = FriendsClass.friendsClass.userList
        
        if searchText != ""
        {
            filteredArray = filteredArray.filter(
                { (user) -> Bool in
                    return user.fullName.lowercased().range(of: searchText!.lowercased()) != nil
            })
        }
        //reloading filtered search results
        tableView.reloadData()
        
    }
    //for clicking search bar
    func searchBarSearchButtonClicked(_ searchBar: UISearchBar) {
        searchController.isActive = false
    }
    //sign out action
    @IBAction func SignOut(_ sender: Any)
    {
        let alert = UIAlertController(title: "Are You Sure", message: "Yes to complete Sign-Out", preferredStyle: .alert)
        
        
        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
        
        
        alert.addAction(UIAlertAction(title: "Yes", style: .default , handler: { _ in
            
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
        
        performSegue(withIdentifier: "goToSignIn", sender: self)
    }
}
