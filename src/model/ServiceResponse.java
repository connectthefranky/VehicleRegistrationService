package model;

public class ServiceResponse {
    private final int status;
    private final String body;

    public ServiceResponse(int status, String body) {
        this.status = status;
        this.body = body;
    }

    public int getStatus() {
        return status;
    }

    public String getBody() {
        return body;
    }
}
