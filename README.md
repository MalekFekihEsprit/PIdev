# TravelMate - Collaborative Travel Management Platform

## Overview

TravelMate is a comprehensive collaborative platform designed to streamline group travel planning and management. The application enables users to collectively organize every aspect of their journeys, from destination selection to budget tracking, ensuring seamless coordination among travel companions.

## System Architecture

The platform follows a modular architecture distributed across six functional domains, each managed independently while maintaining data consistency through shared entities.

## Core Modules

### 🌍 **Module 1: Destination & Accommodation Management**
Centralized management of travel locations and lodging options.
- Destination catalog with multi-criteria search
- Accommodation availability and pricing
- Geolocation integration for proximity search
- One-to-many relationship between destinations and accommodations

### 🏄 **Module 2: Activity & Category Management**
Organization and discovery of activities during travel.
- Hierarchical activity categorization
- Activity recommendations based on destination
- Multi-criteria filtering (price, duration, type)
- Category-based activity grouping

### ✈️ **Module 3: Travel Planning & Participation**
 Trip creation, scheduling, and participant coordination.
- Trip lifecycle management (planned, ongoing, completed)
- Role-based permissions for trip modifications
- Participant invitation and acceptance workflow
- Travel itinerary generation

### 💰 **Module 4: Budget & Expense Tracking**
Financial planning and expense splitting among participants.
- Multi-currency budget allocation
- Real-time expense tracking
- Automatic expense splitting
- Budget vs. actual variance analysis
- Payment reconciliation

### 🗺️ **Module 5: Itinerary & Stop Management**
Detailed day-by-day travel scheduling and activity sequencing.
- Chronological activity sequencing
- Location-based stop optimization
- Time conflict detection
- Dynamic itinerary adjustments

### 👥 **Module 6: User Management & Authentication**
User account management, security, and access control.
- Secure authentication (BCrypt)
- Role-based access control
- Session management
- User profile customization

## Technical Architecture
### Frontend
- **Framework:** JavaFX for rich desktop client
- **UI/UX:** Responsive design with custom styling
- **Navigation:** Modular navigation between functional domains

### Backend
- **Database:** MySQL for relational data persistence
- **Data Access:** JDBC with prepared statements
- **API Layer:** RESTful services for cross-module communication

## Development Workflow
1. **Module Development:** Each team member develops their assigned module independently
2. **Integration:** Modules are integrated through shared database schema and defined interfaces
3. **Testing:** Unit testing for individual modules, integration testing for cross-module functionality
4. **Deployment:** Consolidated application packaging


### Prerequisites
- Java 17+
- MySQL 8.0+
- Maven 3.8+
- Python 3.10+

### Installation
```bash
# Clone repository
git clone https://github.com/your-organization/travelmate.git

# Configure database
mysql -u root -p < database/schema.sql

# Build application
mvn clean package

# Run application
java -jar target/travelmate.jar
