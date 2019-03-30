//
//  SignUpViewController.swift
//  AF_test
//
//  Created by RupalT on 12/21/18.
//  Copyright © 2018 RupalT. All rights reserved.
//

import UIKit
import Alamofire

class SignUpViewController: UIViewController, UITextFieldDelegate {

    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var firstNameTextField: UITextField!
    @IBOutlet weak var lastNameTextField: UITextField!
    @IBOutlet weak var usernameTextField: UITextField!
    @IBOutlet weak var passwordTextField: UITextField!
    @IBOutlet weak var emailTextField: UITextField!
    @IBOutlet weak var signupButton: UIButton!
    override func viewDidLoad() {
        super.viewDidLoad();
        self.hideKeyboardWhenTappedAround()
        shiftFrameOnKeyBoardPopUp();
        setStyleOfElements();
        self.navigationController?.view.tintColor = UIColor.white;
//        self.navigationController?.view.tot
        self.navigationController?.navigationBar.titleTextAttributes = [NSAttributedString.Key.font:  GlobalVariables.navBarTitle
        ]

        // Do any additional setup after loading the view.
    }
    

    @IBAction func signUp(_ sender: Any) {
        // Post a new user
        let username = usernameTextField.text;
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
        let urlString = GlobalVariables.rootUrl  + "register/" ;
        let parameters: [String: Any] = [
            "firstname": firstname!,
            "lastname": lastname!,
            "username" : username!,
            "password" : password!
        ]
        Alamofire.request(urlString, method: .post, parameters: parameters, encoding: JSONEncoding.default)
            .responseJSON { response in
                if let jsonObj: Dictionary = response.result.value as? Dictionary<String, Any>{
                    let status:Int = (response.response?.statusCode)!;
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
        
        let textfields = [firstNameTextField, lastNameTextField, usernameTextField, passwordTextField, emailTextField];
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
        firstNameTextField.autocorrectionType = .no
        firstNameTextField.autocapitalizationType = .words
        firstNameTextField.delegate = self as? UITextFieldDelegate
        firstNameTextField.tag = 0
        
        lastNameTextField.frame = CGRect(x: subX, y: loginHeight + subY * -1, width: subWidth, height: subHeight);
        lastNameTextField.autocorrectionType = .no
        lastNameTextField.autocapitalizationType = .words
        lastNameTextField.delegate = self as? UITextFieldDelegate
        lastNameTextField.tag = 1
        
        usernameTextField.frame = CGRect(x: subX, y: loginHeight + subY * 0, width: subWidth, height: subHeight);
        usernameTextField.autocorrectionType = .no
        usernameTextField.autocapitalizationType = .none
        usernameTextField.delegate = self as? UITextFieldDelegate
        usernameTextField.tag = 2
        
        emailTextField.frame = CGRect(x: subX, y: loginHeight + subY * 1, width: subWidth, height: subHeight);
        emailTextField.autocorrectionType = .no
        emailTextField.autocapitalizationType = .none
        emailTextField.delegate = self as? UITextFieldDelegate
        emailTextField.tag = 3
        
        passwordTextField.frame = CGRect(x: subX, y: loginHeight + subY * 2, width: subWidth, height: subHeight);
        passwordTextField.autocorrectionType = .no
        passwordTextField.autocapitalizationType = .none
        passwordTextField.isSecureTextEntry = true
        passwordTextField.delegate = self as? UITextFieldDelegate
        passwordTextField.tag = 4
        
        signupButton.frame = CGRect(x: subX, y: loginHeight + subY * 4, width: subWidth, height: subHeight);
        
        }
    func shiftFrameOnKeyBoardPopUp(){
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillShow), name: UIResponder.keyboardWillShowNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(keyboardWillHide), name: UIResponder.keyboardWillHideNotification, object: nil)
    }
    @objc func keyboardWillShow(notification: NSNotification) {
        if ((notification.userInfo?[UIResponder.keyboardFrameBeginUserInfoKey] as? NSValue)?.cgRectValue) != nil {
            if self.view.frame.origin.y == 0 {
                self.view.frame.origin.y -= self.view.frame.height * 0.16
            }
        }
    }
    
    @objc func keyboardWillHide(notification: NSNotification) {
        if self.view.frame.origin.y != 0 {
            self.view.frame.origin.y = 0
        }
    }
    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        // Try to find next responder
        if let nextField = textField.superview?.viewWithTag(textField.tag + 1) as? UITextField {
            nextField.becomeFirstResponder()
        } else {
            // Not found, so remove keyboard.
            textField.resignFirstResponder()
        }
        // Do not add a line break
        return false
    }

}
