import jdk.jfr.Unsigned;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class Server implements Runnable {

    private DatagramSocket socket;
    private Session session;
    private int udpPacketSize;
    private int serverPort;
    private byte fileContent[];
    private ArrayList<byte[]> segments;
    private int packetCounts;
    private InetAddress clientIP;
    private int clientPort;


    public Server(Session s) throws SocketException {
        this.serverPort = 7654;
        this.udpPacketSize = 512;
        this.session = s;
        this.socket = new DatagramSocket(serverPort);
        this.clientIP = null;
        this.clientPort = -1;
        segments = new ArrayList<>();
        System.out.println("server is listening for clients...");
    }

    @Override
    public void run() {
        readFileFromDisk(); //read file as array of bytes from disk
        segmentationFile();

        boolean shouldContinue = true;
        while (shouldContinue) {
            StringBuilder receivedMessage = listeningForBroadCastMessage();
            try {
                if (hasServerThisFile(receivedMessage) && makeAcknowledge()) {
                    shouldContinue = false;
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("try to listen to another");
            clientPort = -1;
            clientIP = null;
        }
        System.out.println("going to send file");
        try {
            sendingFileToClient();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean hasServerThisFile(StringBuilder s) {
        if (s.toString().equals(session.getFileName()))
            return true;
        else
            return false;
    }

    private StringBuilder listeningForBroadCastMessage() {
        StringBuilder result;
        byte[] receive = new byte[udpPacketSize];
        DatagramPacket DpReceive = null;
        while (true) {
            // Step 2 : create a DatagramPacket to receive the data.
            DpReceive = new DatagramPacket(receive, receive.length);

            // Step 3 : receive the data in byte buffer.
            try {
                socket.receive(DpReceive);
            } catch (IOException e) {
                e.printStackTrace();
            }

            result = data(receive);
            clientIP = DpReceive.getAddress();
            clientPort = DpReceive.getPort();
            break;
        }
        return result;
    }

    private StringBuilder data(byte[] a) {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0) {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    }

    private void readFileFromDisk() {
        File file = new File(session.getPath());
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(file);
            fileContent = new byte[(int) file.length()];
            fin.read(fileContent);
        } catch (FileNotFoundException e) {
            System.out.println("File not found" + e);
        } catch (IOException ioe) {
            System.out.println("Exception while reading file " + ioe);
        } finally {
            // close the streams using close method
            try {
                if (fin != null) {
                    fin.close();
                }
            } catch (IOException ioe) {
                System.out.println("Error while closing stream: " + ioe);
            }
        }
        int numberOfPacket = (fileContent.length / (udpPacketSize - 1)) + 1;
        this.packetCounts = numberOfPacket;
    }

    private boolean makeAcknowledge() throws IOException {
        //1 - send number of packet to client
        int remainLast = (packetCounts * (udpPacketSize - 1 ) - fileContent.length);
        String packetCount = "p" + packetCounts + "l" + remainLast;
        DatagramPacket sendPacket =
                new DatagramPacket(packetCount.getBytes(), packetCount.getBytes().length, clientIP, clientPort);

        socket.send(sendPacket);

        //2 - wait to receive "ok"
        StringBuilder result = new StringBuilder("");
        byte[] receive = new byte[udpPacketSize];
        DatagramPacket DpReceive = null;
        int limit = 256;
        int i = 0;
        socket.close();
        socket = new DatagramSocket(serverPort);

        // Step 2 : create a DatagramPacket to receive the data.
        DpReceive = new DatagramPacket(receive, receive.length);

        // Step 3 : receive the data in byte buffer.
        try {
            socket.receive(DpReceive);
        } catch (IOException e) {
            e.printStackTrace();
        }

        result = data(receive);


        if (result.toString().equals("ok"))
            return true;
        else
            return false;
    }

    private void sendingFileToClient() throws IOException {
        for (int i = 0; i < segments.size(); i++) {
            DatagramPacket sendPacket =
                    new DatagramPacket(segments.get(i), segments.get(i).length , clientIP, clientPort);
            socket.send(sendPacket);
        }
    }

    private void segmentationFile() {
        byte [] bytes;
        int init = 0,fin = 0;
        for (int i = 0; i < packetCounts; i++) {
            bytes = new byte[udpPacketSize];
            bytes[0] = new Integer(i - 128).byteValue();
            init = (udpPacketSize) * i;
            if(i == (packetCounts - 1))
                fin = fileContent.length;
            else
                fin = (udpPacketSize) * (i + 1);

            for (int j = init, k = 1; j < fin; j++, ++k) {
                bytes[k] = fileContent[j];
            }
            segments.add(bytes);
        }
    }
}
