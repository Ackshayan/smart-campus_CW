package com.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Part 5.4 — Catch-all mapper for any unhandled Throwable → HTTP 500.
 *
 * Security note: the full stack trace is ONLY logged internally.
 * It is NEVER returned to the client — doing so would leak class names,
 * library versions, and internal logic that attackers could exploit.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG =
            Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable ex) {
        // Log full details server-side only
        LOG.log(Level.SEVERE, "Unhandled exception: " + ex.getMessage(), ex);

        ErrorResponse body = new ErrorResponse(
                500,
                "Internal Server Error",
                "An unexpected error occurred. Please contact the administrator."
        );
        return Response.serverError()
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
