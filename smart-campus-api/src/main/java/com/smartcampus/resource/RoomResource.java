package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/**
 * Part 2 — Room CRUD.
 *
 * GET    /api/v1/rooms            → list all rooms
 * POST   /api/v1/rooms            → create a room
 * GET    /api/v1/rooms/{roomId}   → get single room
 * DELETE /api/v1/rooms/{roomId}   → delete room (blocked if sensors assigned)
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore store = DataStore.getInstance();

    // ---- GET all rooms ----
    @GET
    public Response getAllRooms() {
        return Response.ok(new ArrayList<>(store.getRooms().values())).build();
    }

    // ---- POST create room ----
    @POST
    public Response createRoom(Room room) {
        if (room.getId() == null || room.getId().isBlank()) {
            return Response.status(400)
                    .entity(err("Room 'id' field is required."))
                    .build();
        }
        if (store.getRoom(room.getId()) != null) {
            return Response.status(409)
                    .entity(err("Room with ID '" + room.getId() + "' already exists."))
                    .build();
        }
        store.putRoom(room);
        return Response.status(201).entity(room).build();
    }

    // ---- GET single room ----
    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRoom(roomId);
        if (room == null) {
            return Response.status(404)
                    .entity(err("Room not found: " + roomId))
                    .build();
        }
        return Response.ok(room).build();
    }

    // ---- DELETE room (blocked when sensors exist — throws 409 via mapper) ----
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRoom(roomId);
        if (room == null) {
            // Idempotent: if it is already gone, still return 204
            return Response.noContent().build();
        }
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                    "Cannot delete room '" + roomId + "'. It still has " +
                    room.getSensorIds().size() + " sensor(s) assigned. " +
                    "Remove all sensors from the room first.");
        }
        store.deleteRoom(roomId);
        return Response.noContent().build();
    }

    // ---- helper ----
    private Map<String, String> err(String msg) {
        return Collections.singletonMap("message", msg);
    }
}
