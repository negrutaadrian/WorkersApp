package demo.controller;

import demo.model.Node;
import demo.model.Worker;
import demo.repository.NodeRepository;
import demo.repository.WorkerRepository;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/registery")
public class RegisteryController {

    @Autowired
    private WorkerRepository workersRepo;

    @Autowired
    private NodeRepository nodeRepo;



    @Transactional
    @GetMapping("/workers")
    public ResponseEntity<Object> getWorkers() {
        long currentTime = System.currentTimeMillis();
        List<Worker> activeWorkers = workersRepo.streamAllBy()
            .filter(worker -> currentTime - worker.getLastHeartbeat() < 120000)
            .collect(Collectors.toList());
        return new ResponseEntity<>(activeWorkers, HttpStatus.OK);
    }

    @PostMapping("/workers")
    public ResponseEntity<Worker> heartbeat(@RequestBody Worker worker) {
        Worker existingWorker = workersRepo.findById(worker.getHostname()).orElse(null);
        if (existingWorker != null) {
            existingWorker.setLastHeartbeat(System.currentTimeMillis());
            workersRepo.save(existingWorker);
        } else {
            worker.setLastHeartbeat(System.currentTimeMillis());
            workersRepo.save(worker);
        }
        return new ResponseEntity<>(worker, HttpStatus.OK);
    }

    @Scheduled(fixedRate = 60000)
    public void cleanUpWorkers() {
        long currentTime = System.currentTimeMillis();
        workersRepo.findAll().forEach(worker -> {
            if (currentTime - worker.getLastHeartbeat() > 120000) {
                workersRepo.delete(worker);
            }
        });
    }

    @GetMapping("/nodes")
    public ResponseEntity<List<Node>> getNodes() {
        List<Node> nodes = nodeRepo.findAll();
        return new ResponseEntity<>(nodes, HttpStatus.OK);
    }

    @PostMapping("/nodes")
    public ResponseEntity<Node> addNode(@RequestBody Node node) {
        System.out.println("Received node: Address = " + node.getAddress() + ", URL = " + node.getUrl());
        nodeRepo.save(node);
        return new ResponseEntity<>(node, HttpStatus.CREATED);
    }
}
