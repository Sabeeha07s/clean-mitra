# Clean Mitra – Smart Waste Management System

A Java desktop application starter for managing waste-collection users, drivers and service issues. It uses Java 21, JavaFX and MongoDB.

## Features
- JavaFX desktop interface
- Add and view waste-collection issues
- Assign a driver name and track issue status
- MongoDB persistence
- Role labels for Admin, Driver and User (demo workflow; add production authorization before deployment)

## Requirements
- JDK 21
- Maven 3.9+
- MongoDB running locally or a MongoDB Atlas URI

## Run
1. Copy `src/main/resources/application.properties.example` to `application.properties`.
2. Set `mongodb.uri` to your MongoDB connection string and database name.
3. Run `mvn clean javafx:run`.

Do not commit real connection strings or credentials. The default local URI is for development only.

## Future enhancements
Add login with hashed passwords, server-side authorization, image attachments, notifications, pagination, audit history and automated tests.
