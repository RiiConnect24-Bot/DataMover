# Data Mover

Program used to move Redis Database data to MySQL.

This program takes data from a JSON file exported using [redis-rdb-tools](https://github.com/sripathikrishnan/redis-rdb-tools)

## Usage

1. Your JSON file must have the following first three sections in order:
- Codes
- Guild Settings
- Birthdays
2. Create a config.txt file with the following information in a separate line:
- user
- password
- host (ADDRESS:PORT)
- database
- useSSL (true or false)
- autoReconnect (true or false)
- verifyServerCertificate (true or false)

3. Run it using `java -jar DataMover.jar yourjsonfile.json`

