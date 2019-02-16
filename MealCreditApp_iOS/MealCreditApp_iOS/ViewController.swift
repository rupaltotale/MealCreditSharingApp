//
//  ViewController.swift
//  AF_test
//
//  Created by RupalT on 12/21/18.
//  Copyright © 2018 RupalT. All rights reserved.
//

import UIKit
import Alamofire

class ViewController: UIViewController {

    
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var usernameTextField: UITextField!
    @IBOutlet weak var passwordTextField: UITextField!
    @IBOutlet weak var loginButton: UIButton!
    @IBOutlet weak var forgotPasswordButton: UIButton!
    @IBOutlet weak var noAccountLabel: UILabel!
    @IBOutlet weak var signupButton: UIButton!
    
    override func viewDidLoad() {
        
        super.viewDidLoad();
//        UserDefaults.standard.removeObject(forKey: "user_id")
        if UserDefaults.standard.string(forKey: "user_id") != nil {
           self.performSegue(withIdentifier: "goToAppFromLogin", sender: self)
        }
        shiftFrameOnKeyBoardPopUp();
        setStyleOfElements();
        
        self.navigationController?.navigationBar.setBackgroundImage(UIImage(), for: .default)
        self.navigationController?.navigationBar.shadowImage = UIImage()
        self.navigationController?.navigationBar.isTranslucent = true
        self.navigationController?.view.backgroundColor = .clear
        self.navigationController?.view.tintColor = UIColor.white;
        
       
    }
    
    @IBAction func login(_ sender: Any) {
        
        let username = usernameTextField.text;
        let password = passwordTextField.text;
        if (username == ""){
            Helper.alert("Username missing", "Please enter username", self);
        }
        if (password == ""){
            Helper.alert("Password missing", "Please enter password", self);
        }
        if (username != "" && password != ""){
            let urlString = GlobalVariables.rootUrl + "login/" ;
            let parameters: [String: Any] = [
                "username" : username!,
                "password" : password!
            ]
            Alamofire.request(urlString, method: .post, parameters: parameters, encoding: JSONEncoding.default)
                .responseJSON { response in
                    if let jsonObj: Dictionary = response.result.value as? Dictionary<String, Any>{
                        let status:Int = (response.response?.statusCode)!;
                        if (status == 200){
                            print(jsonObj)
                            UserDefaults.standard.set(jsonObj["token"] as! String, forKey: "token");
                            UserDefaults.standard.set(jsonObj["user_id"] as? Int, forKey: "user_id");
                            UserDefaults.standard.set(jsonObj["username"] as? String, forKey: "username");
                            self.performSegue(withIdentifier: "goToAppFromLogin", sender: self)
                        }
                        else{
                            Helper.alert("Error logging in", "Invalid username or password", self);
                        }
                    }
            }
            
        }
        
    }
    
    
    @IBAction func forgotPassword(_ sender: Any) {
        
    }
    @IBAction func signUp(_ sender: Any) {
        
    }
    
    
    func shiftFrameOnKeyBoardPopUp(){
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow), name: UIResponder.keyboardWillShowNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillHide), name: UIResponder.keyboardWillHideNotification, object: nil)
    }
    @objc func keyboardWillShow(notification: NSNotification) {
        if let keyboardSize = (notification.userInfo?[UIResponder.keyboardFrameBeginUserInfoKey] as? NSValue)?.cgRectValue {
            if self.view.frame.origin.y == 0 {
                self.view.frame.origin.y -= keyboardSize.height * 0.2
            }
        }
    }
    
    @objc func keyboardWillHide(notification: NSNotification) {
        if self.view.frame.origin.y != 0 {
            self.view.frame.origin.y = 0
        }
    }

    func setStyleOfElements(){
        self.view.backgroundColor = GlobalVariables.mainColor;
        
        titleLabel.font = GlobalVariables.heading1;
        titleLabel.textColor = UIColor.white;
        
        
        loginButton.layer.cornerRadius = 2;
        signupButton.layer.cornerRadius = 2;
        
        let textfields = [usernameTextField, passwordTextField];
        for i in textfields{
            Helper.setTextFieldStyle(i!);
        }
        
        let width = self.view.frame.width;
        let height = self.view.frame.height;
        let loginHeight = height/2;
        let subWidth = 0.7 * width;
        let subHeight:CGFloat = 45.0;
        let subX = width/2 - subWidth/2;
        let subY = subHeight + 10;
        
        titleLabel.sizeToFit();
        titleLabel.frame = CGRect(x: subX, y: loginHeight + subY * -3.7, width: titleLabel.frame.width, height: subHeight);
        
        usernameTextField.frame = CGRect(x: subX, y: loginHeight + subY * -2, width: subWidth, height: subHeight);
        passwordTextField.frame = CGRect(x: subX, y: loginHeight + subY * -1, width: subWidth, height: subHeight);
        loginButton.frame = CGRect(x: subX, y: loginHeight, width: subWidth, height: subHeight);
        forgotPasswordButton.frame = CGRect(x: subX, y: loginHeight + subY * 1, width: subWidth, height: subHeight);
        
        noAccountLabel.frame = CGRect(x: subX, y: loginHeight + subY * 3, width: subWidth, height: subHeight);
        signupButton.frame = CGRect(x: subX, y: loginHeight + subY * 3.7, width: subWidth, height: subHeight);
    }

}

