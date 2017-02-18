package com.gmail.lepeska.martin.udplib.files;

import com.gmail.lepeska.martin.udplib.UDPLibException;
import com.gmail.lepeska.martin.udplib.datagrams.ADatagram;
import com.gmail.lepeska.martin.udplib.datagrams.files.FileShareTextPart;
import com.gmail.lepeska.martin.udplib.server.GroupServerThread;
import com.gmail.lepeska.martin.udplib.util.ConfigLoader;
import com.gmail.lepeska.martin.udplib.util.Encryptor;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Class representing text file, which will be divided into segments and shared
 * to GroupNetwork.
 *
 * @author Martin Lepeška
 */
public class ServerSharedTextFile extends AServerSharedFile {

    /**
     * List of supported text files, if your format is not listed here, you can
     * change it
     */
    public static String TEXT_FILES = ".*\\.(txt|xml|json|js|html|htm|php|java|css|doc|docx|odt|xls|csv|py|lua|svg|conf|log|ini|yaml)";

    private final ArrayList<String> parts = new ArrayList<>();

    /**
     * @param fileToShare Data to share
     * @param name Unique String ID, under which will be file accessible at
     * AGroupNetwork class
     * @param groupServer Thread responsible for sending datagrams to group
     * @param encryptor Class responsible for encrypting file content on network
     * @param deadTime Time, which will server wait after finishing file
     * sharing. At this time client may request resending some of the parts
     * @param listener can be null, object which will be notified about
     * success/fail
     */
    public ServerSharedTextFile(File fileToShare, String name, GroupServerThread groupServer, Encryptor encryptor, int deadTime, IServerShareListener listener) {
        super(fileToShare, name, groupServer, encryptor, deadTime, listener);
    }

    /**
     * Makes sure, that lines are not too large to be sent across network by
     * splitting the large ones
     */
    private void validate() {
        for (int i = 0; i < parts.size(); i++) {
            String line = parts.get(i);
            while (line.length() > ADatagram.MAXIMUM_DATAGRAM_LENGTH / 2) {
                String cutted = line.substring(line.length() / 2);
                line = line.substring(0, line.length() / 2);

                parts.set(i, line);
                parts.add(i + 1, cutted);
            }
        }
    }

    @Override
    protected void readFile() throws IOException, UDPLibException {
        Path path = fileToShare.toPath();

        if (fileToShare.getName().matches(TEXT_FILES)) {
            Files.lines(path, StandardCharsets.UTF_8).forEach((String t) -> {
                parts.add(t + "\n");
            });

            validate();
        } else {
            throw new UDPLibException("Cannot send binary file");
        }
    }

            
            
    @Override
    protected void sendPartDatagrams() throws InterruptedException{
       ADatagram datagram;
            
        int waitingTime = ConfigLoader.getInt("file-time", 5);

        for(int i = 0; i < parts.size(); i++){
            datagram = new FileShareTextPart(encryptor, name, parts.get(i), i, parts.size());
            groupServer.sendMulticastDatagram(datagram);
            Thread.sleep(waitingTime);
        }
    }

    @Override
    public void partRequest(int index) {
        wasRequest = true;
        ADatagram datagram = new FileShareTextPart(encryptor, name, parts.get(index), index, parts.size());
        groupServer.sendMulticastDatagram(datagram);
    }
    
    /**
     * @param part Datagram part data to be sent
     * @return checksum of String without \n (Thank you, encodings for wasting hours of my time!)
     */
    public static int getChecksum(String part){
        return part.replaceAll("\n", "").length();
    }
}