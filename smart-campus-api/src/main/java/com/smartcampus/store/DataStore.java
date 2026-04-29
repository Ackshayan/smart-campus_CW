package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe in-memory singleton data store.
 * Replaces a database — uses ConcurrentHashMap and CopyOnWriteArrayList
 * to safely handle concurrent HTTP requests (JAX-RS per-request instances
 * all share this single instance).
 */
public class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    private final Map<String, Room>               rooms    = new ConcurrentHashMap<>();
    private final Map<String, Sensor>             sensors  = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    private DataStore() {
        // ---- seed demo data so the API has content out of the box ----
        Room r1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room r2 = new Room("LAB-101", "Computer Lab A", 30);
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);

        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE",  22.5,  "LIB-301");
        Sensor s2 = new Sensor("CO2-001",  "CO2",         "ACTIVE",  410.0, "LAB-101");
        sensors.put(s1.getId(), s1);
        sensors.put(s2.getId(), s2);

        r1.getSensorIds().add(s1.getId());
        r2.getSensorIds().add(s2.getId());

        readings.put(s1.getId(), new CopyOnWriteArrayList<>());
        readings.put(s2.getId(), new CopyOnWriteArrayList<>());
    }

    public static DataStore getInstance() { return INSTANCE; }

    // ---- Room operations ----
    public Map<String, Room> getRooms()         { return rooms; }
    public Room              getRoom(String id)  { return rooms.get(id); }
    public void              putRoom(Room r)     { rooms.put(r.getId(), r); }
    public void              deleteRoom(String id){ rooms.remove(id); }

    // ---- Sensor operations ----
    public Map<String, Sensor> getSensors()          { return sensors; }
    public Sensor              getSensor(String id)   { return sensors.get(id); }
    public void                putSensor(Sensor s)    { sensors.put(s.getId(), s); }

    // ---- Reading operations ----
    public List<SensorReading> getReadings(String sensorId) {
        return readings.getOrDefault(sensorId, new CopyOnWriteArrayList<>());
    }

    public void addReading(String sensorId, SensorReading r) {
        readings.computeIfAbsent(sensorId, k -> new CopyOnWriteArrayList<>()).add(r);
    }
}
