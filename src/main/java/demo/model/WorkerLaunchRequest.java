package demo.model;

public class WorkerLaunchRequest {
    private String workerName;
    private String service;
    private int port;

    public WorkerLaunchRequest(String workerName, String service, int port) {
        this.workerName = workerName;
        this.service = service;
        this.port = port;
    }

    // Getters and setters

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
