<div align="center">

# 📦 StockManagementApp

**Empowering Seamless Inventory, Accelerating Business Growth**

[![Java](https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-25-007396?style=for-the-badge&logo=java&logoColor=white)](https://openjfx.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![Maven](https://img.shields.io/badge/Maven-3.x-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)](https://maven.apache.org/)

[![JUnit5](https://img.shields.io/badge/JUnit-5.10-25A162?style=flat-square&logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![TestFX](https://img.shields.io/badge/TestFX-4.0-6DA55F?style=flat-square)](https://github.com/TestFX/TestFX)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)](LICENSE)
[![Last Commit](https://img.shields.io/github/last-commit/Anass7933/StockManagementApp?style=flat-square&logo=git&logoColor=white&color=0080ff)](https://github.com/Anass7933/StockManagementApp)

---

*A full-featured inventory and sales management system built with JavaFX and PostgreSQL*

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
  - [Prerequisites](#prerequisites)
  - [Database Setup](#database-setup)
  - [Installation](#installation)
  - [Running the Application](#running-the-application)
- [Default Credentials](#-default-credentials)
- [Testing](#-testing)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🎯 Overview

**StockManagementApp** is a comprehensive desktop application designed to help businesses efficiently manage their inventory, track sales, and streamline operations. Built with a modular architecture, it separates concerns across backend services, database layers, and intuitive front-end interfaces providing a robust and maintainable codebase.

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| 🧩 **Modular Design** | Clean separation of controllers, models, and services for easy maintenance |
| 🗃️ **Database Integration** | PostgreSQL with schema migrations and demo data for rapid setup |
| 🔐 **User Authentication** | Secure login with role-based access control (Admin, Stock Manager, Cashier) |
| 📊 **Dashboard Analytics** | Real-time statistics for total products, stock levels, and inventory status |
| 🛒 **Point of Sale** | Integrated cashier interface with cart management and checkout |
| 📦 **Stock Management** | Add, edit, delete, and restock products with category filtering |
| 👥 **User Management** | Admin panel for managing system users and their roles |
| 🐳 **Docker Support** | Containerized database for consistent environment setup |
| 🧪 **Comprehensive Testing** | Unit tests (JUnit 5) and UI tests (TestFX) for quality assurance |

---

## 🛠️ Tech Stack

<table>
<tr>
<td align="center" width="120">
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original.svg" width="48" height="48" alt="Java"/>
<br><b>Java 25</b>
</td>
<td align="center" width="120">
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/postgresql/postgresql-original.svg" width="48" height="48" alt="PostgreSQL"/>
<br><b>PostgreSQL</b>
</td>
<td align="center" width="120">
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/docker/docker-original.svg" width="48" height="48" alt="Docker"/>
<br><b>Docker</b>
</td>
<td align="center" width="120">
<img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/apache/apache-original.svg" width="48" height="48" alt="Maven"/>
<br><b>Maven</b>
</td>
</tr>
</table>

**Additional Libraries:**
- **JavaFX 25** - Modern UI framework for desktop applications
- **JUnit 5** - Unit testing framework
- **TestFX** - JavaFX UI testing library

---

## 📁 Project Structure

```
StockManagementApp/
├── 📂 docker/
│   ├── 📂 database/
│   │   ├── create_tables.sql      # Database schema
│   │   └── insert_demo_data.sql   # Sample data for testing
│   └── docker-compose.yml         # Docker configuration
├── 📂 src/
│   ├── 📂 main/
│   │   ├── 📂 java/com/stockapp/
│   │   │   ├── 📂 controllers/    # JavaFX controllers
│   │   │   │   └── 📂 cashier/    # Cashier-specific controllers
│   │   │   ├── 📂 models/
│   │   │   │   ├── 📂 entities/   # Data models (User, Product, Sale)
│   │   │   │   ├── 📂 enums/      # Enumerations (Category, Role)
│   │   │   │   └── 📂 interfaces/ # Model interfaces
│   │   │   ├── 📂 services/
│   │   │   │   ├── 📂 impl/       # Service implementations
│   │   │   │   └── 📂 interfaces/ # Service contracts
│   │   │   ├── 📂 utils/          # Utility classes
│   │   │   └── Main.java          # Application entry point
│   │   └── 📂 resources/
│   │       ├── 📂 css/            # Stylesheets
│   │       └── 📂 fxml/           # UI layout files
│   └── 📂 test/                   # Test classes
└── pom.xml                        # Maven configuration
```

---

## 🚀 Getting Started

### Prerequisites

Before you begin, ensure you have the following installed:

| Requirement | Version | Download |
|-------------|---------|----------|
| **Java JDK** | 25+ | [Download](https://jdk.java.net/) |
| **Maven** | 3.x | [Download](https://maven.apache.org/download.cgi) |
| **Docker** | Latest | [Download](https://www.docker.com/get-started) |

### Database Setup

1. **Start the PostgreSQL database using Docker:**

```bash
cd docker
docker-compose up -d
```

This will:
- Pull the PostgreSQL 15 image
- Create a database named `stockdb`
- Execute the schema creation and demo data scripts
- Expose PostgreSQL on port `5432`

2. **Verify the database is running:**

```bash
docker ps
```

You should see a container named `stock_db` running.

### Installation

1. **Clone the repository:**

```bash
git clone https://github.com/Anass7933/StockManagementApp.git
cd StockManagementApp
```

2. **Install dependencies:**

```bash
mvn clean install
```

### Running the Application

**Using Maven (Recommended):**

```bash
mvn javafx:run
```

**Alternative - Using Java directly:**

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.stockapp.Main"
```

---

## 🔑 Default Credentials

The demo data includes the following test accounts:

| Role | Username | Password |
|------|----------|----------|
| **Admin** | `admin` | `admin123` |
| **Stock Manager** | `manager` | `manager123` |
| **Cashier** | `cashier` | `cashier123` |

> ⚠️ **Note:** These are demo credentials. In a production environment, always use strong, unique passwords.

---

## 🧪 Testing

The project includes comprehensive tests using **JUnit 5** for backend logic and **TestFX** for UI testing.

**Run all tests:**

```bash
mvn test
```

**Run specific test class:**

```bash
mvn test -Dtest=ClassName
```

**Test coverage includes:**
- ✅ Service layer tests
- ✅ Utility class tests  
- ✅ Controller UI tests
- ✅ Authentication tests

---

## 🤝 Contributing

Contributions are welcome! Here's how you can help:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/AmazingFeature`)
3. **Commit** your changes (`git commit -m 'Add some AmazingFeature'`)
4. **Push** to the branch (`git push origin feature/AmazingFeature`)
5. **Open** a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

<div align="center">

**Made with ❤️ for efficient stock management**

[⬆ Back to Top](#-stockmanagementapp)

</div>
