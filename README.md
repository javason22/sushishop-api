# sushishop-api
A sushi Shop Restful APIs made by Spring boot  
## Sushi API  
This is a RESTful API for a Sushi ordering system built with Spring Boot. It provides endpoints for creating orders, getting orders status, cancel orders(only orders with create, in-progress, and pause status are allowed to be cancel), pause orders(only orders with create and in-progress status can be pause) and resume (only orders with pause status can be resume) orders. The program uses hazelcast scheduler to instantiate three chef to pick the orders from the queue.

## Getting Started
### Prerequisites
Java 17 or higher
Maven
## Installation
Clone the repository
```sh
git clone https://github.com/Jasonkcwong/sushishop-api.git
```  
Build the project  
```sh
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
| `POST /api/orders` | Order sushi  |
| `DELETE /api/orders/{order_id}` | Cancel an order |
| `PUT /api/orders/{order_id}/pause` | Pause an order |
| `PUT /api/orders/{order_id}/resume` | Resume an order |
## Built With
Spring Boot - The web framework used  
Maven - Dependency Management  
H2 Database - In-memory database  
Embedded Redis - In-memory data structure store  
### License
This project is licensed under the MIT License - see the LICENSE.md file for details.
