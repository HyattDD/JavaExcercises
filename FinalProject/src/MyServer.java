import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MyServer implements Runnable {
    private String remoteRepoPath;
    MyServer(String remoteRepoPath) {
        this.remoteRepoPath = remoteRepoPath;
    }

    @Override
    public void run() {
        try {
            TCPServer(remoteRepoPath);
        } catch (Exception e) {
            System.out.println("Sorry, failed to run receive server...");
        }
    }

    public static void TCPServer(String remoteRepoPath) throws Exception{
        ServerSocket serverSocket = new ServerSocket(8888);
        System.out.println("Server is starting and listening...");
        Socket socket = serverSocket.accept();
        try (
            // Create the input and output streams
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = new FileOutputStream(new File(remoteRepoPath));
        ) {
            // Create a buffer to read the file in chunks
            byte[] buffer = new byte[1024];
            // Read the file and write it to the output stream
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        serverSocket.close();
        socket.close();
        System.out.println("File is received successfully.");
    }
}
