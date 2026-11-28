# Procesos del administrador y bibliotecario

[Resumen de la aplicación](../README.md) · [Inicio de sesión](acceso.md)

## Permisos y panel

Los dos roles acceden al panel administrativo. Pueden gestionar libros, autores,
géneros, categorías y préstamos, y consultar usuarios y auditoría. La modificación
de roles y la activación de usuarios están reservadas al administrador; esos
controles aparecen deshabilitados para el bibliotecario.

**Recargar datos** vuelve a consultar las tablas y los desplegables desde PostgreSQL.
No guarda las modificaciones que hayas escrito en un formulario. Los filtros de
préstamos permanecen aplicados después de recargar.

## Agregar un libro

En **Libros, Géneros y Categorías**:

1. Completa título, ISBN y sección.
2. Busca y selecciona el autor, y elige el género.
3. Elige una categoría de formato o **Sin categoría**.
4. Indica el número de ejemplares disponibles.
5. Pulsa **Guardar Libro**.

El identificador del libro se genera automáticamente y el ISBN debe ser único.

La búsqueda de autores muestra resultados al escribir y no distingue mayúsculas
ni acentos. Elige un resultado con el ratón o con las flechas y Enter: escribir
texto sin seleccionar un resultado no asigna un autor. Si hay muchos resultados,
afina el texto de búsqueda.

## Editar un libro

Haz doble clic sobre una fila de la tabla de libros. Se abre una ventana modal con
los datos actuales del registro. Puedes modificar título, ISBN, sección, autor,
género, categoría y cantidad de ejemplares disponibles.

Pulsa **Guardar cambios** para actualizar el registro o **Cancelar** para cerrar
sin guardar. La búsqueda de autores funciona igual que en el formulario de alta.

La cantidad indicada es la disponibilidad actual, no un total que incluya copias
prestadas. Si otra operación modifica el libro o su inventario mientras tienes
abierta la ventana, el sistema rechaza el cambio para no sobrescribir esos datos.
Cierra el modal, recarga y vuelve a abrir el registro.

## Agregar autores, géneros y categorías

- **Autor:** introduce nombres y apellido paterno; el apellido materno es opcional.
  Pulsa **Agregar** en el formulario del autor.
- **Género:** escribe el nombre de la clasificación temática y pulsa **Agregar**
  en su formulario. Puede representar temas como historia, artes o ciencia.
- **Categoría:** escribe el nombre y pulsa **Agregar categoría**. Se usa como
  clasificación adicional, por ejemplo impreso, digital o audiolibro.

Cada libro tiene un género y puede tener una categoría. Una misma categoría puede
compartirse entre muchos libros. Los nuevos registros quedan disponibles en los
formularios de alta y edición de libros.

## Buscar y filtrar préstamos

En **Préstamos y Reservas**, el campo de búsqueda encuentra registros por nombre
del lector, correo o título del libro. La búsqueda no distingue mayúsculas.

Puedes combinarla con:

- **Estados:** marca uno o varios. Todos están seleccionados inicialmente; si
  desmarcas todos, no se muestran registros.
- **Inicio desde / hasta:** filtra la fecha de inicio del préstamo, incluyendo
  ambos días. Puedes dejar cualquiera de los límites vacío.

Si el límite inicial es posterior al final, aparece un aviso y no se muestran
resultados. **Limpiar filtros** borra la búsqueda, limpia las fechas y vuelve a
seleccionar todos los estados.

Para consultar préstamos activos, conserva los estados **Reservado**, **Prestado**
y **Vencido**, y desmarca **Devuelto** y **Cancelado**.

## Entregar, devolver o cancelar un préstamo

Selecciona una fila. Los botones aparecen únicamente cuando se pueden aplicar
al estado de ese registro:

| Estado seleccionado | Acciones disponibles |
| --- | --- |
| Reservado | Cancelar préstamo; marcar como prestado (entregado). |
| Prestado | Cancelar préstamo; marcar como devuelto. |
| Vencido | Cancelar préstamo; marcar como devuelto. |
| Devuelto o cancelado | Ninguna. |
| Sin selección | Ninguna. |

**Marcar como Prestado (Entregado)** registra la entrega. **Marcar como Devuelto**
registra la fecha de devolución y devuelve un ejemplar al inventario disponible.
**Cancelar préstamo** solicita confirmación y también devuelve un ejemplar.

Las operaciones actualizan las tablas y generan una notificación interna para el
lector. Si el préstamo cambió desde que lo seleccionaste, recarga los datos antes
de volver a operar. El sistema impide aplicar dos veces la misma devolución o
cancelación.

## Gestionar usuarios: solo administrador

1. Abre **Gestión de Usuarios** y selecciona una cuenta.
2. Elige su rol y marca o desmarca **Usuario Activo**.
3. Pulsa **Guardar Cambios**.

Una cuenta desactivada no puede iniciar sesión. Al modificar un rol, el panel
correspondiente se selecciona en el siguiente inicio de sesión. Si tus permisos
fueron revocados durante la sesión, el sistema rechaza la modificación de usuarios;
vuelve a iniciar sesión para consultar tus permisos actuales.

## Consultar auditoría

**Auditoría del Sistema** muestra las últimas cien operaciones registradas, con
usuario, acción, tabla, detalle y fecha. Usa **Recargar datos** para consultar las
operaciones más recientes. Esta vista es de consulta.
