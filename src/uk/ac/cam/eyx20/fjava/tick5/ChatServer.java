package uk.ac.cam.eyx20.fjava.tick5;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

import uk.ac.cam.cl.fjava.messages.Message;

public class ChatServer {
    public static void main(String args[]) throws InterruptedException {
        int port = 0;
        final ServerSocket s;
        Database db;
        MultiQueue multiq = new MultiQueue<Message>();
        
        try {
            port = Integer.parseInt(args[0]);
            s = new ServerSocket(port);
            db = new Database(args[1]);
            while (true) {
                Socket newSock = s.accept();
                ClientHandler clienthand = new ClientHandler(newSock, multiq, db);
                clienthand.input.start();
                clienthand.output.start();
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Usage: java ChatServer <port> <database>");
            return;
        } catch (IOException e) {
            System.err.println("Cannot use port number " + port);
            return;
        } catch (IllegalArgumentException e) {
            System.err.println("Cannot use port number " + port);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
   }
