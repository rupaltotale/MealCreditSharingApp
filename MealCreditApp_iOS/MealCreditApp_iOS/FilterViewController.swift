//
//  FilterViewController.swift
//  MealCreditApp_iOS
//
//  Created by RupalT on 2/1/19.
//  Copyright © 2019 RupalT. All rights reserved.
//

import UIKit
import Alamofire

// filter options
var sortByOption = ""
var locationsChecked = [String]()
var start_time:Any = false
var end_time:Any = false
var max_price = ""
var username = ""
var applyFilterAndSortG = false

class FilterViewController: UIViewController, UIPickerViewDelegate, UIPickerViewDataSource, UIScrollViewDelegate {
    static var list:Array<AvailableObject> = [];
    
    var sortByLabel = PaddingLabel()
    var sortByButton = UIButton()
    var sortByPicker = UIPickerView()
    var maxPriceTextBox = UITextField()
    var priceLabel = PaddingLabel()
    var startTimeLabel = PaddingLabel()
    var startDatePicker = UIDatePicker()
    var endTimeLabel = PaddingLabel()
    var endDatePicker = UIDatePicker()
    var locationLabel = PaddingLabel()
    var scrollView = UIScrollView()
    var containerView = UIView()
    var resetButton = UIBarButtonItem()
    var locationButtons: Array<UIButton> = [];
    
    
    
    let sortFactors = ["Location", "Price", "Recency", "Start Time", "End Time"]
    let leftMargin:CGFloat = 0;
    override func viewDidLoad() {
        super.viewDidLoad()
        self.hideKeyboardWhenTappedAround() 
        setupScrollView()
        
        setupSortByLabel()
        setupSortByButton()
        setupSortByPicker()
        
        setupPriceLabel()
        setupMaxPriceTextField()
        
        setupTimeElements()
        
        setupLocationLabel()
        setupLocationCheckBoxes()
        
        setupApplyButton()
        setupResetButton()
//        self.view.backgroundColor = GlobalVariables.mainColor
        
    }
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        scrollView.frame = CGRect(x: 0, y: 0, width: self.view.frame.width, height: self.view.frame.height * 0.88)
        containerView.frame = CGRect(x: 0, y: 0, width: scrollView.contentSize.width, height: scrollView.contentSize.height)
    }
    func setupScrollView(){
        self.scrollView.delegate = self
        scrollView.addSubview(containerView)
        view.addSubview(scrollView)
    }
    
    //MARK: SORT
    func setupSortByLabel() {
        Helper.formatTitleLabel(sortByLabel)
        sortByLabel.text = "Sort By: " + sortByOption
        sortByLabel.sizeToFit()
        sortByLabel.frame = CGRect(x: leftMargin, y: self.view.frame.height * 0.025, width: self.view.frame.width, height: sortByLabel.frame.height)
        containerView.addSubview(sortByLabel)
    }
    func setupSortByButton(){
        sortByButton.setImage(UIImage(named: "increasing"), for: .normal);
        sortByButton.setTitle("", for: .normal);
        Helper.setNormalButtonStyle(sortByButton, self)
        sortByButton.frame = CGRect(x: self.view.frame.width - leftMargin - 30, y: self.view.frame.height * 0.05, width: 30, height: 30)
        sortByButton.layer.cornerRadius = sortByButton.frame.size.width / 3
//        containerView.addSubview(sortByButton)
    }
    func setupSortByPicker(){
        self.sortByPicker.dataSource = self
        self.sortByPicker.delegate = self
        sortByPicker.backgroundColor = UIColor.white
        sortByPicker.showsSelectionIndicator = true
        sortByPicker.sizeToFit()
        sortByPicker.frame = CGRect(x:(self.view.frame.width - sortByPicker.frame.width)/2, y: sortByLabel.frame.origin.y + sortByLabel.frame.height, width: sortByPicker.frame.width, height: sortByPicker.frame.height)
        var row = 0
        for factor in sortFactors {
            if factor == sortByOption{
                sortByPicker.selectRow(row, inComponent: 0, animated: true)
            }
            row += 1
        }
        containerView.addSubview(sortByPicker)
    }
    
    //MARK: PRICE
    func setupPriceLabel(){
        Helper.formatTitleLabel(priceLabel)
        priceLabel.text = "Max Price: "
        priceLabel.sizeToFit()
        priceLabel.frame = CGRect(x: leftMargin, y: sortByPicker.frame.origin.y + sortByPicker.frame.height, width: self.view.frame.width, height: priceLabel.frame.height)
        containerView.addSubview(priceLabel)
        
    }
    
    func setupMaxPriceTextField(){
        maxPriceTextBox.placeholder = "eg. 5.0"
        maxPriceTextBox.text = max_price
        Helper.setTextFieldStyle(maxPriceTextBox)
        maxPriceTextBox.layer.borderColor = UIColor.darkGray.cgColor
        maxPriceTextBox.layer.borderWidth = 2;
        maxPriceTextBox.sizeToFit()
        maxPriceTextBox.frame = CGRect(x: 25, y: priceLabel.frame.origin.y + priceLabel.frame.height + 25, width: maxPriceTextBox.frame.width * 3, height: priceLabel.frame.height * 1.5)
        maxPriceTextBox.addTarget(self, action: #selector(updateMaxPrice(_:)), for: .editingChanged)
        maxPriceTextBox.keyboardType = UIKeyboardType.decimalPad
        containerView.addSubview(maxPriceTextBox)
    }
    @objc func updateMaxPrice(_ textField: UITextField) {
        max_price = maxPriceTextBox.text!
    }
    
    //MARK: TIME
    func setupTimeElements(){
        Helper.formatTitleLabel(startTimeLabel)
        startTimeLabel.text = "Start Time:"
        if (start_time as? Date != nil){
            startTimeLabel.text = "Start Time:  \(Helper.getFormattedDate(start_time as! Date))"
        }
        startTimeLabel.sizeToFit()
        startTimeLabel.frame = CGRect(x: leftMargin, y: maxPriceTextBox.frame.origin.y + maxPriceTextBox.frame.height + 25, width: self.view.frame.width, height: startTimeLabel.frame.height)
        containerView.addSubview(startTimeLabel)
        
        // start date picker setup
        if (start_time as? Bool == false){
            startDatePicker.setDate(Date(), animated: true)
        }
        else{
            startDatePicker.setDate(start_time as! Date, animated: true)
        }
        startDatePicker.sizeToFit()
        startDatePicker.frame = CGRect(x: (self.view.frame.width - startDatePicker.frame.width)/2, y: startTimeLabel.frame.origin.y + startTimeLabel.frame.height + 10, width: self.startDatePicker.frame.width, height: startDatePicker.frame.height)
        startDatePicker.addTarget(self, action: #selector(changeStartDate), for: .valueChanged)
        containerView.addSubview(startDatePicker)
        
        //end time label setup
        Helper.formatTitleLabel(endTimeLabel)
        endTimeLabel.text = "End Time: "
        if (end_time as? Date != nil){
            endTimeLabel.text = "End Time:  \(Helper.getFormattedDate(end_time as! Date))"
        }
        endTimeLabel.sizeToFit()
        endTimeLabel.frame = CGRect(x: leftMargin, y: startDatePicker.frame.origin.y + startDatePicker.frame.height + 10, width: self.view.frame.width, height: endTimeLabel.frame.height)
        containerView.addSubview(endTimeLabel)
        
        // end date picker setup
        if (end_time as? Bool == false){
            endDatePicker.setDate(Date(), animated: true)
        }
        else{
            endDatePicker.setDate(end_time as! Date, animated: true)
        }
        endDatePicker.sizeToFit()
        endDatePicker.frame = CGRect(x: (self.view.frame.width - endDatePicker.frame.width)/2, y: endTimeLabel.frame.origin.y + endTimeLabel.frame.height + 10, width: self.endDatePicker.frame.width, height: endDatePicker.frame.height)
        endDatePicker.addTarget(self, action: #selector(changeEndDate), for: .valueChanged)
        containerView.addSubview(endDatePicker)
        
    }
    @objc func changeStartDate() {
        start_time = startDatePicker.date
        startTimeLabel.text = "Start Time:  \(Helper.getFormattedDate(start_time as! Date))"
    }
    @objc func changeEndDate() {
        end_time = endDatePicker.date
        endTimeLabel.text = "End Time:  \(Helper.getFormattedDate(end_time as! Date))"
    }
    
    //MARK: LOCATIONS
    func setupLocationLabel() {
        Helper.formatTitleLabel(locationLabel)
        locationLabel.text = "Location(s):"
        locationLabel.sizeToFit()
        locationLabel.frame = CGRect(x: leftMargin, y: endDatePicker.frame.origin.y + endDatePicker.frame.height + 10, width: self.view.frame.width, height: locationLabel.frame.height)
        containerView.addSubview(locationLabel)
    }
    func setupLocationCheckBoxes(){
        var y = locationLabel.frame.origin.y + locationLabel.frame.height + 25
        for location in GlobalVariables.locations{
            let checkBox = UIButton(type: .custom)
            // 0 = unchecked, 1 = checked
            if locationsChecked.contains("   " + location){
                checkBox.tag = 1
                checkBox.setImage(UIImage(named: "checked.png"), for: .normal)
            }
            else{
                checkBox.tag = 0
                checkBox.setImage(UIImage(named: "unchecked.png"), for: .normal)
            }
            checkBox.setTitle("   " + location, for: .normal)
            checkBox.titleLabel?.font = UIFont(name: GlobalVariables.normalFont, size: 20)
            checkBox.setTitleColor(UIColor.gray, for: .normal)
            checkBox.sizeToFit()
            checkBox.frame = CGRect(x: 25, y: y, width: checkBox.frame.width, height: checkBox.frame.height)
            
            y += checkBox.frame.height + 5
            checkBox.addTarget(self, action: #selector(toggleState(sender:)), for: .touchUpInside)
            containerView.addSubview(checkBox)
            locationButtons.append(checkBox)
        }
        scrollView.contentSize = CGSize(width: self.view.frame.width, height: y)
    }
    @objc func toggleState(sender: UIButton)
    {
        let y = sender.frame.origin.y
        if sender.tag == 0{
            sender.setImage(UIImage(named: "checked.png"), for: .normal)
            sender.tag = 1
            sender.sizeToFit()
            sender.frame = CGRect(x: 25, y: y, width: sender.frame.width, height: sender.frame.height)
            locationsChecked.append((sender.titleLabel?.text)!)
        }
        else{
            sender.setImage(UIImage(named: "unchecked.png"), for: .normal)
            sender.tag = 0
            sender.sizeToFit()
            sender.frame = CGRect(x: 25, y: y, width: sender.frame.width, height: sender.frame.height)
            var n = 0
            for i in locationsChecked{
                if i == sender.titleLabel?.text{
                    locationsChecked.remove(at: n)
                }
                n += 1
            }
        }
    }
    
    func setupApplyButton(){
        let applyButton = UIButton()
        Helper.addFilterButton(applyButton, self)
        applyButton.setTitle("Apply Changes", for: .normal)
        applyButton.addTarget(self, action: #selector(applyChanges), for: .touchUpInside)
        applyButton.sizeToFit()
        applyButton.frame = CGRect(x: (self.view.frame.width - applyButton.frame.width * 1.5)/2, y: applyButton.frame.origin.y, width: applyButton.frame.width * 1.5, height: applyButton.frame.height)
    }
    
    @objc func applyChanges(){
        applyFilterAndSortG = true
        let limit = -1 // NO CHANGE
        var location: Any = false
        let username = false // NO CHANGE
        var start: Any = false
        var end: Any = false
        var price:Any = false
        var sort: Any = false
        
        start = "\(start_time)".replacingOccurrences(of: " ", with: "")
        end = "\(end_time)".replacingOccurrences(of: " ", with: "")
        if Double(max_price) != nil{
            price = Double(max_price)!
        } else if(max_price != ""){
            Helper.alert("Error in Filtering", "Please enter a valid max price", self)
        }
        if sortByOption == "Location"{
            sort = "location"
        }
        if sortByOption == "Price"{
            sort = "asking_price"
        }
        if sortByOption == "Recency"{
            sort = "av_id"
        }
        if sortByOption == "Start Time"{
            sort = "start_time"
        }
        if sortByOption == "End Time"{
            sort = "end_time"
        }
        if locationsChecked.count == 0 { // this is so that other filters can take effect if no locations were checked.
            locationsChecked.append("false")
        }
        var avList:Array<AvailableObject> = [];
        FilterViewController.list = [];
        for l in locationsChecked{
            location = l.trimmingCharacters(in: .whitespacesAndNewlines) // locations have a tab at the start
            let urlString = GlobalVariables.rootUrl + "availability-list/" + "\(limit)/\(location)/\(username)/\(start)/\(end)/\(price)/\(sort)"
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
                FilterViewController.list += avList;

            }
        }
        
        // Seque back once done
        if let navController = self.navigationController {
            print(FilterViewController.list.count)
            navController.popViewController(animated: true)
        }
    }
    
    func setupResetButton(){
        resetButton.title = "Reset"
        resetButton.target = self;
        resetButton.action = #selector(resetAllFactors(sender:))
        self.navigationItem.rightBarButtonItem = resetButton;
    }
    
    @objc func resetAllFactors(sender: AnyObject) {
        sortByOption = ""
        locationsChecked = [String]()
        start_time = false
        end_time = false
        max_price = ""
        username = ""
        applyFilterAndSortG = false
        sortByLabel.text = "Sort By: " + sortByOption
        maxPriceTextBox.placeholder = "eg. 5.0"
        maxPriceTextBox.text = max_price
        startTimeLabel.text = "Start Time:"
        startDatePicker.setDate(Date(), animated: true)
        endTimeLabel.text = "End Time: "
        endDatePicker.setDate(Date(), animated: true)
        sortByPicker.selectRow(0, inComponent: 0, animated: true)
        for button in locationButtons{
            if button.tag == 1{
                self.toggleState(sender: button)
            }
        }
    }
    
    // Picker Functions
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return sortFactors.count
    }
    func pickerView(_ pickerView: UIPickerView, attributedTitleForRow row: Int, forComponent component: Int) -> NSAttributedString? {
        let string = sortFactors[row]
        return NSAttributedString(string: string, attributes: [NSAttributedString.Key.foregroundColor: GlobalVariables.mainColor])
    }
    
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        sortByOption = sortFactors[row]
        sortByLabel.text = "Sort By: " + sortFactors[row];
    }

    /*
     //MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
    }
    */

}
