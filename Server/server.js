const express = require('express')
const bodyParser = require('body-parser')
const jwt = require('jsonwebtoken');
const fs = require('fs');
const nodemailer = require('nodemailer');
const tokenAge = "2h";
const cryenc = require('./cryptoencryption');

/** PUT STATUS CODES HERE
 * 200 == OK
 * 404 == Not Found
 * 403 == Permission Denied
 * 401 == Unacceptable input
 * 500 == SQL error
 * 501 == email error
 */

const WrapperObj = require('./sql_wrapper')

var wrapper = new WrapperObj({
    host : "localhost",
    user : "mealRoot",
    password : "paulhatalsky",
    database : "mealcredit"
});

// This info should be stored in environment variables eventually
var smtpTransport = nodemailer.createTransport({
    service: "Gmail",
    auth: {
        user: "mealcreditverifier@gmail.com",
        pass: "paulhatalsky1928"
    }
});

const tokenReturn = function(sendName, user_id) {
    if(user_id !== null) {
        let tokenMade = makeTokenUser(user_id);
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

let app = express()

app.use(bodyParser.urlencoded({extended: true}))
app.use(bodyParser.json())
app.enable('trust proxy')

/**
 * Regular functions
 */

 // Have a regular every hour to delete old availability/hunger
 const makeDateTime = (year, month, day, hour, minute, second) => {
    return `${year}-${month}-${day} ${hour}:${minute}:${second}`;
 };

 function deleteOld() {
    let dateObj = new Date();
    let day = dateObj.getDay();
    let month = dateObj.getMonth();
    let year = dateObj.getFullYear();
    let currDateTime = makeDateTime(year, month, day, dateObj.getHours(), dateObj.getMinutes(), dateObj.getSeconds());
    let prevDay = day == 0 ? 31 : day - 1; // Test if end of month
    let newMonth = (prevDay == 31 && month == 0) ? 12 : month; // When month needs to change to last year (Dec 31)
    newMonth = (newMonth == month && prevDay == 31) ? month - 1 : newMonth; // When month needs to change but not to last year
    let newYear = (newMonth == 12 && prevDay == 31) ? year - 1 : year;
    let pastDateTime = makeDateTime(newYear, newMonth, prevDay, dateObj.getHours(), dateObj.getMinutes(), dateObj.getSeconds());
    
    wrapper.deleteOldObjects(pastDateTime, currDateTime);
 }
 deleteOld();
 setInterval(deleteOld, 3600000);

/** GET requests -- getting any data from the database
 * Availability
 * Hunger
 */

app.get('/', function (req, res) {
    res.send('Start');
});

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

// Email verification
app.get('/confirmation/:token', (req, res) => {
    let token = cryenc.decrypt(req.params.token);
    let publicKey  = fs.readFileSync(__dirname + '/public.key', 'utf8');
    let verifyOptions = {
        issuer : "meal-server",
        subject : "meal-user",
        audience : "meal-app",
        maxAge : "1d",
        algorithm : ["RS256"]
    };
    let retToken;
    try {
        retToken = jwt.verify(token, publicKey, verifyOptions);
        //console.log(retToken);
    }
    catch(err) {
        return res.status(401).json({
            message : "Invalid token on email verification"
        });
    }

    if(retToken !== false) {
        let username = retToken.username;
        let password = cryenc.decrypt(retToken.password_enc);
        let firstname = retToken.username;
        let lastname = retToken.lastname;
        let email = retToken.email;
        let phonenumber = retToken.phonenumber;
        if(username !== undefined && password !== undefined && firstname !== undefined && lastname !== undefined) {
            if(retToken.phonenumber !== undefined) {
                wrapper.postUserObject(firstname, lastname, username, password, phonenumber, email).then((result) => {
                    tokenReturn(res, result);
                    return;
                });
            }
            else {
                wrapper.postUserObject(firsname, lastname, username, password, null, email).then((result) => {
                    tokenReturn(res, result);
                    return;
                });
            }
        }
    }
    else {
        res.status(401).json({
            message : "Invalid token for email confirmation",
            email_confirmed : false
        });
    }
});


/** Post Requests -- Creates/Inserts into database
 * User (on sign up)
 * Availability
 * Hunger
 */

 // creates and signs a new token
 function makeTokenUser(user_id) {
    let privateKey = fs.readFileSync(__dirname + '/private.key', 'utf8');
    let payload = {
        "user-id" : user_id,
        "login" : "yes"
    };
    let signOptions = {
        issuer : "meal-server",
        subject : "meal-user",
        audience : "meal-app",
        expiresIn : tokenAge,
        algorithm : "RS256"
    };

    return jwt.sign(payload, privateKey, signOptions);
 }

 // creates and signs an email token
 function makeTokenEmail(fn, ln, un, password, pn, em) {
    let privateKey = fs.readFileSync(__dirname + '/private.key', 'utf8');
    let passwordEnc = cryenc.encrypt(password);
    let payload = {
        password_enc : passwordEnc,
        firstname : fn,
        lastname : ln,
        phonenumber : pn,
        username : un,
        email : em
    };
    let signOptions = {
        issuer : "meal-server",
        subject : "meal-user",
        audience : "meal-app",
        expiresIn : "1d",
        algorithm : "RS256"
    };

    return cryenc.encrypt(jwt.sign(payload, privateKey, signOptions));
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
                let token = makeTokenUser(loginChecked["id"]);
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

 const sendEmail = (email, token, req, res) => {
    let rand = Math.floor((Math.random() * 100) + 54);
    let host = req.get('host');
    let link = "http://" + host + "/confirmation/" + token;
    let hover = ''; //`onmouseover="() => { this.style.opacity = "0.7"; }"`; // Cant get this to work
    let style = 'background-color: green; color: white; width: 4em; text-decoration: none; padding: 1vh 1vw; border-radius: 5%;';
    let mailOptions = {
        to : email,
        subject : "Please confirm your Meal Credit email account",
        html : `Hello,<br> Please Click on the button to verify your email.<br><br><a ${hover} style="${style}" href="${link}">Click here to verify</a><br><br>`
    };
    //console.log(mailOptions);
    smtpTransport.sendMail(mailOptions, function(error, response) {
        if(error) {
            console.log(error);
            return res.status(501).json({
                message : error
            });
        }
        else {
            return res.json({
                message : "email sent"
            });
        }
    });
 };

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
            // Result variable = user_id (null or number)
            if (phonenumber != null && email != null) {
                let token = makeTokenEmail(firstname, lastname, username, password, phonenumber, email);
                return sendEmail(email, token, req, res);
            }
            else if (email != null) {
                let token = makeTokenEmail(firstname, lastname, username, password, null, email);
                return sendEmail(email, token, req, res);
            }
            else if (phonenumber != null) {
                wrapper.postUserObject(firstname, lastname, username, password, phonenumber).then((result) => {
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
