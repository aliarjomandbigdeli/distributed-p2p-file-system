import java.util.ArrayList;

/**
 * This class contains all data about two side of connection
 * also running mode of application, ip and port of two side and anything we must keep during session
 */
public class Session {
    private boolean isServer;
    private String path;
    private String fileName;


    public Session(boolean isServer){
        this.isServer = isServer;
    }

    public boolean isServer() {
        return isServer;
    }

    public void setServer(boolean server) {
        isServer = server;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
        if(!isServer){
            path = fileName;
        }
    }
}
