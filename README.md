# SmartTask Spring Boot Backend Application

## Overview
SmartTask is a lightweight, scalable **Spring Boot** application designed to manage and automate smart tasks using a modern **RESTful API**. It leverages the **Spring Boot ecosystem** to provide a robust backend for **task creation, scheduling, prioritization, and tracking**.

## Features
- **Task Management**: Create, update, and delete tasks with attributes like priority, due dates, and status.
- **Smart Automation**: Intelligent task categorization and assignment based on predefined rules or user input.
- **Database Integration**: Uses **Spring Data JPA** with an embedded **H2 database** (configurable for PostgreSQL) for persistence.
- **Kafka-Powered Email Service**: Implements **Apache Kafka** for asynchronous email notifications (task assignments, completions, or overdue alerts).
- **RESTful API Endpoints**: Seamless integration with front-end applications or external services.
- **Error Handling**: Graceful exception management and meaningful response codes for better debugging.

## Tech Stack
- **Java 17+**
- **Spring Boot 3.x**
- **Maven** for dependency management
- **H2 Database** (in-memory, configurable for PostgreSQL)
- **Lombok** for reducing boilerplate code
- **Apache Kafka** for event-driven notifications

## Getting Started
### Prerequisites
Ensure you have the following installed:
- **Java 17+**
- **Maven**
- **Kafka** (for email notifications)

### Installation
1. **Clone the repository**:
   ```sh
   git clone https://github.com/yidnetessema/smart_task_backend.git
   ```
2. **Navigate to the project directory**:
   ```sh
   cd smart_task_backend
   ```
3. **Build and run the application**:
   ```sh
   mvn spring-boot:run
   ```

### Access the API
Once the application is running, you can access the API at:
```
http://localhost:8080/api/v1/
```

## Contributing
Contributions are welcome! Feel free to **fork this repo**, **submit issues**, or **send pull requests**.


Happy coding! 🚀

