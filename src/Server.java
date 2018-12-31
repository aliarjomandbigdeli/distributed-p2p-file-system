import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Server implements Runnable {

    private DatagramSocket socket;
    private Session session;
    private int udpPacketSize;
    private int serverPort;
    private byte fileContent[];

    private InetAddress clientIP;
    private int clientPort;


    public Server(Session s) throws SocketException {
        this.serverPort = 7654;
        this.udpPacketSize = 512;
        this.session = s;
        this.socket = new DatagramSocket(serverPort);
        this.clientIP = null;
        this.clientPort = -1;
        System.out.println("server is listening for clients...");
    }

    @Override
    public void run() {
        readFileFromDisk(); //read file as array of bytes from disk

        boolean shouldContinue = true;
        while (shouldContinue) {
            StringBuilder receivedMessage = listeningForBroadCastMessage();
            try {
                if (hasServerThisFile(receivedMessage) && makeAcknowledge())
                    break;
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
    }

    private boolean makeAcknowledge() throws IOException {
        boolean res = false;
        int numberOfPacket = (fileContent.length / 511) + 1;

        //1 - send number of packet to client
        String packetCount = "p:" + numberOfPacket;
        DatagramPacket sendPacket =
                new DatagramPacket(packetCount.getBytes(), packetCount.getBytes().length, clientIP, clientPort);

        socket.send(sendPacket);

        //2 - wait to receive "ok"
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
            break;
        }
        if(result.toString().equals("ok"))
            return true;
        else
            return false;
    }

    private void sendingFileToClient() throws IOException {
        int i = 0;
        while (i < 100){
            String packetCount = "p:" + i;
            DatagramPacket sendPacket =
                    new DatagramPacket(packetCount.getBytes(), packetCount.getBytes().length, clientIP, clientPort);

            socket.send(sendPacket);
            ++i;
        }
    }
}
