package com.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Part 5.2 — Maps LinkedResourceNotFoundException → HTTP 422 Unprocessable Entity.
 * Triggered when POST /sensors is called with a roomId that does not exist.
 * 422 is correct here (not 404) because the request URL was found and the JSON
 * was parsed successfully — the problem is a referential integrity failure
 * inside the payload.
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper
        implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {
        ErrorResponse body = new ErrorResponse(422, "Unprocessable Entity", ex.getMessage());
        return Response.status(422)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
