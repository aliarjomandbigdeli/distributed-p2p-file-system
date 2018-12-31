import java.io.IOException;
import java.net.*;

public class Client implements Runnable{

    private DatagramSocket socket;
    private InetAddress address;
    private String broadcastMessage;
    private Seesion seesion;

    public Client(Seesion s) throws SocketException, UnknownHostException {
        socket = new DatagramSocket();
        socket.setBroadcast(true);
        address = InetAddress.getByName("255.255.255.255");

        this.seesion = s;
        broadcastMessage = "Hello";
    }

    public void run() {
        int i = 0;
        while (i < 1000){
            byte[] buffer = (broadcastMessage + i).getBytes();

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 4445);
            try {
                socket.send(packet);
            } catch (IOException e) {
                System.out.println("socket can not send data");
            }
            ++i;
        }
        System.out.printf("close");
        socket.close();
    }


}
