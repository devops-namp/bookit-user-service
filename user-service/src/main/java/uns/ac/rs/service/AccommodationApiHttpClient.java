package uns.ac.rs.service;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import uns.ac.rs.controller.dto.ReservationsCheckDto;
import uns.ac.rs.controller.request.CheckReservationsRequest;

@RegisterRestClient(configKey="accommodation-api")
public interface AccommodationApiHttpClient {

    @GET
    @Path("/reservations/check")
    @Produces(MediaType.APPLICATION_JSON)
    ReservationsCheckDto getResource(CheckReservationsRequest request);
}
