# Sistema de Votación Distribuido

Este proyecto implementa un **sistema de votación electrónica distribuido**, diseñado para garantizar **escalabilidad**, **seguridad**, y **alta disponibilidad**, simulando un entorno nacional con múltiples estaciones de votación y consulta.

## 🚀 Tecnologías y Herramientas

- **Java 17**
- **Gradle**
- **ZeroC ICE** para comunicación distribuida
- **PostgreSQL** para persistencia de datos
- **Caffeine Cache** para proxy de consultas
- **HMAC-SHA256** para autenticación
- **ZeroTier** para interconexión de nodos
- **CSV** para exportación de resultados
- **Shell Scripts** para despliegue automatizado

---

## 🧱 Arquitectura General

```plaintext
+------------------+         +--------------------+
|  VoteStation     | <-----> |   LoadBalancer     |
|  QueryStation    | <-----> |     (Broker)       |
+------------------+         +--------------------+
       |                            |
       | (ICE Proxies)              | (HMAC, Load balance, Cache)
       v                            v
+------------------+        +------------------+
|  Server Central  | <----> |   PostgreSQL DB  |
|  (Gestor votos)  |        +------------------+
+------------------+
