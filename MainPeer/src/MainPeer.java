//Roberto Olivera
import java.io.*;
import java.net.*;
import java.util.*;

public class MainPeer {
    public static final int PORT = 55555;
    public static DatagramSocket peerSocket;
    public static DatagramPacket rcvdPkt;
    public static DatagramPacket sendPacket;
    public static int count = 0;
    private static ArrayList<Integer> senders = new ArrayList<>();
    private static ArrayList<P2PClientConnection> peers = new ArrayList<P2PClientConnection>();
    public static String ack = "ACK";
    public static String info = "!";
    
    public static void main(String[] args) throws SocketException, IOException {
        peerSocket = new DatagramSocket(PORT);
        System.out.println("Listening for messages on port " + PORT + "...\n");

        while(true){
            byte[] receiveBuffer = new byte[1024];
            byte[] sendBuffer  = new byte[1024];
            
            rcvdPkt = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            peerSocket.receive(rcvdPkt);
            
            InetAddress IP = rcvdPkt.getAddress();
            int portNum = rcvdPkt.getPort();
            int length = rcvdPkt.getLength();
            String newsData = new String(rcvdPkt.getData(), 0, length);
            
            if(newsData.equals("reqJoin")){ //new peer is requesting to join
                peers.add(new P2PClientConnection(String.valueOf(count), portNum, IP)); //add new peer to "database" (seqNum, portNum, IP)
                sendBuffer = ack.getBytes(); //add ACK message to buffer
                
                //System.out.println(s1);
                sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, IP, portNum); //create packet
                peerSocket.send(sendPacket); //send ACK packet to new peer
                count++;
                info = getPeers(info); //get info on current connected peers
                sendBuffer = info.getBytes(); //add peer info to buffer
                sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, IP, portNum); //create packet
                peerSocket.send(sendPacket); //send peer info packet
                System.out.println("*New Peer " + portNum + " ACKd*\n"); //print out statemtnt that new peer has been ACKd       
            }else if(newsData.equals("bye")){ //might not need this, might delete later
                System.out.println("[" + portNum + "]: " + newsData); //display closing message
                System.out.println("\n*Sender " + portNum + " has left.*"); //display that a sender has left
                int temp = senders.indexOf(portNum);
                senders.remove(temp); //remove sender from known sender list
            }else{
                if(senders.contains(portNum)){ //if sender has sent messages before
                    if(newsData.equals("ls")){
                        if(peers.isEmpty()){
                            System.out.println("\nNo peers currently connected");
                        }else{
                            System.out.println("\nList of connected peers:");
                            
                            for(int i = 0; i < peers.size(); i++){
                                System.out.println(peers.get(i).getID() + " " + peers.get(i).getIP() + " " + peers.get(i).getPort());
                            }
                            
                            System.out.println("\n");
                        }                      
                    }else{
                        System.out.println("[" + portNum + "]: " + newsData);
                        sendToFirstPeer(newsData);
                    }   
                }else{ //if a new sender sends a message
                    System.out.println("\nReceiving news from IP: " + IP + "\nPort number: " + portNum);
                    senders.add(portNum);
                    
                    if(newsData.equals("ls")){
                        if(peers.isEmpty()){
                            System.out.println("\nNo peers currently connected");
                        }else{
                            System.out.println("\nList of connected peers:");
                            
                            for(int i = 0; i < peers.size(); i++){
                                System.out.println(peers.get(i).getID() + " " + peers.get(i).getIP() + " " + peers.get(i).getPort());
                            }
                            System.out.println("\n");
                        }                      
                    }else{
                        System.out.println("\n[" + portNum + "]: " + newsData);
                        sendToFirstPeer(newsData);
                    } 
                }
            }
        }//end infinite loop
    }//end main        

    public static void sendToFirstPeer(String data) throws IOException{
        if(!peers.isEmpty()){
            byte[] sBuffer = new byte[1024];
            
            sBuffer = data.getBytes();
            String s1 = "";
            for(int i = 0; i < sBuffer.length; i++){
                s1 += String.format("%8s", Integer.toBinaryString(sBuffer[i] & 0xFF)).replace(' ', '0');
            }
            
            sBuffer = s1.getBytes();
            sendPacket = new DatagramPacket(sBuffer, sBuffer.length, peers.get(0).getIP(), peers.get(0).getPort());
            peerSocket.send(sendPacket);
            
            sBuffer = data.getBytes();
            sendPacket = new DatagramPacket(sBuffer, sBuffer.length, peers.get(0).getIP(), peers.get(0).getPort());
            peerSocket.send(sendPacket);

        }
    }
    
    public static String getPeers(String pInfo){
        int size = peers.size();
        int i = size-1;
        
        System.out.println("Current peer pool size is: " + size);
        pInfo += peers.get(i).getID() + "," + peers.get(i).getPort() + "," + peers.get(i).getIP() + "!,";

        return pInfo;
    }
    
}//end Peer Class

