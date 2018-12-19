import mysql.connector as mariadb
import string
import random

firstNames = []
lastNames = []
userNames = []
numberOfRows = 10 # This is also equal to number of distinct users.

letters = "abcdefghijklmnopqrstuvwxyz"

# Inserts dummy data into the three lists above
for x in range(numberOfRows):
    firstName = ""
    lastName = ""
    for i in range(3):
        firstName += random.choice(letters)
        lastName += random.choice(letters)
    firstNames.append(firstName)
    lastNames.append(lastName)
    userNames.append(firstName + "_" + lastName + str(x))
locations = ["Avenue", "805", "Subway", "Campus Market", "Mustang", "Einstein", "Canyon Cafe", "Red Radish"]

def insert_data():
    mariadb_connection = mariadb.connect(user='mealRoot', password='paulhatalsky', database='mealcredit')
    cursor = mariadb_connection.cursor()
    # Resets the tables in order to ensure consistency. 
    # Note: you have to delete all data from Availability and Hunger before Users because their data
    # is dependent on Users Table (Foreign Key constraints).
    cursor.execute("DELETE from Availability;")
    cursor.execute("DELETE from Hunger;")
    cursor.execute("DELETE from Users;")
    cursor.execute("ALTER TABLE Users AUTO_INCREMENT = 1;")

    # Alters the datatype of start_time and end_time in case it isn't DATETIME
    cursor.execute("ALTER TABLE Availability MODIFY start_time DATETIME;")
    cursor.execute("ALTER TABLE Availability MODIFY end_time DATETIME;")
    cursor.execute("ALTER TABLE Hunger MODIFY start_time DATETIME;")
    cursor.execute("ALTER TABLE Hunger MODIFY end_time DATETIME;")

    # Alter Users to incorporate password (uncomment only if Users does not have password)
    # cursor.execute("alter table Users drop column Phone;")
    # cursor.execute("alter table Users drop column Email;")
    # cursor.execute("alter table Users add column password_hash varchar (500);")
    # cursor.execute("alter table Users add column salt varchar (500);")
    # cursor.execute("alter table Users add column phone varchar (10);")
    # cursor.execute("alter table Users add column email varchar (64);")
    
    # Inserts dummy data into the Users table
    for i in range (numberOfRows):
        query = "Insert into Users (firstname, lastname, username, password_hash, salt) VALUES ('{}', '{}', '{}', 'password', 'salt')".format(firstNames[i], lastNames[i], userNames[i])
        cursor.execute(query)
        mariadb_connection.commit()
    
    # Inserts dummy data into the Availability table
    insert_data_helper("Availability", cursor, mariadb_connection, "asking_price")

    # Inserts dummy data into the Hunger table
    insert_data_helper("Hunger", cursor, mariadb_connection, "max_price")

    mariadb_connection.close()
    
def insert_data_helper(tablename, cursor, mariadb_connection, priceFieldName):
    for i in range (numberOfRows):
        # The supported range is '1000-01-01 00:00:00' to '9999-12-31 23:59:59'
        month = random.randint(1,12)
        day = random.randint(1,28)
        hour = random.randint(1,20)
        minute = random.randint(1,59)
        start_time = "2019-{}-{} {}:{}:00".format(month, day, hour, minute)
        # The time period is set to 3 hours for now just for the sake of convenience. 
        end_time = "2019-{}-{} {}:{}:00".format(month, day, hour + 3, minute)

        asking_price = (random.randint(1,6) + random.randint(1,6))/2.0 # Max asking price is 6

        query = "Insert into {} (user_id, {}, location, start_time, end_time) VALUES ({}, {}, '{}', '{}', '{}');\
        ".format(tablename, priceFieldName, random.randint(1,numberOfRows), asking_price, random.choice(locations), start_time, end_time)

        cursor.execute(query)
        mariadb_connection.commit()

insert_data()