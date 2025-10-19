# Orders Service

## Descripción General
La idea de este proyecto es la implementacion de un microservicio para la gestión de órdenes de entrega, desarrollado con **Spring Boot 3.5.6** y diseñado bajo una arquitectura limpia orientada a microservicios.

El servicio permite crear, consultar y actualizar órdenes, persistiendo los datos en **MongoDB**, utilizando **Redis** como mecanismo de caché y publicando eventos en **Kafka** ante cambios de estado.

El objetivo es demostrar la aplicación de buenas prácticas de ingeniería de software, manejo de mensajería asíncrona y uso de bases de datos NoSQL, siguiendo los lineamientos de la prueba técnica.

Las indicaciones permitian escoger entre Java y Go, asi que elegi Java porque es el lenguaje que yo manejo, de Go tengo pequeñas nociones, pero no lograria mostrar mi mejor desempeño, a como si trate de hacerlo con Java, espero se veo reflejado en mi implementacion final

## Arquitectura y Tecnologías

**Lenguaje:** Java 21
**Framework:** Spring Boot 3.5.6
**Base de datos:** MongoDB
**Cache:** Redis
**Mensajería:** Kafka
**Build Tool:** Maven
**Mapeo:** MapStruct
**Inyección de dependencias:** Spring Context
**Pruebas:** Spring Boot Test + Testcontainers
**Contenedores:** Docker y Docker Compose

El microservicio está organizado en capas (Controller, Service, Repository, Config, Mapper, DTOs y Modelos) para mantener una estructura modular y fácilmente extensible.
## Estructura del Proyecto
``` 
orders-service/
├── Dockerfile
├── docker-compose.yml
├── .env.example
├── pom.xml
├── src/
│   ├── main/
│   │   └── java/com/example/orders/
│   │       ├── OrdersApplication.java
│   │       ├── config/
│   │       │   ├── KafkaConfig.java
│   │       │   ├── RedisConfig.java
│   │       │   └── MongoConfig.java
│   │       ├── controller/
│   │       │   └── OrderController.java
│   │       ├── dto/
│   │       │   ├── CreateOrderRequest.java
│   │       │   ├── UpdateStatusRequest.java
│   │       │   └── OrderResponse.java
│   │       ├── mapper/
│   │       │   └── OrderMapper.java
│   │       ├── model/
│   │       │   ├── Order.java
│   │       │   ├── OrderAudit.java
│   │       │   └── OrderStatus.java
│   │       ├── repository/
│   │       │   └── OrderRepository.java
│   │       │   └── OrderAuditRepository.java
│   │       ├── service/
│   │       │   └── OrderService.java
│   │       ├── kafka/
│   │       │   └── OrderEventProducer.java
│   │       │   └── OrderEventConsumer.java
│   │       └── exception/
│   │           ├── NotFoundException.java
│   │           └── GlobalExceptionHandler.java
│   └── resources/
│       └── application.yml
└── src/
    └── test/
        ├── java/com/sjar/orders/
        │   ├── integration/
        │   │   ├── AbstractIntegrationTest.java
        │   │   └── OrderIntegrationTest.java
        │   └── unit/
        │       ├── OrderControllerTest.java
        │       └── OrderServiceTest.java
        └── resources/
            └── application-test.yml
``` 

## Requisitos Funcionales Implementados

### API REST
| Método | Endpoint | Descripción | 
|--------|-----------|--------------| 
| POST | `/orders` | Crea una nueva orden | 
| GET | `/orders/{id}` | Obtiene una orden por su identificador | 
| GET | `/orders?status=NEW&customerId=123` | Lista órdenes filtradas | 
| PATCH | `/orders/{id}/status` | Cambia el estado de una orden y publica un evento en Kafka | 

### Persistencia (MongoDB)

Cada orden se guarda con la siguiente estructura:

```json 
{ 
	"_id": "uuid", 
	"customerId": "string", 
	"status": "string", 
	"items": 
	[ 
		{ 
			"sku": "string", 
			"quantity": 1, 
			"price": 100.0 
		} 
	], 
	"createdAt": "ISODate", 
	"updatedAt": "ISODate" 
} 
``` 

### Cache (Redis)

- La respuesta del endpoint `GET /orders/{id}` se cachea por **60 segundos** como se solicito.
- Cuando el estado de la orden cambia, el caché se invalida automáticamente mediante `@CacheEvict`.

### Mensajería (Kafka)

Cada cambio de estado genera un evento publicado en el tópico `orders.events` con el siguiente formato:

```json 
{ 
	"orderId": "uuid", 
	"oldStatus": "NEW", 
	"newStatus": "DELIVERED", 
	"timestamp": "2025-10-16T00:00:00Z" 
} 
```

### Consumer de Kafka
Implemente un consumer basico para registrar en la base de datos todos los cambios de estados que son procesado:
```json 
[
    {
        "id": "68f56c87ccec75fc415109cf",
        "orderId": "68f56c7cccec75fc415109ce",
        "oldStatus": "NEW",
        "newStatus": "DELIVERED",
        "timestamp": "2025-10-19T22:56:07.074Z"
    },
    {
        "id": "68f56cb1ccec75fc415109d0",
        "orderId": "68f56c7cccec75fc415109ce",
        "oldStatus": "DELIVERED",
        "newStatus": "CANCELLED",
        "timestamp": "2025-10-19T22:56:49.661Z"
    }
]
```

## Configuración de Entorno
El servicio utiliza variables de entorno definidas en el archivo `.env.example`, para poder levantar el proyecto, hagan una copia como `.env`:

```bash 
# Config BD de mongo  
SPRING_DATA_MONGODB_URI=mongodb://${MONGO_USER}:${MONGO_PASSWORD}@mongo:27017/${MONGO_DATABASE}?authSource=admin  
MONGO_USER=admin  
MONGO_PASSWORD=123456  
MONGO_DATABASE=ordersdb  
MONGO_DATA_PATH=./database-data/mongo-data  
  
# App  
SERVER_PORT=8080  
SPRING_REDIS_HOST=redis  
SPRING_REDIS_PORT=6379  
KAFKA_BOOTSTRAP_SERVERS=kafka:9092  
APP_KAFKA_TOPIC=orders.events
``` 

## Ejecución Local

### Opción 1: Docker Compose

El entorno completo (MongoDB, Redis, Kafka y el servicio) se levanta mediante un solo comando, lo cual es lo que yo personalmente recomiendo para evitar problemas de ejecucion local:

```bash 
docker-compose up --build 
``` 
> Asegurarse de tener la version 21 de Java, fue la que use para este proyecto

Una vez iniciado, la aplicación estará disponible en:
``` 
http://localhost:8080 
``` 
### Opción 2: Maven Local

Si se prefiere ejecutar sin contenedores:
1. Iniciar instancias locales de MongoDB, Redis y Kafka.
2. Configurar las variables de entorno.
3. Ejecutar el servicio con Maven:
```bash 
mvn spring-boot:run 
``` 
> Podria implicar problemas de ejecucion local (Por el IDE, version de Maven, version de Java, configuracion locales, etc). Por lo cual recomiendo usar la primer opcion de docker compose.

## Ejemplos de Uso (curl)
### Crear una orden
```bash 
curl -L 'http://localhost:8080/orders' -H 'Content-Type: application/json' -d '{"customerId":"sergio123","items":[{"sku":"Pepsi","quantity":2,"price":28},{"sku":"Gatorade","quantity":1,"price":88.9}]}'
``` 
### Consultar una orden
```bash 
curl -L 'http://localhost:8080/orders/68f3e180f9eeb14ec1a982d3'
``` 
### Consultar todas las ordenes
```bash 
curl -L 'http://localhost:8080/orders'
``` 
### Filtrar por estado y cliente
```bash 
curl -L 'http://localhost:8080/orders?status=DELIVERED&customerId=i4y564fr'
``` 
### Cambiar el estado de una orden
```bash 
curl -L -X PATCH 'http://localhost:8080/orders/68f4539fe12dc4f35ebf5007/status' -H 'Content-Type: application/json' -d '{"status":"DELIVERED"}'
``` 
### Revisar los logs guardados por el consumer de kafka
```bash 
curl -L 'http://localhost:8080/orders/audit'
```

## Health Check
El endpoint `/actuator/health` verifica la disponibilidad de MongoDB, Redis y Kafka. Ejemplo de respuesta:
```json 
{
    "status": "UP",
    "components": {
        "diskSpace": {
            "status": "UP",
            "details": {
                "total": 1081101176832,
                "free": 984398229504,
                "threshold": 10485760,
                "path": "/app/.",
                "exists": true
            }
        },
        "kafka": {
            "status": "UP",
            "details": {
                "Kafka": "Up"
            }
        },
        "mongo": {
            "status": "UP",
            "details": {
                "maxWireVersion": 17
            }
        },
        "ping": {
            "status": "UP"
        },
        "redis": {
            "status": "UP",
            "details": {
                "version": "7.4.6"
            }
        },
        "ssl": {
            "status": "UP",
            "details": {
                "validChains": [],
                "invalidChains": []
            }
        }
    }
} 
``` 

## Decisiones Técnicas
1. **Spring Boot** fue elegido por mayor control de conocimientos con el lenguaje de Java
2. **MapStruct** se usa para mantener la conversión limpia entre entidades y DTOs.
3. **Redis CacheManager** gestiona la caché declarativa con TTL de 60 segundos.
4. **Actuator** se configura automaticamente leyendo el `Application.yml` para mongoDB y Redis, y una configuracion extra para `Kafka` implementada en `KafkaConfig`  con un `HealthIndicator` personalizado para validar su conectividad.
5. **GlobalExceptionHandler** centraliza el manejo de errores y respuestas HTTP coherentes.
6. **Configuración externa** mediante `.env` y variables de entorno permite portabilidad entre entornos.
7. **Docker Compose** simplifica la orquestación de los servicios dependientes. Asi me evito que el proyecto siempre compile sin importar el entorno local, de lo contrario, en otros equipos este API podria tener problemas al compilar (IDE, Java, Maven, etc)

## Testing
El módulo de pruebas incluye:
- **Unit Tests** para `OrderService` y `OrderController`.
- **Integration Tests** usando **Testcontainers**, en la clase `AbstractIntegrationTest` se encuentra la configuracion para MongoDB, Redis y Kafka. Y en `OrderIntegrationTest` hago la implementacion y llamado de los metodos a testear.
- Simulación de eventos Kafka y validación de publicación correcta.

Para ejecutar estos test, deben abrir una terminal y ejecutar:
```bash 
mvn clean test
```
En caso de solo querer ejecutar un o unos test en especifico, ejecutar:
```bash 
mvn clean test -Dtest=OrderIntegrationTest
```

## Licencia
Este proyecto fue desarrollado con fines de evaluación técnica y no está destinado a uso comercial.

## Autor
Desarrollado por **Sergio Ayerdis (SJAR03)** como parte de una evaluación técnica de backend.