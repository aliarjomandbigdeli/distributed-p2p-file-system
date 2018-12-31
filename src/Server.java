import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Server implements Runnable{

    private DatagramSocket socket;
    private Session session;
    private int udpPacketSize;
    private int serverPort;

    public Server(Session s) throws SocketException {
        this.serverPort = 7654;
        this.udpPacketSize = 512;
        this.session = s;
        this.socket = new DatagramSocket(serverPort);
        System.out.println("server is listening for clients...");
    }

    @Override
    public void run() {
        while (true) {
            StringBuilder receivedMessage = listeningForBroadCastMessage();
            if (hasServerThisFile(receivedMessage))
                break;
        }
        System.out.println("going to send file");
    }

    private boolean hasServerThisFile(StringBuilder s){
        if(s.equals(session.getFileName()))
            return true;
        else
            return false;
    }

    private StringBuilder listeningForBroadCastMessage(){
        StringBuilder result;
        byte[] receive = new byte[udpPacketSize];
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

            result = data(receive);
            break;
        }
        return result;
    }

    private   StringBuilder data(byte[] a)
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
