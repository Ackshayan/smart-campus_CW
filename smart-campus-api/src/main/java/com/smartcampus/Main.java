package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import java.io.IOException;
import java.net.URI;

public class Main {

    public static final String BASE_URI = "http://localhost:8080/api/v1";

    public static void main(String[] args) throws IOException {
        final HttpServer server = GrizzlyHttpServerFactory
                .createHttpServer(URI.create(BASE_URI), new SmartCampusApplication());

        System.out.println("==============================================");
        System.out.println("  Smart Campus API started!");
        System.out.println("  Base URL : http://localhost:8080/api/v1");
        System.out.println("  Press ENTER to stop the server...");
        System.out.println("==============================================");

        try {
            System.in.read();
        } finally {
            server.stop();
        }
    }
}
