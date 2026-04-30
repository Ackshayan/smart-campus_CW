package com.smartcampus.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG =
            Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable ex) {

        // If JAX-RS already produced a proper HTTP response (404, 415, etc.)
        // pass it through unchanged — do NOT convert to 500
        if (ex instanceof WebApplicationException) {
            return ((WebApplicationException) ex).getResponse();
        }

        // Only unknown runtime exceptions become 500
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