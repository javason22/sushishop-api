<p align="center">
      <img src="https://img.shields.io/badge/Release-V0.0.1-green.svg" alt="Downloads">
      <img src="https://img.shields.io/badge/JDK-17+-green.svg" alt="Build Status">
  <img src="https://img.shields.io/badge/Springdoc%20Open%20API-1.8.0-blue.svg" alt="Build Status">
   <img src="https://img.shields.io/badge/Emdeded%20Redis-1.4.3-red.svg" alt="Coverage Status">
   <img src="https://img.shields.io/badge/Spring%20Boot-3.2.5-blue.svg" alt="Downloads">
   <img src="https://img.shields.io/badge/Author-Jason%20Wong-ff69b4.svg" alt="Downloads">
 </a>
</p>  

# Sushi Shop-API
A sushi Shop API simulates a server-side application that allows takes orders from the customers, processes the orders with predefined processing duration in parallel, shows and updates the order status.
This application demonstrates the queuing and processing of orders with 3 chefs in parallel using Redis leftPop and rightPush operations. 
The application also uses H2 in-memory database to store the orders and their status.

## Features
- Pending orders in Redis's list and processed by left push and right pop operations
- The orders are processed by 3 chef schedulers in parallel

## Getting Started
### Prerequisites
- Java 17 or higher
- Maven

## Installation
1. Clone the repository
```sh
git clone https://github.com/javason22/sushishop-api.git
```  
2. Build the project  
```sh
mvn clean install
```  
3. Run the application  
```sh
mvn spring-boot:run
```  

## API Endpoints
| Endpoint                            | Description                                     |
|-------------------------------------|-------------------------------------------------|
| `GET /api/orders/status`            | Get a list of all orders, group by order status |
| `POST /api/orders`                  | Order sushi                                     |
| `DELETE /api/orders/{order_id}`     | Cancel an order                                 |
| `PUT /api/orders/{order_id}/pause`  | Pause an order                                  |
| `PUT /api/orders/{order_id}/resume` | Resume an order                                 |

## API Documentation
The API documentation is powered by Springdoc OpenAPI. It will be available at `http://localhost:8080/swagger-ui.html` after running the application.

## Built With
Spring Boot - The web framework used  
Maven - Dependency Management  
H2 Database - In-memory database  
Embedded Redis - In-memory data structure store  

## License
This project is licensed under the MIT License - see the LICENSE.md file for details.
