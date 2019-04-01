import mysql.connector as mariadb

def doTest():
    print("Hello")
    mariadb_connection = mariadb.connect(user='bglossner', password='Doritos1', database='mealcredit')
    cursor = mariadb_connection.cursor()
    cursor.execute("insert into Users (user_id, username, password_hash) values (default, 'urmum', 'urdad');")
    #cursor.execute("DELETE FROM Users WHERE username='time_test'")
    mariadb_connection.commit()
    mariadb_connection.close()

if __name__ == "__main__":
    doTest()