# 🛡️ Microservicio de Autenticación - Spring WebFlux

Este microservicio permite registrar usuarios en el sistema mediante un endpoint REST `POST /api/v1/usuarios`, utilizando Spring WebFlux y arquitectura hexagonal. La solución incluye validaciones, manejo de excepciones, trazabilidad con logs y persistencia transaccional.

---

## 📌 Características principales

- Arquitectura hexagonal (puertos y adaptadores).
- Spring WebFlux (reactivo y no bloqueante).
- Persistencia con MongoDB (reactiva).
- Seguridad basada en JWT.
- Validación de datos de entrada.
- Logs trazables con `Slf4j`.
- Manejo centralizado de excepciones (`GlobalExceptionHandler`).
- Controladores REST en capa `adapter`.

---

## 📤 Endpoint principal

### Registrar nuevo usuario

**POST** `/api/v1/usuarios`

### Body esperado:
```json
{
  "nombres": "Juan",
  "apellidos": "Pérez",
  "fechaNacimiento": "1990-05-15",
  "direccion": "Av. Siempre Viva 123",
  "telefono": "987654321",
  "correoElectronico": "juan.perez@example.com",
  "salarioBase": 4500
}
```

### Validaciones:
- `nombres`, `apellidos`, `correoElectronico` y `salarioBase` son obligatorios.
- `correoElectronico` debe tener formato válido y no existir previamente.
- `salarioBase` debe ser numérico entre 0 y 15,000,000.

### Respuesta:
- ✅ **201 Created**: Usuario creado exitosamente.
- ❌ **400 Bad Request**: Datos inválidos.
- ❌ **409 Conflict**: El correo ya está registrado.
- ❌ **500 Internal Server Error**: Error inesperado.

---

## 🧩 Estructura del proyecto

```
src/
├── application/
│   └── handler/              → Lógica del caso de uso (RegisterUserHandler)
├── domain/
│   ├── model/                → Entidades de dominio (Usuario)
│   └── port/                 → Interfaces de entrada/salida
├── infrastructure/
│   ├── adapter/
│   │   ├── controller/       → Controlador REST (UserController)
│   │   └── repository/       → Adaptador de MongoRepository
│   └── config/               → Beans, JWT, Seguridad, etc.
└── service/
    └── impl/                 → Servicios de dominio implementados
```

---

## 🔐 Seguridad

- JWT configurado para autenticación y autorización.
- `JwtAuthenticationFilter` se encarga de extraer y validar el token JWT.
- Implementación de `ReactiveUserDetailsService` para autenticación reactiva.

---

## 💾 Persistencia

- MongoDB usando `ReactiveMongoRepository`.
- Persistencia transaccional usando `@Transactional`.

---

## 🧪 Tests

Incluye pruebas unitarias para validación y lógica de negocio. Puedes correrlas con:

```bash
./mvnw test
```

---

## 🚀 Cómo ejecutar el proyecto

```bash
./mvnw spring-boot:run
```

---

## 🛠️ Requisitos

- Java 17+
- Maven 3.8+
- MongoDB en local o remoto
- (Opcional) Docker para levantar MongoDB localmente

---

## 🧠 Próximas mejoras

- Documentar con Swagger/OpenAPI.
- Agregar colección Postman.
- Registrar logs en AWS CloudWatch o ELK.