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
        print(returnMsg)
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
//    static func convertFromDateToDateTime(dateTime: Date) -> String?{
//
////        let date = ""
////        date += dateTime.
////        return nil
//    }
    
    
    static func getNewToken(){
        var token = "";
        let user_id = UserDefaults.standard.string(forKey: "user_id")
        let urlString = "http://" + "127.0.0.1:8000/" + "token/" + user_id! ;
        Alamofire.request(urlString).responseJSON{response in
            let obj:Dictionary = response.result.value! as! Dictionary<String, Any>
            token = obj["token"]! as! String
            UserDefaults.standard.set(token, forKey: "token")
        }
        
    }
    
//    static func setButtonStyle(){
//
//    }

}
