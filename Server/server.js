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
    wrapper.getAvailabilityList().then((result) => {
        res.send(result);
    });
});

app.listen(8000, '127.0.0.1', () => {
    console.log("Connected to port 8000");
});