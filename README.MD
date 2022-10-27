##Gas & Water Usage Monitoring Application

To build application and run tests:

````
mvn clean install
````
To start application:
````
mvn spring-boot:run
````

SWAGGER UI will be available at
````
http://localhost:8080/swagger-ui.html
````

Used:

- 'openapi-generator-maven-plugin' to generate Controller and DTO classes. API described in open-api.yml file.
- HSQLDB in-memory implementation. Tables will be created with Liquibase at application start.
- Standart Spring-boot logging, rolling file
- Spring data-JPA for data-layer
- End-to-end tests with MockMVC

Request examples:

- Create user and save measurement:
````
curl -X POST http://localhost:8080/app/user/user1/measurement -H "Content-Type: application/json" -d '{"id":null,"userLogin":null,"dateSaved":null,"consumptions":[{"type":"GAS","value":1000},{"type":"HOT_WATER","value":1000},{"type":"COLD_WATER","value":1000}]}'
````
- Get user's measurements:
````
curl -X GET "http://localhost:8080/app/user/user1/measurements?offset=0&size=10"
````