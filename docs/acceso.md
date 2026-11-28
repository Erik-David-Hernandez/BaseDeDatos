# Acceso: inicio de sesión y registro

[Resumen de la aplicación](../README.md) · [Instalación y configuración](instalacion.md)

## Al abrir la aplicación

La aplicación comprueba la conexión a PostgreSQL y los elementos del esquema que
necesita. Si requiere configurar el servidor o actualizar el esquema, abre una
terminal independiente. Completa allí los permisos y las preguntas de instalación;
la contraseña de sudo se introduce en esa terminal, no en la pantalla de acceso.

Cuando finaliza la configuración, la aplicación vuelve a comprobar la conexión.
La terminal conserva el resultado hasta pulsar Enter. Si ocurre un error, revisa
su salida y utiliza **Reintentar** después de corregirlo, o **Cerrar** para salir.
Las herramientas requeridas por cada sistema se describen en la
[guía de instalación](instalacion.md).

## Iniciar sesión

1. Introduce el correo electrónico de tu cuenta.
2. Introduce la contraseña respetando sus mayúsculas, espacios y demás caracteres.
3. Pulsa **Iniciar Sesión**.

Una cuenta activa con credenciales correctas accede al panel correspondiente:
los lectores ven su catálogo e historial; administradores y bibliotecarios
acceden al panel administrativo.

Si el sistema indica que las credenciales son incorrectas, comprueba ambos campos.
Si la cuenta está desactivada, un administrador debe activarla para permitir el
acceso.

## Crear una cuenta de lector

1. Desde la pantalla de acceso, pulsa **Crear Cuenta**.
2. Completa nombre, correo, contraseña y confirmación de contraseña.
3. Marca la aceptación del aviso de privacidad y protección de datos.
4. Pulsa **Registrarme**.

Los asteriscos rojos indican los datos obligatorios, incluida la aceptación del
aviso. Puedes pulsar la casilla o el texto del aviso para marcarlo.
**Registrarme** permanece deshabilitado si faltan campos, no se acepta el aviso
o las contraseñas no coinciden. La diferencia entre contraseñas se muestra junto
al campo de confirmación y se comprueba mientras escribes en cualquiera de ellos.

Al enviar el formulario también se comprueba el correo, la longitud mínima de
seis caracteres de la contraseña y que el correo no esté registrado. Una cuenta
nueva recibe el rol de lector. Después del registro, vuelve a iniciar sesión;
la confirmación puede consultarse en la pestaña **Notificaciones**.

La flecha **‹**, en la esquina superior izquierda de la tarjeta, regresa a la
pantalla de inicio de sesión.

## Cuentas de demostración

Cuando se cargan los datos iniciales, estas cuentas usan la contraseña `123456`:

| Rol | Correo |
| --- | --- |
| Administrador | `carlos.gomez@email.com` |
| Bibliotecario | `elena.morales@email.com` |
| Lector | `juan.perez@email.com` |

Diego Torres y Roberto Ramos están desactivados en los datos iniciales. Las fechas
y los préstamos de estas cuentas son ejemplos; algunos ya tienen fecha de
recogida pasada.

La contraseña de una cuenta de lector es independiente de las credenciales que
la aplicación utiliza para conectarse a PostgreSQL. Estas últimas se configuran
según la [guía de instalación](instalacion.md#credenciales-de-postgresql).

## Cerrar sesión

Pulsa **Cerrar Sesión** en tu panel. La aplicación limpia la sesión y vuelve a la
pantalla de acceso.
