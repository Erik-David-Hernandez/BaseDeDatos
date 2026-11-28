# Instalación, configuración y ejecución

[Resumen de la aplicación](../README.md) · [Acceso a la plataforma](acceso.md)

## Requisitos

Para compilar y ejecutar el proyecto durante el desarrollo, utiliza Maven y un
JDK 11 o posterior. La creación de paquetes nativos requiere un JDK 17 o posterior
que incluya `jpackage` y `jlink`. Make es opcional y ofrece los atajos del repositorio.

PostgreSQL debe ejecutarse localmente en el puerto `5432`. La aplicación utiliza
la base `Biblioteca_virtual`. El procedimiento de configuración puede instalar
el servidor, iniciarlo, crear la base e importar los datos iniciales cuando falta.

| Sistema | Herramientas de configuración |
| --- | --- |
| Debian/Ubuntu, incluido WSL | apt, herramientas de clúster de PostgreSQL y sudo. |
| Arch/CachyOS | pacman, systemd y sudo. |
| macOS | Homebrew instalado previamente; ejecutar como usuario normal. |
| Windows | Git para Windows, su terminal Git Bash y winget. El instalador de PostgreSQL es interactivo. |

Otros sistemas quedan fuera de la instalación automática del script. En Linux,
la ventana de configuración utiliza una terminal compatible: Konsole, GNOME
Terminal, XFCE Terminal, MATE Terminal, xterm, Kitty, Alacritty o
x-terminal-emulator. macOS utiliza Terminal y puede solicitar permiso para
controlarlo; Windows utiliza la terminal mintty incluida en Git Bash.

## Ejecutar durante el desarrollo

Desde la raíz del repositorio:

```bash
make run
```

Sin Make, utiliza Maven directamente desde la misma ubicación:

```bash
mvn -f Biblioteca/biblioteca/pom.xml javafx:run
```

## Compilar y ejecutar el JAR

```bash
make build
java -jar Biblioteca/biblioteca/target/biblioteca.jar
```

La compilación genera el JAR, sus dependencias y los archivos de configuración
junto a él:

```text
target/
├── biblioteca.jar
├── lib/
└── database/
    ├── setup-postgresql.sh
    ├── biblioteca_virtual.sql
    └── migrations/
```

Para distribuir el JAR, conserva `lib/` y `database/` junto a `biblioteca.jar`.
El equipo donde se ejecute necesita Java. La aplicación puede localizar el script
junto al JAR aunque el directorio de trabajo sea diferente.

## Crear un paquete nativo

```bash
make package
```

El resultado aparece dentro de `Biblioteca/biblioteca/target/dist/`. Distribuye la
carpeta completa de la aplicación, que incluye su entorno Java y sus archivos.

| Sistema | Lanzador dentro de `target/dist/` |
| --- | --- |
| Linux | `Biblioteca/bin/Biblioteca` |
| Windows | `Biblioteca/Biblioteca.exe` |
| macOS | `Biblioteca.app`, o `Biblioteca.app/Contents/MacOS/Biblioteca`. |

Para crear un instalador, elige un formato válido para el sistema donde compilas:

```bash
make package TYPE=deb
make package TYPE=rpm
make package TYPE=exe
make package TYPE=dmg
```

jpackage no crea paquetes para otros sistemas operativos desde un mismo equipo.
Compila en cada sistema de destino para incluir sus bibliotecas nativas de JavaFX.
Los instaladores pueden requerir herramientas adicionales indicadas por jpackage,
como WiX en Windows según el JDK o rpmbuild para RPM.

Sin Make, el comando equivalente para una imagen de aplicación es:

```bash
mvn -f Biblioteca/biblioteca/pom.xml -Pnative clean verify
```

Para un instalador, añade `-Djpackage.type=deb`, `rpm`, `exe` o `dmg`.
`make clean` elimina los archivos de compilación de `target/`; no elimina la base
de datos.

## Preparar o actualizar PostgreSQL

```bash
make setup
```

El equivalente sin Make es:

```bash
bash database/setup-postgresql.sh --app
```

El script crea `Biblioteca_virtual` y carga el SQL inicial únicamente si la base
no existe. Sobre una base existente aplica las migraciones incluidas y configura
el acceso de la cuenta de la aplicación; no vuelve a insertar los datos iniciales.
Las migraciones se incluyen tanto en la distribución del JAR como en los paquetes
nativos.

El arranque también ofrece configuración si no puede conectarse o detecta que
falta el esquema de categorías o su acceso. Para aplicar migraciones nuevas a una
base que ya permite conectarse, ejecuta `make setup` explícitamente. Completa las
preguntas y la autenticación del sistema en la terminal de configuración.

## Credenciales de PostgreSQL

Estas variables afectan a la conexión y configuración de la base, no a la
contraseña que los lectores introducen en el formulario de inicio de sesión:

| Variable | Valor predeterminado | Uso |
| --- | --- | --- |
| `BIBLIOTECA_DB_USER` | `biblioteca` | Cuenta de PostgreSQL utilizada por la aplicación. |
| `BIBLIOTECA_DB_PASSWORD` | `admin` | Contraseña de esa cuenta de PostgreSQL. |
| `POSTGRES_ADMIN_PASSWORD` | `admin` | Contraseña administrativa usada al configurar desde la app cuando se necesita, por ejemplo en Windows. |
| `BIBLIOTECA_SETUP_SCRIPT` | Localización automática | Ruta absoluta opcional al script `database/setup-postgresql.sh`. |

Configura las variables antes de iniciar la aplicación. Los valores
predeterminados corresponden al entorno de demostración. El script crea una
cuenta de aplicación si falta, pero conserva las contraseñas de cuentas existentes;
utiliza las credenciales actuales de tu instalación.

En Linux, sudo solicita la contraseña del usuario del sistema. En Windows, la
contraseña administrativa de PostgreSQL es la elegida durante su instalación.
La ejecución manual del script utiliza `PGPASSWORD` o su pregunta de contraseña
en Windows para la autenticación administrativa.

## Datos de demostración y migraciones

El [SQL inicial](../database/biblioteca_virtual.sql) incluye quince libros y
quince cuentas. Los formatos impreso, digital y audiolibro se asignan al azar una
vez y se conservan fijos en el archivo para que las inicializaciones sean
reproducibles. Las fechas de devolución de los préstamos iniciales son simuladas.

- [001-categorias.sql](../database/migrations/001-categorias.sql) agrega la tabla
  de categorías y la relación opcional con libros, conservando los registros.

Los cambios del SQL inicial se aplican al crear una base nueva. En una base
existente, `make setup` conserva los registros y ejecuta las migraciones
disponibles; no vuelve a cargar ni a reemplazar los préstamos de demostración.

Las cuentas de ejemplo se describen en la [guía de acceso](acceso.md#cuentas-de-demostración).
