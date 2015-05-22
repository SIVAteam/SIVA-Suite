Installation of SIVA Player Stats
- Apache 2 Web Server
- Apache mod_rewrite enabled

# apt-get install apache2
# apt-get install php5
# apt-get install php5-pgsql
# a2enmod rewrite

Set short_open_tag directive to On in php.ini (/etc/php5/apache2/php.ini).

Restart Apache web server to apply all changes.
# /etc/init.d/apache2 restart

You can change the default virtual host settings in /etc/apache2/sites-enabled.
We go with the default virtual host in /var/www/html

Depending on your system you have to allow mod_rewrite to rewrite rules on your virtual host. And you should disable indexes for security reasons. Do this in your virtual host configuration and restart Apache afterwards.
There should be something like this in within the virtual host's configuration:
		<Directory /var/www/html>
                Options +FollowSymLinks +MultiViews -Indexes
                AllowOverride FileInfo
        </Directory>

Put all extracted files and directories from the SIVA Player Stats into this directory.
Make the directories exports/ and pages/ writable for all users.

Then you can open the URL of the SIVA Player Stats in your browser and complete the installation there.

Create a cronjob that creates the cleaned logging data every day:
# crontab -e
## 0 7 * * * php5 -f /var/www/html/pages/update.php