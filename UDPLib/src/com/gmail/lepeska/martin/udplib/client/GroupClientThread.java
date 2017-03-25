package com.gmail.lepeska.martin.udplib.client;

import com.gmail.lepeska.martin.udplib.util.ConfigLoader;
import com.gmail.lepeska.martin.udplib.DatagramTypes;
import com.gmail.lepeska.martin.udplib.AGroupThread;
import com.gmail.lepeska.martin.udplib.StoredMessage;
import com.gmail.lepeska.martin.udplib.UDPLibException;
import com.gmail.lepeska.martin.udplib.datagrams.ADatagram;
import com.gmail.lepeska.martin.udplib.datagrams.AccessRequestDatagram;
import com.gmail.lepeska.martin.udplib.datagrams.Datagrams;
import com.gmail.lepeska.martin.udplib.datagrams.IsAliveDatagram;
import com.gmail.lepeska.martin.udplib.datagrams.MessageDatagram;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Martin Lepeška
 */
public class GroupClientThread extends AGroupThread {

    /**
     * Users in group
     */
    private final ArrayList<GroupUser> groupUsers = new ArrayList<>();

    /**
     * Expected server address
     */
    private final InetAddress serverAddress;

    /**
     * Creates new GroupClientRunnable bound on interface of given hostAddress
     * with given password and server address.
     *
     * @param userName User's name in network
     * @param groupPassword Password required to access this group or null, if
     * none
     * @param serverAddress Address of group owner
     * @param port Port of server socket
     * @throws UnknownHostException
     */
    public GroupClientThread(String userName, String groupPassword, String serverAddress, int port) throws UnknownHostException {
        super(userName, groupPassword, port);
        this.serverAddress = InetAddress.getByName(serverAddress);
    }

    /**
     * Creates important components, which cannot be created in constructor,
     * before starting loop.
     */
    private void init() {
        try {
            socket = new MulticastSocket(port);
            sendSockets.add(socket);

            ADatagram data = new AccessRequestDatagram(encryptor, userName, groupPassword);
            sendDatagram(serverAddress, data);

            byte[] responseData = new byte[ADatagram.MAXIMUM_DATAGRAM_LENGTH];
            DatagramPacket response = new DatagramPacket(responseData, responseData.length);

            socket.setSoTimeout(ConfigLoader.getInt("dead-time", 2000));
            socket.receive(response);

            if (Datagrams.getDatagramType(response) == DatagramTypes.SERVER_ACCEPT_CLIENT_RESPONSE) {
                ADatagram responseDatagram = Datagrams.reconstructDatagram(encryptor, response);
                
                String[] responseSplit = responseDatagram.getStringMessage();

                if (responseSplit.length < 3) {
                    throw new UDPLibException("Received response with uncomplete data!" + Arrays.toString(responseSplit));
                }

                if (responseSplit[1].equals("1")) {
                    groupAddress = InetAddress.getByName(responseSplit[0]);
                    //no other way to know from which interface server contacted
                    hostAddress = InetAddress.getByName(responseSplit[2]);

                    NetworkInterface nic = NetworkInterface.getByInetAddress(hostAddress);
                    Logger.getLogger(GroupClientThread.class.getName()).log(Level.INFO, "Listening with {0}", nic);
                    socket.setNetworkInterface(nic);
                    socket.joinGroup(groupAddress);

                    socket.setSoTimeout(0);
                    //socket.setSoTimeout(ConfigLoader.getInt("dead-time", 2000)*10);

                    GroupUser me = new GroupUser(userName, hostAddress);

                    this.groupUsers.add(me);
                    listeners.stream().forEach((listener) -> {
                        listener.joined(me);
                    });
                } else {
                    throw new UDPLibException("Wrong password!");
                }
            } else {
                throw new UDPLibException("Received garbage response!");
            }
        } catch (SocketTimeoutException ex) {
            finishThread();
            Logger.getLogger(ConfigLoader.class.getName()).log(Level.SEVERE, "Server did not responded! ", ex);

            throw new UDPLibException("Server did not responded! ", ex);
        } catch (IOException | UDPLibException e) {
            finishThread();
            Logger.getLogger(ConfigLoader.class.getName()).log(Level.SEVERE, "Thread shut down! ", e);

            throw new UDPLibException("Thread shut down! ", e);
        }
    }

    @Override
    public void run() {
        init();

        while (!Thread.currentThread().isInterrupted() && socket != null) {
            try {
                byte[] buf = new byte[ADatagram.MAXIMUM_DATAGRAM_LENGTH];

                DatagramPacket packet = new DatagramPacket(buf, buf.length);

                socket.receive(packet);

                ADatagram datagram = Datagrams.reconstructDatagram(encryptor, packet);
                
                if (datagram != null) {
                    dealWithDatagram(packet, datagram);
                } else {
                    //trash    
                    Logger.getLogger(GroupClientThread.class.getName()).log(Level.WARNING, 
                            "Trash received: {0}", Arrays.toString(buf));
                }

            } catch (Exception e) {
                Logger.getLogger(GroupClientThread.class.getName()).log(Level.SEVERE, 
                        "Error when parsing datagram! ", e);
            }
        }

        finishThread();
    }

    @Override
    protected void childDealWithDatagram(DatagramPacket source, ADatagram datagram) {
        String[] messageSplit = datagram.getStringMessage();

        GroupUser user;
        try {
            switch (datagram.getType()) {
                case SERVER_CLIENTS_INFO:
                    user = findGroupUserbyInetAddr(InetAddress.getByName((messageSplit[1])));
                    if (user != null) {
                        user.setPingToHost(Long.parseLong(messageSplit[2]));
                    } else {
                        GroupUser newUser = new GroupUser(messageSplit[0], !messageSplit[1].equals("0.0.0.0") ? InetAddress.getByName(messageSplit[1]) : serverAddress);
                        newUser.setPingToHost(Long.parseLong(messageSplit[2]));
                        groupUsers.add(newUser);
                        listeners.stream().forEach((listener) -> {
                            listener.userJoined(newUser);
                        });
                    }
                    break;
                case SERVER_CLIENT_DEAD:
                    GroupUser kickedUser = findGroupUserbyInetAddr(InetAddress.getByName(messageSplit[1]));
                    if (kickedUser != null) {
                        groupUsers.remove(kickedUser);

                        if (kickedUser.name.equals(userName) && kickedUser.ip.equals(hostAddress)) {
                            finishThread();
                            throw new UDPLibException("Kicked out from group!");
                        } else {
                            listeners.stream().forEach((listener) -> {
                                listener.userKicked(kickedUser);
                            });
                        }
                    }
                    break;
                case SERVER_IS_ALIVE_REQUEST:
                    sendDatagram(serverAddress, new IsAliveDatagram(encryptor, false));
                    break;
                case CLIENT_UNICAST_MESSAGE:
                    user = findGroupUserbyInetAddr(source.getAddress());
                    if (!(user != null && user.name.equals(userName) && user.ip.equals(hostAddress))) { //discard own messages
                        addMessage(new StoredMessage(Datagrams.reconstructMessage(messageSplit, 0), user, false));
                    }
                    break;
                case CLIENT_MULTICAST_MESSAGE:
                    user = findGroupUserbyInetAddr(source.getAddress());
                    if (!(user != null && user.name.equals(userName) && user.ip.equals(hostAddress))) {
                        addMessage(new StoredMessage(Datagrams.reconstructMessage(messageSplit, 0), user, true));
                    }
                    break;
                case SERVER_UNICAST_MESSAGE:
                    addMessage(new StoredMessage(Datagrams.reconstructMessage(messageSplit, 0), findGroupUserbyInetAddr(source.getAddress()), false));
                    break;
                case SERVER_MULTICAST_MESSAGE:
                    addMessage(new StoredMessage(Datagrams.reconstructMessage(messageSplit, 0), findGroupUserbyInetAddr(source.getAddress()), true));
                    break;
            }
        } catch (NumberFormatException | UnknownHostException | UDPLibException e) {
            Logger.getLogger(ConfigLoader.class.getName()).log(Level.SEVERE, "Error when parsing datagram! ", e);
            if(messageSplit != null){
                Logger.getLogger(ConfigLoader.class.getName()).log(Level.SEVERE, "Error when parsing datagram! {0}", Arrays.toString(messageSplit));
            }
        }
    }

    @Override
    public void leave() {
        try{
            socket.setTimeToLive(1); //die immediatly
        }catch(Exception e){}
        
        this.interrupt();
    }

    @Override
    public void sendMessage(GroupUser target, String message) {
        ADatagram datagram = new MessageDatagram(encryptor, message, false, false);
        sendDatagram(target, datagram);
    }

    @Override
    public void sendMulticastMessage(String message) {
        ADatagram datagram = new MessageDatagram(encryptor, message, true, false);
        sendMulticastDatagram(datagram);
    }

    @Override
    public List<GroupUser> getCurrentGroupUsers() {
        LinkedList<GroupUser> groupUsersToReturn = new LinkedList<>();
        Collections.copy(groupUsersToReturn, groupUsers);

        return groupUsersToReturn;
    }

    /**
     * @param ip ip of user
     * @return user with given ip or null
     */
    private GroupUser findGroupUserbyInetAddr(InetAddress ip) {
        for (GroupUser user : groupUsers) {
            if (user.ip.getHostName().equals(ip.getHostName())) {
                return user;
            }
        }

        if (ip.getHostName().equals("0.0.0.0")) {
            return findGroupUserbyInetAddr(serverAddress);
        }

        return null;
    }
}
