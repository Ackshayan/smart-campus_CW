package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Part 4 — Sub-resource for /api/v1/sensors/{sensorId}/readings.
 *
 * This class has NO @Path annotation at the class level.
 * It is instantiated by SensorResource's sub-resource locator and
 * receives the sensorId via constructor injection.
 *
 * GET  /api/v1/sensors/{sensorId}/readings  → full reading history
 * POST /api/v1/sensors/{sensorId}/readings  → add a new reading
 *      (also updates the parent sensor's currentValue as a side effect)
 *      (throws 403 via mapper if sensor is in MAINTENANCE status)
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore store = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // ---- GET reading history ----
    @GET
    public Response getReadings() {
        if (store.getSensor(sensorId) == null) {
            return Response.status(404)
                    .entity(Collections.singletonMap("message", "Sensor not found: " + sensorId))
                    .build();
        }
        List<SensorReading> history = store.getReadings(sensorId);
        return Response.ok(history).build();
    }

    // ---- POST new reading ----
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(404)
                    .entity(Collections.singletonMap("message", "Sensor not found: " + sensorId))
                    .build();
        }

        // Part 5.3 — MAINTENANCE sensors cannot accept readings → 403 via mapper
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor '" + sensorId + "' is currently under MAINTENANCE " +
                    "and cannot accept new readings.");
        }

        // Auto-assign id and timestamp if client did not supply them
        if (reading.getId() == null || reading.getId().isBlank()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        store.addReading(sensorId, reading);

        // Required side effect: keep the parent sensor's currentValue up to date
        sensor.setCurrentValue(reading.getValue());

        return Response.status(201).entity(reading).build();
    }
}
