package uk.ac.cam.eyx20.fjava.tick2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.ac.cam.cl.fjava.messages.ChangeNickMessage;
import uk.ac.cam.cl.fjava.messages.ChatMessage;
import uk.ac.cam.cl.fjava.messages.DynamicObjectInputStream;
import uk.ac.cam.cl.fjava.messages.Execute;
import uk.ac.cam.cl.fjava.messages.NewMessageType;
import uk.ac.cam.cl.fjava.messages.RelayMessage;
import uk.ac.cam.cl.fjava.messages.StatusMessage;

@FurtherJavaPreamble(
        author = "Elise Xue",
        date = "21st October 2015",
        crsid = "eyx20",
        summary = "Chat client that communicates with the server using serialised methods",
        ticker = FurtherJavaPreamble.Ticker.UNKNOWN)
public class ChatClient {

    public static String username = "New User";

    private static String printDate() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(date);
    }
    
    private static String printDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(date);
    }
   
    public static void main(String[] args) {

        String server = null;
        int port = 0;
        final Socket s;
        ObjectOutputStream os;
        DynamicObjectInputStream is;

        try {
            assert (args.length == 2);
            server = args[0];
            port = Integer.parseInt(args[1]);
            s = new Socket(server, port);
            os = new ObjectOutputStream(s.getOutputStream());
            is = new DynamicObjectInputStream(s.getInputStream());
            System.out.println(printDate() + " [Client] Connected to " + server + " on port " + port + ".");
            
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
                boolean flag = true;
                while (flag) {
                    try {
                        Object obj = is.readObject();
                        if (obj instanceof StatusMessage) {
                            StatusMessage stat = new StatusMessage(((StatusMessage) obj).getMessage());
                            System.out.println(printDate(stat.getCreationTime()) + " [Server] " + stat.getMessage());
                        }
                        else if (obj instanceof RelayMessage) {
                            RelayMessage relay = (RelayMessage) obj;
                            System.out.println(printDate(relay.getCreationTime()) + " [" + relay.getFrom() + "] " + relay.getMessage());
                        }
                        else if (obj instanceof NewMessageType) {
                            NewMessageType newmsg = (NewMessageType) obj;
                            is.addClass(newmsg.getName(), newmsg.getClassData());
                            System.out.println(printDate(newmsg.getCreationTime()) + " [Client] New class " + newmsg.getName() + " loaded.");
                        }
//                        else {
//                            Class<?> someClass = obj.getClass();
//                            Field[] fields = someClass.getDeclaredFields();
//                            String fieldString = "";
//                            for (Field field : fields) {
//                                field.setAccessible(true);
//                                fieldString += field.getName() + "(" + field.get(obj) + "), ";
//                            }
//                            if (fieldString.length() > 0) {
//                                fieldString = fieldString.substring(0, fieldString.length() - 2);
//                            }
//                            
//                            System.out.println(printDate() + " [Client] " + someClass.getSimpleName()+ ": " + fieldString);
//                            Method[] methods = someClass.getDeclaredMethods();
//                            for (Method method : methods) {
//                                boolean execute = false;
//                                Annotation[] annotations = method.getAnnotations();
//                                for (Annotation annotation : annotations) {
//                                    if (annotation instanceof Execute) {
//                                        execute = true;
//                                    }
//                                }
//                                if (execute && method.getParameterCount() == 0) {
//                                    method.invoke(obj);
//                                }
//                            }
//                            System.out.println(printDate() + " [Client] New message of unknown type received.");
//                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        flag = false;
//                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
//                    catch (IllegalAccessException e) {
//                        e.printStackTrace();
//                    } 
//                    catch (InvocationTargetException e) {
//                        e.printStackTrace();
//                    }
                    
                }
            }
        };
        output.setDaemon(true);
        output.start();

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        while ( true ) {
            try {
                String chattext = input.readLine();
                if (chattext.length() > 0) {
                    if (chattext.substring(0, 1).equals("\\")) {
                        String[] arguments = chattext.split(" ");
                        if (arguments[0].equals("\\nick")) {
                            ChangeNickMessage nickmsg = new ChangeNickMessage(arguments[1]);
                            os.writeObject(nickmsg);
                            os.flush();
                        }
                        else if (arguments[0].equals("\\quit")) {
                            s.close();
                            System.out.println(printDate() + " [Client] Connection terminated.");
                            return;
                        }
                        else {
                            System.out.println(printDate() + " [Client] Unknown command \"" + arguments[0] + "\"");
                        }
                    }
                }
                ChatMessage chatmsg = new ChatMessage(chattext);
                //System.out.print("wrote a chatmessage");
                os.writeObject(chatmsg);
                os.flush();
            } catch (IOException e) {
                System.err.println("Cannot connect to [machine] on port [port]");
            }
        }
        
    }
    
}
