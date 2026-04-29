package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Part 1.2 — Discovery / HATEOAS root endpoint.
 * GET /api/v1  →  returns API metadata and hypermedia links.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response discover() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("name",        "Smart Campus Sensor & Room Management API");
        info.put("version",     "1.0.0");
        info.put("description", "RESTful API for managing university campus rooms and IoT sensors");
        info.put("contact",     "admin@smartcampus.ac.uk");

        // Hypermedia links — allows clients to discover resources without hard-coding URLs
        Map<String, String> links = new LinkedHashMap<>();
        links.put("self",    "/api/v1");
        links.put("rooms",   "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        info.put("_links", links);

        return Response.ok(info).build();
    }
}
