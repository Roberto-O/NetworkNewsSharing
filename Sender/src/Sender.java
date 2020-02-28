//Roberto Olivera
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Sender {
    public static BufferedReader clientRead;
    public static String username;
    public static InetAddress IP;
    public static DatagramSocket senderSocket;
    public static final int portNumber = 55555;
    public static String lastCommand = new String();
    
    public static void main(String[] args) throws SocketException, IOException {
        System.out.println("Send news to peers...");
        
        clientRead = new BufferedReader(new InputStreamReader(System.in));
        IP = InetAddress.getByName("localhost"); //use this if running locally
        //IP = InetAddress.getByName("10.18.40.44"); //anyone can be a sender, just enter whoever's running MainPeer's IPv4 Address
        senderSocket = new DatagramSocket();
      
        while(true){
            byte[] sendBuffer = new byte[1024];
      
            System.out.print("\nMessage: ");
            String clientData = clientRead.readLine();
            sendBuffer = clientData.getBytes();        
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, IP, portNumber);
            senderSocket.send(sendPacket);
            
            if(clientData.equalsIgnoreCase("bye")){
                System.out.println("Connection ended by client");
                break;
            }
            
        }//end while
        senderSocket.close();
    }
}
