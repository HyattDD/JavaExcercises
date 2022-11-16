import java.io.BufferedReader;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.Socket;

public class TCPClient {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("127.0.0.1", 8888);
        System.out.println("Client is starting and sending file...");
        OutputStream out = socket.getOutputStream();
        Reader fileReader = new FileReader("../sendFiles/sent.txt");
        try (
            BufferedReader bufferReader = new BufferedReader(fileReader)
        ) {
            String buffer;
            while ((buffer = bufferReader.readLine()) != null) {
                buffer += "\n";
                out.write(buffer.getBytes());
            }
        }
        out.close();
        socket.close();
        System.out.println("File has been sent successfully.");
    }
}
