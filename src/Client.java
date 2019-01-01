import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Objects;

public class Client implements Runnable {

    private DatagramSocket socket;
    private Session session;
    private ArrayList<InetAddress> broadcastList; // this list contains broadcast ip address of each network interface
    private InetAddress serverIP;
    private int serverPort;
    private int numberOfPacket;
    private int lasPacketSize;
    private int udpPacketSize;
    private ArrayList<byte[]> receivedSegments;

    public Client(Session s) throws SocketException {
        broadcastList = new ArrayList<>();
        getBroadCastIPs(broadcastList);
        socket = new DatagramSocket();
        socket.setBroadcast(true);
        this.session = s;
        udpPacketSize = 512;
        receivedSegments = new ArrayList<>();
    }

    public void run() {
        while (true) {
            try {
                sendBroadCastMessage();
            } catch (SocketException e) {
                e.printStackTrace();
            }
            try {
                if (makeAcknowledge())
                    break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        receivingFile();
        joinSegments();
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

    private void sendBroadCastMessage() throws SocketException {
        socket.setBroadcast(true);
        byte[] buffer = (session.getFileName()).getBytes();
        for (int j = 0; j < broadcastList.size(); j++) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, broadcastList.get(j), 7654);
            try {
                socket.send(packet);
            } catch (IOException e) {
                System.out.println("socket can not send data");
            }
        }
        socket.setBroadcast(false);
    }

    private void receivingFile() {
        ArrayList<Integer> helpIndex = new ArrayList<>();
        int limit = numberOfPacket;
        byte[] receive = new byte[udpPacketSize];
        DatagramPacket DpReceive = null;
        while (limit > 0) {
            // Step 2 : create a DatagramPacket to receive the data.
            DpReceive = new DatagramPacket(receive, receive.length);

            // Step 3 : receive the data in byte buffer.
            try {
                socket.receive(DpReceive);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(!helpIndex.contains(new Integer(receive[0] + 128))){
                helpIndex.add(new Integer(receive[0] + 128));
                receivedSegments.add(receive);
                limit --;
            }
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
        String s1 = s.substring(1, s.indexOf('l'));
        String s2 = s.substring(s.indexOf('l') + 1);
        try {
            numberOfPacket = Integer.parseInt(s1);
            lasPacketSize = Integer.parseInt(s2);
        } catch (NumberFormatException e) {
            serverPort = -1;
            serverIP = null;
            return false;
        }
        System.out.println(numberOfPacket);
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

    private void joinSegments(){
        receivedSegments.sort(new Comparator<byte[]>() {
            @Override
            public int compare(byte[] o1, byte[] o2) {
                return (new Integer(o1[0] + 128)).compareTo(new Integer(o2[0] + 128));
            }
        });
        byte res[] =  new byte[(numberOfPacket - 1) * (udpPacketSize - 1) + lasPacketSize];
        int k = 0;
        for (int i = 0; i < receivedSegments.size(); i++) {
            int fin  = (i == receivedSegments.size() - 1) ? lasPacketSize  + 1: receivedSegments.get(i).length;
            for (int j = 1; j < fin; j++) {
                res [k++] = receivedSegments.get(i)[j];
             }
        }
        try (FileOutputStream fos = new FileOutputStream(session.getPath())) {
            fos.write(res);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
