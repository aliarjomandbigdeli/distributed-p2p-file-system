import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Objects;

public class Client implements Runnable{

    private DatagramSocket socket;
    private Session session;
    private ArrayList<InetAddress> broadcastList; // this list contains broadcast ip address of each network interface

    public Client(Session s) throws SocketException {
        broadcastList = new ArrayList<>();
        getBroadCastIPs(broadcastList);
        socket = new DatagramSocket();
        socket.setBroadcast(true);
        this.session = s;
    }

    public void run() {
        sendBroadCastMessage();
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

    }

}
