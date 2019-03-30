//
//  Helper.swift
//  AF_test
//
//  Created by RupalT on 12/21/18.
//  Copyright © 2018 RupalT. All rights reserved.
//

import Foundation
import UIKit
import Alamofire
class Helper: UIViewController {
    enum LINE_POSITION {
        case LINE_POSITION_TOP
        case LINE_POSITION_BOTTOM
    }
    static func setButtonStyle(_ button:UIButton){
        button.titleLabel?.font = UIFont(name: GlobalVariables.buttonFont, size: 24)
        button.titleLabel?.textColor = GlobalVariables.mainColor
        button.backgroundColor =  UIColor.darkGray
        button.layer.cornerRadius = 3
 
    }
    static func setNormalButtonStyle(_ button:UIButton, _ view: UIViewController){
        button.titleLabel?.font = UIFont(name: GlobalVariables.buttonFont, size: 22)
        button.titleLabel?.textColor = UIColor.white
        button.backgroundColor = GlobalVariables.mainColor
        button.layer.cornerRadius = 3
    }
    static func setTextFieldStyle(_ i:UITextField){
        i.textAlignment = .center;
        i.backgroundColor = UIColor(red: 1, green: 1, blue: 1, alpha: 0.8);
        i.layer.masksToBounds = true;
        i.layer.cornerRadius = 2;
        Helper.addLineToView(view: i, position:.LINE_POSITION_BOTTOM, color: UIColor.darkGray, width: 0.5)
    }
    static func addLineToView(view : UIView, position : LINE_POSITION, color: UIColor, width: Double) {
        let lineView = UIView()
        lineView.backgroundColor = color
        lineView.translatesAutoresizingMaskIntoConstraints = false // This is important!
        view.addSubview(lineView)
        
        let metrics = ["width" : NSNumber(value: width)]
        let views = ["lineView" : lineView]
        view.addConstraints(NSLayoutConstraint.constraints(withVisualFormat: "H:|[lineView]|", options:NSLayoutConstraint.FormatOptions(rawValue: 0), metrics:metrics, views:views))
        
        switch position {
        case .LINE_POSITION_TOP:
            view.addConstraints(NSLayoutConstraint.constraints(withVisualFormat: "V:|[lineView(width)]", options:NSLayoutConstraint.FormatOptions(rawValue: 0), metrics:metrics, views:views))
            break
        case .LINE_POSITION_BOTTOM:
            view.addConstraints(NSLayoutConstraint.constraints(withVisualFormat: "V:[lineView(width)]|", options:NSLayoutConstraint.FormatOptions(rawValue: 0), metrics:metrics, views:views))
            break
        }
    }
    
    static func alert(_ title: String, _ message: String, _ view: UIViewController) {
        let alert = UIAlertController(title: title, message: message, preferredStyle: .alert)
        var returnMsg = "";
        alert.addAction(UIAlertAction(title: "OK", style: .default, handler: { action in
            switch action.style{
            case .default:
                returnMsg = "default";
                
            case .cancel:
                returnMsg =  "cancel";
                
            case .destructive:
                returnMsg =  "destructive";
                
            }}));
        view.present(alert, animated: true, completion: nil);
    }
    
    static func convertFromDateTimeToDate(dateTime: String) -> Date?{
        
        let dateFormatter = DateFormatter()
        dateFormatter.locale = Locale(identifier: "en_US_POSIX")
        dateFormatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
        dateFormatter.timeZone = TimeZone(abbreviation: "GMT")
//        let dateFormatterPrint = DateFormatter()
//        dateFormatterPrint.dateFormat = "MMM dd,yyyy"
        if let date: Date = dateFormatter.date(from: dateTime) {
            return date
        }
        return nil
    }
    
    
    static func getNewToken(){
        var token = "";
        let user_id = UserDefaults.standard.string(forKey: "user_id")
        let urlString = GlobalVariables.rootUrl + "token/" + user_id! ;
        Alamofire.request(urlString).responseJSON{response in
            let obj:Dictionary = response.result.value! as! Dictionary<String, Any>
            token = obj["token"]! as! String
            UserDefaults.standard.set(token, forKey: "token")
        }
        
    }
    static func addFilterButton(_ filterButton: UIButton, _ view : UIViewController) {
        filterButton.setTitle("Filter and Sort", for: .normal)
        Helper.setNormalButtonStyle(filterButton, view)
        filterButton.sizeToFit()
        filterButton.frame = CGRect(x: (view.view.frame.width - filterButton.frame.width * 1.5)/2, y: view.view.frame.height - filterButton.frame.height*6, width: filterButton.frame.width * 1.5, height: filterButton.frame.height * 1)
        filterButton.layer.cornerRadius = filterButton.frame.size.width / 11
        filterButton.layer.shadowColor = UIColor(red: 0, green: 0, blue: 0, alpha: 0.15).cgColor
        filterButton.layer.shadowOffset = CGSize(width: 0.0, height: 5.0)
        filterButton.layer.shadowOpacity = 0.7
        filterButton.layer.shadowRadius = 0.0
        view.view.addSubview(filterButton)
    }
    static func getAvailabilities(limit:Int = -1, location:Any = false, start_time:Any = false, end_time:Any = false, price:Any = false, sortBy:Any = false, username:Any = false) -> Array<AvailableObject>{
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
                
                
            }
        }
        return avList;
    }
    
    static func getFormattedDate(_ datePicker: Date) -> String{
        let dateformatter = DateFormatter()
        dateformatter.dateStyle = DateFormatter.Style.medium
        dateformatter.timeStyle = DateFormatter.Style.short
        let dateTime = dateformatter.string(from: datePicker)
        return dateTime
    }
    
    static func formatTitleLabel(_ label: UILabel){
        label.font = UIFont(name: GlobalVariables.normalFont, size: 21)
        label.backgroundColor = UIColor(red: 0.9, green: 0.9, blue: 0.9, alpha: 0.5)
        label.layer.borderWidth = 2
        label.layer.borderColor = UIColor(red: 0.9, green: 0.9, blue: 0.9, alpha: 0.75).cgColor
    }
    
//    static func setButtonStyle(){
//
//    }

}
extension UIViewController {
    func hideKeyboardWhenTappedAround() {
        let tap: UITapGestureRecognizer = UITapGestureRecognizer(target: self, action: #selector(UIViewController.dismissKeyboard))
        tap.cancelsTouchesInView = false
        view.addGestureRecognizer(tap)
    }
    
    @objc func dismissKeyboard() {
        view.endEditing(true)
    }
}
