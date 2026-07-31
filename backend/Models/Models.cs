namespace CommunityRideBackend.Models
{
    public enum UserRole
    {
        CLIENTE,
        CONDUCTOR,
        ADMINISTRADOR
    }

    public enum DriverStatus
    {
        DISPONIBLE,
        OCUPADO,
        FUERA_DE_SERVICIO
    }

    public enum TripStatus
    {
        SOLICITADO,
        ACEPTADO,
        EN_CAMINO_A_RECOGER,
        LLEGA_A_PUNTO,
        EN_CURSO,
        COMPLETADO,
        CANCELADO
    }

    public class UserDto
    {
        public string Id { get; set; } = Guid.NewGuid().ToString();
        public string Nombre { get; set; } = string.Empty;
        public string Apellido { get; set; } = string.Empty;
        public string Telefono { get; set; } = string.Empty;
        public string Email { get; set; } = string.Empty;
        public string Residencia { get; set; } = string.Empty;
        public UserRole Rol { get; set; } = UserRole.CLIENTE;
        public long FechaRegistro { get; set; } = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        public bool Activo { get; set; } = true;
    }

    public class LoginRequest
    {
        public string Nombre { get; set; } = string.Empty;
        public string Apellido { get; set; } = string.Empty;
        public string Telefono { get; set; } = string.Empty;
        public string Email { get; set; } = string.Empty;
        public string Residencia { get; set; } = string.Empty;
        public UserRole Rol { get; set; } = UserRole.CLIENTE;
    }

    public class DriverDto
    {
        public string Id { get; set; } = Guid.NewGuid().ToString();
        public string UsuarioId { get; set; } = string.Empty;
        public string Nombre { get; set; } = string.Empty;
        public string Telefono { get; set; } = string.Empty;
        public DriverStatus Status { get; set; } = DriverStatus.DISPONIBLE;
        public double LatitudActual { get; set; } = -12.0856;
        public double LongitudActual { get; set; } = -77.0348;
        public double Rating { get; set; } = 4.9;
        public int TotalViajes { get; set; } = 0;
        public string PlacaVehiculo { get; set; } = "TM-2024";
        public string ModeloVehiculo { get; set; } = "E-Trike 300";
    }

    public class CommunityPointDto
    {
        public string Id { get; set; } = string.Empty;
        public string Nombre { get; set; } = string.Empty;
        public string Descripcion { get; set; } = string.Empty;
        public double Lat { get; set; }
        public double Lng { get; set; }
        public string Tipo { get; set; } = "PUNTO";
    }

    public class TripDto
    {
        public string Id { get; set; } = Guid.NewGuid().ToString();
        public string ClienteId { get; set; } = string.Empty;
        public string ClienteNombre { get; set; } = string.Empty;
        public string ClienteTelefono { get; set; } = string.Empty;
        public string? ConductorId { get; set; }
        public string? ConductorNombre { get; set; }
        public string? ConductorTelefono { get; set; }
        public CommunityPointDto Origen { get; set; } = new();
        public CommunityPointDto Destino { get; set; } = new();
        public TripStatus Estado { get; set; } = TripStatus.SOLICITADO;
        public double PrecioEstimado { get; set; } = 3.50;
        public int DistanciaMetros { get; set; } = 850;
        public int TiempoEstimadoMinutos { get; set; } = 4;
        public long TimestampSolicitud { get; set; } = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        public long? TimestampAceptado { get; set; }
        public long? TimestampCompletado { get; set; }
    }

    public class RequestTripModel
    {
        public string ClienteId { get; set; } = string.Empty;
        public string ClienteNombre { get; set; } = string.Empty;
        public string ClienteTelefono { get; set; } = string.Empty;
        public CommunityPointDto Origen { get; set; } = new();
        public CommunityPointDto Destino { get; set; } = new();
        public double PrecioEstimado { get; set; } = 3.50;
    }

    public class LocationUpdateModel
    {
        public string DriverId { get; set; } = string.Empty;
        public double Latitude { get; set; }
        public double Longitude { get; set; }
    }
}
