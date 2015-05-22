Installation of SIVA Server

REQUIREMENTS
- PostgreSQL 9.1 or newer
- Apache Tomcat 7
- Java 7
- An SMTP account

First you need to install the requirements:
ATTENTION: Java 7 or higher is required. Otherwise Tomcat will not deploy the SIVA Server. If there is already a Java version installed make sure it is Java 7 or higher. Otherwise it is recommended to uninstall it before.
# apt-get install postgresql
# apt-get install openjdk-7-jre
# apt-get install tomcat7

Additionally, you can install Tomcat's manager webapp for deploying the SIVA Server via a web GUI. We won't use it here.

Put the WAR archive containing the SIVA Server into Tomcat's webapps directory. This usually can be found at /var/lib/tomcat7/webapps.
# mv sivaServer.war /var/lib/tomcat7/webapps

If you want the SIVA Server to be deployed in a subdirectory of your domain you have to rename the file to the preferred directory name (e.g. if it should be deployed at www.example.com:8080/videoServer you have to rename the file to videoServer.war)
If you dont't want the SIVA Server to be in a subdirectory of your domain you have to remove the directory ROOT in the webapps directory and rename the file to ROOT.war.

Create the configuration directory .sivaServer in the home directory of the user who executes Tomcat (usually tomcat7) and give him write access to the new directory.
# cd ~tomcat7
# mkdir -m 0775 .sivaServer
# chgrp -c tomcat7 .sivaServer

Then Tomcat has to be restarted for deploying the SIVA Server:
# /etc/init.d/tomcat7 restart

For security reasons we create a new database and a new database user (no superuser) for the installation instead of using the root account.
# su - postgres
# psql
## CREATE USER sivaserver WITH PASSWORD 'myPassword';
## CREATE DATABASE sivaserver;
## GRANT ALL PRIVILEGES ON DATABASE sivaserver to sivaserver;
## \q

Open the database and enable pgcrypto extension.
# psql sivaserver
## CREATE EXTENSION IF NOT EXISTS pgcrypto;
## \q
# exit

Now go to the URL of your installation using a web browser. An installation page will appear after some seconds (the first time request after (re)starting Tomcat takes longer).
There you have to specify the database settings, SMTP settings and the account information for the administrator account to complete the installation.

Customization of the appearance and texts can be done in the branding subdirectory in the configuration folder of SIVA Server (usually /usr/share/tomcat7/.sivaServer/brandings).
You can create your own directory and file structure there. By using the URL prefix /branding/ you can access those files from the GUI. Example: <img src="/branding/logo.jpg" alt="logo" />
Directly in the configuration folder you will find BrandingConfiguration.properties where texts for some pages are defined and where you can exchange for example the logo file.