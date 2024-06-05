package demo.model;

import jakarta.persistence.*;

@Entity
public class Worker {
    @Id
    private String hostname;
    private long lastHeartBeat;

    public Worker() {
    }
    public Worker(String hostname) {
        this.hostname = hostname;
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
}
