//
//  EditObjectViewController.swift
//  
//
//  Created by RupalT on 12/22/18.
//

import UIKit
import Alamofire
class EditObjectViewController: UIViewController, UITableViewDataSource, UITableViewDelegate, UIPickerViewDelegate, UIPickerViewDataSource {
    
    var priceTextField = UITextField()
    var startDatePicker = UIDatePicker();
    var endDatePicker = UIDatePicker();
    var locationPicker = UIPickerView()
    var startLabel = UILabel();
    var endLabel = UILabel();
    var locationLabel = UILabel();
    var saveButton = UIButton();
    @IBOutlet weak var table: UITableView!
    
    /** TAGS
     * 1: cell has a date picker
     * 2: cell is a start time label
     * 3: cell is an end time label
     * 4: cell is a location label
     */
    
    var array = [
        ["Price Label"],
        ["Start Time Label", "End Time Label"],
        ["Location Label"]
    ]
    let locations = ["Mustang", "Red Radish", "Canyon Cafe", "The Avenue", "805"]
    var showStartTimePicker = false
    var showEndTimePicker = false
    var showLocationPicker = false
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setStyle()
        
        // Element settings
        self.locationPicker.dataSource = self
        self.locationPicker.delegate = self
        priceTextField.text = "\(String(describing: globalAvObject.price!))"
        startDatePicker.date = (globalAvObject.start_time?.addingTimeInterval(TimeInterval(exactly: 14)!))!
        endDatePicker.date = globalAvObject.end_time!
        startLabel.text = "\(startDatePicker.date)"
        endLabel.text = "\(endDatePicker.date)"
        locationLabel.text = globalAvObject.location
        
        table.reloadData()
        table.frame = CGRect(x: 0, y: 0, width: self.view.frame.width, height: table.contentSize.height)
        
        createSaveButton()
        
    }
    func createSaveButton(){
        saveButton.setTitle("Save", for: .normal)
        saveButton.titleLabel?.font = UIFont(name: GlobalVariables.buttonFont, size: 24)
        saveButton.titleLabel?.textColor = UIColor.white
        saveButton.backgroundColor = GlobalVariables.mainColor
        saveButton.sizeToFit()
        saveButton.frame = CGRect(x: (self.view.frame.width - saveButton.frame.width * 1.5)/2, y: table.frame.origin.y + table.frame.height + 25, width: saveButton.frame.width * 1.5, height: saveButton.frame.height * 1)
        saveButton.layer.cornerRadius = 3
        saveButton.addTarget(self, action: #selector(saveChanges), for: .touchUpInside)
        
        self.view.addSubview(saveButton)
    }
    @objc func saveChanges(){
        
        if Double(priceTextField.text! ) == nil{
            Helper.alert("Error", "Please make sure price is in decimal format", self)
        }
        else{
            let price = priceTextField.text
            var start_time = "\(startDatePicker.date)"
            start_time = String(start_time.dropLast(6))
            var end_time = "\(endDatePicker.date)"
            end_time = String(end_time.dropLast(6))
            let location = locationLabel.text

            let urlString = "http://" + "127.0.0.1:8000/" + "change/availability/"
            
            let parameters: [String: Any] = [
                "user_id": UserDefaults.standard.string(forKey: "user_id")!,
                "token": UserDefaults.standard.string(forKey: "token")!,
                "av_id": globalAvObject.av_id,
                "asking_price": price!,
                "start_time": start_time,
                "end_time" : end_time,
                "location" : location!
            ]
            print(location!)
            Alamofire.request(urlString, method: .put, parameters: parameters, encoding: JSONEncoding.default)
                .responseJSON { response in
                    if response.response?.statusCode != 200{
                        Helper.alert("Error", "Could not change availability", self)
                    }
            }
            if let navController = self.navigationController {
                navController.popViewController(animated: true)
            }
        }
    }
    // Picker Functions
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return locations.count
    }
    func pickerView(_ pickerView: UIPickerView, attributedTitleForRow row: Int, forComponent component: Int) -> NSAttributedString? {
        let string = locations[row]
        return NSAttributedString(string: string, attributes: [NSAttributedString.Key.foregroundColor: GlobalVariables.mainColor])
    }
    
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        locationLabel.text = locations[row]
    }
    
    // Table Functions
    
    // # OF SECTIONS
    func numberOfSections(in tableView: UITableView) -> Int {
        return array.count
    }
    
    // # OF ROWS
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return array[section].count
    }
    
    // SET VALUES
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell:UITableViewCell = UITableViewCell(style: .default, reuseIdentifier: "cell")
        cell.textLabel?.text = ""
        cell.textLabel?.textColor = UIColor.white
        let label = UILabel()
        let image = UIImageView(image: UIImage(named: "money"))
        
        // PRICE TEXT FIELD
        if (array[indexPath.section][indexPath.row] == "Price Label"){
            priceTextField.font = UIFont(name: GlobalVariables.normalFont, size: 20)
            priceTextField.sizeToFit()
            priceTextField.frame = CGRect(x: 45, y: cell.frame.height/2 - priceTextField.frame.height/2, width: cell.frame.width, height: priceTextField.frame.height)
            label.textColor = GlobalVariables.mainColor
            cell.addSubview(priceTextField)
            
        }
        // START TIME LABLE
        if (array[indexPath.section][indexPath.row] == "Start Time Label"){
            cell.tag = 2
            startLabel.font = UIFont(name: GlobalVariables.normalFont, size: 20)
            startLabel.sizeToFit()
            startLabel.text = "\(startDatePicker.date)"
            startLabel.frame = CGRect(x: 45, y: cell.frame.height/2 - startLabel.frame.height/2, width: cell.frame.width, height: startLabel.frame.height)
            cell.addSubview(startLabel)
        }
        // END TIME LABEL
        if (array[indexPath.section][indexPath.row] == "End Time Label"){
            cell.tag = 3
            endLabel.font = UIFont(name: GlobalVariables.normalFont, size: 20)
            endLabel.sizeToFit()
            endLabel.text = "\(endDatePicker.date)"
            endLabel.frame = CGRect(x: 45, y: cell.frame.height/2 - endLabel.frame.height/2, width: cell.frame.width, height: endLabel.frame.height)
            cell.addSubview(endLabel)
        }
        // LOCATION LABEL
        if (array[indexPath.section][indexPath.row] == "Location Label"){
            cell.tag = 4
            locationLabel.font = UIFont(name: GlobalVariables.normalFont, size: 20)
            locationLabel.sizeToFit()
            locationLabel.frame = CGRect(x: 45, y: cell.frame.height/2 - locationLabel.frame.height/2, width: cell.frame.width, height: locationLabel.frame.height)
            cell.addSubview(locationLabel)
        }
        // PICKER FOR START TIME
        if (array[indexPath.section][indexPath.row] == "Start Time Picker" ){
            startDatePicker.sizeToFit()
            startDatePicker.frame = CGRect(x: 45, y: 0, width: cell.frame.width, height: 150)
            startDatePicker.addTarget(self, action: #selector(changeDate), for: .valueChanged)
            cell.addSubview(startDatePicker)
            cell.tag = 1;
        }
        // PICKER FOR END TIME
        if (array[indexPath.section][indexPath.row] == "End Time Picker"){
            endDatePicker.sizeToFit()
            endDatePicker.frame = CGRect(x: 45, y: 0, width: cell.frame.width, height: 150)
            endDatePicker.addTarget(self, action: #selector(changeDate), for: .valueChanged)
            cell.addSubview(endDatePicker)
            cell.tag = 1;
        }
        // PICKER FOR LOCATION
        if (array[indexPath.section][indexPath.row] == "Location Picker" ){
            locationPicker.backgroundColor = UIColor.white
            locationPicker.showsSelectionIndicator = true
            locationPicker.frame = CGRect(x:15, y: 0, width: cell.frame.width, height: 150)
            cell.addSubview(locationPicker)
            cell.tag = 1;
        }
        
        
        image.contentMode = .scaleAspectFit
        image.image = image.image!.withRenderingMode(.alwaysTemplate)
        image.tintColor = GlobalVariables.mainColor
        image.frame = CGRect(x: 0, y: cell.frame.height/2 - 30, width: 60, height: 60)
        
        if (indexPath.row == 0){
            cell.addSubview(image)
        }
        return cell
    }
    @objc func changeDate(){
        print("changing")
        startLabel.text = "\(startDatePicker.date)"
        endLabel.text = "\(endDatePicker.date)"
        
    }
    // CLICK ROW
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        // If start time label is clicked ...
        if (tableView.cellForRow(at: indexPath)?.tag == 2){
            showEndTimePicker = false
            showStartTimePicker = !showStartTimePicker
            if (showStartTimePicker){
                array[1] = ["Start Time Label", "Start Time Picker", "End Time Label"]
                tableView.reloadData()
            }
            else {
                array[1] = ["Start Time Label", "End Time Label"]
                tableView.reloadData()
            }
        }
        // If end time label is clicked ...
        if (tableView.cellForRow(at: indexPath)?.tag == 3){
            showStartTimePicker = false
            showEndTimePicker = !showEndTimePicker
            if (showEndTimePicker){
                array[1] = ["Start Time Label", "End Time Label", "End Time Picker"]
            }
            else{
                array[1] = ["Start Time Label", "End Time Label"]
            }
        }
        // If location label is clicked
        if (tableView.cellForRow(at: indexPath)?.tag == 4){
            showLocationPicker = !showLocationPicker
            if (showLocationPicker){
                array[2] = ["Location Label", "Location Picker"]
            }
            else{
                array[2] = ["Location Label"]
            }
        }
        tableView.reloadData()
        var addedHeight: CGFloat = 0.0
        if (showStartTimePicker || showEndTimePicker){
            addedHeight += (150 - 45)
        }
        if (showLocationPicker){
            addedHeight += (150 - 45)
        }
        table.frame = CGRect(x: 0, y: 0, width: self.view.frame.width, height: table.contentSize.height + addedHeight)
        
        saveButton.sizeToFit()
        saveButton.frame = CGRect(x: (self.view.frame.width - saveButton.frame.width * 1.5)/2, y: table.frame.origin.y + table.frame.height + 25, width: saveButton.frame.width * 1.5, height: saveButton.frame.height * 1)
    }
    
    // HEIGHT OF ROW
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        if (tableView.cellForRow(at: indexPath)?.tag == 1){
            return 150
        } else {
            return UITableView.automaticDimension
        }
        
    }
    
    // SECTION APPEARANCE
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let label = UILabel()
        label.text = ""
        label.backgroundColor = self.view.backgroundColor
        label.textColor = UIColor.white
        label.font = UIFont(name: "Times New Roman", size: 30)
        label.frame = CGRect(x: 10, y: 10, width: 100, height: 100)
        label.drawText(in: CGRect(x: 10, y: 10, width: 100, height: 100))
        return label
    }
    
    
    
    func setStyle(){
        self.title = "Edit My Availability";
        self.navigationController?.navigationBar.titleTextAttributes = [NSAttributedString.Key.foregroundColor : UIColor.white, NSAttributedString.Key.font:  GlobalVariables.navBarTitle
        ]
        self.view.backgroundColor = UIColor(red: 0.95, green: 0.95, blue: 0.95, alpha: 1);
    }

}
