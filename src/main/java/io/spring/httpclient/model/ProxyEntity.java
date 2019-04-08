package io.spring.httpclient.model;

public class ProxyEntity{
    private int id;
    private String host;
    private int port;
    private String username = "";
    private String password = "";
    private String proto;
    private String availability;

    public ProxyEntity() {

    }

    public ProxyEntity(String host, int port) {
        this.host = host;
        this.port = port;
        this.proto = ProxyType.HTTP.name().toLowerCase();
        this.username = "";
        this.password = "";
    }

    public ProxyEntity(String host, int port, String proto) {
        this.host = host;
        this.port = port;
        this.proto = proto;
        this.username = "";
        this.password = "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProto() {
        return proto;
    }

    public void setProto(String proto) {
        this.proto = proto;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
