package demo.controller;

import demo.model.Node;
import demo.model.WorkerLaunchRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/api/server")
public class Server {

    @Value("${registry.url}")
    private String registryUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final int BASE_PORT = 8000;

    @GetMapping("/launch/{service}")
    public ResponseEntity<String> launchWorkers(@PathVariable String service, @RequestParam int nbw) {
        // Get the list of available nodes from the registry
        ResponseEntity<List<Node>> response = restTemplate.exchange(
            registryUrl + "/api/registery/nodes",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Node>>() {}
        );

        List<Node> nodes = response.getBody();
        if (nodes == null || nodes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No nodes available");
        }

        for (int i = 0; i < nbw; i++) {
            Node node = nodes.get(i % nodes.size()); // Distribute workers evenly across nodes
            String workerName = service + "-worker-" + i;
            int port = BASE_PORT + i; 
            WorkerLaunchRequest workerLaunchRequest = new WorkerLaunchRequest(workerName, service, port);
            try {
                String nodeUrl = node.getUrl() + "/api/node/launchWorker";
                restTemplate.postForObject(nodeUrl, workerLaunchRequest, String.class);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to launch worker: " + e.getMessage());
            }
        }
        return ResponseEntity.ok("Workers launched successfully");
    }
}
