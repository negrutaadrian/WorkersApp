package demo.controller;

import demo.model.Worker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;

@Controller
public class WorkerController {
    private String hostname;
    private Worker self;

    @Value("${registry.url}")
    private String registryUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    public WorkerController() {
        this.hostname = System.getenv("HOSTNAME");
        String service = System.getenv("SERVICE");
        String portStr = System.getenv("PORT");

        if (this.hostname != null && service != null && portStr != null) {
            int port = Integer.parseInt(portStr);
            this.self = new Worker(hostname, service, port);
            signalRegistry();
        } else {
            this.self = null;
        }
    }

    private void signalRegistry() {
        if (this.self != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Worker> request = new HttpEntity<>(this.self, headers);

            try {
                restTemplate.postForEntity(registryUrl + "/api/registery/workers", request, Worker.class);
            } catch (Exception e) {
                System.err.println("Failed to signal registry: " + e.getMessage());
            }
        }
    }

    @Scheduled(fixedRate = 120000)
    public void sendHeartbeat() {
        if (this.self != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Worker> request = new HttpEntity<>(this.self, headers);

            try {
                restTemplate.postForEntity(registryUrl + "/api/registery/workers", request, Worker.class);
            } catch (Exception e) {
                System.err.println("Failed to send heartbeat: " + e.getMessage());
            }
        }
    }

    @PostMapping("/task")
    public ResponseEntity<String> handleTask() {
        System.out.println("Task received and processed by " + hostname);
        return ResponseEntity.ok("Task handled by " + hostname);
    }
}
