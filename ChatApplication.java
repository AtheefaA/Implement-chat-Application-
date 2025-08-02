import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class ChatApplication {
    private static final int PORT = 12345;

    public static void main(String[] args) throws IOException {
        BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Enter 'server' to run as server or 'client' to run as client:");
        String mode = consoleInput.readLine();

        if ("server".equalsIgnoreCase(mode)) {
            runServer();
        } else if ("client".equalsIgnoreCase(mode)) {
            runClient();
        } else {
            System.out.println("Invalid input. Exiting...");
        }
    }

    private static void runServer() {
        System.out.println("Chat Server started...");
        ExecutorService pool = Executors.newFixedThreadPool(10);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                pool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
            ) {
                out.println("Welcome to the chat server! Type 'exit' to quit.");
                String message;
                while ((message = in.readLine()) != null) {
                    if ("exit".equalsIgnoreCase(message)) {
                        out.println("Goodbye!");
                        break;
                    }
                    System.out.println("Received: " + message);
                    out.println("Server Echo: " + message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                    System.out.println("Client disconnected");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void runClient() {
        System.out.println("Connecting to the chat server...");
        try (
            Socket socket = new Socket("localhost", PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))
        ) {
            System.out.println("Connected to the chat server");
            System.out.println(in.readLine()); // Welcome message from server

            String userInput;
            while ((userInput = consoleInput.readLine()) != null) {
                out.println(userInput);
                if ("exit".equalsIgnoreCase(userInput)) {
                    break;
                }
                System.out.println(in.readLine()); // Response from server
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
