using Microsoft.AspNetCore.SignalR;
using CommunityRideBackend.Models;
using CommunityRideBackend.Services;

namespace CommunityRideBackend.Hubs
{
    public interface IRideClient
    {
        Task TripRequested(TripDto trip);
        Task TripAccepted(TripDto trip);
        Task TripStatusChanged(string tripId, string newStatus);
        Task DriverLocationUpdated(string driverId, double lat, double lng);
        Task TripCancelled(string tripId);
    }

    public class RideHub : Hub<IRideClient>
    {
        private readonly TripStateService _tripState;

        public RideHub(TripStateService tripState)
        {
            _tripState = tripState;
        }

        public async Task RequestTrip(RequestTripModel request)
        {
            var trip = _tripState.CreateTrip(request);
            // Broadcast to all connected drivers
            await Clients.All.TripRequested(trip);
        }

        public async Task AcceptTrip(string tripId, string driverId)
        {
            var trip = _tripState.AcceptTrip(tripId, driverId);
            if (trip != null)
            {
                // Notify passenger and all drivers
                await Clients.All.TripAccepted(trip);
            }
        }

        public async Task UpdateTripStatus(string tripId, TripStatus newStatus)
        {
            var trip = _tripState.UpdateTripStatus(tripId, newStatus);
            if (trip != null)
            {
                await Clients.All.TripStatusChanged(tripId, newStatus.ToString());
            }
        }

        public async Task UpdateDriverLocation(string driverId, double lat, double lng)
        {
            _tripState.UpdateDriverLocation(driverId, lat, lng);
            // Broadcast driver location in real-time to active passenger maps
            await Clients.All.DriverLocationUpdated(driverId, lat, lng);
        }

        public async Task CancelTrip(string tripId)
        {
            _tripState.UpdateTripStatus(tripId, TripStatus.CANCELADO);
            await Clients.All.TripCancelled(tripId);
        }
    }
}
