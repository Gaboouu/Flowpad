# Flowpad: configuración y entrega

## 1. Abrir el proyecto
Descomprime Flowpad.zip y abre la carpeta Flowpad (la que contiene settings.gradle.kts) en Android Studio. Usa JDK 17, instala Android SDK 35 y permite la descarga de las dependencias. El proyecto incluye Gradle Wrapper 8.9. En Windows puedes compilar con `gradlew.bat :app:assembleDebug`.

## 2. Configurar Firebase (obligatorio para el acceso)
1. En https://console.firebase.google.com crea o selecciona TU proyecto.
2. Registra una aplicación Android con el identificador exacto `com.udb.flowpad`.
3. Descarga google-services.json y colócalo en `Flowpad/app/google-services.json`.
4. Abre Authentication, habilita el proveedor Correo electrónico/contraseña y guarda.
5. Sincroniza Gradle y vuelve a instalar/ejecutar la app.
6. Crea una cuenta desde la app; no uses una contraseña personal para la demostración.
No necesitas Firestore ni Realtime Database: la persistencia del contenido utiliza Room. No compartas claves de servicio o credenciales administrativas. El JSON de configuración del cliente se ignora en Git; cada evaluador debe configurarlo para compilar con autenticación.

Si no agregas el JSON, la app muestra instrucciones de configuración; no permite saltarse el login. Si activas políticas adicionales de contraseña, prueba que el registro cumpla esas políticas.

## 3. Datos existentes
La nueva versión utiliza una base local separada por UID de Firebase. No importa automáticamente los datos de la versión anterior (flowpad.db), porque esa versión no identificaba a su propietario. La base anterior no se borra por esta actualización. Si tienes datos importantes, conserva la instalación anterior y solicita una migración antes de desinstalar. Cada cuenta nueva comienza con un proyecto de ejemplo. No hay sincronización de proyectos entre dispositivos. Desinstalar o borrar los datos del sistema elimina el contenido local.

## 4. Prueba obligatoria en tu equipo
- Compilar e instalar en un dispositivo Android 8 o superior.
- Registrar cuenta; probar correo inválido, campos vacíos, contraseña corta y confirmación distinta.
- Intentar registrar un correo existente. Probar contraseña incorrecta e inicio sin internet.
- Iniciar sesión correctamente, cerrar sesión y comprobar que no se puede volver al contenido con Atrás.
- Crear dos cuentas y verificar que cada una ve únicamente sus propios proyectos.
- Crear, editar, buscar y eliminar un proyecto. La app conserva al menos un proyecto.
- Crear, renombrar y mover tarjetas por los tres estados; cancelar y confirmar su eliminación.
- Crear tarea, tocar el texto para editar, completar y eliminar.
- Crear notas y encabezados, editar texto, subir/bajar y eliminar.
- Reiniciar la app y confirmar persistencia. Tras cerrar sesión, entrar en la misma cuenta y confirmar sus datos.
- Probar modo oscuro, rotación y textos largos. Las cuentas requieren internet para registrarse/iniciar sesión; el contenido local funciona sin internet en una sesión conservada.

## 5. Completar la entrega
Los manuales PDF incluidos describen la implementación y contienen mockups ilustrativos, NO capturas reales. Antes de entregar, sustituye/complementa las ilustraciones con capturas de TU ejecución, especialmente registro/login y CRUD. Las fuentes editables de los manuales están en docs/*.md.
Completa integrantes y carnets en README.md. Confirma el equipo actual; no se han supuesto integrantes de otros proyectos.
Crea un repositorio público en GitHub, sube código y docs, comprueba enlaces sin iniciar sesión. Cada integrante debe aportar commits reales desde su cuenta; no se han inventado contribuciones. Comparte el enlace en aula digital. El PDF de la asignatura indica el 26 de octubre hasta las 11:59 p. m.

## Estado de verificación
Código revisado estáticamente. Gradle Wrapper incluido. Intento de compilación detenido por falta de conexión del entorno a services.gradle.org; no hay APK validado ni prueba real de Firebase. Ejecuta la lista anterior antes de declarar la app 100% funcional.

Referencia oficial: https://firebase.google.com/docs/auth/android/password-auth
