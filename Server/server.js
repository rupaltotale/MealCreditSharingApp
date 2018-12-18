const express = require('express')
const bodyParser = require('body-parser')
const mysql = require('mysql')

const WrapperObj = require('./sql_wrapper')

let wrapper = new WrapperObj({
    host : "localhost",
    user : "mealRoot",
    password : "paulhatalsky",
    database : "mealcredit"
});

let app = express()

app.use(bodyParser.urlencoded({extended: true}))
app.use(bodyParser.json())
app.enable('trust proxy')

app.get('/', function (req, res) {
    res.send('Start');
});

app.get('/availability-list', (req, res) => {
    //get data
    wrapper.getAvailabilityList(-1, false, false, false, false, false).then((result) => {
        res.send(result);
    });
});

app.get('/availability-list/:size/:where/:who/:start/:end/:price', (req, res) => {
    let holder = {
        "size" : req.params.size,
        "where" : req.params.where,
        "who" : req.params.who,
        "start" : req.params.start,
        "end" : req.params.end,
        "price" : req.params.price
    }
    for(let key in holder) {
        if(holder[key] == "false") {
            holder[key] = false;
        }
    }

    wrapper.getAvailabilityList(holder["size"], holder["where"], holder["who"], holder["start"], holder["end"], holder["price"]).then((result) => {
        res.send(result);
    });
});


/** Post Requests -- Creates/Inserts into database
 * User (on sign up)
 * Availability
 * Hunger
 */

// Creates a new user object. What about password?
app.post('/user/:firstname/:lastname/:username/:phonenumber?/:email?', function(req, res){
    var firstname=req.params.firstname;
    var lastname=req.params.lastname;
    var username=req.params.username; // Needs to be checked if unique!! Create another function?
    var phonenumber=req.params.phonenumber; // Make sure on the front end that this is a number!
    var email=req.params.email;
    
    // There has to be a better way to do this checking of null values.
    if (phonenumber != null & email != null){
        wrapper.postUserObject(firstname, lastname, username, phonenumber, email);
    }
    else if (phonenumber != null){
        wrapper.postUserObject(firstname, lastname, username, phonenumber);
    }
    else if (email != null){
        wrapper.postUserObject(firstname, lastname, username, null, email);
    }
    else{
        wrapper.postUserObject(firstname, lastname, username);
    }
    
    res.send("Created Object"); // This should be changed later on. 
});


// Creates a new availability object
app.post('/availability/:user_id/:asking_price/:location/:start_time/:end_time', function(req, res){
    var user_id=req.params.user_id;
    var asking_price=req.params.asking_price;
    var location=req.params.location;
    var start_time=req.params.start_time;
    var end_time=req.params.end_time;
    
    wrapper.postAvailabilityObject(Number(user_id), Number(asking_price), location, start_time, end_time);
    res.send("Created Object"); // This should be changed later on. 
});

// Creates a new hunger object
app.post('/hunger/:user_id/:max_price/:location/:start_time/:end_time', function(req, res){
    var user_id=req.params.user_id;
    var max_price=req.params.max_price;
    var location=req.params.location;
    var start_time=req.params.start_time;
    var end_time=req.params.end_time;
    
    wrapper.postHungerObject(Number(user_id), Number(max_price), location, start_time, end_time);
    res.send("Created Object"); // This should be changed later on. 
});


app.listen(8000, '127.0.0.1', () => {
    console.log("Connected to port 8000");
});