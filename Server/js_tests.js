const WrapperObj = require('./sql_wrapper');

// Instance of sql_swapper.js. Allows us to use function within it. 
let wrapper = new WrapperObj({
    host : process.env.DB_HOST,
    user : process.env.DB_USERNAME,
    password : process.env.DB_PASSWORD,
    database : process.env.DB_NAME
});
console.log("start");
wrapper.postAvailabilityObject(12, 0, "test1", "2019-5-5 12:00:00", "2019-5-8 12:00:00");
wrapper.postAvailabilityObject(12, 0, "test1", "2019-5-5 00:00:00", "2019-5-8 12:00:00");
wrapper.postAvailabilityObject(12, 0, "test1", "2019-5-5 19:00:00","2019-8-5 12:00:00");
wrapper.postAvailabilityObject(12, 0, "test1", "2019-1-5 19:00:00","2019-1-30 12:00:00");
wrapper.postAvailabilityObject(12, 0, "test1", "2019-1-5 19:00:00","2019-2-2 12:00:00");
console.log("end");
//wrapper.getAvailabilityListUser(12);
//process.exit()