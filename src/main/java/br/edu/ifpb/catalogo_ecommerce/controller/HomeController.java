package br.edu.ifpb.catalogo_ecommerce.controller;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HomeController {

    private static final String HEALTH_COLLECTION = "_health";
    private static final String HEALTH_DOCUMENT = "ping";

    private final Firestore firestore;

    public HomeController(Firestore firestore) {
        this.firestore = firestore;
    }

    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("aplicacao", "catalogo-ecommerce");
        response.put("status", "online");
        response.put("firestore", "/health/firestore");
        return response;
    }

    @GetMapping("/health/firestore")
    public ResponseEntity<Map<String, Object>> firestoreHealth() {
        Map<String, Object> response = new LinkedHashMap<>();
        String emulatorHost = System.getenv("FIRESTORE_EMULATOR_HOST");
        response.put("emulatorHost", emulatorHost != null ? emulatorHost : "não definido");
        response.put("projectId", firestore.getOptions().getProjectId());

        long startedAt = System.currentTimeMillis();
        try {
            Map<String, Object> data = Map.of("ultimoPing", Instant.now().toString());

            firestore.collection(HEALTH_COLLECTION)
                    .document(HEALTH_DOCUMENT)
                    .set(data)
                    .get();

            DocumentSnapshot snapshot = firestore.collection(HEALTH_COLLECTION)
                    .document(HEALTH_DOCUMENT)
                    .get()
                    .get();

            response.put("conectado", true);
            response.put("status", "ok");
            response.put("documento", HEALTH_COLLECTION + "/" + HEALTH_DOCUMENT);
            response.put("dados", snapshot.getData());
            response.put("latenciaMs", System.currentTimeMillis() - startedAt);

            return ResponseEntity.ok(response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            response.put("conectado", false);
            response.put("status", "erro");
            response.put("erro", "Requisição interrompida");
            response.put("latenciaMs", System.currentTimeMillis() - startedAt);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        } catch (Exception e) {
            response.put("conectado", false);
            response.put("status", "erro");
            response.put("erro", e.getClass().getSimpleName() + ": " + e.getMessage());
            response.put("latenciaMs", System.currentTimeMillis() - startedAt);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
    }
}
