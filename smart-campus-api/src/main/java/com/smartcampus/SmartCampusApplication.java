package com.smartcampus;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api/v1")
public class SmartCampusApplication extends ResourceConfig {

    public SmartCampusApplication() {
        // Auto-scans all @Path resources, @Provider mappers and @Provider filters
        packages("com.smartcampus");
        // Enables JSON serialisation / deserialisation via Jackson
        register(JacksonFeature.class);
    }
}
