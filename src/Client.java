import java.io.IOException;
import java.net.*;

public class Client implements Runnable{

    private DatagramSocket socket;
    private InetAddress address;
    private String broadcastMessage;

    public Client() throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        socket.setBroadcast(true);
        address = InetAddress.getByName("255.255.255.255");
        broadcastMessage = "Hello";
    }

    public void run(){
        byte[] buffer = broadcastMessage.getBytes();

        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 4445);
        try {
            socket.send(packet);
        } catch (IOException e) {
            System.out.println("socket can not send data");
        }
    }
}
