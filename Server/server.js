const express = require('express')
const bodyParser = require('body-parser')
const jwt = require('jsonwebtoken');
const fs = require('fs');
const tokenAge = "2h";
const WrapperObj = require('./sql_wrapper')

// Instance of sql_swapper.js. Allows us to use function within it. 
let wrapper = new WrapperObj({
    host : "localhost",
    user : "mealRoot",
    password : "paulhatalsky",
    database : "mealcredit"
});
let app = express()

/** PUT STATUS CODES HERE
 * 200 == OK
 * 404 == Not Found
 * 403 == Permission Denied
 * 401 == Unacceptable input
 * 500 == SQL error
 */
app.use(bodyParser.urlencoded({extended: true}))
app.use(bodyParser.json())
app.enable('trust proxy')

app.get('/', function (req, res) {
    res.send('Start');
});

/** GET requests -- getting any data from the database
 * Availability
 * Hunger
 */

app.get('/availability-list', (req, res) => {
    //get all availability data
    wrapper.getAvailabilityList(-1, false, false, false, false, false).then((result) => {
        res.send(res.status(200).json({
            "result" : result
        }));
    });
    // What about error handling? Catch?
});

app.get('/availability-list/:size/:where/:who/:start/:end/:price', (req, res) => {
    // Creates an object representing the parameters for the SQL wrapper
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
        // What about status?
        res.send(result); 
    });
});

// Same as availability but for hunger
app.get('/hunger-list/:size/:where/:who/:start/:end/:price', (req, res) => {
    // Creates an object representing the parameters for the SQL wrapper
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
            holder[key] = "";
        }
    }

    wrapper.getNeedsList(holder["size"], holder["where"], holder["who"], holder["start"], holder["end"], holder["price"]).then((result) => {
        // What about status?
        res.send(result);
    });
});
/** Token Helper functions
 * makeToken: makes a token with private key, payload, and options by signing. 
 * verifyToken: verifies a token by extracting payload and checking if it contains correct info.
 */
function makeToken(user_id) {
    let privateKey = fs.readFileSync(__dirname + '/private.key', 'utf8');
    let payload = {
        "user-id" : user_id,
        "login" : "yes"
    };
    let options = {
        "issuer" : "meal-server",
        "subject" : "meal-user",
        "audience" : "meal-app",
        // tokenAge : "2h",
        "algorithm" : "RS256"
    };

    return jwt.sign(payload, privateKey, options);
 }

 function verifyToken(token) {
    let publicKey  = fs.readFileSync(__dirname + '/public.key', 'utf8');
    let options = {
        "issuer" : "meal-server",
        "subject" : "meal-user",
        "audience" : "meal-app",
        // "tokenAge" : tokenAge,
        "algorithm" : ["RS256"]
    };
    let payload;
    try {
        payload = jwt.verify(token, publicKey, options);
    }
    catch(err) {
        return false;
    }
    if((payload["login"] == "yes")) {
        return Number(payload["user-id"]);
    }
    else {
        // Implies that User does not have permission..?
        return false;
    }
}
// Test: Authentication of a user with their user_id and token
// Do basic OAuth check (tester function). ADMIN function test (need to know the user_id)
app.post('/verify/:id/:jwt', (req, res) => {
    let publicKey  = fs.readFileSync(__dirname + '/public.key', 'utf8');
    let options = {
        "issuer" : "meal-server",
        "subject" : "meal-user",
        "audience" : "meal-app",
        "tokenAge" : tokenAge,
        "algorithm" : ["RS256"]
    };
    let payload;
    try {
        payload = jwt.verify(req.params.jwt, publicKey, options);
    }
    catch(err) {
        return res.status(403).json({
            "message" : "VERIFICATION FAILED"
        });
    }
    if((payload["login"] == "yes") && (Number(payload["user-id"]) == Number(req.params.id))) {
        return res.status(200).json({
            "message" : "VERIFIED"
        });
    }
    else {
        return res.status(403).json({
            "message" : "VERIFIED, but wrong ID"
        });
    }
 });


 // Login. Sends back a JWT for authentication
 app.post('/login/:username/:password', (req, res) => {
    let username = req.params.username;
    let password = req.params.password;

    // LoginChecked is an object returned by checkUser. 
    wrapper.checkUser(username, password).then((loginChecked) => {
        // if matched exists..
        if("matched" in loginChecked) {
            // Both matched must be set to true and "id" must exist for the returned object to be acceptable
            if(loginChecked["matched"] && "id" in loginChecked) {
                let token = makeToken(loginChecked["id"]);
                return res.status(200).json({
                    message: "OAuth successful",
                    "token" : token
                });
            }
            // Login or password does not match
            else {
                return res.status(401).json({
                    message : "OAuth failed"
                });
            }
        }
        else {
            return res.status(401).json({
                message : "OAuth failed"
            });
        }
    });
 });
/** Post Requests -- Creates/Inserts into database
 * User (on sign up)
 * Availability
 * Hunger
 */

// Creates a new user object. (Register)
app.post('/user/:firstname/:lastname/:username/:password/:phonenumber?/:email?', function(req, res) {
    var firstname = req.params.firstname;
    var lastname = req.params.lastname;
    var username = req.params.username; // Needs to be checked if unique!!
    var password = req.params.password; // Will be hashed with salt
    var phonenumber = req.params.phonenumber; // Make sure on the front end that this is a number!
    var email = req.params.email; // Do email regex checking on front end
    //console.log('received post request')

    // Checks if the username given is unique. 
    wrapper.isUniqueUsername(username).then((unique) => {
        if(!unique) {
            return res.status(401).json({
                "message" : "username taken"
            });
        }
        // If it is unique ... 
        else {

            // TokenReturn is what happens once the user has been successfully created. 
            const tokenReturn = function(res, user_id) {
                if(user_id !== null) {
                    let token = makeToken(user_id);
                    return res.json({
                        "message" : "User created",
                        "user_id" : user_id,
                        "token" : token
                    });
                }
                else {
                    return res.status(500).json({
                        "message" : "error in user creation"
                    });
                }
            };

            // Creates a new user based on whether a phone or email has been provided
            // Change: Maybe we could create the user without the phone and email and then check if 
            // either is or both are are given and if they are we alter the Users table to add them. 
            if (phonenumber != null && email != null) {
                wrapper.postUserObject(firstname, lastname, username, password, phonenumber, email).then((result) => {
                    tokenReturn(res, result);
                    return;
                });
            }
            else if (phonenumber != null) {
                wrapper.postUserObject(firstname, lastname, username, password, phonenumber).then((result) => {
                    tokenReturn(res, result);
                    return;
                });
            }
            else if (email != null) {
                wrapper.postUserObject(firstname, lastname, username, password, null, email).then((result) => {
                    tokenReturn(res, result);
                    return;
                });
            }
            else {
                wrapper.postUserObject(firstname, lastname, username, password).then((result) => {
                    tokenReturn(res, result);
                    return;
                });
            }
        }
    });
});

function authenticate(token, res, user_id){
    if(token == undefined || token == "" || token == null) {
        return res.status(401).json({
            "message" : "Invalid token - 1"
        });
    }

    // ret token is either false (authentication failed) or equal to a user_id
    let retToken = verifyToken(token);
    // The === on the false is important, as 0 can be a user_id
    if(retToken === false) {
        return res.status(401).json({
            "message" : "Invalid token - 2"
        });
    }
    else if(retToken != user_id) {
        return res.status(401).json({
            "message" : "Invalid user ID"
        });
    }
    return true;
}

function sendResult(res, result){
    if(result) {
        return res.json({
            "message" : "success"
        });
    }
    else {
        return res.status(500).json({
            "message" : "insertion failure"
        });
    }
}
// Creates a new availability object
app.post('/availability/:user_id/:asking_price/:location/:start_time/:end_time/:token', function(req, res) {
    let user_id = req.params.user_id;
    let asking_price = req.params.asking_price;
    let location = req.params.location;
    let start_time = req.params.start_time;
    let end_time = req.params.end_time;
    let token = req.params.token;
    
    // Authentiates if the user has the permission to do an action. 
    let authentic = authenticate(token, res, user_id);
    if (authentic != true){
        // This means that the user does not have permission or that something went wrong
        return authentic;
    }

    wrapper.postAvailabilityObject(Number(user_id), Number(asking_price), location, start_time, end_time).
    then((result) => sendResult(res, result));
});

// Creates a new hunger object
app.post('/hunger/:user_id/:max_price/:location/:start_time/:end_time/:token', function(req, res) {
    let user_id = req.params.user_id;
    let max_price = req.params.max_price;
    let location = req.params.location;
    let start_time = req.params.start_time;
    let end_time = req.params.end_time;
    let token = req.params.token;
    
    // Authentiates if the user has the permission to do an action. 
    let authentic = authenticate(token, res, user_id);
    if (authentic != true){
        // This means that the user does not have permission or that something went wrong
        return authentic;
    }
    
    wrapper.postHungerObject(Number(user_id), Number(max_price), location, start_time, end_time).
    then((result) => sendResult(res, result));
});


/** PUT Requests -- Alters the database
 * User 
 * Availability
 * Hunger ?
 */
app.put('/change/availability/:user_id/:token/:av_id', (req, res) => {
    let user_id = Number(req.params.user_id);
    let availability_id = Number(req.params.av_id);
    let token = req.params.token;

    // Authentiates if the user has the permission to do an action. 
    let authentic = authenticate(token, res, user_id);
    if (authentic != true){
        // This means that the user does not have permission or that something went wrong
        return authentic;
    }
    let asking_price = req.body.asking_price;
    let location = req.body.location;
    let start_time = req.body.start_time;
    let end_time = req.body.end_time;
    if (asking_price != null){
        wrapper.changeTable("Availability", "asking_price", asking_price, availability_id, "av_id")
        .then((result) => {
            if (!result){
                return res.status(500).json({
                    "message" : "insertion failure"
                });
            }
        });
    }
    if (location != null){
        wrapper.changeTable("Availability", "location", location, availability_id, "av_id")
        .then((result) => {
            if (!result){
                return res.status(500).json({
                    "message" : "insertion failure"
                });
            }
        });
    }
    if (start_time != null){
        wrapper.changeTable("Availability", "start_time", start_time, availability_id, "av_id")
        .then((result) => {
            if (!result){
                return res.status(500).json({
                    "message" : "insertion failure"
                });
            }
        });
    }
    if (end_time != null){
        wrapper.changeTable("Availability", "end_time", end_time, availability_id, "av_id")
        .then((result) => sendResult(res, result));
    }
    return res.status(500).json({
        "message" : "insertion success"
    });

})

/** DELETE requests - remove objects
 * Availability
 * Hunger
 * Change: What if someone with an id = 3 deletes an av obj whose user_id = 5? 
 * We need to make sure that the user_id of the av obj matches the req.params.user_id
 */

 app.delete('/delete/availability/:user_id/:token/:av_id', (req, res) => {
    let user_id = Number(req.params.user_id);
    let availability_id = Number(req.params.av_id);
    let token = req.params.token;

    // Authentiates if the user has the permission to do an action. 
    let authentic = authenticate(token, res, user_id);
    if (authentic != true){
        // This means that the user does not have permission or that something went wrong
        return authentic;
    }
    wrapper.deleteAvailabilityObject(availability_id).then((result) => sendResult(res, result));
 });

 app.delete('/delete/hunger/:id/:token/:hg_id', (req, res) => {
    let user_id = Number(req.params.id);
    let hunger_id = req.params.hg_id;
    let token = req.params.token;

    // Authentiates if the user has the permission to do an action. 
    let authentic = authenticate(token, res, user_id);
    if (authentic != true){
        // This means that the user does not have permission or that something went wrong
        return authentic;
    }
    wrapper.deleteHungerObject(hunger_id).then((result) => sendResult(res, result));
 });


app.listen(8000, '127.0.0.1', () => {
    console.log("Connected to port 8000");
});
