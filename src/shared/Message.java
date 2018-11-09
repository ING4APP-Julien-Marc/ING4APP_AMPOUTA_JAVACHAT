package shared;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import server.ClientHandler;

public class Message implements Serializable {
    /**
     *  The different types of message sent by the Client
     *  WHOISIN to receive the list of the users connected
     *  MESSAGE an ordinary text message
     *  EXIT to disconnect from the Server
     *  TOPIC to receive the list of topic
     **/
	public static final int MESSAGE = 0;
	public final static int TOPIC = 1;
    public static final int USERS = 2;
	public final static int EXIT = 3;
    private int type;
    
    private String message;
    private SimpleDateFormat simpleDateFormat;

    private ArrayList<ClientHandler> arrayListClientHandler;
    private ArrayList<String> arrayListNameTopic;

    /*
    # \fn Message()
    # \brief...........: Constructor of the class
    */
    public Message (int type, String message) {
        this.type = type;
        this.message = message;
    }
    /**
     * \fn display
     * @param msg .: message to display
     * \brief .....: message to display with time
     */
    public void display(String msg) {
        String time = simpleDateFormat.format(new Date()) + " " + msg;
        System.out.println(time);
    }


    public int getType() { return type; }

    public String getMessage() { return message; }
}
