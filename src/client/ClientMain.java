package client;

import java.io.IOException;
import java.util.Scanner;

public class ClientMain {
    /*----------------------------------------------------------------------
                                MAIN PROGRAM
    -----------------------------------------------------------------------*/
    public static void main(String args[]) throws IOException {
    	// Variable pour la saisie
        Scanner sc = new Scanner(System.in);

        // Creation clientThread (avec fin du client si erreur sur la connexion/creation)
        Client client = new Client();
        if (client.start() == false) {
            return;
        }

        // En ecoute continue sur la saisie client
        client.saisieChoixMenu(sc, client);;
        sc.close();
    }
}
