//Roberto Olivera
import java.net.*;

public class P2PClientConnection {
    private String id;
    private int port;
    private InetAddress ip;
    
    public P2PClientConnection(String id, int port, InetAddress ip){
        this.id = id;
        this.port = port;
        this.ip = ip;
    }
    
    public String getID(){
        return id;
    }
    
    public int getPort(){
        return port;
    }
    
    public InetAddress getIP(){
        return ip;
    }
    
    @Override
    public String toString(){
        return getID() + " " + getPort() + getIP();
    }
}
