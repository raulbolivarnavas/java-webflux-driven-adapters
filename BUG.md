# Bug

Causa raíz confirmada: Spring Boot 4.1.0 gestiona r2dbc-mssql:1.0.4.RELEASE, que tiene un bug de compatibilidad con Netty 4.2.x (el que usa Spring Boot 4.x). La versión 1.0.4 crea el SSLEngine sin peer host para la fase TLS de
login de TDS; en Netty 4.2.x el endpointIdentificationAlgorithm se activa por defecto, lo que hace que el JDK busque un hostname que es null → "Hostname or IP address is undefined". Spring Boot 4.1.1 actualiza a               
r2dbc-mssql:1.0.5.RELEASE que corrige exactamente este problema.

Cambios aplicados:
- gradle.properties: springBootVersion=4.1.0 → 4.1.1
- build.gradle (raíz): plugin de Spring Boot 4.1.0 → 4.1.1
- libs.versions.toml: spring-boot = "4.1.0" → 4.1.1
- Revertidos todos los cambios incorrectos en DatabaseConfiguration.java, application.yml y sp-engine-executor/build.gradle

