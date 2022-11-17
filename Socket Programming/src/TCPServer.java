import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(8888);
        System.out.println("Server is starting and listening...");
        Socket socket = serverSocket.accept();
        try (
            BufferedReader bufferedReader = 
            new BufferedReader(new InputStreamReader(socket.getInputStream())); 
            FileOutputStream outputFile = 
            new FileOutputStream("../receFiles/receive.txt");
        ) {
            String buffer;
            while ((buffer = bufferedReader.readLine()) != null) {
                outputFile.write(buffer.getBytes()); 
                outputFile.write('\n');
            }
        }
        serverSocket.close();
        socket.close();
        System.out.println("File is received successfully.");
    }
}
