using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.SignalR;
using CommunityRideBackend.Hubs;
using CommunityRideBackend.Models;
using CommunityRideBackend.Services;

namespace CommunityRideBackend.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class DriversController : ControllerBase
    {
        private readonly TripStateService _stateService;
        private readonly IHubContext<RideHub, IRideClient> _hubContext;

        public DriversController(TripStateService stateService, IHubContext<RideHub, IRideClient> hubContext)
        {
            _stateService = stateService;
            _hubContext = hubContext;
        }

        [HttpGet("available")]
        public IActionResult GetAvailableDrivers()
        {
            return Ok(_stateService.GetAvailableDrivers());
        }

        [HttpPost("{driverId}/location")]
        public async Task<IActionResult> UpdateLocation(string driverId, [FromBody] LocationUpdateModel model)
        {
            var driver = _stateService.UpdateDriverLocation(driverId, model.Latitude, model.Longitude);
            if (driver == null) return NotFound();
            await _hubContext.Clients.All.DriverLocationUpdated(driverId, model.Latitude, model.Longitude);
            return Ok(driver);
        }
    }
}
