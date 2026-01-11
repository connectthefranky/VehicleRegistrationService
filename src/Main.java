import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            server.Server.startServer(args);
        } catch (IOException exception) {
            System.out.print("Server exception: " + exception.getMessage());
        }
    }
}
