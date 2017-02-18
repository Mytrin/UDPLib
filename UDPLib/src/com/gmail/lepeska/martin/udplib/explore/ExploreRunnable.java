package com.gmail.lepeska.martin.udplib.explore;

import com.gmail.lepeska.martin.udplib.DatagramTypes;
import com.gmail.lepeska.martin.udplib.UDPLibException;
import com.gmail.lepeska.martin.udplib.datagrams.ADatagram;
import com.gmail.lepeska.martin.udplib.datagrams.Datagrams;
import com.gmail.lepeska.martin.udplib.datagrams.explore.ExploreRequest;
import com.gmail.lepeska.martin.udplib.util.ConfigLoader;
import com.gmail.lepeska.martin.udplib.util.Encryptor;
import java.net.DatagramPacket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * This simple task scans on all networks fro UDPLib servers using either given or default groupAddress and port.
 * 
 * @author Martin Lepeška
 */
public class ExploreRunnable implements Runnable{

    private MulticastSocket socket;
    private final int port;
    private final InetAddress groupAddress;
    private final IExploreListener listener;
    
    /**
     * Creates new ExploreRunnable searching for servers with default parameters
     * @param listener object, which should receive evaluated data
     * @throws java.net.UnknownHostException
     */
    public ExploreRunnable(IExploreListener listener) throws UnknownHostException, UDPLibException{
        this(ConfigLoader.getString("default-group", "225.226.227.228"), ConfigLoader.getInt("default-port", 52511), listener);
    }

    /**
     * Creates new ExploreRunnable searching for servers with given parameters
     * @param groupAddress group to scan
     * @param port port, on which should be wanted servers listening
     * @param listener object, which should receive evaluated data
     * @throws UnknownHostException
     * @throws UDPLibException 
     */
    public ExploreRunnable(String groupAddress, int port, IExploreListener listener) throws UnknownHostException, UDPLibException{
        if(ConfigLoader.isConfigLoaded()){
            this.groupAddress = InetAddress.getByName(groupAddress);
            this.port = port;
            this.listener = listener;
        }else{
            throw new UDPLibException("Config was not loaded yet!");
        }
    }
    
    @Override
    public void run(){
        
        try{
            socket = new MulticastSocket(port);

            ADatagram exploreDatagram = new ExploreRequest();
            DatagramPacket packet = exploreDatagram.createPacket(groupAddress, port);

            //send multicast on all posible interfaces
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface nic : Collections.list(nets)){
                    if(!nic.isLoopback() && nic.supportsMulticast() && nic.isUp()){
                        Enumeration<InetAddress> addresses = nic.getInetAddresses();
                        
                        while(addresses.hasMoreElements()){
                            InetAddress address = addresses.nextElement();
                            if(!address.getHostName().equals("0.0.0.0") && !address.isLinkLocalAddress() && !address.isLoopbackAddress() && !(address instanceof Inet6Address)){
                                socket.setNetworkInterface(nic);
                                socket.send(packet);
                            }
                        }
                    }
            }
            
            //wait for deadTime * 2 for responses
            int waitingTime = ConfigLoader.getInt("dead-time", 5000) * 2;
            long start = System.currentTimeMillis();
            long elapsed = 0;
            
            
            byte[] responseData = new byte[ADatagram.MAXIMUM_DATAGRAM_LENGTH];
            DatagramPacket response = new DatagramPacket(responseData, responseData.length);
            socket.setSoTimeout(waitingTime);
            
            while(elapsed < waitingTime){
                try{
                    socket.receive(response);
                    ADatagram responseDatagram = Datagrams.reconstructDatagram(Encryptor.DUMMY, response);
                    if(responseDatagram != null && responseDatagram.getType() == DatagramTypes.SERVER_EXPLORE_RESPONSE){
                        
                        AvailableServerRecord record = new AvailableServerRecord(groupAddress, port, 
                                response.getAddress(), responseDatagram.getStringMessage()[0].equals("1"));
                        if(listener != null){
                            listener.receive(record);
                        }
                    }
                    
                    elapsed = System.currentTimeMillis() - start;
                    if(elapsed < waitingTime){
                        socket.setSoTimeout(waitingTime - (int)elapsed);
                        
                        responseData = new byte[ADatagram.MAXIMUM_DATAGRAM_LENGTH];
                        response = new DatagramPacket(responseData, responseData.length);
                    }                    
                }catch(Exception e){
                    //time run out
                    elapsed = waitingTime;
                }
            }
        }catch(Exception e){
            Logger.getLogger(ConfigLoader.class.getName()).log(Level.WARNING, "Exception when exploring: {1}", e);
        }
        
        if(socket != null){
            socket.close();
        }
        
        if(listener != null){
            listener.finished();
        }
    }
    
}