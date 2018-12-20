const express = require('express')
const bodyParser = require('body-parser')
const jwt = require('jsonwebtoken');
const fs = require('fs');
const tokenAge = "2h";

/** PUT STATUS CODES HERE
 * 200 == OK
 * 404 == Not Found
 * 403 == Permission Denied
 * 401 == Unacceptable input
 * 500 == SQL error
 */

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
        res.send(result);
    });
});


/** Post Requests -- Creates/Inserts into database
 * User (on sign up)
 * Availability
 * Hunger
 */

 // creates and signs a new token
 function makeToken(user_id) {
    let privateKey = fs.readFileSync(__dirname + '/private.key', 'utf8');
    let payload = {
        "user-id" : user_id,
        "login" : "yes"
    };
    let signOptions = {
        issuer : "meal-server",
        subject : "meal-user",
        audience : "meal-app",
        expiresIn : "2h",
        algorithm : "RS256"
    };

    return jwt.sign(payload, privateKey, signOptions);
 }

 // verfies a given token
function verifyToken(token) {
    let publicKey  = fs.readFileSync(__dirname + '/public.key', 'utf8');
    let verifyOptions = {
        issuer : "meal-server",
        subject : "meal-user",
        audience : "meal-app",
        maxAge : tokenAge,
        algorithm : ["RS256"]
    };
    let payload;
    try {
        payload = jwt.verify(token, publicKey, verifyOptions);
    }
    catch(err) {
        return false;
    }
    if((payload["login"] == "yes")) {
        return Number(payload["user-id"]);
    }
    else {
        return false;
    }
}

 // Login. Sends back a JWT for authentication
 app.post('/login/:username/:password', (req, res) => {
    let username = req.params.username;
    let password = req.params.password;

    wrapper.checkUser(username, password).then((loginChecked) => {
        if("matched" in loginChecked) {
            // Both matched must be set to true and "id" must exist for the returned object to be acceptable
            if(loginChecked["matched"] && "id" in loginChecked) {
                let token = makeToken(loginChecked["id"]);
                return res.status(200).json({
                    message: "OAuth successful",
                    jwt : token
                });
            }
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

 // Do basic OAuth check (tester function). ADMIN function test (need to know the user_id)
 app.post('/verify/:id/:jwt', (req, res) => {
    let publicKey  = fs.readFileSync(__dirname + '/public.key', 'utf8');
    let verifyOptions = {
        issuer : "meal-server",
        subject : "meal-user",
        audience : "meal-app",
        maxAge : tokenAge,
        algorithm : ["RS256"]
    };
    let payload;
    try {
        payload = jwt.verify(req.params.jwt, publicKey, verifyOptions);
    }
    catch(err) {
        return res.status(403).json({
            message : "VERIFICATION FAILED"
        });
    }
    if((payload["login"] == "yes") && (Number(payload["user-id"]) == Number(req.params.id))) {
        return res.status(200).json({
            message : "VERIFIED"
        });
    }
    else {
        return res.status(403).json({
            message : "VERIFIED, but wrong ID"
        });
    }
 });

// Creates a new user object.
app.post('/user/:firstname/:lastname/:username/:password/:phonenumber?/:email?', function(req, res) {
    var firstname = req.params.firstname;
    var lastname = req.params.lastname;
    var username = req.params.username; // Needs to be checked if unique!!
    var password = req.params.password; // Will be hashed with salt
    var phonenumber = req.params.phonenumber; // Make sure on the front end that this is a number!
    var email = req.params.email; // Do email regex checking on front end
    //console.log('received post request')

    wrapper.isUniqueUsername(username).then((result) => {
        if(!result) {
            return res.status(401).json({
                message : "username taken"
            });
        }
        else {
            const tokenReturn = function(sendName, user_id) {
                if(user_id !== null) {
                    let tokenMade = makeToken(user_id);
                    return sendName.json({
                        message : "User created",
                        id : user_id,
                        token : tokenMade
                    });
                }
                else {
                    return sendName.status(500).json({
                        message : "error in user creation"
                    });
                }
            };

            // There has to be a better way to do this checking of null values.
            // Result variable = user_id (null or number)
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


// Creates a new availability object
app.post('/availability/:user_id/:asking_price/:location/:start_time/:end_time/:token', function(req, res) {
    let user_id = req.params.user_id;
    let asking_price = req.params.asking_price;
    let location = req.params.location;
    let start_time = req.params.start_time;
    let end_time = req.params.end_time;
    let token = req.params.token;
    
    if(token == undefined || token == "" || token == null) {
        return res.status(401).json({
            message : "Invalid token - 1"
        });
    }

    let retToken = verifyToken(token);
    // The === on the false is important, as 0 can be a user_id
    if(retToken === false) {
        return res.status(401).json({
            message : "Invalid token - 2"
        });
    }
    else if(retToken != user_id) {
        return res.status(401).json({
            message : "Invalid user ID"
        });
    }
    
    wrapper.postAvailabilityObject(Number(user_id), Number(asking_price), location, start_time, end_time).then((result) => {
        if(result) {
            return res.json({
                message : "success"
            });
        }
        else {
            return res.status(500).json({
                message : "insertion failure"
            });
        }
    });
});

// Creates a new hunger object
app.post('/hunger/:user_id/:max_price/:location/:start_time/:end_time/:token', function(req, res) {
    let user_id = req.params.user_id;
    let max_price = req.params.max_price;
    let location = req.params.location;
    let start_time = req.params.start_time;
    let end_time = req.params.end_time;
    let token = req.params.token;
    
    if(token == undefined || token == "" || token == null) {
        return res.status(401).json({
            message : "Invalid token - 1"
        });
    }

    let retToken = verifyToken(token);
    // The === on the false is important, as 0 can be a user_id
    if(retToken === false) {
        return res.status(401).json({
            message : "Invalid token - 2"
        });
    }
    else if(retToken != user_id) {
        return res.status(401).json({
            message : "Invalid user ID"
        });
    }
    
    wrapper.postHungerObject(Number(user_id), Number(max_price), location, start_time, end_time).then((result) => {
        if(result) {
            return res.json({
                message : "success"
            });
        }
        else {
            return res.status(500).json({
                message : "insertion failure"
            });
        }
    });
});


/** PUT Requests -- Creates/Inserts into database
 * User 
 * Availability
 * Hunger ?
 */


/** DELETE requests - remove objects
 * Availability
 * Hunger
 */

 app.delete('/delete/availability/:id/:token/:av_id', (req, res) => {
    let id = Number(req.params.id);
    let availability_id = req.params.av_id;
    let token = req.params.token;

    if(token == undefined || token == "" || token == null) {
        return res.status(401).json({
            message : "Invalid token - 1"
        });
    }

    let retToken = verifyToken(token);
    // The === on the false is important, as 0 can be a user_id
    if(retToken === false) {
        return res.status(401).json({
            message : "Invalid token - 2"
        });
    }
    else if(retToken != id) {
        return res.status(401).json({
            message : "Invalid user ID"
        });
    }

    wrapper.deleteAvailabilityObject(availability_id).then((result) => {
        if(result) {
            return res.json({
                message : "success"
            });
        }
        else {
            return res.status(500).json({
                message : "deletion failure"
            });
        }
    });
 });

 app.delete('/delete/hunger/:id/:token/:hg_id', (req, res) => {
    let id = Number(req.params.id);
    let hunger_id = req.params.hg_id;
    let token = req.params.token;

    if(token == undefined || token == "" || token == null) {
        return res.status(401).json({
            message : "Invalid token - 1"
        });
    }

    let retToken = verifyToken(token);
    // The === on the false is important, as 0 can be a user_id
    if(retToken === false) {
        return res.status(401).json({
            message : "Invalid token - 2"
        });
    }
    else if(retToken != id) {
        return res.status(401).json({
            message : "Invalid user ID"
        });
    }

    wrapper.deleteHungerObject(hunger_id).then((result) => {
        if(result) {
            return res.json({
                message : "success"
            });
        }
        else {
            return res.status(500).json({
                message : "deletion failure"
            });
        }
    });
 });


app.listen(8000, '127.0.0.1', () => {
    console.log("Connected to port 8000");
});
