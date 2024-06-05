package demo.controller;

import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import demo.model.WorkerLaunchRequest;

@RestController
@RequestMapping("/api/node")
public class NodeController {

    @PostMapping("/launchWorker")
    public String launchWorker(@RequestBody WorkerLaunchRequest request) {
        String command = String.format(
            "docker run -d -e HOSTNAME=%s -e SERVICE=%s -e PORT=%d -p %d:80 my-worker-image --name %s",
            request.getWorkerName(), request.getService(), request.getPort(), request.getPort(), request.getWorkerName()
        );

        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
            return output.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to launch worker: " + e.getMessage();
        }
    }
}
