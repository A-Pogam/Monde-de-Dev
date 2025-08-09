# Monde de Dev

````markdown
# Project: API for Articles, Themes, and User Management

## Description

This project is a backend API built with Spring Boot for managing articles, themes, and user profiles. It allows users to register, log in, view and update their profiles, subscribe/unsubscribe to themes, and post articles with comments.

## Features

- **User Authentication**: Register, log in, and manage user credentials.
- **Articles Management**: Create, view, and comment on articles.
- **Themes Management**: Create and subscribe to themes.
- **JWT Authentication**: Secure API endpoints with JSON Web Tokens (JWT).
- **Database Integration**: Uses MySQL for data storage.

## Requirements

- **Java 17+**
- **MySQL** or any other relational database
- **Maven** for building the project

## Setup Instructions

### Step 1: Clone the repository

```bash
git clone https://your-repository-url.git
cd project-folder
````

### Step 2: Configure the Database

1. Create a database in MySQL (or your preferred RDBMS):

   ```sql
   CREATE DATABASE MDD;
   ```

2. Update the database configuration in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/MDD
spring.datasource.username=root
spring.datasource.password=rootroot
```

### Step 3: Build the Project

Make sure Maven is installed on your system, then build the project:

```bash
mvn clean install
```

### Step 4: Run the Application

Run the application with the following command:

```bash
mvn spring-boot:run
```

The application will run on `http://localhost:3001`.

### Step 5: Access the API

You can now access the following API endpoints:

#### User Authentication

* **POST** `/api/auth/register`: Register a new user
* **POST** `/api/auth/login`: Login with username/email and password

#### User Management

* **GET** `/api/users`: Get the current user's profile
* **PUT** `/api/users`: Update the current user's profile

#### Articles Management

* **GET** `/api/articles`: Get all articles for the user
* **GET** `/api/articles/{id}`: Get details of a specific article
* **POST** `/api/articles`: Create a new article
* **POST** `/api/articles/comment/`: Post a comment on an article

#### Theme Management

* **GET** `/api/themes`: Get all themes
* **GET** `/api/themes/subscribed`: Get the themes the user is subscribed to
* **POST** `/api/themes/subscribe/`: Subscribe to a theme
* **POST** `/api/themes/unsubscribe/`: Unsubscribe from a theme

## Configuration

### Application Properties

* **MySQL Database**: The application uses MySQL for storing user, article, comment, and theme data. Update the `application.properties` file with your database credentials.
* **JWT Secret**: The JWT secret key is defined in the `application.properties`. You can change the key for increased security.

```properties
jwt.secret=ThisIsMySuperSecretKeyThatIsLongEnoughToBeSafe1234567890abcDEF!!
```

### Tomcat Server Configuration

The application runs on port `3001` by default:

```properties
server.port=3001
```

### Logger Configuration

The logging level is set to `INFO` for general logging, and `DEBUG` for your specific package:

```properties
logging.level.root=INFO
logging.level.org.springframework=INFO
logging.level.com.openclassrooms=DEBUG
```

## JWT Authentication

The API uses JWT for authentication. When registering or logging in, the server will issue a JWT token that you need to include in the `Authorization` header as a Bearer token for secured API endpoints.

Example of an `Authorization` header:

```bash
Authorization: Bearer <your-jwt-token>
```

## Sample Requests

### 1. Register

```json
POST /api/auth/register
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securePassword123"
}
```

### 2. Login

```json
POST /api/auth/login
{
  "identifier": "john_doe",
  "password": "securePassword123"
}
```

### 3. Update User Profile

```json
PUT /api/users
{
  "username": "john_doe_updated",
  "email": "john_updated@example.com",
  "password": "newSecurePassword123"
}
```

### 4. Create Article

```json
POST /api/articles
{
  "title": "How to use Spring Boot",
  "description": "A guide on setting up Spring Boot projects.",
  "content": "Spring Boot makes it easy to create stand-alone, production-grade Spring-based applications."
}
```

### 5. Subscribe to a Theme

```json
POST /api/themes/subscribe?themeId=1
```
