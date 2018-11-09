package server;

import java.io.IOException;

public class ServerMain {
    /*----------------------------------------------------------------------
                                  MAIN PROGRAM
    -----------------------------------------------------------------------*/
    public static void main(String args[]) throws IOException {
    	// Creation du serveur
        Server server = new Server();
        server.start();
    }
}
