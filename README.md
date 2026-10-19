# Evaluacion2 — Lou Araya

Aplicación Android nativa en Kotlin, Jetpack Compose y Material 3. La navegación usa Navigation Compose, la presentación sigue MVVM y los datos locales se conservan en SQLite mediante `AppDatabaseHelper` y repositorios existentes.

## Funcionalidades

- Inicio de sesión, registro y sesión local con perfiles de cliente y administrador.
- Catálogo, productos, favoritos, solicitudes, mapa y escáner QR.
- Gestión administrativa local de campañas/colecciones: crear, editar, activar/desactivar y definir fechas. Los clientes ven únicamente campañas activas.
- Perfil actual con edición de nombre, alias y teléfono; correo, contraseña y rol no se editan aquí.
- Preferencia por usuario para notificaciones, canal local Android y prueba manual. En Android 13+ la app solicita `POST_NOTIFICATIONS` únicamente cuando el usuario activa esa preferencia. No hay notificaciones push ni backend.

## Requisitos y ejecución

- Android Studio con SDK de compilación API 37 y JDK 11.
- `./gradlew :app:assembleDebug`
- `./gradlew :app:testDebugUnitTest`

La base de datos existente se mantiene en el dispositivo; no se añadió una migración ni se recrea la base de datos para estas funciones. Los usuarios deben iniciar sesión con la cuenta local existente. La cuenta administrativa inicial se define en `AppDatabaseHelper`.

## Notificaciones locales

El canal `Novedades y campañas` se registra de forma idempotente. El botón **Probar notificación local** publica una notificación inmediata solo si la preferencia está activa y el sistema lo permite. En Android 13 o superior, si se deniega el permiso, la preferencia queda desactivada y no se publica la notificación. La app no programa anuncios ni promete avisos de campañas en segundo plano; tampoco recibe push.

## Limitaciones

- Los datos y cambios son locales a esta instalación, sin sincronización entre dispositivos.
- Las fechas de campaña se validan y guardan en formato `AAAA-MM-DD`.
- El cambio de usuario cierra la sesión local actual y permite iniciar sesión con otra cuenta existente; no borra cuentas ni datos.
