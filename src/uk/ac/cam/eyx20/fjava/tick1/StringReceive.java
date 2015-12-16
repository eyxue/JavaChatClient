package uk.ac.cam.eyx20.fjava.tick1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class StringReceive {

    @SuppressWarnings("deprecation")
    
    /**
     * Reads a String stream from a server and prints the contents to the console
     * @param args : a hostname and a portnumber.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        
        try {
            assert (args.length == 2);
            String hostname = args[0];
            String portnumber = args[1];
            Socket smtpSocket = new Socket(hostname, Integer.parseInt(portnumber));
            DataOutputStream os = new DataOutputStream(smtpSocket.getOutputStream());
            DataInputStream is = new DataInputStream(smtpSocket.getInputStream());
            Scanner s = new java.util.Scanner(is).useDelimiter("\n");
            while (s.hasNext()) {
                System.out.println(s.next());
            }
        } catch (UnknownHostException e) {
            System.err.println("Cannot connect to " + args[0] + " on port " + args[1]);
            return;
        } catch (IOException e) {
            System.err.println("Cannot connect to " + args[0] + " on port " + args[1]);
            return;
        } catch (IllegalArgumentException e) {
            System.err.println("This application requires two arguments: <machine> <port>");
            return;
        } catch (IndexOutOfBoundsException e) {
            System.err.println("This application requires two arguments: <machine> <port>");
            return;
        } catch (AssertionError e) {
            System.err.println("This application requires two arguments: <machine> <port>");
            return;
        } 

    }
}
