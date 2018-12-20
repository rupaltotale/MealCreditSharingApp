import mysql.connector as db

def reset_all():
    mariadb_connection = mariadb.connect(user='mealRoot', password='paulhatalsky', database='mealcredit')
    cursor = mariadb_connection.cursor()

    names = ['Users', 'Availability', 'Hunger']

    import make_tables