//
//  AvailableObjsViewController.swift
//  AF_test
//
//  Created by RupalT on 12/21/18.
//  Copyright © 2018 RupalT. All rights reserved.
//

import UIKit
import Alamofire

var globalAvObject: AvailableObject!;

class AvailableObjsViewController: UIViewController, UITableViewDelegate, UITableViewDataSource {
    var list:Array<AvailableObject> = [];
    let filterButton: UIButton = UIButton()
    @IBOutlet weak var table: UITableView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setStyle();
        Helper.addFilterButton(filterButton, self);
        filterButton.addTarget(self, action: #selector(goToFilterPage), for: .touchUpInside)
        table.tableFooterView = UIView()
        table.frame = CGRect(x: table.frame.origin.x, y: table.frame.origin.y, width: self.view.frame.width, height: self.view.frame.height * 0.7)        
    }
    
    override func viewDidAppear(_ animated: Bool) {
        let username: String = UserDefaults.standard.string(forKey: "username")!;
        getAvailabilities(username: username);
    }
    
    
    // TABLE FUNCTIONS -->
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return list.count;
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = UITableViewCell(style: .default, reuseIdentifier: "avObj")
        let avObj = list[indexPath.row];
        // Location Label
        let locationLabel = UILabel(frame: CGRect(x: 0, y: 0, width: 0, height: 0))
        locationLabel.font = UIFont(name: GlobalVariables.normalFont, size: 20)
        locationLabel.textColor = UIColor.black;
        locationLabel.text = avObj.location!
        locationLabel.sizeToFit()
        locationLabel.frame = CGRect(x: (cell.textLabel?.frame.origin.x)! + 15, y: 25, width: locationLabel.frame.width, height: locationLabel.frame.height)
        cell.addSubview(locationLabel)
        cell.sizeToFit()
        // Price label
        let priceLabel = UILabel(frame: CGRect(x: 10, y: 10, width: 10, height: 10))
        priceLabel.textColor = GlobalVariables.mainColor
        priceLabel.text = "$\(String(describing: avObj.price!))"
        priceLabel.font = UIFont(name: GlobalVariables.normalFont, size: 25)
        priceLabel.sizeToFit()
        priceLabel.frame = CGRect(x: cell.frame.width - priceLabel.frame.width, y: (table.rowHeight - priceLabel.frame.height)/2, width: priceLabel.frame.width, height: priceLabel.frame.height)
        cell.addSubview(priceLabel)
        cell.sizeToFit()
        // Timing
        let timeLabel = UILabel(frame: CGRect(x: 0, y: 0, width: 0, height: 0))
        timeLabel.font = UIFont(name: GlobalVariables.normalFont, size: 18)
        timeLabel.textColor = UIColor.gray;
        let start_time = Helper.getFormattedDate(avObj.start_time!)
        let end_time = Helper.getFormattedDate(avObj.end_time!)
        timeLabel.text = "\(start_time)\n\(end_time)"
        timeLabel.lineBreakMode = .byWordWrapping
        timeLabel.numberOfLines = 0
        timeLabel.sizeToFit()
        timeLabel.frame = CGRect(x: (cell.textLabel?.frame.origin.x)! + 15, y: table.rowHeight - timeLabel.frame.height - 15, width: timeLabel.frame.width, height: timeLabel.frame.height)
        cell.addSubview(timeLabel)
        cell.sizeToFit()
        cell.accessoryType = .disclosureIndicator
        
        return cell;
    }
    
    // WHEN A TABLE ROW IS CLICKED
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        globalAvObject = list[indexPath.row]
        let backItem = UIBarButtonItem()
        backItem.title = ""
        navigationItem.backBarButtonItem = backItem
        self.performSegue(withIdentifier: "goToEditAv", sender: self);
    }

    // <-- TABLE Functions
    
    // Add Button Pressed
    @objc func addAvObject(sender: AnyObject) {
        self.performSegue(withIdentifier: "goToAddAv", sender: self);
    }
   
    // Filter and sort button pressed
    @objc func goToFilterPage() {
        
    }
    
    func setStyle(){
        table.rowHeight = 125;
        self.navigationController?.navigationBar.barTintColor = GlobalVariables.mainColor;
        self.navigationController?.navigationBar.tintColor = UIColor.white;
        self.navigationController?.navigationBar.isTranslucent = false;
        self.title = "My Availabilities";
        self.navigationController?.navigationBar.titleTextAttributes = [NSAttributedString.Key.foregroundColor : UIColor.white,
            NSAttributedString.Key.font:  GlobalVariables.navBarTitle
        ]
        self.navigationController?.navigationBar.frame = CGRect(x: 0, y: 0, width: 100, height: 500)
        
        self.tabBarController?.tabBar.barTintColor = UIColor.darkGray;
        self.tabBarController?.view.tintColor = GlobalVariables.mainColor;
        self.tabBarController?.view.isOpaque = true;
        self.tabBarItem = UITabBarItem(title: "Availabilities", image: UIImage(named: "availabilities"), selectedImage: UIImage(named: "availabilities"))
        let addButton: UIBarButtonItem = UIBarButtonItem(barButtonSystemItem: .add, target: self, action: #selector(addAvObject(sender:)))
        self.navigationItem.rightBarButtonItem = addButton
        
    }
    func getAvailabilities(limit:Int = -1, location:Any = false, start_time:Any = false, end_time:Any = false, price:Any = false, sortBy:Any = false, username:Any = false) {
        var avList:Array<AvailableObject> = [];
        let urlString = GlobalVariables.rootUrl + "availability-list/" + "\(limit)/\(location)/\(username)/\(start_time)/\(end_time)/\(price)/\(sortBy)"
        Alamofire.request(urlString).responseJSON{response in
            if let jsonObj = response.result.value{
                let avObjs:Dictionary = jsonObj as! Dictionary<String, Any>;
                let resultArray:NSArray = avObjs["result"] as! NSArray;
                for i in resultArray{
                    let obj:Dictionary = i as! Dictionary<String, Any>
                    let avObj:AvailableObject = AvailableObject(av_id: obj["av_id"] as! Int, price: obj["asking_price"] as? Double, start_time: Helper.convertFromDateTimeToDate(dateTime: obj["start_time"] as! String), end_time: Helper.convertFromDateTimeToDate(dateTime: obj["end_time"] as! String), user_id: obj["user_id"] as? Int, location: obj["location"] as? String);
                    avList.append(avObj)
                }
                
                self.list = avList;
                self.table.reloadData();
            }
        }
    }

}

