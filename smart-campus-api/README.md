# Smart Campus Sensor & Room Management API

# 1.Overview of API Design

This project delivers a fully functional RESTful web service designed to manage a university Smart Campus environment. Built using Java with the JAX-RS (Jersey) framework and a Grizzly embedded HTTP server, the API handles three core domain entities: Rooms, IoT Sensors, and Sensor Readings.

The design follows REST architectural constraints throughout — HTTP verbs are used semantically (GET, POST, DELETE), HTTP status codes are returned precisely (200, 201, 204, 400, 403, 404, 409, 422, 500), and resources are organised in a logical nested hierarchy (`/rooms, /sensors, /sensors/{id}/readings`). All data is persisted across requests using a thread-safe in-memory `DataStore` singleton backed by `ConcurrentHashMap` and `CopyOnWriteArrayList` — no external database is required. Resilient error handling is achieved through four dedicated JAX-RS `ExceptionMapper` providers, ensuring the API never exposes internal implementation details to consumers.

---

# 2.Build and Launch Instruction 

Prerequisites:
* Java Development Kit (JDK) 11 or higher
* Apache Maven 3.8 or higher
* An IDE such as IntelliJ IDEA or Eclipse

Steps to Launch:
1. Clone the repostory:
  ```bash
  git clone <YOUR_GITHUB_REPO_URL>
  cd smart-campus-api
  ```

2. Build the project - Maven will download all jersey and grizzly dependencies automatically:
  ```bash
  mvn clean package
  ```

3. Start the server - run the packaged JAR directly:
  ```bash
  java -jar target/smart-campus-api-1.0-SNAPSHOT.jar
  ```

4. Verify - the console print:
  Smart Campus API started!
  Base URL : http://localhost:8080/api/v1
  Press ENTER to stop the server...

# 3.Sample API Interactions (curl commands)

```bash
# 1. View API
curl -X GET http://localhost:8080/api/v1

# 2. Create a room
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"HALL-01","name":"Main Hall","capacity":200}'

# 3. List all rooms
curl -X GET http://localhost:8080/api/v1/rooms

# 4. Register a Sensor in a room (replace <ROOM_ID>)
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-002","type":"Temperature","status":"ACTIVE","currentValue":0.0,"roomId":"<ROOM_ID>"}'

# 5. Record a New Sensor Reading: (replace <SENSOR_ID>)
curl -X POST http://localhost:8080/api/v1/sensors/<SENSOR_ID>/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 23.5}'

# 6. Filter sensors by type
curl -X GET "http://localhost:8080/api/v1/sensors?type=Temperature"

# 7. Attempt to Delete a Room with Active Sensors (expect 409 Conflict):
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301

# 8. Attempt to Register a Sensor with an Invalid Room ID (expect 422 Unprocessable Entity):
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"X-999","type":"CO2","status":"ACTIVE","currentValue":0,"roomId":"NONEXISTENT"}'
```

---

# Question & Answer Section

# Part 1 - Service Architecture & Setup
1. Project & Application Configuration

Question: In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.

Answer: The default JAX-RS resource lifecycle is per-request — the runtime instantiates a fresh resource object for each incoming HTTP request and discards it once the response is dispatched. Storing data as instance variables would therefore mean every request starts with an empty state and all previously written data would be permanently lost. To solve this, the project uses the Singleton design pattern via a dedicated `DataStore` class. All resource instances access this single shared object via `DataStore.getInstance()`. The `DataStore` uses `ConcurrentHashMap` for rooms and sensors and `CopyOnWriteArrayList` for readings, ensuring that multiple simultaneous HTTP requests can safely read and write shared data without causing race conditions or data corruption.

2. . The ”Discovery” Endpoint

Question: Why is the provision of ”Hypermedia” (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

Answer: HATEOAS (Hypermedia as the Engine of Application State) elevates an API from a simple data endpoint to a self-navigating, self-documenting service. Rather than requiring client developers to memorise or hardcode URL structures from static documentation, the server embeds navigational links directly into its responses. For example, the discovery endpoint at `GET /api/v1` returns `_links` containing the URLs for `/rooms` and `/sensors`. This means clients can dynamically follow links rather than constructing paths manually. If the server's URL structure changes in a future version, compliant clients continue to work without modification — significantly reducing coupling between the client and the server implementation.

# Part 2 - Room Management
1. RoomResource Implementation

Questioin: When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects?Consider network bandwidth and client side processing.

Answer: Response that includes only IDs results in an extremely small initial response body and saves bandwidth. But it requires the client to make individual HTTP GET requests for each of these IDs to get the room information — commonly referred to as the “N+1” problem. In case there are 100 rooms in the campus, it makes a total of 101 HTTP requests. Response with entire room objects results in a larger initial response body but eliminates the need for further requests. The client is able to display the whole list in one go, resulting in lesser latencies and computational cost.

2. RoomDeletion & Safety Logic

Question: Is the DELETE operation idempotent in your implementation? Provideadetailedjustification by describing whathappens if a client mistakenly sends the exact same DELETE request for a room multiple times.

Answer: Yes, DELETE requests are idempotent. Idempotence in HTTP states that the state of the server will always be the same after any number of repetitions of the exact same request. In this case, the first DELETE request will delete a room (that does not have any sensors attached to it) in the `DataStore` and return status code `204 No Content`. The second DELETE request on the same room will get us `204 No Content` status code again since there is no room anymore instead of `404 Not Found` that one might expect.

# Part 3: Sensor Operations & Linking
1. Sensor Resource & Integrity

Question: We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

Answer: The `@Consumes` annotation functions as a content negotiation gate enforced by the JAX-RS runtime before any application code executes. If a client sends a request with a `Content-Type` header of `text/plain` or `application/xml`, the Jersey container inspects the header, finds no matching resource method, and immediately rejects the request with an HTTP 415 Unsupported Media Type response — without invoking our resource method at all. This is an important security and correctness feature: it guarantees that malformed or unexpected content formats never reach business logic.

2.  Filtered Retrieval & Search

Question: You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why is the query parameter approach generally considered superior for filter ingand searching
collections?

Answer: URL path segments are semantically intended to identify a specific resource in a hierarchy. Query parameters are semantically intended to modify or filter the view of a collection. Using path segments for filtering breaks REST conventions and creates rigid, fragile URLs — for example, combining two filters would require deeply nested paths like /sensors/type/CO2/status/ACTIVE. Query parameters handle this naturally and cleanly: ?type=CO2&status=ACTIVE. They are also entirely optional by design, meaning GET /sensors continues to return all sensors when no filter is provided, without any URL restructuring. This flexibility, readability, and adherence to REST semantics makes @QueryParam the correct choice for filtering.

# Part 4: Deep Nesting with Sub- Resources
1. The Sub-Resource Locator Pattern

Question: Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class?

Answer: The Sub-Resource Locator pattern gives us the flexibility to use a different class (`SensorReadingResource`) to handle requests on a sub-resource path `(/sensors/{id}/readings)` rather than adding code inside the root resource class `(SensorResource)`. It follows the principle of Single Responsibility where the domain concepts are managed by only one class each. Without using this approach, we end up having one big resource class managing the room, sensors, and their readings, making the entire application hard to understand, manage, and test. The locator also helps serve as a gateway to perform validations for the existence of the parent resource before delegating the request further.

# Part 5: Advanced Error Handling, Exception Mapping & Logging

2. Dependency Validation (422 Unprocessable Entity)

Question: Why is HTTP 422 often considered more semantically accurate than a standard 404 whenthe issue is a missing reference inside a valid JSON payload?

Answer: HTTP `404 Not Found` specifically communicates that the URI endpoint itself could not be located. When a client sends a `POST /api/v1/sensors` request with a `roomId` that does not exist, the `/sensors` endpoint is perfectly reachable — `404` would therefore be misleading. The JSON body is also syntactically valid, ruling out `400 Bad Request`. The actual problem is a semantic business rule violation: the payload references an entity `(roomId)` that does not exist in the system. HTTP 422 Unprocessable Entity is the precise status code for this scenario — it tells the client that the server fully understood the request format and the endpoint, but could not process the payload due to a logical/referential integrity failure.

4. The Global Safety Net (500)

Question: From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

Answer: Exposing a raw Java stack trace to an external client constitutes a serious information disclosure vulnerability. A stack trace can reveal: the exact names of internal packages and classes (mapping the codebase structure for attackers), the specific frameworks and library versions in use (enabling targeted exploitation of known CVEs), internal server file paths, and the precise logical flow of the application (making it easier to craft inputs designed to trigger specific failure paths). The `GlobalExceptionMapper` in this project mitigates this entirely — any unhandled `Throwable` is caught, the full trace is written to the server log internally, and the client receives only a safe, generic `500 Internal Server Error` message with no implementation details.

5. API Request & Response Logging Filters

Question: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?

Answer: Manually inserting logging statements into every resource method violates the DRY (Don't Repeat Yourself) principle and introduces significant maintenance risk — developers forget to add them, use inconsistent formats, or omit them during rapid development. A JAX-RS `ContainerRequestFilter` and `ContainerResponseFilter`, implemented in a single `LoggingFilter` class, intercepts every request and response automatically and unconditionally, with no changes required to any resource class. This is an application of Aspect-Oriented Programming (AOP) — separating operational concerns (logging) from business concerns (room/sensor management) entirely. Adding new endpoints in the future requires zero logging changes, and modifying the log format requires editing exactly one file.