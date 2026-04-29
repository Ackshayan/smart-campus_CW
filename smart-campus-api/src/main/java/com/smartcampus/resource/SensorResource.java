package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Part 3 — Sensor CRUD + optional ?type= filter.
 * Part 4 — Sub-resource locator for /sensors/{sensorId}/readings.
 *
 * GET  /api/v1/sensors              → list all sensors (optionally filtered by ?type=)
 * POST /api/v1/sensors              → register new sensor (validates roomId exists)
 * GET  /api/v1/sensors/{sensorId}   → get a single sensor
 * *    /api/v1/sensors/{id}/readings → delegated to SensorReadingResource
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    // ---- GET all sensors, with optional ?type= query parameter filter ----
    @GET
    public Response getSensors(@QueryParam("type") String type) {
        List<Sensor> list = new ArrayList<>(store.getSensors().values());

        if (type != null && !type.isBlank()) {
            list = list.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }
        return Response.ok(list).build();
    }

    // ---- POST create sensor (validates that the referenced room exists) ----
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor.getId() == null || sensor.getId().isBlank()) {
            return Response.status(400)
                    .entity(Collections.singletonMap("message", "Sensor 'id' field is required."))
                    .build();
        }

        // Validate linked room — throws 422 via mapper if room not found
        if (sensor.getRoomId() == null || store.getRoom(sensor.getRoomId()) == null) {
            throw new LinkedResourceNotFoundException(
                    "Room with ID '" + sensor.getRoomId() +
                    "' does not exist. Cannot register sensor against a non-existent room.");
        }

        store.putSensor(sensor);

        // Keep the room's sensorIds list in sync
        Room room = store.getRoom(sensor.getRoomId());
        if (!room.getSensorIds().contains(sensor.getId())) {
            room.getSensorIds().add(sensor.getId());
        }

        return Response.status(201).entity(sensor).build();
    }

    // ---- GET single sensor ----
    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(404)
                    .entity(Collections.singletonMap("message", "Sensor not found: " + sensorId))
                    .build();
        }
        return Response.ok(sensor).build();
    }

    /**
     * Sub-resource locator (Part 4).
     * JAX-RS does NOT invoke this method directly as a request handler.
     * Instead it returns the sub-resource object and delegates further
     * path matching to SensorReadingResource.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(
            @PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
