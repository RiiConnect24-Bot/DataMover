# Data Mover

[![License](https://img.shields.io/github/license/riiconnect24-bot/datamover.svg)](https://github.com/RiiConnect24-Bot/DataMover/blob/master/LICENSE)
[![Discord Server](https://img.shields.io/discord/206934458954153984.svg)](https://discord.gg/b4Y7jfD)
[![Support Server](https://img.shields.io/discord/366320450114158593.svg)](https://discord.gg/5rw6Tur)
[![Jenkins](https://img.shields.io/jenkins/s/https/jenkins.artuto.tk/view/RC24/job/RC24-Bot-DataMover.svg)](https://jenkins.artuto.tk/job/RC24-Bot-DataMover)
[![AppVeyor](https://ci.appveyor.com/api/projects/status/n3kbh5qocy87fpm8?svg=true)](https://ci.appveyor.com/project/Artuto/datamover)


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

