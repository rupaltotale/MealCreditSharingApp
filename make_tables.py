import mysql.connector as mariadb

def make_tables():
    mariadb_connection = mariadb.connect(user='mealRoot', password='paulhatalsky', database='mealcredit')
    cursor = mariadb_connection.cursor()

    cursor.execute('create table Users (user_id INT AUTO_INCREMENT, firstname varchar(32), lastname varchar(32), username varchar(32), \
                    password_hash varchar(500), salt varchar(500), phone varchar(10), email varchar(64) NULL, PRIMARY KEY (user_id), \
                    UNIQUE (username), UNIQUE(email))')

    cursor.execute('create table Availability (av_id INT AUTO_INCREMENT, user_id INT, asking_price FLOAT NOT NULL, location varchar(64), start_time DATETIME, end_time DATETIME, \
                    foreign key (user_id) references Users(user_id), primary key (av_id))')

    cursor.execute('create table Hunger (hg_id INT AUTO_INCREMENT, user_id INT, max_price FLOAT NOT NULL, location varchar(64), start_time DATETIME, end_time DATETIME, \
                    foreign key (user_id) references Users(user_id), primary key (hg_id))')

    mariadb_connection.close()

make_tables()