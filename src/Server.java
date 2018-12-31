import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Server implements Runnable{

    private DatagramSocket socket;
    private Seesion seesion;
    private int udpPacketSize;
    private int serverPort;

    public Server(Seesion s) throws SocketException {
        this.serverPort = 7654;
        this.udpPacketSize = 512;
        this.seesion = s;
        this.socket = new DatagramSocket(serverPort);


        System.out.println("server is listening for clients...");
    }

    @Override
    public void run() {
        byte[] receive = new byte[65535];

        DatagramPacket DpReceive = null;
        while (true)
        {

            // Step 2 : create a DatgramPacket to receive the data.
            DpReceive = new DatagramPacket(receive, receive.length);

            // Step 3 : revieve the data in byte buffer.
            try {
                socket.receive(DpReceive);
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Client:-" + data(receive));

            // Exit the server if the client sends "bye"
            if (data(receive).toString().equals("bye"))
            {
                System.out.println("Client sent bye.....EXITING");
                break;
            }

            // Clear the buffer after every message.
            receive = new byte[65535];
        }
    }

    public  StringBuilder data(byte[] a)
    {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0)
        {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    }
}
