package uk.ac.cam.eyx20.fjava.tick2;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;

class TestMessageReadWrite {
    
    static boolean writeMessage(String message, String filename) {
        try {
            TestMessage testmessage = new TestMessage();
            testmessage.setMessage(message);
            FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(testmessage);
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    static String readMessage(String location) {
        
        try {
            System.out.println(location.substring(0, 7));
            System.out.println(location.substring(0, 7).equals("http://"));
            if (location.substring(0, 7).equals("http://")) {
                URL url = new URL(location);
                URLConnection connection1 = url.openConnection();
                TestMessage testmessage = new TestMessage();
                ObjectInputStream in = new ObjectInputStream(connection1.getInputStream());
                testmessage.setMessage(((TestMessage) in.readObject()).getMessage());
                //testmessage.setMessage(in.readObject().toString());
                return testmessage.getMessage();
            } else {
                FileInputStream fis = new FileInputStream(location);
                ObjectInputStream in = new ObjectInputStream(fis);
                TestMessage testmessage = new TestMessage();
                testmessage.setMessage(((TestMessage) in.readObject()).getMessage());
                return testmessage.getMessage();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String args[]) {
        System.out.println(readMessage("http://www.cl.cam.ac.uk/teaching/current/FJava/testmessage-eyx20.jobj"));
    }
   }
