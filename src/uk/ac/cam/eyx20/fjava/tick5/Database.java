package uk.ac.cam.eyx20.fjava.tick5;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.ac.cam.cl.fjava.messages.RelayMessage;

public class Database {
    
    private Connection connection;
    
    public Database(String databasePath) throws SQLException {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        String pathprefix = databasePath;
        connection = DriverManager.getConnection("jdbc:hsqldb:file:"
                + pathprefix,"SA","");

        Statement delayStmt = connection.createStatement();
        try {delayStmt.execute("SET WRITE_DELAY FALSE");}
        finally {
            delayStmt.close();
        }
        
        connection.setAutoCommit(false);
        
        Statement sqlStmt = connection.createStatement();
        try {
         sqlStmt.execute("CREATE TABLE messages(nick VARCHAR(255) NOT NULL,"+
                         "message VARCHAR(4096) NOT NULL,timeposted BIGINT NOT NULL)");
        } catch (SQLException e) {
        } finally {
         sqlStmt.close();
        }
        
        Statement sqlStmtStats = connection.createStatement();
        Statement sqlStmtStats1 = connection.createStatement();
        Statement sqlStmtStats2 = connection.createStatement();
        try {
         sqlStmtStats.execute("CREATE TABLE statistics(key VARCHAR(255),value INT)");
         sqlStmtStats1.execute("INSERT INTO statistics(key,value) VALUES ('Total messages',0)");
         sqlStmtStats2.execute("INSERT INTO statistics(key,value) VALUES ('Total logins',0)");
        } catch (SQLException e) {
        } finally {
         sqlStmtStats.close();
         sqlStmtStats1.close();
         sqlStmtStats2.close();
        }
           
    }
    
    public void close() throws SQLException { 
        this.connection.close();
    }
    
    public void incrementLogins() throws SQLException { 
        Statement sqlStmt = connection.createStatement();
        sqlStmt.execute("UPDATE statistics SET value = value+1 WHERE key='Total logins'");
        sqlStmt.close();
    }
    
    public synchronized void addMessage(RelayMessage m) throws SQLException { 
        String stmt = "INSERT INTO MESSAGES(nick,message,timeposted) VALUES (?,?,?)";
        PreparedStatement insertMessage = connection.prepareStatement(stmt);
        try {
         insertMessage.setString(1, m.getFrom());
         insertMessage.setString(2, m.getMessage());
         insertMessage.setLong(3, System.currentTimeMillis());
         insertMessage.executeUpdate();
        } finally { 
            insertMessage.close();
        }
        
        connection.commit();
    }
    
    public List<RelayMessage> getRecent() throws SQLException {
        List<RelayMessage> outputlist = new ArrayList<RelayMessage>();
        String stmt = "SELECT nick,message,timeposted FROM messages "+
                "ORDER BY timeposted DESC LIMIT 10";
        PreparedStatement recentMessages = connection.prepareStatement(stmt);
        try {
            ResultSet rs = recentMessages.executeQuery();
            try {
                while (rs.next())
                    outputlist.add(new RelayMessage(rs.getString(1), rs.getString(2), new Date(rs.getLong(3))));
            } finally {
                rs.close();
            }
        } finally {
            recentMessages.close();
        }
        return outputlist;
    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        
        try {
        Class.forName("org.hsqldb.jdbcDriver");
        String pathprefix = args[0];
        Connection connection = DriverManager.getConnection("jdbc:hsqldb:file:"
        + pathprefix,"SA","");

        Statement delayStmt = connection.createStatement();
        try {delayStmt.execute("SET WRITE_DELAY FALSE");}  //Always update data on disk
        finally {delayStmt.close();}
        
        connection.setAutoCommit(false);
        
        Statement sqlStmt = connection.createStatement();
        try {
         sqlStmt.execute("CREATE TABLE messages(nick VARCHAR(255) NOT NULL,"+
                         "message VARCHAR(4096) NOT NULL,timeposted BIGINT NOT NULL)");
        } catch (SQLException e) {
         System.out.println("Warning: Database table \"messages\" already exists.");
        } finally {
         sqlStmt.close();
        }

        String stmt = "INSERT INTO MESSAGES(nick,message,timeposted) VALUES (?,?,?)";
        PreparedStatement insertMessage = connection.prepareStatement(stmt);
        try {
         insertMessage.setString(1, "Alastair"); //set value of first "?" to "Alastair"
         insertMessage.setString(2, "Hello, Andy");
         insertMessage.setLong(3, System.currentTimeMillis());
         insertMessage.executeUpdate();
        } finally { //Notice use of finally clause here to finish statement
            insertMessage.close();
        }

        connection.commit();

        stmt = "SELECT nick,message,timeposted FROM messages "+
                "ORDER BY timeposted DESC LIMIT 10";
        PreparedStatement recentMessages = connection.prepareStatement(stmt);
        try {
            ResultSet rs = recentMessages.executeQuery();
            try {
                while (rs.next())
                    System.out.println(rs.getString(1)+": "+rs.getString(2)+
                            " ["+rs.getLong(3)+"]");
            } finally {
                rs.close();
            }
        } finally {
            recentMessages.close();
        }

        connection.close();
        }
        catch (IndexOutOfBoundsException e) {
            System.err.println("Usage: java uk.ac.cam.crsid.fjava.tick5.Database <database name>");
        }
    }
}
