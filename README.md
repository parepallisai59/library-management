# Library Management System

A full-stack Library Management System built using Java, Spring Boot, Spring Security, MySQL, JPA/Hibernate, Thymeleaf, HTML, CSS, and JavaScript.

## 🌐 Live Demo

🚀 **Live Application:**  
https://library-management-1-ty4o.onrender.com

> Note: The application is deployed on Render using Docker and uses TiDB Cloud as the production database.


## Features

* User registration and login
* Role-based authentication for USER and ADMIN
* Admin dashboard
* Book management

  * Add books
  * Edit books
  * Delete books
  * Search books
* Issue and return books
* Automatic book quantity updates
* Due-date tracking
* Automatic fine calculation
* User dashboard
* Issue history
* Password reset functionality
* REST APIs
* MySQL database integration
* Spring Security with BCrypt password encryption

## Technology Stack

| Technology          | Usage                            |
| ------------------- | -------------------------------- |
| Java 17             | Backend development              |
| Spring Boot 3.3.5   | Application framework            |
| Spring Security     | Authentication and authorization |
| Spring Data JPA     | Database access                  |
| Hibernate           | ORM                              |
| MySQL               | Database                         |
| Thymeleaf           | Server-side web pages            |
| HTML/CSS/JavaScript | Frontend                         |
| Maven               | Build and dependency management  |

## Project Structure

```text
src/
├── main/
│   ├── java/com/library/management/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── exception/
│   │   ├── model/
│   │   ├── repository/
│   │   └── service/
│   │
│   └── resources/
│       ├── static/
│       ├── templates/
│       └── application.properties
│
└── test/
```

## Database Configuration

Create a MySQL database named:

```sql
CREATE DATABASE library_management;
```

The application uses an environment variable for the MySQL password:

```properties
spring.datasource.password=${DB_PASSWORD}
```

Set the environment variable before running the application.

### Windows PowerShell

```powershell
$env:DB_PASSWORD="YOUR_MYSQL_PASSWORD"
```

## Running the Project

Clone the repository:

```bash
git clone https://github.com/parepallisai59/library-management.git
```

Open the project folder:

```bash
cd library-management
```

Run the application using Maven Wrapper:

### Windows

```powershell
.\mvnw.cmd spring-boot:run
```

Open:

```text
http://localhost:8080
```

## User Roles

### USER

Users can:

* Log in
* View available books
* Search books
* View issued books
* Check due dates
* View issue history
* View applicable fines

### ADMIN

Administrators can:

* Manage books
* Manage users
* Issue books
* Process returns
* View library statistics
* Manage library operations

## Security

The application uses Spring Security for authentication and authorization.

Passwords are stored using BCrypt hashing, and role-based access control separates USER and ADMIN functionality.

Sensitive configuration such as the database password is not stored in the repository.

## Future Improvements

* Email notifications for due dates
* Book reservation system
* Advanced reporting and analytics
* Cloud deployment
* Improved UI/UX
* Automated testing expansion

## Author

**Sai Parepalli**

GitHub:
https://github.com/parepallisai59
