package demo.controller;

import demo.model.Worker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Random;

@RestController
public class LoadBalancer {
    /* Role of LoadBalancer is to receive requests on 2 urls: service/hello/nameWorker and service/chat */
    /* LoadBalancer will request worker for delegation of a work */
    /* If the worker will not respond, then the request is sent to a random */

    @Value("${registry.url}") 
    private String registryUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();

    /* Endpoint to set a worker */
    @PostMapping("/setWorker")
    public ResponseEntity<String> setWorker(@RequestBody Worker worker) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Worker> request = new HttpEntity<>(worker, headers);
            restTemplate.postForEntity(registryUrl + "/workers", request, Worker.class);
            return ResponseEntity.ok("Worker set successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add worker: " + e.getMessage());
        }
    }

    /* Implement just the first logic of the Monitoring part */
    @GetMapping("/hi")
    public ResponseEntity<String> delegateTask () {
        List<Worker> workers = getActiveWorkers();
        
        if (workers == null || workers.isEmpty()) {
            return new ResponseEntity<>("No workers available", HttpStatus.SERVICE_UNAVAILABLE);
        }

        return delegateTaskToWorker(workers);
    }
    
    private ResponseEntity<String> delegateTaskToWorker(List<Worker> workers) {
        /* If there is a worker, then assign to him  */
        while (!workers.isEmpty()) {
            Worker worker = workers.get(random.nextInt(workers.size()));
            try {
                ResponseEntity<String> response = restTemplate.getForEntity("http://" + worker.getHostname() + ":8081/task", String.class);
                // If there is a succesful assignement of the task for the worker, then return the response 
                if (response.getStatusCode().is2xxSuccessful()) {
                    return response;
                }
            } catch (Exception e) {
                System.err.println("Failed to delegate task to " + worker.getHostname() + ": " + e.getMessage());
                workers.remove(worker);
            }
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("All workers failed");
    }
    
    private List<Worker> getActiveWorkers() {
        /* Will return the list of active workers */
        try {
            ResponseEntity<List<Worker>> response = restTemplate.exchange(
                registryUrl + "/workers",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Worker>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            System.err.println("Failed to get workers list: " + e.getMessage());
            return null;
        }
    }

}
