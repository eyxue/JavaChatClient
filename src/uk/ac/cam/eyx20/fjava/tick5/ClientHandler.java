package uk.ac.cam.eyx20.fjava.tick5;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import uk.ac.cam.cl.fjava.messages.ChangeNickMessage;
import uk.ac.cam.cl.fjava.messages.ChatMessage;
import uk.ac.cam.cl.fjava.messages.DynamicObjectInputStream;
import uk.ac.cam.cl.fjava.messages.Execute;
import uk.ac.cam.cl.fjava.messages.Message;
import uk.ac.cam.cl.fjava.messages.NewMessageType;
import uk.ac.cam.cl.fjava.messages.RelayMessage;
import uk.ac.cam.cl.fjava.messages.StatusMessage;

public class ClientHandler {
    private Database database;
    private Socket socket;
    private MultiQueue<Message> multiQueue;
    private String nickname;
    private MessageQueue<Message> clientMessages;
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private boolean flag;
    
    public ClientHandler(Socket s, MultiQueue<Message> q, Database db) throws SQLException {
        database = db;
        db.incrementLogins();
        flag = true;
        socket = s;
        multiQueue = q;
        clientMessages = new SafeMessageQueue<Message>();
        multiQueue.register(clientMessages);
        nickname = "Anonymous" + (int) (Math.random()*100000);
        List<RelayMessage> recent = database.getRecent();
        Collections.reverse(recent);
        for (RelayMessage msg : recent) {
            clientMessages.put(msg);
        }
        StatusMessage connect = new StatusMessage(printDate() + " [Server] " + nickname + 
                " connected from " + socket.getInetAddress().getHostName() + ".");
        multiQueue.put(connect);
        try {
            is = new ObjectInputStream(socket.getInputStream());
            os = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            StatusMessage disconnect = new StatusMessage(printDate() + " [Server] " + nickname + 
                    "  has disconnected.");
            multiQueue.put(disconnect);
            multiQueue.deregister(clientMessages);
        }
        
    }  
    
    Thread input = new Thread() {
        @Override
        public void run() {
            while (flag) {
                try {
                    Object obj = is.readObject();
                    if (obj instanceof ChangeNickMessage) {
                        ChangeNickMessage nick = new ChangeNickMessage(((ChangeNickMessage) obj).name);
                        StatusMessage serve = new StatusMessage(printDate(nick.getCreationTime()) + 
                                " [Server] " + nickname + " is now known as " + nick.name + ".");
                        nickname = nick.name;
                        multiQueue.put(serve);
                    }
                    else if (obj instanceof ChatMessage) {
                        ChatMessage chat = (ChatMessage) obj;
                        RelayMessage relay = new RelayMessage(nickname, chat);
                        database.addMessage(relay);
                        multiQueue.put(relay);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    StatusMessage disconnect = new StatusMessage(printDate() + 
                            " [Server] " + nickname + "  has disconnected.");
                    multiQueue.put(disconnect);
                    multiQueue.deregister(clientMessages);
                    flag = false;
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    Thread output = new Thread() {
        @Override
        public void run() {
            while (flag) {
                try {
                    Message msg = clientMessages.take();
                    if (msg != null) {
                        os.writeObject(msg);
                        os.flush();
                    }
                } catch (IOException e) {
                    flag = false;
                    
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private static String printDate() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(date);
    }
    
    private static String printDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(date);
    }
    
   }
