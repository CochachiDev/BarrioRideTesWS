using System.Collections.Concurrent;
using CommunityRideBackend.Models;

namespace CommunityRideBackend.Services
{
    public class TripStateService
    {
        private readonly ConcurrentDictionary<string, TripDto> _trips = new();
        private readonly ConcurrentDictionary<string, DriverDto> _drivers = new();
        private readonly ConcurrentDictionary<string, UserDto> _users = new();

        public TripStateService()
        {
            // Seed default community drivers
            var defaultDriver1 = new DriverDto
            {
                Id = "drv_001",
                UsuarioId = "usr_drv1",
                Nombre = "Carlos Mendoza",
                Telefono = "+51 987 654 321",
                Status = DriverStatus.DISPONIBLE,
                LatitudActual = -12.0856,
                LongitudActual = -77.0348,
                Rating = 4.95,
                TotalViajes = 142,
                PlacaVehiculo = "TM-4091",
                ModeloVehiculo = "GreenE-Motion Trike"
            };

            var defaultDriver2 = new DriverDto
            {
                Id = "drv_002",
                UsuarioId = "usr_drv2",
                Nombre = "María Delgado",
                Telefono = "+51 912 345 678",
                Status = DriverStatus.DISPONIBLE,
                LatitudActual = -12.0870,
                LongitudActual = -77.0360,
                Rating = 4.88,
                TotalViajes = 98,
                PlacaVehiculo = "TM-1052",
                ModeloVehiculo = "Eco-Cab 200"
            };

            _drivers[defaultDriver1.Id] = defaultDriver1;
            _drivers[defaultDriver2.Id] = defaultDriver2;
        }

        public UserDto RegisterOrGetUser(LoginRequest request)
        {
            var existing = _users.Values.FirstOrDefault(u => u.Email.Equals(request.Email, StringComparison.OrdinalIgnoreCase));
            if (existing != null)
            {
                return existing;
            }

            var newUser = new UserDto
            {
                Id = "usr_" + Guid.NewGuid().ToString()[..8],
                Nombre = request.Nombre,
                Apellido = request.Apellido,
                Telefono = request.Telefono,
                Email = request.Email,
                Residencia = request.Residencia,
                Rol = request.Rol,
                FechaRegistro = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds()
            };

            _users[newUser.Id] = newUser;

            if (request.Rol == UserRole.CONDUCTOR)
            {
                var newDriver = new DriverDto
                {
                    Id = "drv_" + Guid.NewGuid().ToString()[..8],
                    UsuarioId = newUser.Id,
                    Nombre = $"{request.Nombre} {request.Apellido}",
                    Telefono = request.Telefono,
                    Status = DriverStatus.DISPONIBLE,
                    LatitudActual = -12.0856,
                    LongitudActual = -77.0348
                };
                _drivers[newDriver.Id] = newDriver;
            }

            return newUser;
        }

        public TripDto CreateTrip(RequestTripModel request)
        {
            var trip = new TripDto
            {
                Id = "trp_" + Guid.NewGuid().ToString()[..8],
                ClienteId = request.ClienteId,
                ClienteNombre = request.ClienteNombre,
                ClienteTelefono = request.ClienteTelefono,
                Origen = request.Origen,
                Destino = request.Destino,
                Estado = TripStatus.SOLICITADO,
                PrecioEstimado = request.PrecioEstimado,
                TimestampSolicitud = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds()
            };

            _trips[trip.Id] = trip;
            return trip;
        }

        public TripDto? AcceptTrip(string tripId, string driverId)
        {
            if (_trips.TryGetValue(tripId, out var trip) && _drivers.TryGetValue(driverId, out var driver))
            {
                trip.Estado = TripStatus.ACEPTADO;
                trip.ConductorId = driver.Id;
                trip.ConductorNombre = driver.Nombre;
                trip.ConductorTelefono = driver.Telefono;
                trip.TimestampAceptado = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();

                driver.Status = DriverStatus.OCUPADO;
                return trip;
            }

            return null;
        }

        public TripDto? UpdateTripStatus(string tripId, TripStatus newStatus)
        {
            if (_trips.TryGetValue(tripId, out var trip))
            {
                trip.Estado = newStatus;
                if (newStatus == TripStatus.COMPLETADO || newStatus == TripStatus.CANCELADO)
                {
                    trip.TimestampCompletado = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
                    if (!string.IsNullOrEmpty(trip.ConductorId) && _drivers.TryGetValue(trip.ConductorId, out var driver))
                    {
                        driver.Status = DriverStatus.DISPONIBLE;
                        if (newStatus == TripStatus.COMPLETADO)
                        {
                            driver.TotalViajes++;
                        }
                    }
                }
                return trip;
            }

            return null;
        }

        public DriverDto? UpdateDriverLocation(string driverId, double lat, double lng)
        {
            if (_drivers.TryGetValue(driverId, out var driver))
            {
                driver.LatitudActual = lat;
                driver.LongitudActual = lng;
                return driver;
            }
            return null;
        }

        public IEnumerable<TripDto> GetAllTrips() => _trips.Values;
        public IEnumerable<TripDto> GetPendingTrips() => _trips.Values.Where(t => t.Estado == TripStatus.SOLICITADO);
        public IEnumerable<DriverDto> GetAvailableDrivers() => _drivers.Values.Where(d => d.Status == DriverStatus.DISPONIBLE);
        public TripDto? GetTripById(string id) => _trips.TryGetValue(id, out var trip) ? trip : null;
    }
}
