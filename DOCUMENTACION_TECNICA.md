# Documentación Técnica - Sistema de Gestión de Cuentas Bancarias

## 📌 Información General

**Desarrollador:** Jefferson Noroña  
**Tecnologías:** Java 21 + Spring Boot 3.5.9 | Angular 21  
**Base de Datos:** MySQL 8.0  
**Testing:** JUnit 5 + Mockito (Backend) | Jest (Frontend)

---

## 🏗️ Arquitectura del Sistema

### Backend - Arquitectura en Capas

```
├── domain/              # Capa de Dominio
│   ├── entity/         # Entidades JPA
│   └── repository/     # Interfaces Repository
├── application/         # Capa de Aplicación
│   ├── service/        # Lógica de negocio
│   └── mapper/         # Mappers DTO ↔ Entity
└── infrastructure/      # Capa de Infraestructura
    ├── controller/     # REST Controllers
    ├── config/         # Configuraciones
    └── exception/      # Manejo de excepciones
```

### Modelo de Datos

**Herencia:** Se implementó herencia de tabla única (SINGLE_TABLE) para Persona → Cliente

```sql
personas (tabla base)
├── id (PK)
├── tipo_persona (discriminador)
├── name, gender, age, identification
├── address, phone
├── password (solo para Cliente)
└── status (solo para Cliente)

cuentas
├── id (PK)
├── account_number (UNIQUE)
├── account_type (AHORRO/CORRIENTE)
├── initial_balance, current_balance
├── status
└── cliente_id (FK → personas)

movimientos
├── id (PK)
├── date, description
├── transaction_type (CREDITO/DEBITO)
├── amount, balance
└── cuenta_id (FK → cuentas)
```

---

## 🎯 Reglas de Negocio Implementadas

### 1. Gestión de Saldos
- **Crédito:** Valores positivos que incrementan el saldo
- **Débito:** Valores positivos que decrementan el saldo
- El saldo se actualiza automáticamente en cada transacción

### 2. Validaciones de Débito
```java
// Saldo cero o insuficiente
if (saldo <= 0 || saldo < monto) {
    throw new BusinessRuleException("Saldo no disponible");
}

// Límite diario de retiro ($1000)
BigDecimal debitosDelDia = repository.sumDebitsForDay(cuentaId, hoy);
if (debitosDelDia + montoDebito > 1000) {
    throw new BusinessRuleException("Cupo diario Excedido");
}
```

### 3. Generación de Reportes
- **Formato JSON:** Estructura detallada con totales por cuenta
- **Formato PDF:** Documento base64 descargable
- **Filtros:** Por cliente y rango de fechas

---

## 🔧 Patrones de Diseño Aplicados

### 1. Repository Pattern
Abstracción del acceso a datos mediante interfaces JPA Repository.

```java
public interface CustomerRepository extends JpaRepository<Customer, String> {
    Optional<Customer> findByIdentification(String identification);
}
```

### 2. Strategy Pattern
Manejo de tipos de transacciones mediante estrategias.

```java
public class TransactionStrategyService {
    private final Map<String, BiFunction<BigDecimal, BigDecimal, BigDecimal>> strategies;
    
    // CREDITO: suma | DEBITO: resta
}
```

### 3. DTO Pattern
Separación entre entidades de dominio y objetos de transferencia.

```java
CustomerDTO ← Mapper → Customer (Entity)
```

### 4. Service Layer Pattern
Lógica de negocio centralizada en servicios.

### 5. Exception Handler Pattern
Manejo global de excepciones con @RestControllerAdvice.

---

## 📡 API REST - Endpoints

### Clientes
```
GET    /api/clientes?page=0&size=10&q=busqueda
POST   /api/clientes
GET    /api/clientes/{id}
PUT    /api/clientes/{id}
PATCH  /api/clientes/{id}
DELETE /api/clientes/{id}
```

### Cuentas
```
GET    /api/cuentas?page=0&size=10&clienteId={id}
POST   /api/cuentas
GET    /api/cuentas/{id}
PATCH  /api/cuentas/{id}
DELETE /api/cuentas/{id}
```

### Movimientos
```
GET    /api/movimientos?page=0&size=10&cuentaId={id}&from={fecha}&to={fecha}
POST   /api/movimientos
DELETE /api/movimientos?id={id}
```

### Reportes
```
GET    /api/reportes?clienteId={id}&from={fecha}&to={fecha}&format={json|pdf}
```

---

## 🧪 Testing

### Backend - 30 Tests Unitarios
```bash
mvn test

Tests run: 30, Failures: 0, Errors: 0, Skipped: 0
```

**Cobertura:**
- AccountValidationServiceTest (6 tests)
- CustomerValidationServiceTest (6 tests)
- DebitValidationServiceTest (7 tests)
- TransactionStrategyServiceTest (7 tests)
- CustomersControllerTest (1 test)
- ReportsControllerTest (2 tests)
- TransactionsControllerTest (1 test)

### Frontend - 19 Tests con Jest
```bash
npm test

Test Suites: 9 passed, 9 total
Tests:       19 passed, 19 total
```

**Cobertura:**
- Componentes: Clients, Accounts, Reports
- Servicios: Client, Account, Movement, Report
- Layout: Header, Aside

---

## 🐳 Docker - Despliegue

### Servicios Configurados

```yaml
services:
  mysql-db:        # MySQL 8.0 con healthcheck
  accounts-backend: # Spring Boot (puerto 8081)
  frontend:        # Angular (puerto 4200)
```

### Comandos Docker

```bash
# Levantar servicios
docker-compose up -d

# Ver logs
docker-compose logs -f accounts-backend

# Detener servicios
docker-compose down

# Limpiar volúmenes
docker-compose down -v
```

---

## 🔐 Seguridad

- **CORS:** Configurado para permitir origen del frontend
- **Spring Security:** Configurado para permitir acceso a todos los endpoints (desarrollo)
- **Validaciones:** A nivel de modelo con anotaciones Jakarta Validation
- **SQL Injection:** Prevenido mediante JPA/Hibernate

---

## 📊 Características Adicionales

### Backend
✅ Uso de Lambdas y Streams
✅ Programación Funcional
✅ Exception Handlers personalizados
✅ Validaciones a nivel de modelo
✅ Configuración externalizada con valores por defecto
✅ Documentación OpenAPI/Swagger
✅ Logs estructurados con SLF4J

### Frontend
✅ Componentes standalone (Angular 21)
✅ Reactive Forms
✅ RxJS para manejo asíncrono
✅ Búsqueda rápida en tablas
✅ Notificaciones visuales
✅ Diseño responsive sin frameworks CSS
✅ Descarga de reportes PDF

---

## 📦 Entregables

1. ✅ Código fuente completo en repositorio
2. ✅ Script de base de datos: `entregables/BaseDatos.sql`
3. ✅ Colección de Postman: `entregables/Accounts Backend API - Casos de Prueba V2.postman_collection.json.json`
4. ✅ Docker Compose configurado
5. ✅ Tests unitarios (Backend: 30, Frontend: 19)
6. ✅ Documentación README.md

---

