const express = require('express')
const bodyParser = require('body-parser')
const jwt = require('jsonwebtoken');
const nodemailer = require('nodemailer');
require('dotenv').load()
const tokenAge = "2h";
const WrapperObj = require('./sql_wrapper');
const cryenc = require('./cryptoencryption');

// Instance of sql_swapper.js. Allows us to use function within it. 
let wrapper = new WrapperObj({
    host : process.env.DB_HOST,
    user : process.env.DB_USERNAME,
    password : process.env.DB_PASSWORD,
    database : process.env.DB_NAME
});

// This info should be stored in environment variables eventually
var smtpTransport = nodemailer.createTransport({
    service: "Gmail",
    auth: {
        user: process.env.GMAIL_ACC,
        pass: process.env.GMAIL_PASS
    }
});

const tokenReturn = function(sendName, user_id) {
    if(user_id !== null) {
        let tokenMade = makeTokenUser(user_id);
        return sendName.json({
            status : 200,
            "message" : "User created",
            "user_id" : user_id,
            "token" : tokenMade
        });
    }
    else {
        return sendName.status(500).json({
            status : 500,
            message : "error in user database creation"
        });
    }
};

let app = express()

/** PUT STATUS CODES HERE
 * 200 == OK
 * 404 == Not Found
 * 403 == Permission Denied
 * 401 == Unacceptable input
 * 500 == SQL error
 * 501 == email error
 */

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
    console.log("REACHED");
    res.send('Start');
});

app.get('/availability-list', (req, res) => {
    //get all availability data
    wrapper.getAvailabilityList(-1, false, false, false, false, false).then((result) => {
        return res.json({
            "result" : result
        });
    });
    // What about error handling? Catch?
});

app.get('/availability-list/:userId', (req, res) => {
    // Creates an object representing the parameters for the SQL wrapper
    let userId = req.params.userId;

    wrapper.getAvailabilityListUser(userId).then((result) => {
        res.status(200).json({
            "result" : result
        });
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
        res.status(200).json({
            "result" : result
        }); 
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
        res.send(res.status(200).json({
            "result" : result
        }));
    });
});

// Email verification
app.get('/confirmation/:token', (req, res) => {
    let token = cryenc.decrypt(req.params.token);
    let publicKey  = process.env.PUBLIC_KEY.replace(/\\n/g, '\n');
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
    }
    catch(err) {
        return res.status(401).json({
            message : "Invalid token on email verification"
        });
    }

    if(retToken !== false) {
        let username = retToken.username;
        let password = cryenc.decrypt(retToken.password_enc);
        let firstname = retToken.firstname;
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

app.get('/send_recovery_mail/:email', function(req, res) {
    let email = req.params.email;
    wrapper.getUserFromEmail(email).then((users) => {
        if (users == []) {
            return res.json({
                "message" : "Email is not registered."
            });
        }
        user = users[0];
        let token = makeTokenEmail(user.firstname, user.lastname, user.username, null, null, user.email, user.user_id);
        return sendRecoveryEmail(user.firstname, user.lastname, email, token, req, res);
    }); 
    
    
});
app.get("/recover_account/:token/", function(req, res){
    let token = cryenc.decrypt(req.params.token);
    let publicKey  = process.env.PUBLIC_KEY.replace(/\\n/g, '\n');
    let verifyOptions = {
        issuer : "meal-server",
        subject : "meal-user",
        audience : "meal-app",
        maxAge : "1d",
        algorithm : ["RS256"]
    };
    let correctToken;
    try {
        correctToken = jwt.verify(token, publicKey, verifyOptions);
    }
    catch(err) {
        return res.status(401).json({
            message : "Invalid token"
        });
    }
    if (correctToken){
        let user_id = correctToken.user_id;
        token = makeTokenUser(user_id);
        return res.json({
            "message": "Valid Token",
            "Logged in" : "yes",
            "Token" : token,
            "User_id" : user_id
        });
        // once the user does this, they should be prompted to a page to change their password
    }


});
const sendRecoveryEmail = (firstname, lastname, email, token, req, res) => {
    let host = req.get('host');
    let link = "http://" + host + "/recover_account/" + token;
    let style = 'background-color: green; color: white; width: 4em; text-decoration: none; padding: 1vh 1vw; border-radius: 5%;';
    let mailOptions = {
        to : email,
        subject : "Recover Your Meal Credit Account",
        html : `Hello ${firstname} ${lastname},<br> Please Click on the button to recover your account.<br><br><a style="${style}" href="${link}">Click here to recover</a><br><br>`
    };
    smtpTransport.sendMail(mailOptions, function(error, response) {
        if(error) {
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
/** Token related functions
 * Make Token User: Makes a token given the user id
 * Make Token Email: makes email toen for email verification given all user details
 * Verify Token: This is the reverse of Make Token User. 
 */

 // creates and signs a new token
 function makeTokenUser(user_id) {
    let privateKey = process.env.PRIVATE_KEY.replace(/\\n/g, '\n');
    let payload = {
        "user-id" : user_id,
        "login" : "yes"
    };
    let signOptions = {
        issuer : "meal-server",
        subject : "meal-user",
        audience : "meal-app",
        //expiresIn : tokenAge,
        algorithm : "RS256"
    };

    return jwt.sign(payload, privateKey, signOptions);
 }

 // creates and signs an email token
 function makeTokenEmail(fn, ln, un, password, pn, em, user_id = null) {
    let privateKey = process.env.PRIVATE_KEY.replace(/\\n/g, '\n');
    let passwordEnc = null;
    if (password !== null){
        passwordEnc = cryenc.encrypt(password);
    }
    let payload = {
        password_enc : passwordEnc,
        firstname : fn,
        lastname : ln,
        phonenumber : pn,
        username : un,
        email : em,
        "user_id": user_id
    };
    let signOptions = {
        issuer : "meal-server",
        subject : "meal-user",
        audience : "meal-app",
        expiresIn : "1d",
        algorithm : "RS256"
    };
    let token = jwt.sign(payload, privateKey, signOptions);
    return cryenc.encrypt(token);
 }

 // verfies a given token
function verifyToken(token) {
    let publicKey  = process.env.PUBLIC_KEY.replace(/\\n/g, '\n');
    let options = {
        "issuer" : "meal-server",
        "subject" : "meal-user",
        "audience" : "meal-app",
        //maxAge : tokenAge,
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
        // Implies that User does not have permission.
        return false;
    }
}

 // Login. Sends back a JWT for authentication
 app.post('/login/', (req, res) => {
    let username = req.body.username;
    let password = req.body.password;
    let email = req.body.email;
    let pass = true;
    if(password === undefined || password == "") {
        pass = false;
    }
    if(username !== undefined && username.length == 0) {
        pass = false;
    }
    else if(email !== undefined && email.length == 0) {
        pass = false;
    }
    if(!pass) {
        return res.status(401).json({
            message : "invalid username or password input",
            status : 401
        });
    }

    //console.log("Login attempt: " + username);

    // LoginChecked is an object returned by checkUser. 
    wrapper.checkUser(username, email, password).then((loginChecked) => {
        // if matched exists..
        if("matched" in loginChecked) {
            // Both matched must be set to true and "id" must exist for the returned object to be acceptable
            if(loginChecked["matched"] && "id" in loginChecked) {
                let retToken = makeTokenUser(loginChecked["id"]);
                return res.status(200).json({
                    username: loginChecked["username"],
                    user_id: loginChecked["id"],
                    firstname : loginChecked["firstname"],
                    lastname : loginChecked["lastname"],
                    message: "OAuth successful",
                    token : retToken,
                });
            }
            // Login or password does not match
            else {
                return res.status(401).json({
                    message : "OAuth failed 1",
                    status: 401
                });
            }
        }
        else {
            return res.status(401).json({
                message : "OAuth failed 2",
                status: 401
            });
        }
    });
 });

 const sendEmail = (firstname, lastname, email, token, req, res) => {
    // let rand = Math.floor((Math.random() * 100) + 54);
    let host = req.get('host');
    let link = "http://" + host + "/confirmation/" + token;
    let hover = ''; //`onmouseover="() => { this.style.opacity = "0.7"; }"`; // Cant get this to work
    let style = 'background-color: green; color: white; width: 4em; text-decoration: none; padding: 1vh 1vw; border-radius: 5%;';
    let htmlToUse;
    if(firstname != null && firstname != "") {
       htmlToUse = `Hello ${firstname} ${lastname},<br> Please click on the button to verify your email.<br><br><a ${hover} style="${style}" href="${link}">Click here to verify</a><br><br>`;
    }
    else {
        htmlToUse = `Hello,<br> Please click on the button to verify your email.<br><br><a ${hover} style="${style}" href="${link}">Click here to verify</a><br><br>`
    }

    let mailOptions = {
        to : email,
        subject : "Please confirm your Meal Credit email account",
        html : htmlToUse
    };
    smtpTransport.sendMail(mailOptions, function(error, response) {
        if(error) {
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
/** Post Requests -- Creates/Inserts into database
 * User (on sign up)
 * Availability
 * Hunger
 */

// Creates a new user object. (Register)
app.post('/register/', function(req, res) {
    var firstname = req.body.firstname;
    var lastname = req.body.lastname;
    var username = req.body.username; 
    var password = req.body.password; // Will be hashed with salt
    var phonenumber = req.body.phonenumber; // Make sure on the front end that this is a number!
    var email = req.body.email; // Do email regex checking on front end
    //console.log("REACHED");
    
    if(username.length == 0 || username === undefined) {
        return res.status(401).json({
            status : 401,
            "message" : "invalid username"
        });
    }

    // Checks if the username given is unique. 
    wrapper.isUniqueUsername(username).then((unique) => {
        if(!unique) {
            return res.status(401).json({
                status : 401,
                "message" : "username taken"
            });
        }
        else {
            // Result variable = user_id (null or number)
            if (phonenumber != null && email != null) {
                wrapper.isUniqueEmail(email).then((unique) => {
                    if(!unique) {
                        return res.status(401).json({
                            status : 401,
                            message : "email taken"
                        });
                    }
                    else {
                        let token = makeTokenEmail(firstname, lastname, username, password, phonenumber, email);
                        return sendEmail(firstname, lastname, email, token, req, res);
                    }
                });
            }
            else if (email != null) {
                wrapper.isUniqueEmail(email).then((unique) => {
                    if(!unique) {
                        return res.status(401).json({
                            status : 401,
                            message : "email taken"
                        });
                    }
                    else {
                        let token = makeTokenEmail(firstname, lastname, username, password, null, email);
                        return sendEmail(firstname, lastname, email, token, req, res);
                    }
                });
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
        return res.status(200).json({
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
app.post('/create/availability/', function(req, res) {
    let user_id = req.body.user_id;
    let asking_price = req.body.asking_price;
    let location = req.body.location;
    let start_time = req.body.start_time;
    let end_time = req.body.end_time;
    let token = req.body.token;
    /*console.log(user_id + " " + asking_price + " " + location + " " + start_time + " " + end_time);
    res.json({
        "message" : "got it"
    });
    return;*/
    
    // Authentiates if the user has the permission to do an action. 
    let authentic = authenticate(token, res, user_id);
    if (authentic != true) {
        // This means that the user does not have permission or that something went wrong
        return authentic;
    }

    wrapper.postAvailabilityObject(Number(user_id), Number(asking_price), location, start_time, end_time).
    then((result) => sendResult(res, result));
});

// Creates a new hunger object
app.post('/create/hunger/', function(req, res) {
    let user_id = req.body.user_id;
    let max_price = req.body.max_price;
    let location = req.body.location;
    let start_time = req.body.start_time;
    let end_time = req.body.end_time;
    let token = req.body.token;
    
    // Authentiates if the user has the permission to do an action. 
    let authentic = authenticate(token, res, user_id);
    if (authentic != true){
        // This means that the user does not have permission or that something went wrong
        return authentic;
    }
    
    wrapper.postHungerObject(Number(user_id), Number(max_price), location, start_time, end_time).then((result) => sendResult(res, result));
});


/** PUT Requests -- Alters the database
 * User 
 * Availability
 * Hunger ?
 */

app.put('/change/availability/', (req, res) => {
    let user_id = Number(req.body.user_id);
    let availability_id = Number(req.body.av_id);
    let token = req.body.token;

    // Authentiates if the user has the permission to do an action. 
    let authentic = authenticate(token, res, user_id);
    if (authentic != true) {
        // This means that the user does not have permission or that something went wrong
        console.log("HACKER!!");
        return authentic;
    }
    let avObj = {
        "asking_price": req.body.asking_price,
        "location":req.body.location,
        "start_time":req.body.start_time,
        "end_time":req.body.end_time
    }
    const keys = Object.keys(avObj)
    for (let i in keys){
        let key = keys[i];
        let value = avObj[key];
        if (value != null){
            wrapper.changeTable("Availability", key, value, availability_id, "av_id")
            .then((result) => {
                if (!result){
                    console.log("unsuccessful")
                    return res.status(500).json({
                        "message" : "insertion failure"
                    });
                }
            });
        }
    }
    console.log("successful");
    return res.status(200).json({
        "message" : "insertion success"
    });

})

app.put('/change/user/', (req, res) => {
    let user_id = Number(req.body.user_id);
    let token = req.body.token;

    // Authentiates if the user has the permission to do an action. 
    let authentic = authenticate(token, res, user_id);
    if (authentic != true){
        // This means that the user does not have permission or that something went wrong
        return authentic;
    }
    let userObj = {
        "firstname": req.body.firstname,
        "lastname":req.body.lastname,
        "phone":req.body.phone,
        "email":req.body.email
    }
    // Code below till the return statement should probably be converted to a func. 
    const keys = Object.keys(userObj)
    for (let i in keys){
        let key = keys[i];
        let value = userObj[key];
        if (value != null){
            wrapper.changeTable("Users", key, value, user_id, "user_id")
            .then((result) => {
                if (!result){
                    return res.status(500).json({
                        "message" : "insertion failure"
                    });
                }
            });
        }
    }
    if (req.body.change_password){
        if (!req.body.require_old_password){
            let new_password = req.body.new_password;
            wrapper.changePassword(user_id, new_password).then((result) => {
                return res.json({
                    "message" : "password changed"
                })
            });
        }
    }
    else{
        return res.status(200).json({
            "message" : "insertion success"
        });
    }
})

/** DELETE requests - remove objects
 * Availability
 * Hunger
 * Change: What if someone with an id = 3 deletes an av obj whose user_id = 5? 
 * We need to make sure that the user_id of the av obj matches the req.params.user_id
 */

 app.delete('/delete/availability/', (req, res) => {
    let user_id = Number(req.body.user_id);
    let availability_id = Number(req.body.av_id);
    let token = req.body.token;

    // Authentiates if the user has the permission to do an action. 
    let authentic = authenticate(token, res, user_id);
    if (authentic != true){
        // This means that the user does not have permission or that something went wrong
        return authentic;
    }
    wrapper.deleteAvailabilityObject(availability_id).then((result) => sendResult(res, result));
 });

 app.delete('/delete/hunger/', (req, res) => {
    let user_id = Number(req.body.id);
    let hunger_id = req.body.hg_id;
    let token = req.body.token;

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
