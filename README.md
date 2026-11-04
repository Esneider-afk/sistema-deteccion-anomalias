# ControlGasto · Sistema de detección de anomalías en gastos empresariales

![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-6DB33F?logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-build-02303A?logo=gradle&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-views-005F0F?logo=thymeleaf&logoColor=white)

Aplicación web que registra los gastos de una empresa y **detecta automáticamente comportamientos inusuales** mediante análisis estadístico de los datos. Permite clasificar, importar, filtrar y comparar gastos, visualizar su comportamiento en gráficos y gestionar el ciclo de revisión de cada anomalía.

> Proyecto de grado · Tecnología en Desarrollo de Sistemas Informáticos · Universidad de Tecnologías y Saberes (UTS)

---

## Tabla de contenido

1. [Descripción del problema](#descripción-del-problema)
2. [Objetivos](#objetivos)
3. [Funcionalidades](#funcionalidades)
4. [Cómo detecta las anomalías](#cómo-detecta-las-anomalías)
5. [Tecnologías](#tecnologías)
6. [Arquitectura](#arquitectura)
7. [Modelo de datos](#modelo-de-datos)
8. [Estructura del proyecto](#estructura-del-proyecto)
9. [Instalación y ejecución](#instalación-y-ejecución)
10. [Importación de gastos](#importación-de-gastos)
11. [Rutas principales](#rutas-principales)
12. [Seguridad](#seguridad)
13. [Pruebas funcionales sugeridas](#pruebas-funcionales-sugeridas)
14. [Solución de problemas](#solución-de-problemas)
15. [Limitaciones y trabajo futuro](#limitaciones-y-trabajo-futuro)
16. [Autor](#autor)

---

## Descripción del problema

En muchas empresas el control de gastos se hace de forma manual o en hojas de cálculo. Esto dificulta detectar a tiempo pagos duplicados, montos muy superiores a lo habitual o proveedores que cobran por encima de su historial. Los errores y posibles irregularidades se descubren tarde, o nunca.

**ControlGasto** centraliza el registro de gastos y aplica criterios estadísticos para señalar automáticamente los registros que merecen una revisión, dejando constancia de quién los revisó y por qué se justificaron.

## Objetivos

**Objetivo general:** desarrollar una aplicación web que permita detectar anomalías en los gastos empresariales mediante análisis de datos.

**Objetivos específicos:**

- Registrar, consultar, modificar y eliminar gastos clasificados por categoría, departamento y proveedor.
- Importar gastos de forma masiva desde archivos Excel y CSV.
- Calcular totales, promedios, porcentajes y variaciones entre periodos.
- Identificar gastos y comportamientos inusuales aplicando criterios estadísticos documentados.
- Gestionar el estado de cada anomalía: pendiente por revisión, justificada o revisada.
- Representar el comportamiento del gasto mediante gráficos y comparaciones.

## Funcionalidades

| Módulo | Descripción |
|---|---|
| **Autenticación** | Registro de usuarios e inicio de sesión. Contraseñas cifradas con BCrypt. |
| **Gastos (CRUD)** | Registrar, consultar, modificar y eliminar gastos con validación de datos. |
| **Clasificación** | Cada gasto se asocia a una categoría, un departamento y un proveedor. |
| **Catálogos** | Administración de categorías, departamentos y proveedores. |
| **Importación** | Carga masiva desde `.xlsx`, `.xls` y `.csv`, con reporte de filas con error. |
| **Filtros** | Por rango de fechas, categoría, proveedor y departamento. |
| **Indicadores** | Total gastado, promedio, cantidad de registros, mayor gasto individual y porcentaje por categoría. |
| **Variaciones** | Variación porcentual mes a mes. |
| **Gráficos** | Evolución mensual, distribución por categoría, gasto por proveedor y por departamento. |
| **Comparación** | Compara dos periodos por categoría, proveedor o departamento, con diferencia y variación. |
| **Alertas** | Detección automática de anomalías al guardar o importar gastos, y análisis manual bajo demanda. |
| **Detalle de anomalía** | Explicación de por qué se marcó y criterios utilizados. |
| **Gestión de anomalías** | Estados: *Pendiente por revisión*, *Justificada* y *Revisada*, con justificación obligatoria cuando se justifica. |
| **Interfaz** | Diseño adaptable a móvil y modo claro/oscuro. |

## Cómo detecta las anomalías

El motor de detección (`DetectorAnomaliasService`) aplica **tres criterios** a cada gasto. Un gasto se marca como anomalía si cumple al menos uno.

### 1. Z-SCORE · monto atípico dentro de su categoría

Se compara el monto del gasto con el promedio y la desviación estándar de **los demás gastos de la misma categoría** (se excluye el propio gasto para que un valor extremo no distorsione la referencia).

```
z = (monto − media_otros) / desviación_estándar_otros
```

- Se genera alerta si **z ≥ 2.5**.
- Se requieren al menos **5 gastos de referencia** en la categoría.

### 2. PROVEEDOR · desvío frente al historial del proveedor

Se genera alerta si el monto es **mayor a 3 veces** el promedio histórico de ese proveedor. Se requieren al menos **3 gastos previos** del proveedor.

### 3. DUPLICADO · posible doble registro

Se genera alerta si existe otro gasto con el **mismo proveedor, la misma fecha y el mismo monto**.

### Severidad

| Severidad | Condición |
|---|---|
| **ALTA** | Se cumplen dos o más criterios, o z ≥ 3.5 |
| **MEDIA** | Se cumple un solo criterio |

Los umbrales están definidos como constantes públicas en `DetectorAnomaliasService` y se muestran a la persona usuaria en la pantalla de anomalías.

### Ciclo de vida de una anomalía

```
Detectada ──► Pendiente por revisión ──► Justificada   (requiere texto de justificación)
                                    └──► Revisada
```

## Tecnologías

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot (Web MVC, Data JPA, Validation) |
| Seguridad | Spring Security |
| Vistas | Thymeleaf + Thymeleaf Extras Spring Security |
| Base de datos | MySQL 8 |
| Build | Gradle |
| Importación | Apache POI 5.2.5 (Excel) · Apache Commons CSV 1.11.0 |
| Utilidades | Lombok |
| Frontend | Bootstrap 5.3, Bootstrap Icons, Chart.js 4.4, tipografía Inter |

## Arquitectura

Aplicación monolítica en capas, con renderizado del lado del servidor:

```
Navegador
   │  HTTP
   ▼
┌─────────────────────────────────────────────┐
│ Controladores (Spring MVC)                  │  Reciben la petición y preparan el modelo
├─────────────────────────────────────────────┤
│ Servicios                                   │  Lógica de negocio y análisis estadístico
│  · GastoService        · EstadisticaService │
│  · ImportService       · UsuarioService     │
│  · DetectorAnomaliasService                 │
├─────────────────────────────────────────────┤
│ Repositorios (Spring Data JPA)              │  Acceso a datos
├─────────────────────────────────────────────┤
│ MySQL                                       │
└─────────────────────────────────────────────┘
        Vistas Thymeleaf + Chart.js ◄── Modelo
```

## Modelo de datos

```
Usuario
 ├─ id, nombre, email (único), password (BCrypt)

Categoria ─┐
Departamento ─┼──< Gasto >── 1:1 ── Anomalia
Proveedor ─┘      │
                  └─ id, fecha, descripcion, monto

Anomalia
 ├─ id, criterios, detalle, severidad (ALTA | MEDIA)
 ├─ estado (PENDIENTE_REVISION | JUSTIFICADA | REVISADA)
 └─ fechaDeteccion, justificacion
```

| Entidad | Atributos principales |
|---|---|
| `Usuario` | nombre, email, password |
| `Categoria` | nombre (único) |
| `Departamento` | nombre (único) |
| `Proveedor` | nombre (único), NIT |
| `Gasto` | fecha, descripción, monto, categoría, departamento, proveedor |
| `Anomalia` | gasto, criterios, detalle, severidad, estado, fecha de detección, justificación |

Al eliminar un gasto se elimina también su anomalía asociada.

## Estructura del proyecto

```
anomalias/
├── build.gradle
├── settings.gradle
└── src/main/
    ├── java/com/empresa/anomalias/
    │   ├── AnomaliasApplication.java
    │   ├── config/        SecurityConfig · CatalogosAdvice · DataInitializer
    │   ├── controller/    Auth · Dashboard · Gasto · Catalogo · Import · Anomalia
    │   ├── dto/           GastoForm · FilaResumen · VariacionPeriodo · FilaComparacion · ImportResultado
    │   ├── model/         Usuario · Categoria · Departamento · Proveedor · Gasto · Anomalia · EstadoAnomalia
    │   ├── repository/    Repositorios JPA
    │   └── service/       Gasto · Estadistica · Import · Usuario · DetectorAnomalias
    └── resources/
        ├── application.properties
        ├── static/
        │   ├── css/estilos.css
        │   └── js/app.js
        └── templates/
            ├── fragments/layout.html
            ├── gastos/        lista · form
            ├── anomalias/     lista · detalle
            └── login · registro · dashboard · importar · comparar · catalogos
```

## Instalación y ejecución

### Requisitos

- JDK 17 o superior
- MySQL 8 en ejecución
- Git

No necesitas instalar Gradle: el proyecto incluye el *wrapper* (`gradlew`).

### Pasos

**1. Clonar el repositorio**

```bash
git clone https://github.com/Esneider-afk/sistema-deteccion-anomalias.git
cd sistema-deteccion-anomalias
```

**2. Configurar la base de datos**

Edita `src/main/resources/application.properties` con el usuario y la contraseña de tu MySQL:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/anomalias_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota
spring.datasource.username=root
spring.datasource.password=TU_CLAVE
```

La base de datos `anomalias_db` y sus tablas se crean automáticamente en el primer arranque.

> No subas tu contraseña real al repositorio.

**3. Ejecutar**

```bash
# Windows
gradlew.bat bootRun

# Linux / macOS
./gradlew bootRun
```

**4. Abrir la aplicación**

```
http://localhost:9090
```

(El puerto es el definido en `server.port` de `application.properties`.)

### Datos de demostración

En el primer arranque, la clase `DataInitializer` carga:

- Un usuario administrador: **`admin@empresa.com`** / **`Admin123*`**
- Categorías, departamentos y proveedores de ejemplo.
- Unos 150 gastos generados con valores realistas.
- Tres anomalías sembradas a propósito: un monto atípico en papelería, otro en tecnología y un gasto duplicado.

Estos datos solo se crean si la base de datos está vacía. Para uso real, elimina o desactiva `DataInitializer`.

### Configuración para Eclipse / Spring Tools

Si ejecutas desde Eclipse y obtienes un error como *"parameter name information not available via reflection"*, activa en **Window → Preferences → Java → Compiler** la opción **Store information about method parameters (usable via reflection)** y haz **Project → Clean**. Añadir este bloque al `build.gradle` refuerza la configuración para las compilaciones con Gradle:

```groovy
tasks.withType(JavaCompile).configureEach {
    options.compilerArgs << '-parameters'
}
```

## Importación de gastos

Se aceptan archivos `.xlsx`, `.xls` y `.csv` de hasta 10 MB. La **primera fila** debe contener los encabezados, en este orden:

| fecha | descripcion | monto | categoria | departamento | proveedor |
|---|---|---|---|---|---|
| 2026-10-15 | Pasajes comerciales | 350000 | Viáticos | Ventas | Distribuidora Andina |
| 16/10/2026 | Resmas de papel | 85000.50 | Papelería | Administrativo | Papelería Central |

Ejemplo de CSV:

```csv
fecha,descripcion,monto,categoria,departamento,proveedor
2026-10-02,Pasajes comerciales,350000,Viáticos,Ventas,Distribuidora Andina
2026-10-05,Resmas de papel,85000,Papelería,Administrativo,Papelería Central
```

**Reglas:**

- **Fecha:** `yyyy-MM-dd` o `dd/MM/yyyy`, o una celda de tipo fecha en Excel.
- **Monto:** número positivo sin separador de miles (`1500000` o `1500000.50`).
- **CSV:** separador coma o punto y coma, codificación UTF-8.
- Si la categoría, el departamento o el proveedor no existen, se crean automáticamente.
- Las filas inválidas **no se importan** y se listan con su número de fila y el motivo del error. Las filas válidas sí se importan.
- Al terminar la importación se ejecuta el análisis de anomalías.

## Rutas principales

| Ruta | Método | Descripción |
|---|---|---|
| `/login` | GET, POST | Inicio de sesión |
| `/registro` | GET, POST | Registro de usuarios |
| `/dashboard` | GET | Indicadores, gráficos y tablas (admite filtros) |
| `/gastos` | GET | Listado de gastos con filtros |
| `/gastos/nuevo` | GET | Formulario de nuevo gasto |
| `/gastos/{id}/editar` | GET | Formulario de edición |
| `/gastos/guardar` | POST | Crear o actualizar un gasto |
| `/gastos/{id}/eliminar` | POST | Eliminar un gasto |
| `/importar` | GET, POST | Importación desde Excel o CSV |
| `/comparar` | GET | Comparación entre periodos |
| `/anomalias` | GET | Lista de anomalías (filtros por estado y severidad) |
| `/anomalias/{id}` | GET | Detalle de una anomalía |
| `/anomalias/{id}/estado` | POST | Cambiar estado y justificación |
| `/anomalias/analizar` | POST | Ejecutar el análisis manualmente |
| `/catalogos` | GET | Categorías, departamentos y proveedores |

## Seguridad

- Todas las rutas requieren autenticación, excepto `/login`, `/registro` y los recursos estáticos.
- Contraseñas almacenadas con **BCrypt**.
- Protección **CSRF** activa en todos los formularios `POST`.
- Validación de datos de entrada en servidor (Jakarta Validation).
- Las contraseñas y credenciales de base de datos no deben incluirse en el repositorio.

## Pruebas funcionales sugeridas

| # | Prueba | Resultado esperado |
|---|---|---|
| 1 | Registrar un usuario e iniciar sesión | Acceso al dashboard |
| 2 | Crear un gasto de Papelería por $5.000.000 | Se genera una alerta de anomalía |
| 3 | Registrar el mismo gasto dos veces (mismo proveedor, fecha y monto) | Se marca como DUPLICADO |
| 4 | Filtrar el dashboard por fecha, categoría y proveedor | Indicadores y gráficos se actualizan |
| 5 | Importar un CSV con una fila de monto inválido | Se importan las filas válidas y se reporta el error |
| 6 | Marcar una anomalía como *Justificada* sin texto | El sistema exige la justificación |
| 7 | Cambiar una anomalía a *Revisada* | El estado se refleja en la lista |
| 8 | Comparar dos meses por proveedor | Tabla y gráfico con diferencia y variación |
| 9 | Eliminar un gasto con anomalía | Se elimina también la anomalía |

## Solución de problemas

| Síntoma | Causa probable | Solución |
|---|---|---|
| `Communications link failure` | MySQL apagado | Inicia MySQL y reinicia la aplicación |
| `Access denied for user` | Credenciales incorrectas | Corrige usuario y contraseña en `application.properties` |
| `Port ... already in use` | Puerto ocupado | Cambia `server.port` |
| Página sin estilos | No carga `/css/estilos.css` | Verifica la ruta del archivo y haz recarga forzada (Ctrl+F5) |
| Error `parameter name information not available` | Eclipse sin la opción `-parameters` | Ver [Configuración para Eclipse](#configuración-para-eclipse--spring-tools) |
| Error de fragmento `topbar` no resuelto | `layout.html` desactualizado o caché de `bin/` | Reemplaza `layout.html`, borra `bin/` y haz *Clean* |

## Limitaciones y trabajo futuro

**Limitaciones actuales**

- El análisis recorre los gastos en memoria; para volúmenes muy grandes convendría pasarlo a consultas SQL.
- Un único rol de usuario: no hay distinción entre administrador y auditor.
- Los criterios usan umbrales fijos, no configurables desde la interfaz.

**Mejoras previstas**

- Roles de administrador y auditor con permisos diferenciados.
- Umbrales de detección configurables desde la interfaz.
- Criterios adicionales: rango intercuartílico (IQR), mediana absoluta de desviaciones (MAD) y detección de estacionalidad.
- Exportación de reportes a PDF y Excel.
- Notificaciones por correo cuando se detecte una anomalía de severidad alta.
- Registro de auditoría con el historial de cambios de estado.
- Pruebas automatizadas del motor de detección.
- Despliegue con Docker.

## Autor

**Sneider**
Tecnología en Desarrollo de Sistemas Informáticos · Universidad de Tecnologías y Saberes (UTS)
Floridablanca, Santander, Colombia
GitHub: [@Esneider-afk](https://github.com/Esneider-afk)
