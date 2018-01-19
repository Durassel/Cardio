package cardio.cardio;

/**
 * Created by Administrateur on 17-Jan-18.
 */

class Server {
    private String ipAddress;
    private static final Server ourInstance = new Server();

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    static Server getInstance() {
        return ourInstance;
    }

    private Server() {
    }
}
