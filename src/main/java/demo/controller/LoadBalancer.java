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
import java.util.stream.Collectors;

@RestController
public class LoadBalancer {
    /* Role of LoadBalancer is to receive requests on 2 urls: service/hello/nameWorker and service/chat */
    /* LoadBalancer will request worker for delegation of a work */
    /* If the worker will not respond, then the request is sent to a random */


    @Value("${registry.url}") 
    private String registryUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();

    /* Endpoint to set a worker (change to take a list of workers that will be instanciated) */
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

    /* 

    If it will not find the worker with the specified name 
    If not found -> find a random worker that is responsive for the asked service 

    */
    @GetMapping("/service/hello")
    public ResponseEntity<String> delegateHelloTask() {
        List<Worker> workers = getActiveWorkers().stream()
                .filter(worker -> "hello".equals(worker.getService()))
                .collect(Collectors.toList());

        if (workers == null || workers.isEmpty()) {
            return new ResponseEntity<>("No workers available for hello service", HttpStatus.SERVICE_UNAVAILABLE);
        }

        Worker selectedWorker = workers.get(random.nextInt(workers.size()));
        return new ResponseEntity<>("Hello, " + selectedWorker.getHostname(), HttpStatus.OK);
    }

    // Endpoint to delegate chat task to a worker
    @GetMapping("/service/chat")
    public ResponseEntity<String> delegateChatTask(@RequestBody String message) {
        List<Worker> workers = getActiveWorkers().stream()
                .filter(worker -> "chat".equals(worker.getService()))
                .collect(Collectors.toList());

        if (workers == null || workers.isEmpty()) {
            return new ResponseEntity<>("No workers available for chat service", HttpStatus.SERVICE_UNAVAILABLE);
        }

        Worker selectedWorker = workers.get(random.nextInt(workers.size()));
        return delegateTaskToWorker(selectedWorker, message);
    }

    private ResponseEntity<String> delegateTaskToWorker(Worker worker, String taskPayload) {
        try {
            String url = "http://" + worker.getHostname() + ":8081/task";
            ResponseEntity<String> response = restTemplate.postForEntity(url, taskPayload, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response;
            }
        } catch (Exception e) {
            System.err.println("Failed to delegate task to " + worker.getHostname() + ": " + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Failed to delegate task to worker: " + worker.getHostname());
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
