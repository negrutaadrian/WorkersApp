package demo.controller;

import demo.model.Worker;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/*

- Workers register themselves by sending a POST request to the registery every 2 minutes to indicate
they are alive

- The registery maintains a list of available workers and provides this list to the load balancer 

- The registry should clean up workers that have not sent a heartbeat in the last 2 minutes 
*/


@RestController
@RequestMapping("/workers")
public class RegisteryController {
    @Autowired
    private WorkerRepository workersRepo;

    @Transactional
    @GetMapping()
    public ResponseEntity<Object> getWorkers() {
        /* Will rertieve all the active workers from workersRepo when a HTTP GET Method is received*/
        long currentTime = System.currentTimeMillis();
        List <Worker> activeWorkers = workersRepo.streamAllBy().filter (worker -> currentTime - worker.getLastHeartbeat() < 120000).collect(Collectors.toList());
        return new ResponseEntity<>(activeWorkers, HttpStatus.OK);
        
    }

    /* Will handle the logic that if a new worker is created and registery receive the first alive sign from it
       then it will be added to the workersRepo in order to be also available for the load balancer */
    
    @PostMapping()
    public ResponseEntity<Worker> heartbeat(@RequestBody Worker worker) {
        /* It will handle the /setWorker from LoadBalancer that will create a new worker in the repository  */        
        /* Is the worker that will send a post method every 2 minutes to the /register */
        Worker existingWorker = workersRepo.findById(worker.getHostname()).orElse(null);
        if (existingWorker != null) {
            existingWorker.setLastHeartbeat(System.currentTimeMillis());
            workersRepo.save(existingWorker);
        } else {
            worker.setLastHeartbeat(System.currentTimeMillis());
            worker.setService(worker.getService());
            workersRepo.save(worker);
        }
        return new ResponseEntity<>(worker, HttpStatus.OK);
    }

    @Scheduled(fixedRate = 60000) // method executed every minute 
    public void cleanUpWorkers() {
        long currentTime = System.currentTimeMillis();
        workersRepo.findAll().forEach(worker -> {
            if (currentTime - worker.getLastHeartbeat() > 120000) { // supasses 2 minutes
                workersRepo.delete(worker); // the worker is no more active
            }
        });
    }

}
