using Microsoft.AspNetCore.Mvc;
using CommunityRideBackend.Models;
using CommunityRideBackend.Services;

namespace CommunityRideBackend.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class AuthController : ControllerBase
    {
        private readonly TripStateService _stateService;

        public AuthController(TripStateService stateService)
        {
            _stateService = stateService;
        }

        [HttpPost("login")]
        public IActionResult LoginOrRegister([FromBody] LoginRequest request)
        {
            var user = _stateService.RegisterOrGetUser(request);
            return Ok(new
            {
                Token = "jwt_token_" + Guid.NewGuid().ToString()[..12],
                User = user
            });
        }
    }
}
