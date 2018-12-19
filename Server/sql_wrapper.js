const mysql = require('mysql');
const bcrypt = require('bcrypt');
const saltRounds = 10;

module.exports = class DataAccess {

    // Host, user, password, database
    constructor (connectionOptions) {
        this._connection = mysql.createConnection(connectionOptions);
    }

    async getAvailabilityList(size, where, who, startTime, endTime, price) {
        let users = [];
        let myQuery = `SELECT * FROM Availability`;

        if(where != "") {
            myQuery += ` WHERE location = \'${where.toLowerCase()}\'`;
        }
        else if(who != "") {
            myQuery = `SELECT Availability.* FROM Availability JOIN Users ON Users.user_id = Availability.user_id \
                        WHERE Users.username = \'${who}\'`; 
        }

        if(startTime) {
            if(who || where) {
                myQuery += ` AND start_time >= \'${startTime}\' AND end_time <= \'${endTime}\'`;
            }
            else {
                myQuery += ` WHERE start_time >= \'${startTime}\' AND end_time <= \'${endTime}\'`;
            }
        }

        if(price) {
            if(who || where || startTime) {
                myQuery += ` AND asking_price <= ${price}`;
            }
            else {
                myQuery += ` WHERE asking_price <= ${price}`;
            }
        }

        myQuery += ` ORDER BY asking_price ASC`;

        if(size != -1) {
            myQuery += ` LIMIT ${size}`;
        }
        console.log(myQuery);

        await new Promise((resolve, reject) => this._connection.query(myQuery, (err, result, fields) => {
            if (err) {
                reject(err);
            }
            else {   
                for(let element of result) {
                    let userRet = {
                        "user_id" : element.user_id,
                        "asking_price" : element.asking_price,
                        "location" : element.location,
                        "start_time" : element.start_time,
                        "end_time" : element.end_time
                    };
                    users.push(userRet);
                }
                resolve(users);
            }
        }));
        
        return users;
    }

    async getUsersFromIDs(start, limit) {
        let users = [];
        let myQuery = ``;

        if(limit == -1) {
            myQuery = `SELECT * FROM Users WHERE user_id > ${start - 1}`;
        }
        else {
            myQuery = `SELECT * FROM Users WHERE user_id > ${start - 1} LIMIT ${limit}`;
        }
        await new Promise((resolve, reject) => this._connection.query(myQuery, (err, result, fields) => {
            if (err) {
                reject(err);
            }
            else {
                for(let element of result) {
                    let userRet = {
                        "user_id" : element.user_id,
                        "username" : element.username
                    };
                    users.push(userRet);
                }
                resolve(users);
            }
        }));
        
        return users;
    }

    // Gets n (= limit) number of availabilities posted by a user (id = user_id)
    async getUserAvalibiltyList(user_id, limit) {
        let availabilities = [];
        if(limit == -1) {
            var myQuery = `SELECT * FROM Availability WHERE location = ${user_id}`;
        }
        else {
            var myQuery = `SELECT * FROM Availability WHERE location = ${user_id} LIMIT ${limit}`;
        }

        await new Promise((resolve, reject) => this._connection.query(myQuery, (err, result, fields) => {
            if (err) {
                reject(err);
            }
            else {
                for(let element of result) {
                    let userRet = {
                        "location" : element.location,
                        "start_time" : element.start_time,
                        "end_time" : element.end_time
                    };
                    availabilities.push(userRet);
                }
                resolve(availabilities);
            }
        }));
        return availabilities;
    }

    // Gives a list of people who don't have a match yet
    async getNeedsList(location, start_time, end_time, limit, price){
        let users = [];
        let myQuery = `SELECT * FROM Hunger`;

        if(location != "") {
            myQuery += ` WHERE location = ${location.toLowerCase()}`;
        }
        if (start_time != ""){
            myQuery += `AND start_time <= ${start_time}`;
        }
        if (end_time != ""){
            myQuery += `AND end_time >= ${end_time}`;
        }
        if(size != -1) {
            myQuery += ` LIMIT ${limit}`;
        }

        await new Promise((resolve, reject) => this._connection.query(myQuery, (err, result, fields) => {
            if (err) {
                reject(err);
            }
            else {
                for(let element of result) {
                    let userRet = {
                        "user_id" : element.user_id,
                        "location" : element.location,
                        "start_time": element.start_time,
                        "end_time": element.end_time
                    };
                    users.push(userRet);
                }
                resolve(users);
            }
        }));
        
        return users;
    }

    async postUserObject(firstname, lastname, username, password, phonenumber = null, email = null){
        let myQuery = "";
        let salt = this.makeSalt(10);
        bcrypt.hash(password + salt, saltRounds).then((hash) => {
            myQuery = `INSERT INTO Users VALUES (DEFAULT, '${firstname}', '${lastname}', '${username}', '${password_hash}','${salt}', '${phonenumber}', '${email}')`;
            this._connection.query(myQuery);
        });
    }

    async postAvailabilityObject(user_id, asking_price, location, start_time, end_time ) {
        let myQuery = `INSERT INTO Availability VALUES (${user_id}, ${asking_price}, '${location}', '${start_time}', '${end_time}')`;
        this._connection.query(myQuery);
    }

    async postHungerObject(user_id, max_price, location, start_time, end_time ) {
        let myQuery = `INSERT INTO Hunger VALUES (${user_id}, ${max_price}, '${location}', '${start_time}', '${end_time}')`;
        this._connection.query(myQuery);
    }
    
    // Password helper functions 

    makeSalt (length) {
        var text = "";
        var possible = "*.-/|{}!@#$%^&():;<>?[]ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        
        for (var i = 0; i < length; i++)
          text += possible.charAt(Math.floor(Math.random() * possible.length));
        return text;
    }

    async checkUser(username, password) {
        let myQuery = `Select * from Users where username = '${username}'`
        let match;
        await new Promise((resolve, reject) => this._connection.query(myQuery, (err, result, fields) => {
            if (err) {
                reject(err);
            }
            else {
                let password_hash = result[0].password_hash;
                let salt = result[0].salt;
                match = bcrypt.compare(password + salt, password_hash);
                if(match) {
                    console.log("Password matches");
                }
                resolve(result);
            }
        }));
        return match;
    }
    
    endConnection() {
        this._connection.end();
    }
}