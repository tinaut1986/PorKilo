# PriceSmart 🛒

**PriceSmart** es una utilidad para Android diseñada para comparar precios de productos basándose en su valor por unidad de medida. Permite calcular el ahorro real en compras diarias, especialmente cuando los envases tienen diferentes tamaños o incluyen ofertas complejas.

## ✨ Funcionalidades

- 📊 **Cálculo por unidad base**: Determina automáticamente el precio por kg, litro o unidad.
- 🗂️ **Formatos guardados**: Guarda la configuración de un producto (nombre, cantidad, unidad, código de barras) como formato reutilizable para futuras comparaciones.
- 📷 **Escáner de código de barras**: Escanea el código de barras de un producto para rellenar automáticamente el formulario, usando el formato guardado si existe.
- 🏷️ **Gestión de Ofertas**:
  - **Lleva X y Paga Y** (3x2, 2x1, etc.).
  - **Descuento en unidades sucesivas** (ej: 2ª unidad al 70%).
  - **Lotes a precio fijo** (ej: 3 unidades por 5€).
  - **Cantidad extra gratis** (ajuste por porcentaje adicional gratuito).
- 📉 **Análisis de Ahorro**:
  - Visualización del precio original y descuento aplicado.
  - Indicador del porcentaje de ahorro real.
  - Comparativa porcentual entre productos de la lista.
- 🌐 **Idiomas**: Localización en 11 idiomas (Español, Inglés, Catalán, Francés, Portugués, Alemán, Vasco, Gallego, Japonés, Holandés y Chino).
- 🌑 **Interfaz**: Soporte para modo oscuro, orientación horizontal y diseño basado en Material 3.

## 🛠️ Tecnologías

- **Kotlin**: Lenguaje de programación.
- **Jetpack Compose**: Kit de herramientas para la interfaz de usuario.
- **Material 3**: Sistema de diseño.
- **Navigation Compose**: Gestión de navegación.
- **Room**: Persistencia local de datos (formatos guardados).
- **Google ML Kit / Code Scanner**: Lectura de códigos de barras.
- **Arquitectura MVVM**: Organización del código para facilitar su mantenimiento.

## 🚀 Cómo empezar

### Para Usuarios
- Puedes descargar la última versión estable (APK) directamente desde la sección de **[Releases](https://github.com/tinaut1986/PriceSmart/releases)** de este repositorio.

### Para Desarrolladores
1. Clona este repositorio.
2. Ábrelo con **Android Studio (Ladybug 2024.2.1 o superior)**.
3. Compila y ejecuta en un dispositivo con Android 5.0 (API 21) o superior.
4. Las compilaciones *debug* se instalan como una app aparte (**PriceSmart Dev**, `com.tinaut1986.pricesmart.debug`, icono rojo), de modo que pueden convivir con la versión estable sin desinstalarla.

---

Desarrollado con ❤️ por **tinaut1986** para ahorrar en el carrito de la compra.
