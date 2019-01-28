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
    let day = dateObj.getDay();
    let month = dateObj.getMonth();
    let year = dateObj.getFullYear();
    let currDateTime = makeDateTime(year, month, day, dateObj.getHours(), dateObj.getMinutes(), dateObj.getSeconds());
    return currDateTime;
}

function getCurrentDayOffset() {
    var dateObj = new Date();
    let day = dateObj.getDay();
    let month = dateObj.getMonth();
    let year = dateObj.getFullYear();
    let prevDay = day == 0 ? 31 : day - 1; // Test if end of month
    let newMonth = (prevDay == 31 && month == 0) ? 12 : month; // When month needs to change to last year (Dec 31)
    newMonth = (newMonth == month && prevDay == 31) ? month - 1 : newMonth; // When month needs to change but not to last year
    let newYear = (newMonth == 12 && prevDay == 31) ? year - 1 : year;
    let pastDateTime = makeDateTime(newYear, newMonth, prevDay, dateObj.getHours(), dateObj.getMinutes(), dateObj.getSeconds());
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