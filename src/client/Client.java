package client;

import java.io.*;
import java.net.*;
import java.util.*;

import shared.Message;

public class Client {
    /*----------------------------------------------------------------------
	    				VARIABLES CLASS
	-----------------------------------------------------------------------*/
	private static String SERVER_HOST = "127.0.0.1";
	private static int SERVER_PORT = 3000;
	
	protected String username;
	protected String password;
	
	protected Socket socketClient;
	protected ObjectInputStream objectInputStream;
	protected ObjectOutputStream objectOutputStream;
	
	private ClientAuthenticationForm connexion;

    /*----------------------------------------------------------------------
                        CLASS THREAD FOREACH CLIENT
    -----------------------------------------------------------------------*/
    public class ClientThread extends Thread {
        public void run() {
            boolean serverRunning = true;
            while (serverRunning) {
                try {
                    String msg = (String) objectInputStream.readObject();
                    System.out.println(msg);
                    System.out.print("> ");
                } catch (IOException i) {
                    System.out.println("*** Le serveur a ferme la connexion : " + i + " ***");
                    try {
                        disconnect();
                    } catch (IOException e) { }
                    serverRunning = false;
                } catch(ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*----------------------------------------------------------------------
                                  FUNCTIONS
    -----------------------------------------------------------------------*/
    ////////////////////////////////////////
    // Client() : CONSTRUCTEUR PAR DEFAUT //
    ////////////////////////////////////////
    Client() { }

    /////////////////////////////////////////////
    // Client() : CONSTRUCTEUR AVEC PARAMETRES //
    /////////////////////////////////////////////
    Client (String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////
    // Start() : CONNEXION AU SERVEUR + INITIALISATION I/O STREAMS + AUTHENTIFICATION CLIENT //
    ///////////////////////////////////////////////////////////////////////////////////////////
    public boolean start() throws IOException {
        /////////////////////////////
        // 1. Connexion au serveur //
        /////////////////////////////
        try {
            socketClient = new Socket(SERVER_HOST, SERVER_PORT);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        System.out.println("Connexion accepte : " + socketClient.getInetAddress() + ":" + socketClient.getPort());
        System.out.println();

        ///////////////////////////////////
        // 2. Initialisation I/O Streams //
        ///////////////////////////////////
        try {
            objectInputStream = new ObjectInputStream(socketClient.getInputStream());
            objectOutputStream = new ObjectOutputStream(socketClient.getOutputStream());
        } catch (IOException e) {
            System.out.println("Exception detecte a la creation des I/O Streams : " + e);
            return false;
        }
        
        ////////////////////////////////////////
        // 3. CONNEXION ou CREATION DE COMPTE //
        ////////////////////////////////////////
        this.connexion = new ClientAuthenticationForm(this);
        username = this.connexion.getUser();
        password = this.connexion.getPassword();

        /////////////////////////////////////
        // 3. ENVOI DES DONNEES AU SERVEUR //
        /////////////////////////////////////
        try {
            objectOutputStream.writeObject(this.connexion.getChoixMenu());
            objectOutputStream.writeObject(username);
            objectOutputStream.writeObject(password);
        } catch (IOException e) {
            System.out.println("Erreur lors de l'envoi de donnees au serveur : " + e);
            // Erreur : donc on deconnecte le socketClient
            try {
                disconnect();
            } catch (IOException i) {
                i.printStackTrace();
            }
            return false;
        }

        //////////////////////////////////////////
        // 4. CONTROLE SI CONNEXION/CREATION OK //
        //////////////////////////////////////////
        try {
        	// Recuperation du resultat de ClientAuthenticationValidation
            String msg_server = (String) objectInputStream.readObject();
            
            // Si authentification du client OK
            if (msg_server.equals("true")) {
                // Creation du client dans un thread
                Client client = new Client(username, password);
                ClientThread t = new ClientThread();
                t.start();
            	// Affichage menu client connecte
            	ClientAuthenticatedMenu menuBienvenue = new ClientAuthenticatedMenu(username, password); // OU -> menuClientConnecte();
            } else if (msg_server.equals("false")) {
            	// SINON : authentification NOK
                System.out.println();
                System.out.println("/!\\ --- Erreur a l'authentification ! Vous etes supprime du serveur --- /!\\");
                System.out.println("/!\\                                                                     /!\\");
                System.out.println("/!\\   Si 1) S'authentifier -> Erreur sur identifiant ou mot de passe.   /!\\");
                System.out.println("/!\\           Si 2) Creer un compte -> Identifiant existant.            /!\\");
                System.out.println("/!\\                                                                     /!\\");
                System.out.println("/!\\ ---              Veuillez relancer un mainClient.               --- /!\\");
                return false;
            }
        } catch(IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException c) {
            c.printStackTrace();
        }
        
        //////////////////////////////////////////
        // RESULTAT FONCTION START : TERMINE OK //
        //////////////////////////////////////////
        return true;
    }
    
    ///////////////////////////////////////////////////////////
    // menuClientConnecte() : AFFICHAGE MENU CLIENT CONNECTE //
    ///////////////////////////////////////////////////////////
    public void menuClientConnecte() {
		System.out.println();
		System.out.println("*****************************************************");
		System.out.println("*                 CONNECTE - MENU                   *");
		System.out.println("*****************************************************");
        System.out.println("Utilisateur : " + username + " | Mot de Passe : " + password);
		System.out.println("*****************************************************");
        System.out.println("1) MESSAGE A TOUS [BROADCAST] :");
        System.out.println("\t Entrez votre message a envoyer a tous les clients actifs.");
        System.out.println("2) TOPICS :");
        System.out.println("\t Entrez TOPIC pour lister tous les topics actifs.");
        System.out.println("\t Entrez #nomDuTopic pour rejoindre un topic.");
        System.out.println("\t Entrez #nomDuTopic + votreMessag pour envoyer un message sur le topic.");
        System.out.println("3) UTILISATEURS CONNECTES :");
        System.out.println("\t Entrez USERS pour lister les clients actifs.");
        System.out.println("\t Entrez @nomDuClient + votreMessage pour envoyer un message privé.");
        System.out.println("4) QUITTER :");
        System.out.println("\t Entrez EXIT pour quitter le serveur.");
		System.out.println("*****************************************************");
    }

    ////////////////////////////////////////////////////////////////////////////////////
    // envoyerMessageAuServeur() : CLIENT CONNECTE VEUT ENVOYER UN MESSAGE AU SERVEUR //
    ////////////////////////////////////////////////////////////////////////////////////
    public void envoyerMessageAuServeur(Message message) throws IOException {
        objectOutputStream.writeObject(message);
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    // saisieChoixMenu() : BOUCLE INFINIE POUR CHOISIR LE MENU ET ENVOYER CHOIX AU SERVEUR //
    /////////////////////////////////////////////////////////////////////////////////////////
    public static void saisieChoixMenu(Scanner sc, Client client) {
        while (true) {
            try {
            	// Affichage du "command prompt" chez le client + attente de sa saisie
                System.out.print("> ");
                String msg = sc.nextLine();
                // SI TOPIC
                if (msg.equals("TOPIC")) {
                    client.envoyerMessageAuServeur(new Message(1, ""));
                } else if (msg.equals("USERS")) {
                	// SINON SI USERS
                    client.envoyerMessageAuServeur(new Message(2, ""));
                } else if (msg.equals("EXIT")) {
                	// SINON SI QUITTER
                    client.envoyerMessageAuServeur(new Message(3, ""));
                    break;
                } else {
                	// SINON : ENVOI MESSAGE A TOUS LES USERS
                    client.envoyerMessageAuServeur(new Message(0, msg));
                }
            } catch (IOException e) {
            	// Erreur detectee
                System.out.println("Exception lors de l'envoi d'un message au serveur : " + e);
                // Deconnexion du client
                try {
                    client.disconnect();
                } catch (IOException i) {
                    i.printStackTrace();
                }
                break;
            }
        }
    }

    /////////////////////////////////////////////////////////////////////
    // disconnect() : DECONNEXION DU CLIENT ET FERMETURE DE SON SOCKET //
    /////////////////////////////////////////////////////////////////////
    public void disconnect() throws IOException {
        if (socketClient != null) {
            socketClient.close();
        }
    }
}
