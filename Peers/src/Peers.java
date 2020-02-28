//Roberto Olivera
import java.io.*;
import java.net.*;
import java.util.*;

public class Peers {
    public static InetAddress IP;
    public static DatagramSocket mySocket;
    public static DatagramPacket receivePacket;
    public static DatagramPacket sendPacket;
    private static ArrayList<String> pID = new ArrayList<>();
    private static ArrayList<String> pIP = new ArrayList<>();
    private static ArrayList<Integer> pPort = new ArrayList<>();
    public static final int sendPort = 55555;
    public static int count = 0;
    public static int pos = -1;
    public static int pSize;
    public static String chksm = "";
    
    public static void main(String[] args) throws SocketException, IOException {
        IP = InetAddress.getLocalHost(); //use this if running locally
        //IP = InetAddress.getByName("10.18.130.134"); //IPv4 of MainPeer (*bug* Only works with 1 peer)
        mySocket = new DatagramSocket();

        while(true){
            byte[] receiveBuffer = new byte[1024];
            byte[] sendBuffer = new byte[1024];
                       
            if(count == 0){
                String clientData = "reqJoin";
                sendBuffer = clientData.getBytes();        
                DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, IP, sendPort);
                mySocket.send(sendPacket);
                
                System.out.println("sent 'reqJoin' my  port is " + mySocket.getLocalPort() + "\n");
                count++; //count = 1
            }
            
            receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            mySocket.receive(receivePacket);
            int length = receivePacket.getLength();
            String serverData = new String(receivePacket.getData(), 0, length);
            
            if(serverData.equals("ACK")){ //just prints out the ACK
                System.out.println("PeerServer: " + serverData + "\n"); 
                
            }else if(serverData.substring(0, 1).equals("!")){ //adds itself to list
                addToLists(serverData);
                
                if(pos == -1){ //sets correct position of current peer
                    pos = pSize;
                    pos = pos - 1;
                }
 
            }else if(serverData.substring(0,1).equals("~")){ //update tables
                addNewPeerToList(serverData);
            }else if(serverData.substring(0,1).equals("0") || serverData.substring(0,1).equals("1")){
                chksm = serverData;
                //System.out.println("TEST: " + chksm);
                
            }else{
                byte [] udpChecksum = new byte[1024];
                udpChecksum = serverData.getBytes();
                String check = "";
                for(int i = 0; i < udpChecksum.length; i++){
                    check += String.format("%8s", Integer.toBinaryString(udpChecksum[i] & 0xFF)).replace(' ', '0');
                }
                if(check.equals(chksm)){
                    System.out.println("Message: " + serverData);
                    System.out.println("Checksum worked\n");
                    passItOn(serverData);
                }else{
                    passItOn(serverData);
                }
                
            }//end else           
        }//end infinite while      
    }//end main
    
    public static void addToLists(String data) throws IOException{
        String[] parts = data.split("!");
        String pInfo1 = "";

        for (int i = 0; i < parts.length; i++){
            pInfo1 += parts[i];
        }

        String[] parts2 = pInfo1.split(",");

        int j = 0;
        for(int i = 0; i < parts2.length ; i++){
            
            if(i % 3 == 0){
                j = 0;
            }

            switch (j) {
                case 0:
                    pID.add(parts2[i]);
                    break;
                case 1:
                    pPort.add(Integer.valueOf(parts2[i]));
                    break;
                case 2:
                    pIP.add(parts2[i]);
                    break;
                default:
                    System.out.println("default j is: " + j);
                    break;
            }//end switch

            j++;
        }//end for
        
        pSize = pID.size(); //update pSize
        
        if(parts2.length > 4){
           updatePeerTables(); 
        }
    }//end addToLists()
    
    public static void addNewPeerToList(String data){
        String[] parts = data.split(",");
                
        int j = 0;
        for(int i = 0; i < parts.length; i++){
            
            if(i % 3 == 0){
                j = 0;
            }
            
            switch (j) {
                case 0:
                    pID.add(parts[i]);
                    break;
                case 1:
                    pPort.add(Integer.valueOf(parts[i]));
                    break;
                case 2:
                    pIP.add(parts[i]);
                    break;
                default:
                    System.out.println("default j is: " + j + " you shouldn't be here");
                    break;
            }//end switch
            
            j++;
        }//end for
        
        pSize = pID.size(); //update pSize
    }//end addNewPeerToList
    
    public static void updatePeerTables() throws UnknownHostException, IOException{ //sends last joined peer to rest of peers
        byte[] sendBuffer  = new byte[1024];
        String updateString = "~" + pID.get(pID.size() - 1) + "," + pPort.get(pPort.size() - 1) + "," + pIP.get(pIP.size() - 1);
        
        for(int i = 0; i < pID.size() - 1; i++){
            sendBuffer = updateString.getBytes();
            sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getLocalHost(), pPort.get(i)); //InetAddress.getByName(pIP.get(i)) breaks
            mySocket.send(sendPacket);
        }
    }//end updatePerrTables()
    
    public static void passItOn(String msg) throws UnknownHostException, IOException{
        int nextPeer = pos + 1;

        if(nextPeer >= pSize){ //won't pass on message if there isn't a next peer
            System.out.println("No more peers to send to. All Peers have received the message.\n"); //commented out because can get annoying
        }else{
            byte[] sendBuffer  = new byte[1024];
            
            sendBuffer = msg.getBytes();
            String s1 = "";
            for(int i = 0; i < sendBuffer.length; i++){
                s1 += String.format("%8s", Integer.toBinaryString(sendBuffer[i] & 0xFF)).replace(' ', '0');
            }

            sendBuffer = s1.getBytes();
            sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getLocalHost(), pPort.get(nextPeer));
            mySocket.send(sendPacket);
             
            sendBuffer = msg.getBytes();
            sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getLocalHost(), pPort.get(nextPeer));
            mySocket.send(sendPacket);
        }
        
    }//end passItOn()
    
}// Peer2 class
