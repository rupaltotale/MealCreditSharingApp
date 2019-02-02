function makeDateTime(year, month, day, hour, minute, second) {
    return `${year}-${month}-${day} ${hour}:${minute}:${second}`;
}

function parseDateTime(dateTime) {
    let dateTime = dateTime.replace("T", " ");
    let periodIndex = dateTime.indexOf(".");
    let firstHy = dateTime.indexOf("-");
    let secondHy = dateTime.substring(firstHy + 1).indexOf("-") + firstHy + 1;
    let spaceIndex = dateTime.indexOf(" ");
    let firstCo = dateTime.indexOf(":");
    let secondCo = dateTime.substring(firstCo + 1).indexOf(":") + firstCo + 1;
    
    let year = dateTime.substring(0, firstHy);
    let month = dateTime.substring(firstHy + 1, secondHy);
    let day = dateTime.substring(secondHy + 1, spaceIndex);
    let hour = dateTime.substring(spaceIndex + 1, firstCo);
    let minute = dateTime.substring(firstCo + 1, secondCo);
    let second = dateTime.substring(secondCo + 1, periodIndex);

    return [year, month, day, hour, minute, second];
}

function makeUsableDateTimeFromServer(serverDateTime) {
    let periodIndex = serverDateTime.indexOf(".");
    return serverDateTime.substring(0, periodIndex);
}

function getCurrentDate() {
    var dateObj = new Date();
    let day = dateObj.getDate();
    let month = dateObj.getMonth() + 1;
    let year = dateObj.getFullYear();
    let currDateTime = makeDateTime(year, month, day, dateObj.getHours(), dateObj.getMinutes(), dateObj.getSeconds());
    return currDateTime;
}

function getCurrentDayOffset() {
    let dateObj = new Date();
    let dateMill = dateObj.getTime();
    const MS_PER_DAY = 3600000 * 24;
    let newDate = new Date(dateMill - MS_PER_DAY);
    let pastDateTime = makeDateTime(newDate.getFullYear(), newDate.getMonth(), newDate.getDate(), 
        newDate.getHours(), newDate.getMinutes(), newDate.getSeconds());
    return pastDateTime;
}

function getHourOffset(serverDateTime, hourOffsetAhead) {
    const MS_PER_HOUR = 3600000;
    let timeOffset = hourOffsetAhead * MS_PER_HOUR;
    serverDateTime = makeUsableDateTimeFromServer(serverDateTime.toISOString());
    //console.log(serverDateTime);
    let firstDate = (new Date(serverDateTime)).getTime();
    //console.log(firstDate - timeOffset);
    let newDate = new Date(firstDate - timeOffset);
    // console.log(serverDateTime + " vs " + newDate.toISOString());
    return newDate.toISOString();
}

module.exports = {
    makeDateTime,
    parseDateTime,
    makeUsableDateTimeFromServer,
    getCurrentDate,
    getCurrentDayOffset,
    getHourOffset
}