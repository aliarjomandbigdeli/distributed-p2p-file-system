import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Objects;

public class Client implements Runnable{

    private DatagramSocket socket;
    private Session session;
    private ArrayList<InetAddress> broadcastList; // this list contains broadcast ip address of each network interface
    private InetAddress serverIP;
    private int serverPort;
    private int numberOfPacket;
    private int udpPacketSize;

    public Client(Session s) throws SocketException {
        broadcastList = new ArrayList<>();
        getBroadCastIPs(broadcastList);
        socket = new DatagramSocket();
        socket.setBroadcast(true);
        this.session = s;
        udpPacketSize = 512;
    }

    public void run() {
        while (true) {
            sendBroadCastMessage();
            try {
                if(makeAcknowledge())
                    break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        receivingFile();
        System.out.println("Connection will be closed...");
        socket.close();
    }


    private void getBroadCastIPs(ArrayList<InetAddress> ipList) throws SocketException {
        Enumeration<NetworkInterface> interfaces
                = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();

            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue;
            }

            networkInterface.getInterfaceAddresses().stream()
                    .map(a -> a.getBroadcast())
                    .filter(Objects::nonNull)
                    .forEach(ipList::add);
        }
    }

    private void sendBroadCastMessage(){
        int i = 0;
        while (i < 1000){
            byte[] buffer = (session.getFileName()).getBytes();
            for (int j = 0; j < broadcastList.size(); j++) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, broadcastList.get(j), 7654);
                try {
                    socket.send(packet);
                } catch (IOException e) {
                    System.out.println("socket can not send data");
                }
            }
            ++i;
        }
    }

    private void receivingFile(){
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
            System.out.println(result);
            receive = new byte[udpPacketSize];
        }
    }

    private boolean makeAcknowledge() throws IOException {

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
            serverIP = DpReceive.getAddress();
            serverPort = DpReceive.getPort();
            result = data(receive);
            break;
        }
        String s = result.toString();
        s = s.substring(1);
        try {
            numberOfPacket = Integer.parseInt(s);
        }catch (NumberFormatException e) {
            serverPort = -1;
            serverIP = null;
            return false;
        }
        System.out.println(numberOfPacket);
        String answer = "ok";
        String packetCount = "ok";
        DatagramPacket sendPacket =
                new DatagramPacket(packetCount.getBytes(), packetCount.getBytes().length, serverIP, serverPort);

        socket.send(sendPacket);
        return true;
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

}
