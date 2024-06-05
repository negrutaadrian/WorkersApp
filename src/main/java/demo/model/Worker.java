package demo.model;

import jakarta.persistence.*;

@Entity
public class Worker {
    @Id
    private String hostname;
    private long lastHeartBeat;
    private String service;
    private int port; 
    
    @ManyToOne
    @JoinColumn(name = "node_id")
    private Node node; 



    // rajouter port 
    public Worker() {
    }
    public Worker(String hostname, String service, int port) {
        this.hostname = hostname;
        this.service = service;
        this.port = port;
        this.lastHeartBeat = System.currentTimeMillis();
    }


    /* Getters and setters */
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

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }


    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
