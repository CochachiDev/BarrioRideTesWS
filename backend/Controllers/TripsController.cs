using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.SignalR;
using CommunityRideBackend.Hubs;
using CommunityRideBackend.Models;
using CommunityRideBackend.Services;

namespace CommunityRideBackend.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class TripsController : ControllerBase
    {
        private readonly TripStateService _stateService;
        private readonly IHubContext<RideHub, IRideClient> _hubContext;

        public TripsController(TripStateService stateService, IHubContext<RideHub, IRideClient> hubContext)
        {
            _stateService = stateService;
            _hubContext = hubContext;
        }

        [HttpGet]
        public IActionResult GetAllTrips()
        {
            return Ok(_stateService.GetAllTrips());
        }

        [HttpGet("pending")]
        public IActionResult GetPendingTrips()
        {
            return Ok(_stateService.GetPendingTrips());
        }

        [HttpGet("{id}")]
        public IActionResult GetTrip(string id)
        {
            var trip = _stateService.GetTripById(id);
            if (trip == null) return NotFound();
            return Ok(trip);
        }

        [HttpPost("request")]
        public async Task<IActionResult> RequestTrip([FromBody] RequestTripModel request)
        {
            var trip = _stateService.CreateTrip(request);
            await _hubContext.Clients.All.TripRequested(trip);
            return Ok(trip);
        }

        [HttpPost("{tripId}/accept")]
        public async Task<IActionResult> AcceptTrip(string tripId, [FromQuery] string driverId)
        {
            var trip = _stateService.AcceptTrip(tripId, driverId);
            if (trip == null) return BadRequest("No se pudo aceptar el viaje.");
            await _hubContext.Clients.All.TripAccepted(trip);
            return Ok(trip);
        }

        [HttpPost("{tripId}/status")]
        public async Task<IActionResult> UpdateStatus(string tripId, [FromQuery] TripStatus status)
        {
            var trip = _stateService.UpdateTripStatus(tripId, status);
            if (trip == null) return NotFound();
            await _hubContext.Clients.All.TripStatusChanged(tripId, status.ToString());
            return Ok(trip);
        }
    }
}
