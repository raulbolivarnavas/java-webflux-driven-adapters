# Handle exceptions

En Spring WebFlux con RouterFunction/HandlerFunction, una buena opción es un AbstractErrorWebExceptionHandler, porque captura los errores que salen del pipeline reactivo sin meter try/catch en los handlers.

```groovy
GenericStoredProcedureAdapter
          │
          ├── R2dbcTimeoutException
          │         ↓
          │  DatabaseUnavailableException
          │         ↓
          │       503
          │
          ├── R2dbcException
          │         ↓
          │  StoredProcedureExecutionException
          │         ↓
          │       500
          │
          └── IllegalArgumentException
                    ↓
             StoredProcedureRequestException
                    ↓
                  400
```

- Bad Request:
```json
{
  "timestamp": "2026-08-30T06:42:00-05:00",
  "status": 400,
  "error": "Bad Request",
  "code": "INVALID_STORED_PROCEDURE_REQUEST",
  "message": "Missing required parameter: cardNumber",
  "path": "/data/v1/execute"
}
```

- Service Unavailable:
```json
{
  "timestamp": "2026-08-30T06:42:00-05:00",
  "status": 503,
  "error": "Service Unavailable",
  "code": "DATABASE_UNAVAILABLE",
  "message": "Base de datos temporalmente no disponible",
  "path": "/data/v1/execute"
}
```

- Internal Server Error:
```json
{
  "timestamp": "2026-08-30T06:42:00-05:00",
  "status": 500,
  "error": "Internal Server Error",
  "code": "STORED_PROCEDURE_EXECUTION_ERROR",
  "message": "Invalid object name 'Object..Detalle'",
  "path": "/data/v1/execute",
  "providerErrorCode": 208,
  "sqlState": "S0002"
}
```
Nota: para produccion conviene devolver mensaje "Error ejecutando operación en base de datos", para no revelar tablas o elementos.