# Procesos del lector

[Resumen de la aplicación](../README.md) · [Inicio de sesión y registro](acceso.md)

## Consultar y buscar libros

En **Catálogo y Reservas**, escribe un título, un nombre de autor o un género y
pulsa **Buscar** o Enter. La búsqueda no distingue mayúsculas de minúsculas.
**Mostrar Todos** limpia el criterio y vuelve a consultar el catálogo completo.

Cada fila muestra los datos del libro y su disponibilidad. Un número mayor que
cero indica ejemplares disponibles; **Agotado (0)** indica que no se puede reservar
un ejemplar en ese momento.

El género sirve para la clasificación temática. La categoría es una clasificación
adicional utilizada para el formato: **Impreso**, **Digital** o **Audiolibro**, por
ejemplo. Los libros que todavía no tienen categoría aparecen como **Sin categoría**.

## Reservar un libro

1. Selecciona un libro del catálogo que tenga ejemplares disponibles.
2. Elige una fecha de recogida igual o posterior a hoy.
3. Indica la duración del préstamo, entre uno y treinta días.
4. Pulsa **Reservar Libro Seleccionado**.

La aplicación registra la reserva, calcula su fecha límite y actualiza la
disponibilidad. Puedes consultar sus detalles en el historial y su confirmación
en **Notificaciones**.

Si la disponibilidad cambió desde que cargaste el catálogo, la reserva puede ser
rechazada. Vuelve a buscar el libro para consultar los datos actualizados.

## Consultar el historial

Abre **Mi Historial de Préstamos y Reservas**. La tabla muestra únicamente los
registros de tu cuenta, con su folio, libro, inicio, fecha límite, fecha de
devolución y estado.

| Columna | Significado |
| --- | --- |
| Fecha Inicio | Fecha y hora de recogida o inicio de la reserva. |
| Fecha límite | Momento previsto para devolver el libro. |
| Fecha de devolución | Momento registrado cuando se marcó el préstamo como devuelto. |

Un préstamo que aún no se ha devuelto muestra **—** en la fecha de devolución.
Un registro devuelto sin esa fecha almacenada muestra **No registrada**.
Las fechas de devolución incluidas en los préstamos iniciales son datos simulados.

El historial se carga al entrar al panel y vuelve a consultarse después de reservar
o cancelar. Para ver cambios realizados por otra sesión, vuelve a iniciar sesión.

## Cancelar una reserva

1. Selecciona la reserva en tu historial.
2. Comprueba que siga en estado **reservado** y que todavía no haya llegado la
   fecha y hora de inicio mostradas en la tabla.
3. Pulsa **Cancelar Reserva Seleccionada**.

El botón queda deshabilitado cuando el registro seleccionado no es elegible.
La base de datos vuelve a comprobar el propietario, el estado y la hora de
recogida al realizar la cancelación. Una reserva ya entregada, cancelada o cuya
recogida ya comenzó no puede cancelarse mediante esta opción.

Una cancelación correcta cambia el estado y devuelve un ejemplar a la disponibilidad.
El catálogo y el historial se actualizan después de la operación.

## Consultar notificaciones

En **Notificaciones** aparece una lista de los registros enviados de tu cuenta,
ordenados del más reciente al más antiguo. Selecciona un elemento para consultar
su asunto, fecha y mensaje en el panel de detalles.

Si todavía no hay registros, la pestaña indica **No tienes notificaciones**.
La lista y los detalles son de solo lectura. **Actualizar** vuelve a consultar
los registros y conserva la selección cuando esa notificación sigue disponible.
También se consultan nuevamente al abrir la pestaña.

Estas notificaciones se registran dentro de la plataforma. No requieren revisar
un buzón de correo externo: esta versión no realiza envíos reales por correo.
