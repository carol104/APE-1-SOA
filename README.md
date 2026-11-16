# APE1 — Gestión de vehículos

Aplicación web para listar, buscar por placa, registrar, editar y eliminar vehículos. Incluye catálogos de marcas, modelos y colores, una API HTTP en Java y una interfaz JSP que consume esa API desde el servidor.

## Estructura y tecnologías

```text
APE1/
├── pom.xml                 # Compila ambos módulos
├── ape/                    # Backend: API y acceso JDBC
├── apefront/               # Frontend: JSP, JavaScript y servlets intermediarios
├── tests/                  # Pruebas JavaScript de carga de modelos
└── apesoa (1).sql           # Esquema y datos de ejemplo
```

| Componente | Tecnología |
| --- | --- |
| Lenguaje y compilación | Java, bytecode compatible con Java 11; Maven |
| Aplicación web | Jakarta EE API 10.0.0, Servlet 6.0, JSP |
| Interfaz | HTML, CSS y JavaScript con Fetch; sin framework ni npm |
| Base de datos | MySQL/MariaDB, consultas JDBC y `PreparedStatement` |
| Controlador JDBC | MySQL Connector/J 8.0.33 |
| JSON | Gson 2.10 en la API y org.json 20240303 |
| Pruebas | JUnit 4.13.2 y el ejecutor integrado de Node.js |
| Empaquetado | Dos archivos WAR: `ape.war` y `apefront.war` |

Flujo: navegador → `apefront` → API de `ape` → base de datos `apesoa`. El navegador usa rutas relativas del frontend; la conexión entre módulos se realiza en Java.

## Requisitos

- JDK 11 o superior, con `JAVA_HOME` apuntando al JDK y `java` disponible en `PATH`. La compilación de esta revisión se verificó con JDK 21.
- Maven 3.6.3 o superior, disponible como `mvn` en `PATH`.
- Apache Tomcat 10.1 para ejecutar los servlets y JSP. Tomcat 9 utiliza el espacio de nombres `javax` y no es compatible con este código `jakarta`.
- MySQL 8 o MariaDB 10.4 o superior, con una base `apesoa`. El volcado incluido procede de MariaDB 10.4.
- Acceso a Maven Central durante la primera compilación para descargar dependencias.
- Node.js 18 o superior, únicamente para ejecutar las pruebas JavaScript; la aplicación no necesita Node.js para funcionar.

NetBeans o IntelliJ son opcionales. PHP, phpMyAdmin y XAMPP tampoco son obligatorios; se puede usar el servicio MariaDB de XAMPP si ya está instalado.

## 1. Preparar la base de datos

Inicia MySQL/MariaDB. Abre su cliente con una cuenta que pueda crear bases y tablas:

```sh
mysql -u root -p
```

Ejecuta lo siguiente. Sustituye la ruta de `SOURCE` por la ubicación real del archivo; en Windows utiliza barras `/`:

```sql
CREATE DATABASE IF NOT EXISTS apesoa
  CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE apesoa;
SOURCE C:/ruta/al/proyecto/APE1/apesoa (1).sql;
SHOW TABLES;
```

También puedes importar el archivo desde phpMyAdmin después de seleccionar `apesoa`. El SQL no selecciona ni crea la base automáticamente: debes hacerlo antes de importarlo. Usa una base vacía; el volcado crea las tablas e inserta datos y no está preparado para ejecutarse varias veces sobre las mismas tablas.

Se crean `marcas`, `modelos`, `colores` y `vehiculos`, con claves foráneas y datos de ejemplo: una marca Audi, modelos A3 y A8, tres colores y cinco vehículos.

Para conectar con un usuario propio, créalo y concédele acceso a las tablas:

```sql
CREATE USER 'ape_app'@'localhost' IDENTIFIED BY 'cambia_esta_clave';
GRANT SELECT, INSERT, UPDATE, DELETE ON apesoa.* TO 'ape_app'@'localhost';
```

## 2. Configurar la aplicación

La configuración se lee de las variables de entorno del proceso que ejecuta Tomcat:

| Variable | Valor predeterminado | Función |
| --- | --- | --- |
| `DB_URL` | `jdbc:mysql://localhost:3306/apesoa` | Conexión JDBC del backend |
| `DB_USER` | `root` | Usuario de la base |
| `DB_PASSWORD` | Cadena vacía | Contraseña de la base |
| `APE_API_URL` | `http://localhost:8081/ape/api` | Dirección de la API que consume el frontend, sin `/` final |

Ejemplo en PowerShell para ejecutar **ambos WAR en un mismo Tomcat**, usando su puerto 8080:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
$env:DB_URL = 'jdbc:mysql://localhost:3306/apesoa'
$env:DB_USER = 'ape_app'
$env:DB_PASSWORD = 'cambia_esta_clave'
$env:APE_API_URL = 'http://localhost:8080/ape/api'
```

En Linux/macOS, usa `export DB_USER=ape_app`, y de igual forma para las demás variables. Las variables deben existir **antes de iniciar el servidor**. Un servicio de Windows o un servidor iniciado desde el IDE necesita esas variables en su propia configuración y un reinicio después de modificarlas. El proyecto no carga archivos `.env` automáticamente.

También puedes mantener dos servidores: frontend en 8080 y backend en 8081. En ese caso el valor predeterminado de `APE_API_URL` ya corresponde al backend. Cada instancia necesita sus propios puertos HTTP y de apagado para evitar conflictos.

## 3. Compilar e instalar

Abre una terminal en la carpeta que contiene este README y el `pom.xml` agregador:

```sh
java -version
mvn -version
mvn clean verify
```

El último comando compila ambos módulos, ejecuta las pruebas Java y genera:

```text
ape/target/ape.war
apefront/target/apefront.war
```

Si solo quieres compilar un módulo:

```sh
mvn -f ape/pom.xml clean verify
mvn -f apefront/pom.xml clean verify
```

## 4. Desplegar y ejecutar

Con Tomcat detenido, copia los WAR a su carpeta `webapps`. Para una instalación única en Windows:

```powershell
# Ajusta esta ruta a tu instalación de Tomcat.
$env:CATALINA_HOME = 'C:\servidores\apache-tomcat-10.1'
Copy-Item .\ape\target\ape.war "$env:CATALINA_HOME\webapps\ape.war"
Copy-Item .\apefront\target\apefront.war "$env:CATALINA_HOME\webapps\apefront.war"
& "$env:CATALINA_HOME\bin\catalina.bat" run
```

Ejecuta estos pasos desde la misma terminal donde definiste las variables. En Linux/macOS, copia los WAR a `$CATALINA_HOME/webapps/` e inicia con `$CATALINA_HOME/bin/catalina.sh run`. Si tu instalación usa `CATALINA_BASE`, utiliza el `webapps` de esa instancia.

Con el conector HTTP en 8080, abre:

- Interfaz: `http://localhost:8080/apefront/`
- Listado de la API: `http://localhost:8080/ape/api/vehiculos`
- Marcas: `http://localhost:8080/ape/api/marcas`

Si empleas instancias separadas, copia `ape.war` al backend y `apefront.war` al frontend y ajusta las URLs al puerto de cada una. En NetBeans/IntelliJ importa los módulos Maven, configura un servidor compatible y conserva los contextos `/ape` y `/apefront`.

No abras `index.jsp` directamente desde el explorador de archivos: necesita un servidor JSP. Los archivos `index.html` originales son páginas de ejemplo; la página de bienvenida del frontend es `index.jsp`.

## Uso

1. Abre la interfaz para ver los vehículos registrados.
2. Usa **Buscar** para buscar una placa exacta y **Ver Todos** para quitar el filtro.
3. En **Nuevo Vehículo**, completa placa, chasis, año, marca, modelo y color. Los modelos se cargan según la marca.
4. Usa **Editar** para actualizar los datos o **Eliminar** y confirma para borrar el vehículo.

La placa admite hasta 8 caracteres y el chasis hasta 17, de acuerdo con el esquema y sus datos de ejemplo. Ambos son obligatorios. El año debe estar entre 1950 y el año actual. Modelo y color deben ser identificadores positivos existentes en sus catálogos. La marca se obtiene a partir del modelo.

La interfaz administra vehículos; los catálogos se consultan y sus modificaciones se realizan directamente en la base de datos. El esquema original no impone unicidad a placa ni chasis.

## API HTTP

Las rutas siguientes son relativas al contexto `/ape`:

| Método | Ruta | Parámetros |
| --- | --- | --- |
| GET | `/api/vehiculos` | Sin parámetros: lista completa; `placa`: coincidencia exacta |
| POST | `/api/vehiculos` | Cuerpo `application/x-www-form-urlencoded`: `placa`, `chasis`, `anio`, `id_modelo`, `id_color` |
| PUT | `/api/vehiculos` | Parámetros en la URL: `id` y todos los campos de POST |
| DELETE | `/api/vehiculos` | `id` en la URL |
| GET | `/api/marcas` | Ninguno |
| GET | `/api/colores` | Ninguno |
| GET | `/api/modelos` | `id`: identificador de la marca |

Las consultas devuelven arrays JSON. Los vehículos incluyen nombres e identificadores de marca, modelo y color. Una búsqueda sin coincidencias devuelve `[]`.

Las modificaciones devuelven un JSON con `success` y `message`: HTTP 200 al completarse, 400 para parámetros inválidos, 404 si el vehículo que se intenta actualizar o eliminar no existe y 500 ante un fallo de base de datos, incluidas referencias inexistentes. El frontend devuelve 502 si no puede comunicarse con la API. Las modificaciones no usan redirecciones como señal de éxito.

Ejemplos en PowerShell, con ambos módulos en 8080:

```powershell
Invoke-RestMethod 'http://localhost:8080/ape/api/vehiculos'
Invoke-RestMethod 'http://localhost:8080/ape/api/modelos?id=1'
Invoke-RestMethod 'http://localhost:8080/ape/api/vehiculos?placa=TCP102'
```

Los archivos de ejemplo de Jakarta REST (`JakartaRestConfiguration` y `JakartaEE10Resource`) no forman parte del CRUD. Tomcat ejecuta los endpoints servlet descritos aquí; esos ejemplos JAX-RS requieren un proveedor adicional o un servidor Jakarta EE completo.

## Pruebas

```sh
# Compilación, pruebas Java y empaquetado de los dos módulos
mvn clean verify

# Pruebas JavaScript, desde la carpeta de este README
node --test tests/modelos.test.cjs
```

Las pruebas Java verifican parámetros inválidos y el cliente HTTP usando un servidor local de prueba: UTF-8, parámetros PUT, redirecciones y estados de error. Las pruebas JavaScript verifican la espera de los modelos, respuestas fuera de orden y recuperación del selector ante un error HTTP. No requieren una base de datos activa.

Para comprobar el flujo completo después del despliegue, registra un vehículo de prueba, búscalo, edítalo, vuelve a consultar sus datos y elimínalo. Comprueba además que al detener la API se muestre un mensaje de conexión y no una lista vacía como si la consulta hubiera tenido éxito.

## Problemas frecuentes

| Síntoma | Qué revisar |
| --- | --- |
| `mvn` no se reconoce | Instala Maven y añade su carpeta `bin` al `PATH`; comprueba `mvn -version`. |
| Error de compilación o Java | Usa un JDK, revisa `JAVA_HOME` y verifica que Maven utiliza ese JDK. |
| HTTP 404 al abrir la aplicación | Comprueba el despliegue de `ape.war` y `apefront.war`, los contextos, el puerto y los registros de Tomcat. |
| HTTP 502 en la interfaz | Comprueba que el backend responde y que `APE_API_URL` contiene puerto y contexto correctos. Con ambos WAR en 8080 debes cambiar el valor predeterminado 8081. |
| HTTP 500 en la API | Comprueba MySQL/MariaDB, las credenciales, la base `apesoa`, las tablas y las claves foráneas. Los detalles se registran en el servidor. |
| `Access denied for user` | Revisa `DB_USER`, `DB_PASSWORD` y los permisos del usuario desde el host donde se ejecuta el backend. |
| No aparecen modelos | Selecciona una marca y prueba `/ape/api/modelos?id=1`; comprueba que existan modelos para esa marca. |
| Aparece “Hello World” | Abre `/apefront/` o `/apefront/index.jsp`, y despliega el WAR actualizado. |
| Cambios que no se reflejan | Ejecuta `mvn clean verify`, actualiza los WAR y reinicia el servidor. |

## Alcance y configuración de acceso

El proyecto es una aplicación académica sin autenticación ni control de usuarios. El filtro del backend permite CORS desde cualquier origen. Antes de exponerlo fuera de un entorno local, configura autenticación, HTTPS, orígenes permitidos y credenciales propias; el usuario `root` sin contraseña solo se conserva como valor inicial para instalaciones locales existentes.
