const mysql = require('mysql')

module.exports = class DataAccess {

    // Host, user, password, database
    constructor (connectionOptions) {
        this._connection = mysql.createConnection(connectionOptions);
    }

    async getAvailabilityList(size, where, who, startTime, endTime) {
        let users = [];
        let myQuery = `SELECT * FROM Availability`;

        if(where != "") {
            myQuery += ` WHERE location = ${where.toLowerCase()}`;
        }
        else if(who != "") {
            myQuery = `SELECT * FROM Availability JOIN ON Users Users.user_id = Availability.user_id WHERE Availability.user_id = ${who}`; 
        }

        if(startTime) {
            if(who || where) {
                myQuery += ` AND start_time > `;
            }
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
                        "username" : element.username
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

    async getUsersFromAlpha(start, limit) {
        let users = [];
        if(limit == -1) {
            var myQuery = `SELECT * FROM Users GROUP BY username`;
        }
        else {
            var myQuery = `SELECT * FROM Users GROUP BY username LIMIT ${limit}`;
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

    endConnection() {
        this._connection.end();
    }
}