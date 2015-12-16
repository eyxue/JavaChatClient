package uk.ac.cam.eyx20.fjava.tick1;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Instant messaging client.
 * @author Elise
 *
 */
public class StringChat {
    /**
     * Instant messaging client, connects to server.
     * @param args : requires two arguments, a hostname and a port number.
     */
    public static void main(String[] args) {

        String server = null;
        int port = 0;
        // 's' is final because we want the socket to be constant. Because we are running
        // multiple threads, there is a risk that the socket could change and we may end
        // up reading/writing to the wrong server or disrupting the data streams.
        final Socket s;
        DataOutputStream os;
        DataInputStream is;

        try {
            assert (args.length == 2);
            server = args[0];
            port = Integer.parseInt(args[1]);
            s = new Socket(server, port);
            os = new DataOutputStream(s.getOutputStream());
            is = new DataInputStream(s.getInputStream());
            
        } catch (IOException e) {
            System.err.println("Cannot connect to " + server + " on port " + port);
            return;
        } catch (AssertionError e) {
            System.err.println("This application requires two arguments: <machine> <port>");
            return;
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("This application requires two arguments: <machine> <port>");
            return;
        } catch (NumberFormatException e) {
            System.err.println("This application requires two arguments: <machine> <port>");
            return;
        } 
        
        Thread output = new Thread() {
            @Override
            public void run() {
                java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\n");
                while (true) {
                    System.out.println(s.next());
                }
            }
        };
//        A daemon thread is a thread, that does not prevent the JVM from exiting when 
//        the program finishes but the thread is still running. 
//        An example for a daemon thread is the garbage collection.
        output.setDaemon(true);
        output.start();

        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        while ( true ) {
            try {
                os.writeUTF(r.readLine() + "\n");
                os.flush();
            } catch (IOException e) {
                System.err.println("Cannot connect to [machine] on port [port]");
            }
        }
    }
}
