import mysql.connector as mariadb

def reset_all():
    mariadb_connection = mariadb.connect(user='mealRoot', password='paulhatalsky', database='mealcredit')
    cursor = mariadb_connection.cursor()

    names = ['Availability', 'Hunger', 'Users']
    for name in names:
        cursor.execute('DROP TABLE IF EXISTS {}'.format(name))

    import make_tables
    import insert_dummy_data

reset_all()