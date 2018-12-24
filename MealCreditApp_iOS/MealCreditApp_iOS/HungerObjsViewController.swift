//
//  HungerObjsViewController.swift
//  AF_test
//
//  Created by RupalT on 12/21/18.
//  Copyright © 2018 RupalT. All rights reserved.
//

import UIKit

class HungerObjsViewController: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
        setStyle()
        // Do any additional setup after loading the view.
    }
    
    func setStyle(){
        self.navigationController?.navigationBar.barTintColor = GlobalVariables.mainColor;
        self.navigationController?.navigationBar.tintColor = UIColor.white;
        self.navigationController?.navigationBar.isTranslucent = false;
        self.title = "Hungry?";
        self.navigationController?.navigationBar.titleTextAttributes = [NSAttributedString.Key.foregroundColor : UIColor.white,
            NSAttributedString.Key.font:  GlobalVariables.navBarTitle
        ]
        self.navigationController?.navigationBar.frame = CGRect(x: 0, y: 0, width: 100, height: 500)
        
        self.tabBarController?.tabBar.barTintColor = UIColor.darkGray;
        self.tabBarController?.view.tintColor = GlobalVariables.mainColor;
        self.tabBarController?.view.isOpaque = true;
//        self.tabBarItem = UITabBarItem(title: "Hungry?", image: UIImage(named: "hungry"), selectedImage: UIImage(named: "hungry"))
//        let addButton: UIBarButtonItem = UIBarButtonItem(barButtonSystemItem: .add, target: self, action: #selector(addAvObject(sender:)))
//        self.navigationItem.rightBarButtonItem = addButton
        
    }

}
