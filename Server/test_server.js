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

app.listen(8000, '127.0.0.1', () => {
    console.log("Connected to port 8000");
});