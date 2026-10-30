# Escuela de Carabineros 📋

Aplicación móvil Android para que **docentes e instructores** de la Escuela de Formación de Carabineros (ESFOCAR) creen rúbricas de evaluación y las apliquen **en terreno** desde el celular o la tablet, reemplazando las rúbricas impresas que hoy se envían por correo.

> Proyecto de la **Evaluación Parcial 2** de *DSY1105 – Desarrollo de Aplicaciones Móviles* (Duoc UC).
> Prototipo con **datos ficticios**, según lo acordado con el socio formador.

## 👥 Integrantes

| Nombre | GitHub |
|---|---|
| Dangelo Rojas Pinto | @Dangelo-Rojas |
| Benjamin Bastias Perez | @BasZG |

## 🎯 Problemática

Hoy las rúbricas de la Escuela son plantillas impresas. En asignaturas prácticas (por ejemplo, Técnicas y Tácticas Policiales) el instructor evalúa en el patio o en una simulación, con papel y carpeta. La app permite:

- Crear y **editar** rúbricas **parametrizables** (criterios con ponderación y descripción de cada nivel de logro).
- Evaluar a un alumno o a un grupo **en el lugar** y en cualquiera de las **sedes** de la Escuela, adjuntando foto de evidencia y ubicación.
- **Compartir el resultado en PDF** para que el docente se lo envíe al alumno.
- Calcular la nota con la regla de la Escuela: **escala 1,00 a 7,00 con dos decimales y sin aproximar** (4,36 no es 4,4).

## ✅ Funcionalidades implementadas

| Requisito de la pauta | Implementación |
|---|---|
| Diseño visual con Material 3 | Tema propio con verdes y amarillo institucionales, modo claro y oscuro, tipografía grande para docentes de todas las edades |
| Navegación funcional | Navigation Compose: Login → Mis rúbricas → Nueva/Editar rúbrica → Evaluar → Historial · Administrar cuentas → Nueva/Editar cuenta (solo Administrador) |
| Formularios validados | Login, Nueva/Editar cuenta (Administrador), Nueva/Editar rúbrica (criterios dinámicos y descripción por nivel) y Evaluación (con selector de sede). Cada campo muestra borde rojo, ícono de advertencia y mensaje; ícono de check cuando es válido |
| Validaciones en la lógica | Reglas centralizadas en `utils/Validadores.kt` y aplicadas en los ViewModels; las pantallas solo muestran el estado (`UiState`) |
| Gestión de estado | `StateFlow` + `data class UiState` por pantalla, observados con `collectAsState()` |
| Almacenamiento local | **Room** (docentes, rúbricas, criterios, evaluaciones) + **SharedPreferences** para la sesión |
| Recursos nativos | 📷 **Cámara** (foto de evidencia) · 📍 **GPS** (ubicación de la evaluación) · 📳 **Vibración** al guardar · 📄 **PDF + compartir** (PdfDocument, FileProvider e Intent de Android). Permisos en tiempo de ejecución con mensaje si el usuario los rechaza |
| Animaciones | Transiciones entre pantallas, entrada animada del login, mensajes de error que aparecen y desaparecen, nota que "cuenta" y cambia de color, barra de ponderaciones animada, tarjetas del historial que se expanden, animación de items en listas |

## 🛡️ Roles y administración de cuentas

| Rol | Puede |
|---|---|
| **Administrador** | Todo lo del docente + **crear cuentas**, **asignar o cambiar roles**, **quitar o devolver el acceso** (activar/desactivar) y **eliminar cuentas** |
| **Docente / Instructor** | Crear, editar y aplicar rúbricas; ver y compartir sus evaluaciones |

Reglas de negocio: el administrador no puede desactivarse ni eliminarse a sí mismo, y siempre debe quedar al menos un administrador activo. Una cuenta desactivada no puede iniciar sesión, pero sus evaluaciones se conservan.

## 🏗️ Arquitectura (MVVM)

```
app/src/main/java/cl/duoc/escuelacarabineros/
├── data/
│   ├── local/          # Room: AppDatabase, entity/, dao/
│   ├── repository/     # AuthRepository, RubricaRepository, EvaluacionRepository
│   ├── session/        # SessionManager (SharedPreferences)
│   └── AppContainer.kt # Inyección de dependencias manual
├── model/              # Reglas de negocio: NivelLogro, CalculadoraNota
├── utils/              # Validadores, FotoStorage, UbicacionHelper, Vibracion, Seguridad
├── viewmodel/          # Un ViewModel por pantalla + AppViewModelProvider
├── navigation/         # Rutas y NavHost
└── ui/
    ├── theme/          # Colores, tipografía y tema Material 3
    ├── components/     # CampoValidado, BotonPrincipal, MensajeError, EstadoVacio
    └── screens/        # Login, Home, CrearRubrica, Evaluar, Historial, Cuentas, CuentaForm
```

**Flujo:** `Screen (Compose)` → eventos → `ViewModel` (valida y actualiza `UiState`) → `Repository` → `DAO (Room)`.

## 🧮 Cálculo de la nota

- Cada criterio tiene una ponderación (la suma debe ser 100 %).
- Niveles de logro: Logrado 100 %, Aceptable 60 %, Incipiente 30 %, No logrado 0 %. Cada criterio puede describir qué significa cada nivel, y esa descripción se muestra al evaluar (retroalimentación, no solo "sí/no").
- Escala 1–7 con 60 % de exigencia. La nota se **trunca** a dos decimales y se guarda en centésimas (`436` = 4,36) para no perder precisión.

## ▶️ Cómo ejecutar

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/Dangelo-Rojas/escuela-carabineros.git
   ```
2. Abrir la carpeta en **Android Studio** y esperar a que termine *Gradle Sync*.
3. Ejecutar en un emulador o dispositivo con **Android 8.0 (API 26)** o superior (botón ▶ *Run 'app'*).
4. Ingresar con una de las cuentas de prueba (botones **"Demo docente"** y **"Demo admin"**):

   | Rol | Correo | Contraseña |
   |---|---|---|
   | Docente | `docente@esfocar.cl` | `demo123` |
   | Administrador | `admin@esfocar.cl` | `admin123` |

   No existe registro público: **solo el Administrador crea las cuentas**, como pidió la Escuela (todos los docentes están inscritos con su correo).

> En el emulador, la ubicación se puede simular desde **Extended controls (⋯) → Location**.

## 🛠️ Tecnologías

Kotlin · Jetpack Compose · Material 3 · Navigation Compose · ViewModel + StateFlow · Room · Activity Result API (permisos y cámara) · LocationManager

## 📌 Planificación

Tablero Trello: https://trello.com/b/I0o4mpcF/esfocar-desarrollo-de-aplicaci%C3%B3n-m%C3%B3vil
