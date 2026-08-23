# Campus Grievance Redressal System (CGRS)

A secure and efficient web-based system for reporting and resolving campus infrastructure issues.

## Features

- **User Registration & Authentication**: Secure user registration and login with role-based access control
- **Grievance Submission**: Submit grievances with proper categorization and location details
- **Admin Dashboard**: Comprehensive admin panel for managing and resolving complaints
- **Email Notifications**: Automated email notifications for grievance status updates
- **Category Management**: Support for multiple grievance categories:
  - Electrical issues (lights, fans, switches)
  - Computer hardware issues (keyboard, monitor, CPU, mouse)
  - Projectors and classroom equipment
  - Library and faculty room infrastructure
  - Water supply and basic campus facilities

## Technology Stack

- **Backend**: Java, Spring Boot 3.2.0, Hibernate (JPA)
- **Security**: Spring Security with JWT support
- **Database**: MySQL
- **Frontend**: Thymeleaf templates
- **Email**: Spring Mail
- **Build Tool**: Maven

## Prerequisites

- Java 21 or higher
- Maven 3.6+
- MySQL 8.0+
- SMTP email account (Gmail recommended)

## Setup Instructions

### 1. Database Setup

Create a MySQL database:
```sql
CREATE DATABASE cgrs_db;
```

### 2. Configuration

Update `src/main/resources/application.properties` with your database and email credentials:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/cgrs_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password

# Email Configuration
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password

# JWT Configuration (change in production)
jwt.secret=your-secret-key-change-this-in-production-minimum-256-bits
```

**Note**: For Gmail, you need to use an App Password instead of your regular password. Enable 2-factor authentication and generate an app password.

### 3. Build and Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will be available at: `http://localhost:8080`

### 4. Default Admin Account

A default admin account is automatically created on first run:
- **Username**: `admin`
- **Password**: `admin123`

**Important**: Change the admin password after first login!

## Usage

### For Users

1. Register a new account at `/register`
2. Login at `/login`
3. Submit grievances from the dashboard
4. Track grievance status and receive email notifications

### For Administrators

1. Login with admin credentials
2. Access the admin dashboard at `/admin/dashboard`
3. View all grievances with filtering options
4. Update grievance status and add remarks
5. Email notifications are automatically sent to users

## Project Structure

```
src/main/java/com/inn/cgrs/
├── config/          # Configuration classes (Security, Data initialization)
├── controller/      # REST controllers
├── model/           # Entity models (User, Grievance, Enums)
├── repository/      # JPA repositories
├── security/        # Security configuration (JWT, UserDetails)
└── service/         # Business logic services

src/main/resources/
├── templates/       # Thymeleaf templates
├── static/css/      # CSS stylesheets
└── application.properties
```

## Security Features

- Password encryption using BCrypt
- Role-based access control (USER, ADMIN)
- Secure session management
- JWT token support for API authentication
- CSRF protection (can be enabled for production)

## Email Notifications

The system sends email notifications for:
- Grievance submission confirmation
- Status updates (Pending → In Progress → Resolved/Rejected)
- Admin remarks and comments

## Development

### Running Tests
```bash
mvn test
```

### Building for Production
```bash
mvn clean package -DskipTests
```

The JAR file will be created in the `target/` directory.

## License

This project is developed for educational purposes.

## Support

For issues and questions, please contact the development team.
