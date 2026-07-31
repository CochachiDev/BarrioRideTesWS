# Backend en .NET 8 (ASP.NET Core Web API + SignalR)

Backend completo en **.NET 8** con **SignalR Hub** en tiempo real y **REST APIs** para la comunicación entre teléfonos móviles en vivo (Pasajero y Conductor).

---

## 🚀 Requisitos e Instalación

1. Tener instalado el SDK de **.NET 8.0** en tu máquina (Windows, macOS o Linux):
   https://dotnet.microsoft.com/download/dotnet/8.0

2. O tener **Docker / Docker Compose** para ejecutarlo con un solo comando.

---

## ⚡ Cómo Ejecutar Localmente

### Opción 1: Con la CLI de .NET 8
```bash
cd backend
dotnet restore
dotnet run
```
El backend iniciará en `http://localhost:5000` (HTTP) y `https://localhost:5001` (HTTPS).
Puedes acceder al panel interactivo de Swagger en:
👉 `http://localhost:5000/swagger`

### Opción 2: Con Docker
```bash
cd backend
docker build -t community-ride-backend .
docker run -p 5000:5000 community-ride-backend
```

---

## 📱 Cómo Probar con 2 Teléfonos Reales en la Misma Red Wi-Fi

1. **Obtener la dirección IP local de tu computadora:**
   - En Windows: Ejecuta `ipconfig` en la terminal (ej. `192.168.1.50`).
   - En Mac/Linux: Ejecuta `ifconfig` o `ip a`.

2. **Endpoints de la API:**
   - **REST API Base:** `http://192.168.1.50:5000/api`
   - **SignalR Hub:** `ws://192.168.1.50:5000/hubs/ride`

3. **Interacción:**
   - **Teléfono A (Cliente):** Solicita un viaje seleccionando el punto de recojo/destino. El evento viaja al Hub de SignalR.
   - **Teléfono B (Conductor):** Recibe la alerta de viaje solicitado en tiempo real vía WebSocket. Al hacer clic en "Aceptar viaje", se notifica instantáneamente al Teléfono A y se habilita la transmisión de ubicación GPS del conductor.

---

## 🛰️ Hub SignalR (`/hubs/ride`)

- `RequestTrip(RequestTripModel)` ➔ Emite `TripRequested` a los conductores.
- `AcceptTrip(tripId, driverId)` ➔ Emite `TripAccepted` al pasajero.
- `UpdateTripStatus(tripId, newStatus)` ➔ Emite `TripStatusChanged`.
- `UpdateDriverLocation(driverId, lat, lng)` ➔ Transmite el pulso GPS en vivo del conductor.
- `CancelTrip(tripId)` ➔ Emite `TripCancelled`.
