package com.smartcampus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Part 5.5 — Request/Response logging filter.
 *
 * Implements BOTH ContainerRequestFilter (fires before the resource method)
 * AND ContainerResponseFilter (fires after the response is produced).
 *
 * Using a filter instead of logging in each resource method:
 *  - Applies consistently to every endpoint with zero duplication (DRY)
 *  - Keeps resource classes focused on business logic only
 *  - Makes changes (e.g., adding a request-ID header) a single-file edit
 */
@Provider
public class LoggingFilter
        implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG =
            Logger.getLogger(LoggingFilter.class.getName());

    /** Fires BEFORE the request reaches any resource method. */
    @Override
    public void filter(ContainerRequestContext requestCtx) throws IOException {
        LOG.info(String.format("[REQUEST]  %s %s",
                requestCtx.getMethod(),
                requestCtx.getUriInfo().getRequestUri()));
    }

    /** Fires AFTER the response has been produced by the resource method. */
    @Override
    public void filter(ContainerRequestContext requestCtx,
                       ContainerResponseContext responseCtx) throws IOException {
        LOG.info(String.format("[RESPONSE] %s %s  →  %d",
                requestCtx.getMethod(),
                requestCtx.getUriInfo().getRequestUri(),
                responseCtx.getStatus()));
    }
}
