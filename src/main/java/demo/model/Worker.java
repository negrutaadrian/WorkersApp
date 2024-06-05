package demo.model;

import jakarta.persistence.*;

@Entity
public class Worker {
    @Id
    private String hostname;
    private long lastHeartBeat;
    private String service;

    public Worker() {
    }
    public Worker(String hostname, String service ) {
        this.hostname = hostname;
        this.service = service;
        this.lastHeartBeat = System.currentTimeMillis();
    }

    public String getHostname() {
        return hostname;
    }
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public long getLastHeartbeat() {
        return lastHeartBeat;
    }
    
    public void setLastHeartbeat(long lastHeartBeat) {
        this.lastHeartBeat = lastHeartBeat;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }


}
