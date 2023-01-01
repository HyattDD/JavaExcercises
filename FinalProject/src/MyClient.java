import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MyClient implements Runnable{
    private String localPath;

    MyClient (String localPath) {
        this.localPath = localPath;
    }

    @Override
    public void run() {
        try {
            TCPClient(localPath);
        } catch (Exception e) {
            System.out.println("Sorry, failed to run sent client while sending files");
        }
    }

    public static void TCPClient(String localPath) throws Exception{
        Socket socket = new Socket("127.0.0.1", 8888);
        System.out.println("Transfering file...");
        
        // Create the input and output streams
        File file = new File(localPath);
        InputStream inputStream = new FileInputStream(file);
        OutputStream outputStream = socket.getOutputStream();
        // Create a buffer to read the file in chunks
        byte[] buffer = new byte[1024];

        // Read the file and write it to the output stream
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        inputStream.close();
        outputStream.close();
        socket.close();
        System.out.println("Transfered files successfully.");
    }
}
