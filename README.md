This project is the backend service for a modern Cab Booking System, built using Spring Boot. It provides RESTful APIs for managing users (riders, drivers, admins), cabs, ride bookings, and payments.
The system is designed to be scalable and secure, utilizing Spring Data JPA for data persistence and Spring Security with JWT for authentication and authorization.

Features
User Management: Register and manage rider and driver accounts. Admin can manage all users.
Authentication & Authorization: Secure user login with JWT (JSON Web Tokens) and role-based access control (RIDER, DRIVER, ADMIN).
Cab Management: Drivers can register and manage their cabs. Update cab details and availability.
Location Tracking: Drivers can update their real-time location.
Ride Booking: Riders can request rides with specified pickup/drop-off locations and preferred cab types.
Ride Lifecycle Management: APIs for drivers to accept, mark as arrived, start, and complete rides. Riders/Drivers/Admins can cancel bookings.
Fare Estimation & Calculation: Basic logic for estimating and calculating ride fares.
Payment Integration (Conceptual): Mock endpoints to simulate payment initiation and webhook callbacks with a payment gateway.
Data Persistence: Uses PostgreSQL as the relational database.
Global Exception Handling: Consistent error responses for API consumers.
JPA Auditing: Automatic population of createdAt and updatedAt timestamps for entities.
