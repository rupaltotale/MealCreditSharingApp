import mysql.connector as mariadb
import requests
import json

def getTimeOffset():
    try:
        stuff_back = requests.get("http://localhost:8000/")
        if stuff_back.text != "Start":
            print("Error: Cannot get proper connection to server!")
            exit()
    except:
        print("Error occurred connecting to server!")
        exit(0)

    mariadb_connection = mariadb.connect(user='mealRoot', password='paulhatalsky', database='mealcredit')
    if not mariadb_connection.is_connected():
        print("No connection to MySQL server. Make sure server is up/exists (mysqld).")
        exit(0)

    def do_deletions(should_close=True):
        # print("Doing deletion")
        cursor.execute("select user_id from Users where username='time_test';")
        row = cursor.fetchone()
        user_id = row[0]
        cursor.execute("delete from Availability where user_id={}".format(user_id))
        cursor.execute("delete from Users where username='time_test'")
        mariadb_connection.commit()
        if should_close:
            mariadb_connection.close()

    def call_register():
        # print("Calling to register")
        return requests.post("http://127.0.0.1:8000/register/", data = {
            "username" : "time_test",
            "password" : "4time_pass_"
        })

    cursor = mariadb_connection.cursor()
    
    stuff_back = call_register()
    # print(stuff_back.text)
    stuff_back = json.loads(stuff_back.text)
    
    if stuff_back["status"] == 401:
        do_deletions(False)
        stuff_back = call_register()
        stuff_back = json.loads(stuff_back.text)
    """ exit() """
    # print("Trying user_id next")
    user_id = stuff_back["user_id"]
    jwt = stuff_back["token"]
    # print(stuff_back.text)
    hour_using = "07"
    hour2_using = "10"
    num_hour = int(hour_using)
    num_hour2 = int(hour2_using)
    start_time_test = "2019-04-17 {}:13:00".format(hour_using)
    end_time_test = "2019-04-17 {}:13:00".format(hour2_using)
    stuff_back = requests.post("http://127.0.0.1:8000/create/availability/", data = {
        "user_id" : user_id,
        "token" : jwt,
        "asking_price" : "0",
        "start_time" : start_time_test,
        "end_time" : end_time_test
    })

    stuff_back = requests.get("http://localhost:8000/availability-list/-1/false/time_test/false/false/false/false")
    stuff_back = json.loads(stuff_back.text)
    stuff_back = stuff_back["result"]

    if len(stuff_back) == 1:
        # print(stuff_back)
        stuff_back = stuff_back[0]
        start_time = stuff_back["start_time"]
        end_time = stuff_back["end_time"]
        """ print("Start time received: {} vs Test: {}".format(start_time, start_time_test))
        print("End time received: {} vs Test: {}".format(end_time, end_time_test)) """
        total_start_test = 17 * 24 + num_hour
        total_start_actual = int(start_time[8:start_time.index("T")]) * 24 + int(start_time[11:start_time.index(":")]) 
        total_start_diff = total_start_actual - total_start_test
        total_end_test = 17 * 24 + num_hour2
        total_end_actual = int(end_time[8:end_time.index("T")]) * 24 + int(end_time[11:end_time.index(":")]) 
        total_end_diff = total_end_actual - total_end_test
        if total_start_diff != total_end_diff:
            print("Error: The differences between start_time and end_time are different.")
            do_deletions()
            return -1
        do_deletions()
        return total_start_diff
    print("Error: There was {} entries under time_test. This is wrong.".format(len(stuff_back)))
    do_deletions()
    return -1


if __name__ == "__main__":
    default_timeset = 18
    time_offset = getTimeOffset()
    lines = []
    filename = "Server/.env"
    if time_offset == -1:
        time_offset = default_timeset
    with open(filename, "r") as f:
        lines = f.readlines()
        lines = [x.strip() for x in lines]
        last_line = lines[len(lines) - 1]
        if "TIME_OFFSET" in last_line:
            l_index = last_line.rfind(" ")
            if l_index > -1:
                curr_offset = last_line[l_index:]
                try:
                    curr_offset = int(curr_offset)
                    if curr_offset != time_offset:
                        print("Difference in offsets...")
                        print("Newly Calculated: {} vs In file: {}".format(time_offset, curr_offset))
                    lines[len(lines) - 1] = "TIME_OFFSET = {}".format(time_offset)
                except:
                    print("Ya, dats not an integer.")
                    lines[len(lines) - 1] = "TIME_OFFSET = {}".format(time_offset)
        else:
            lines.append("TIME_OFFSET = {}".format(time_offset))

    with open(filename, "w") as f:
        for line in lines:
            f.write(line + "\n")
        