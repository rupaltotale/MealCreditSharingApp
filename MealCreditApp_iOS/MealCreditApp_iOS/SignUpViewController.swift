//
//  SignUpViewController.swift
//  AF_test
//
//  Created by RupalT on 12/21/18.
//  Copyright © 2018 RupalT. All rights reserved.
//

import UIKit
import Alamofire

class SignUpViewController: UIViewController {

    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var firstNameTextField: UITextField!
    @IBOutlet weak var lastNameTextField: UITextField!
    @IBOutlet weak var userNameTextField: UITextField!
    @IBOutlet weak var passwordTextField: UITextField!
    @IBOutlet weak var emailTextField: UITextField!
    @IBOutlet weak var signupButton: UIButton!
    override func viewDidLoad() {
        super.viewDidLoad();
        setStyleOfElements();
        self.navigationController?.view.tintColor = UIColor.white;
//        self.navigationController?.view.tot
        self.navigationController?.navigationBar.titleTextAttributes = [NSAttributedString.Key.font:  GlobalVariables.navBarTitle
        ]

        // Do any additional setup after loading the view.
    }
    

    @IBAction func signUp(_ sender: Any) {
        // Post a new user
        let username = userNameTextField.text;
        let password = passwordTextField.text;
        let firstname = firstNameTextField.text;
        let lastname = lastNameTextField.text;
        if (username == ""){
            Helper.alert("Username missing", "Please enter a username", self);
        }
        if (password == ""){
            Helper.alert("Password missing", "Please enter a password", self);
        }
        if (firstname == ""){
            Helper.alert("First name missing", "Please enter your first name", self);
        }
        if (lastname == ""){
            Helper.alert("Last name missing", "Please enter your last name", self);
        }
        if (username != "" && password != "" && firstname != "" && lastname != ""){
        let urlString = "http://" + "127.0.0.1:8000/" + "register/" ;
        let parameters: [String: Any] = [
            "firstname": firstname!,
            "lastname": lastname!,
            "username" : username!,
            "password" : password!
        ]
        Alamofire.request(urlString, method: .post, parameters: parameters, encoding: JSONEncoding.default)
            .responseJSON { response in
                if let jsonObj: Dictionary = response.result.value as? Dictionary<String, Any>{
                    let status:Int = jsonObj["status"] as! Int;
                    let message:String = jsonObj["message"] as! String;
                    if (status == 200){
                        UserDefaults.standard.set(jsonObj["token"] as! String, forKey: "token");
                        UserDefaults.standard.set(jsonObj["user_id"] as? Int, forKey: "user_id");
                        UserDefaults.standard.set(username!, forKey: "username");
                        self.performSegue(withIdentifier: "goToAppFromSignup", sender: self);
                    }
                    else if (status == 401 && message == "username taken"){
                        Helper.alert("Error registering", "Username is taken. Try another one.", self);
                    }
                    else{
                        Helper.alert("Error registering", "Please try again later.", self);
                    }
                }
        }
    }
    }
    
    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
    }
    */
    
    func setStyleOfElements(){
        self.view.backgroundColor = GlobalVariables.mainColor;
        titleLabel.font = GlobalVariables.heading1;
        titleLabel.textColor = UIColor.white;
        titleLabel.sizeToFit();
        
        let textfields = [firstNameTextField, lastNameTextField, userNameTextField, passwordTextField, emailTextField];
        for i in textfields{
            Helper.setTextFieldStyle(i!);
        }
        
        
        signupButton.backgroundColor = UIColor.black;
        signupButton.layer.cornerRadius = 2;
        signupButton.setTitleColor(UIColor.white, for: UIControl.State.normal);
        
        let width = self.view.frame.width;
        let height = self.view.frame.height;
        let loginHeight = height/2;
        let subWidth = 0.7 * width;
        let subHeight:CGFloat = 45.0;
        let subX = width/2 - subWidth/2;
        let subY = subHeight + 7;
        
        titleLabel.frame = CGRect(x: subX, y: loginHeight + subY * -3.7, width: titleLabel.frame.width, height: subHeight);
        
        firstNameTextField.frame = CGRect(x: subX, y: loginHeight + subY * -2, width: subWidth, height: subHeight);
        lastNameTextField.frame = CGRect(x: subX, y: loginHeight + subY * -1, width: subWidth, height: subHeight);
        userNameTextField.frame = CGRect(x: subX, y: loginHeight + subY * 0, width: subWidth, height: subHeight);
        emailTextField.frame = CGRect(x: subX, y: loginHeight + subY * 1, width: subWidth, height: subHeight);
        passwordTextField.frame = CGRect(x: subX, y: loginHeight + subY * 2, width: subWidth, height: subHeight);
        signupButton.frame = CGRect(x: subX, y: loginHeight + subY * 4, width: subWidth, height: subHeight);
        
        }

}
