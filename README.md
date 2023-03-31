# sushishop-api
A sushi Shop Restful APIs made by Spring boot  
## Sushi API  
This is a RESTful API for a Sushi ordering system built with Spring Boot. It provides endpoints for creating orders, updating orders, and retrieving orders.

## Getting Started
### Prerequisites
Java 11 or higher
Maven
## Installation
Clone the repository
```sh
git clone https://github.com/Jasonkcwong/sushishop-api.git
```  
Build the project  
```sh
cd sushi-api
mvn clean install
```  
Run the application  
```sh
mvn spring-boot:run
```  
Access the API
```bash
http://localhost:9000/api/orders/status
```  
## API Endpoints
| Endpoint | Description |
| --- | --- |
| `GET /api/orders/status` | Get a list of all orders |
| `GET /api/orders/{order_id}` | Get details for a specific order |
| `DELETE /api/orders` | Cancel an order |
| `PUT /api/orders/{order_id}/pause` | Pause an order |
| `PUT /api/orders/{order_id}/resume` | Resume an order |
## Built With
Spring Boot - The web framework used  
Maven - Dependency Management  
H2 Database - In-memory database  
### License
This project is licensed under the MIT License - see the LICENSE.md file for details.