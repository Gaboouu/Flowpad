# Flowpad
Organizador Android nativo en Kotlin y Jetpack Compose: proyectos, tablero Kanban, tareas y notas. Room para contenido local por cuenta; Firebase Authentication para acceso con correo y contraseña.

## Integrantes
- Gabriel Arnoldo Esquivel Rivera - ER241617
- Alvaro Ricardo Salazar Hernandez - SH191788

## Documentación
- [Manual de usuario](docs/Manual_Usuario.pdf)
- [Manual técnico](docs/Manual_Tecnico.pdf)
- [Mockups](docs/Mockups.pdf)
- [Configuración y pruebas](CONFIGURACION.md)

## Compilar
Android Studio, JDK 17 y SDK 35. Añade tu `app/google-services.json`, habilita Correo/contraseña en Firebase Authentication y sincroniza Gradle. Windows: `gradlew.bat :app:assembleDebug`. Linux/macOS: `./gradlew :app:assembleDebug`.

## Funciones
Registro, login, logout; contenido separado por cuenta; CRUD de proyectos, tarjetas, tareas y notas; búsqueda; validaciones; mensajes de error; confirmación de borrado; modo oscuro; persistencia local.
Los proyectos no se sincronizan en la nube. No se migran automáticamente los datos anónimos de versiones anteriores;


