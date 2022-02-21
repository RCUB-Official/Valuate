## About Valuate
An unfinished web application for receiving feedbacks for various web sites, by their users (valuators), using provided code snippets.
Cross-origin resource sharing issues are solved by setting the "Access-Control-Allow-Origin" header in the ScriptServer.java servlet which delivers the JavaScript, inspired by Facebook SDK code snippet for Facebook comment sections on other (non-Facebook) sites.

## Getting started

```
git clone https://github.com/RestlessDevil/Valuate
cd Valuate
mvn package
```

## Configuration
Enter your database credientals and Valuate server url in configuration.properties file
```
./src/main/resources/config/configuration.properties
```

Encode your password in hex(SHA-512) and save it in auxiliary-auth.xml fajl. These user accounts will be automatically inserted into the database as administrators, upon their first login.
```
./src/main/resources/config/auxiliary-auth.xml
```

Both above mentioned configuration files can be overriden by specifying the absolute path to their respective override files (RestlessFramework feature), in overrides.properties file. If the overriden file is properties-type, then the override should contain only those specific property lines that should be overriden (non-overriden lines will have their values from the WAR).
```
./src/main/resources/config/overrides.properties
```

Before deploying the WAR file, make sure you have made the database and initialized it with the valuate.sql script.

## TODO:

1. Implement a user management panel and registration page (until then, you need to insert them manually into the database)
2. Implement report-view page instead of the mock one - use caching if it is too slow and use feedback received event to invalidate the cache.
3. Implement an attribute field management page.
4. Enforce additional anti-spam measures, e.g if site has anti-spam flag enabled, do not accept feedback unless the was a request for JavaScript from the same IP, some time ago...

### Credits
Up to 2022 Valuate was developed and hosted by [Vasilije Rajović](https://github.com/RestlessDevil) on behalf of RCUB.
