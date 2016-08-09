# README #

This README presents PASTA Arrabbiata with an updated interface. Pasta helps instructors assessing the skills of students and provides these students with instant feedback not exclusively based on their programming skills but also on
their ability to design algorithms, understand network protocols, test databases, reason logically, measure complexity, etc.

### Requirements ###

PASTA Arrabbiata requires
* Apache Tomcat 8 or later
* Java 8 or later
* MySQL (tested with Ver 14.14 Distrib 5.5.50)

### Setup ###
  * Create a database called 'pasta'
  * Create a directory on which user 'tomcat' has write permissions (e.g., 'submissions')
  * Create a new WAR file from the source: 
    * `ant deploywar`
  * Deploy the war file using the standard tomcat manager interface

### Configure ###
  * Go to the deployment directory (e.g., `/opt/tomcat/webapps`, `/var/lib/tomcat8/webapps`)
  * Configure file `./PASTA/WEB-INF/classes/database.properties` with
    * database URL
    * database credentials (username and password) with write permissions
  * Configure `./PASTA/WEB-INFO/classes/project.properties` and
  set `project.location` to the PASTA directory
  * Configure `./PASTA/WEB-INFO/classes/log4j.properties` and
    * set `log4j.appender.file.File` to the desired log file
  * Configure `./PASTA/WEB-INFO/classes/messages.properties` and 
    * set UOS to the desired name for this Unit of Study or Course
  * Configure the database with a new user with identifier (e.g., unikey) 'login':
    * `INSERT INTO users(active, permission_level, username) VALUES (1, 'INSTRUCTOR', 'login');`
  * Restart the application from the tomcat management interface

### Test ###

* Go to http://localhost:8080 and click on the 'Manage App' button
* Login as a tomcat manager and start the PASTA app
* Click on the app or go to http://localhost:8080/PASTA/login/

### Who do I talk to? ###

* Vincent Gramoli <vincent.gramoli@sydney.edu.au>
* Martin McGrane <mmcg5982@uni.sydney.edu.au>