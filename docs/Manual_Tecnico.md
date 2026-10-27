# Manual técnico

## Arquitectura y alcance

Aplicación Android nativa con Kotlin 2.0.21, Jetpack Compose y Material 3. Usa Android Gradle Plugin 8.7.3, Gradle Wrapper 8.9, JDK 17 y compileSdk/targetSdk 35; minSdk 26.

Capas: data define entidades, DAO y RoomDatabase; ui contiene pantallas y ViewModel; ui/theme centraliza estilos y colores del tema. MainActivity presenta FlowpadApp y AuthGate decide si mostrar acceso o contenido.

Flujo: interacción Compose → método de FlowpadViewModel → DAO suspendido → Room/SQLite → Flow → StateFlow → interfaz. Coroutines ejecutan operaciones; collectAsStateWithLifecycle observa el estado.

Firebase Authentication se limita a las cuentas. Room persiste el contenido local. Esta opción satisface la alternativa de base local de la guía y permite trabajar con contenido sin conexión cuando existe una sesión conservada.

## Modelo y persistencia

projects: id, name, description, emoji, coverIndex. cards: id, projectId, title, status, position. tasks: id, projectId, text, completed. notes: id, projectId, type, content, position. settings: name, value.

projects tiene una relación uno a muchos con cards, tasks y notes. Las claves foráneas usan CASCADE: eliminar un proyecto elimina sus registros dependientes. projectId está indexado.

Cada UID autenticado selecciona una base flowpad_<hash SHA-256 del UID>.db. El hash sirve para nombrar el archivo; no cifra la base. Los datos dependen del almacenamiento privado de Android y el respaldo automático está desactivado.

FlowpadDao registra, consulta, actualiza y elimina. Las tareas permiten editar texto y estado. Las notas intercambian posiciones mediante una transacción. La búsqueda se realiza en memoria sobre las listas observadas.

La base anterior flowpad.db se conserva pero no se importa para evitar asignar datos sin propietario a una cuenta arbitraria. Una migración requeriría confirmar el propietario. La app crea datos de ejemplo una vez por base.

## Autenticación y configuración

AuthGate inicializa Firebase y observa AuthStateListener. Si no existe configuración, muestra instrucciones. LoginScreen valida campos, correo, longitud mínima y confirmación antes de invocar el servicio.

createUserWithEmailAndPassword registra; signInWithEmailAndPassword autentica; signOut cierra sesión. Se representan errores de red, cuenta duplicada, credenciales, contraseña débil y exceso de intentos.

El ViewModelStore del contenido se crea para el UID activo y se limpia al abandonar ese contenido. Las contraseñas no se guardan en Room ni se registran en logs. La sesión es administrada por el SDK de Firebase.

Configurar: registrar com.udb.flowpad en Firebase, descargar google-services.json a app/, habilitar Email/Password y sincronizar Gradle. El plugin Google Services se aplica cuando existe el JSON. Sin él no hay acceso simulado ni bypass.

Dependencias de autenticación fijadas: Firebase BoM 33.7.0 y google-services 4.4.2. Referencia: https://firebase.google.com/docs/auth/android/password-auth

## Validaciones y experiencia

Nombres de proyectos, tarjetas y tareas: de 1 a 200 caracteres, espacios recortados al guardar. UI y ViewModel validan; los formularios muestran indicaciones y bloquean envíos inválidos.

Las escrituras tienen un manejador de excepciones que publica un mensaje para Snackbar. Los flujos de lectura reintentan cada tres segundos cuando fallan. No se exponen detalles internos del error al usuario.

Las eliminaciones muestran confirmación. El último proyecto no se puede borrar. Colores de MaterialTheme y recursos de strings.xml se reutilizan en la interfaz. Los estados de carga de autenticación bloquean envíos duplicados.

La búsqueda afecta el proyecto activo y la lista de proyectos del menú; no consulta globalmente todas las bases. El modo oscuro se conserva por cuenta. Las contraseñas de formularios se mantienen solo en memoria.

Límites: sin sincronización del contenido, exportación de respaldo, colaboración multiusuario o recuperación de contraseña en pantalla. Estas funciones no son parte del mínimo implementado.

## Verificación y mantenimiento

Las capturas aportadas muestran Flowpad ejecutándose en el emulador Pixel 9, acceso al contenido con una cuenta, navegación por los módulos y visualización del modo oscuro. Firebase Authentication muestra un usuario registrado.

La evidencia visual confirma esas pantallas y la existencia de la cuenta; no demuestra por sí sola todas las operaciones de creación, edición, eliminación, persistencia tras reinicio o separación entre dos cuentas.

Las comprobaciones estáticas y SQL realizadas sobre el proyecto verificaron recursos XML, referencias de textos, Wrapper, edición de tareas, cambio de estado de tarjetas, orden de notas y borrado en cascada en un esquema SQLite reconstruido. No sustituyen pruebas instrumentadas de Room.

Pruebas de aceptación: correo inválido, cuenta duplicada, credenciales incorrectas, desconexión, cierre de sesión, CRUD de los cuatro módulos, búsqueda, cancelación de borrados, reinicio, dos cuentas y rotación. Registrar resultados reales antes de afirmar cobertura completa.

Para futuras modificaciones del esquema Room, incrementar la versión y definir migraciones que conserven datos. Mantener dependencias y configuración documentadas. Entregar fuentes, README, manuales y mockups en el repositorio público con aportes auténticos de los integrantes.