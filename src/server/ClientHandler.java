package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import shared.Message;

public	class ClientHandler extends Thread implements Serializable {
    /*----------------------------------------------------------------------
                           VARIABLES CLASS 
    -----------------------------------------------------------------------*/
    Socket socket;
    ObjectOutputStream objectOutputStream;
    ObjectInputStream objectInputStream;

    String username, password, date, topic_name;

    ClientAuthenticationValidation clientAccount = new ClientAuthenticationValidation();

    boolean isRunning = false;

    private Message afficherAuClient, messageEnvoyerParClient;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");;
    
    private ArrayList<ClientHandler> arrayListClientHandler;
    private ArrayList<String> arrayListNameTopic;
    
    int id;
    private static int uniqueId;
    
    ////////////////////////////////////////////////////
    // ClientHandler() : CONSTRUCTEUR AVEC PARAMETRES //
    ////////////////////////////////////////////////////
    ClientHandler(Socket socket, ArrayList<ClientHandler> arrayListClientHandler, ArrayList<String> arrayListNameTopic, Message afficher) {
    	////////////////////////
    	// 1. Initialisations //
    	////////////////////////
    	// Socket client (I/O) Streams 
        this.socket = socket;
        // Listes ClientsConnectes et TopicsEnCours
        this.arrayListClientHandler = arrayListClientHandler;
        this.arrayListNameTopic = arrayListNameTopic;
        // Afficher cote client actuel
        this.afficherAuClient = afficher;

    	////////////////////////////////////////
        // 2. Abonnement topic principal HOME //
    	////////////////////////////////////////
        this.topic_name = new String("Home");
        id = ++uniqueId;
        // Date temps reel
        date = new Date().toString() + "\n";

    	///////////////////////////////////////////
        // 3. Creation des I/O Streams du client //
    	///////////////////////////////////////////
        System.out.println();
        System.out.println("UN CLIENT S'EST CONNECTE");
        System.out.println("-> Creation des I/O Streams du client");
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream  = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            afficher.display("Exception lors de la creation des I/O Streams: " + e);
            return;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////
    // run() : AUTHENTIFICATION DU CLIEN + ECOUTER/ENVOYER MESSAGE DEPUIS CLIENT //
    ///////////////////////////////////////////////////////////////////////////////
    public void run() {
        String choixAuthentication;

        /////////////////////////////////////
        // 1. CONNEXION ou CREATION CLIENT //
        /////////////////////////////////////
        try  {
        	// Recuperation du choixMenu + userSaisi + passwordSaiside AuthenticationFom 
        	choixAuthentication = (String) objectInputStream.readObject();
            username = (String) objectInputStream.readObject();
            password = (String) objectInputStream.readObject();
            // SI client demande a se connecter
            if (choixAuthentication.equals("1")) {
            	// Verification existance et concordance login et pwd
                if (clientAccount.ClientAuthenticationValidation(username, password)) {
                	// SI OK
                    isRunning = true;
                } else {
                	// SI NOK
                    isRunning = false;
                }
            } else if (choixAuthentication.equals("2")) {
            	// SINON SI client demande creer compte
                if (clientAccount.createAccount(username, password)) {
                	// SI OK
                    isRunning = true;
                } else {
                	// SI NOK
                    isRunning = false;
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        } catch(ClassNotFoundException c) {
            c.printStackTrace();
        }

        //////////////////////////////////
        // 2. CONNEXION CLIENT REUSSI ? //
        //////////////////////////////////
        // SI connexion reussie : client informe
        if (isRunning == true) {
            try {
                objectOutputStream.writeObject("true");
                broadcast("*** " + clientAccount.getUsername() + " a rejoint le chat room. ***", this);
            } catch (IOException i) {
                i.printStackTrace();
            }
        } else if(isRunning == false) {
        	// SINON informe client echec connexion/creation
            try {
                objectOutputStream.writeObject("false");
            } catch (IOException i) {
                i.printStackTrace();
            }
        }

        //////////////////////////////////////////////////////////////////////////////////////
        // 3. CLIENT CONNECTE : Boucle infinie en ecoute/attente client + traite le message //
        //////////////////////////////////////////////////////////////////////////////////////
        // SI connexion client reussie ALORS attente de message depuis client
        while (isRunning) {
            // 1. Recuperation du message envoye depuis client
            try {
                messageEnvoyerParClient = (Message) objectInputStream.readObject();
                //System.out.println("\tMESSAGE RECEPTION DANS CLIENT CONNECTE: " + message.getType());
            } catch (IOException e) {
            	afficherAuClient.display(username + " Exception a la lecture du stream provenant de client : " + e);
                isRunning = false;
                break;
            } catch (ClassNotFoundException c) {
                c.printStackTrace();
                break;
            }

            // 2. Conversion du message recu en string
            String stringMessageReceptionDeClient = messageEnvoyerParClient.getMessage();
            
            // 3. Determiner le type de reponse a envoyer
            determinerTypeMessage(stringMessageReceptionDeClient);
        }
        
        //////////////////////////////////////
        // 4. FIN : Fermeture socket client //
        //////////////////////////////////////
        remove(id, this);
        close();
    }

    ///////////////////////////////////////////////////////////////
    // close() : FERMETURE I/O STREAMS ET SOCKET CLIENT CONNECTE //
    ///////////////////////////////////////////////////////////////
    public void close() {
        try {
            if(objectOutputStream != null)
                objectOutputStream.close();
        } catch(Exception e) { }
        
        try {
            if(objectInputStream != null)
                objectInputStream.close();
        } catch(Exception e) { }
        
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) { }
    }
    
    ////////////////////////////////////////
    // writeMsg() : ENVOYER MSG AU CLIENT //
    ////////////////////////////////////////
    public boolean writeMsg (String msg) {
        // SI client deconnecte ALORS fermer client
        if (!socket.isConnected()) {
            close();
            return false;
        }
        // SINON client toujours connecte ALORS envoi msg au client
        try {
            objectOutputStream.writeObject(msg);
        } catch(IOException e) {
        	afficherAuClient.display("*** Erreur lors de l'envoi de message a " + username + "***");
        	afficherAuClient.display(e.toString());
        }
        // Return true si ok
        return true;
    }

    /////////////////////////////////////////////////////////////////////
    // selectTypeMessage() : DETERMINER LE MESSAGE A ENVOYER AU CLIENT //
    /////////////////////////////////////////////////////////////////////
    public void determinerTypeMessage (String stringMessageReceptionDeClient) {
        switch (messageEnvoyerParClient.getType()) {
            // 0 : Message.MESSAGE - BROADCAST - MESSAGE POUR TOPIC_PRIVE_TOUS ?
            case Message.MESSAGE :
                if (broadcast(clientAccount.getUsername() + ": " + stringMessageReceptionDeClient, this) == false) {
                    String msg = "*** Client non trouve. ***";
                    writeMsg(msg);
                }
                break;
            // 1 : Message.TOPIC - AFFICHER LISTE
            case Message.TOPIC :
                writeMsg("Liste des topics en cours :");
                for (int i = 0; i < arrayListNameTopic.size(); i++) {
                    String topic = arrayListNameTopic.get(i);
                    writeMsg((i+1) + ". #" + topic);
                }
                break;
             // 2 : Message.USERS - AFFICHER LISTE
            case Message.USERS :
                writeMsg("Liste des utilisateurs connectes : ");
                for (int i = 0; i < arrayListClientHandler.size(); ++i) {
                    ClientHandler clientHandlerConnected = arrayListClientHandler.get(i);
                    writeMsg((i+1) + ". " + clientHandlerConnected.username + " depuis " + clientHandlerConnected.date);
                }
                break;
            // 3 : Message.EXIT - AFFICHER QUITTER
            case Message.EXIT :
            	writeMsg(clientAccount.getUsername() + " s'est deconnecte.");
                isRunning = false;
                break;
        }
    }
    
    ////////////////////////////////////////////////////
    // broadcast() : DETERMINER OU ENVOYER LE MESSAGE //
    ////////////////////////////////////////////////////
    public synchronized boolean broadcast(String message, ClientHandler clientHandler) {
        boolean isPrive = false;
        boolean isTopic = false;
        String time = simpleDateFormat.format(new Date());

        ////////////////////////////////////////
        // 1. Separer le message en 3 parties //
        ////////////////////////////////////////
        String[] forWho = message.split(" ",3);

        ///////////////////////////
        // 2. Traiter le message //
        ///////////////////////////
        // SI @ ALORS message a envoyer en prive a un utilisateur
        if (forWho[1].charAt(0)=='@') {
        	isPrive = true;
        }

        // SI # ALORS message a envoyer dans un topic
        if (forWho[1].charAt(0)=='#') {
            isTopic = true;
        }

        ///////////////////////////
        // 3. Envoyer le message //
        ///////////////////////////
        // SI message prive pour un utilisateur
        if (isPrive==true) {
            if (!privateMessage(forWho, message, time, clientHandler)) {
                return false;
            }
        } else if (isTopic==true) {
            // SINON creer topic et envoyer message dans le topic
            topicMessage(forWho, message, time, clientHandler);
        } else {
            // SINON diffuser message a tout le monde
            broadcastMessage(message, time);
        }
        return true;
    }

    /////////////////////////////////////////////////////////////////////
    // privateMessage() : ENVOYER UN MESSAGE EN PRIVE A UN UTILISATEUR //
    /////////////////////////////////////////////////////////////////////
    public boolean privateMessage(String[] forWho, String message, String time, ClientHandler clientHandler) {
    	///////////////////////////////////////////////////////////////////////
    	// 1. Recuperer l'username a contacter : [0]@ [1]username [2]message //
    	///////////////////////////////////////////////////////////////////////
        String usernameClient = forWho[1].substring(1, forWho[1].length());
        message = "[PRIVATE] " + time + " " + forWho[0] + " " + forWho[2] + "\n";

    	////////////////////////////////////////////////////////////////
    	// 2. Rechercher l'username dans la liste des users connectes //
    	////////////////////////////////////////////////////////////////
        boolean found=false;
        for (int i = 0; i < arrayListClientHandler.size(); i++) {
            clientHandler = arrayListClientHandler.get(i);
            String ifUserExist= clientHandler.getUsername();
            // SI username trouve ALORS envoyer du message a l'user
            if (ifUserExist.equals(usernameClient)) {
                clientHandler.writeMsg(message);
                found=true;
                break;
            }
        }
        
        ///////////////////////////////////////////////
        // 3. RESULTAT DE L'ENVOI DE MESSAGE PRIVATE //
        ///////////////////////////////////////////////
        if (found!=true) {
            return false;
        }
        return true;
    }

    ///////////////////////////////////////////////////////
    // topicMessage() : ENVOYER UN MESSAGE DANS UN TOPIC //
    ///////////////////////////////////////////////////////
    public void topicMessage(String[] forWho, String message, String time, ClientHandler clientHandler) {
    	////////////////////////////////////////////////////////////////////////////////
    	// 1. Recuperer le nom du topic a diffuser message : [0]# [1]topic [2]message //
    	////////////////////////////////////////////////////////////////////////////////
        boolean isFound = false;
        String name_topic = forWho[1].substring(1, forWho[1].length());

    	//////////////////////////////////////////////////////////////
    	// 2. Rechercher le topic dans la liste des topics en cours //
    	//////////////////////////////////////////////////////////////
        for(String topic: arrayListNameTopic) {
        	// SI tropic trouve -> OK
            if(topic.equals(name_topic)) {
                isFound = true;
            }
        }
        // SINON (topic non trouve) ALORS creation du topic
        if (isFound == false) {
            arrayListNameTopic.add(name_topic);
        }

        /////////////////////////////////////////////////////
        // 3. ENVOYER LE MESSAGE AUX UTILISATEURS DU TOPIC //
        /////////////////////////////////////////////////////
        // Ajout du topic au client
        clientHandler.setTopic_name(name_topic);

        // Parcourt de la liste des utilisateurs connectes
        for(int i = 0; i < arrayListClientHandler.size(); i++) {
            ClientHandler clientHandlerList = arrayListClientHandler.get(i);
            String topic_client = clientHandlerList.getTopic_name();
            // SI user(i) abonne a ce topic ALORS afficher message
            if(topic_client.equals(name_topic)) {
                String message_time = "[" +name_topic + "] " + time + " " + message + "\n";
                clientHandlerList.writeMsg(message_time);
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////
    // broadcastMessage() : ENVOYER UN MESSAGE A TOUS LES CLIENTS CONNECTES //
    //////////////////////////////////////////////////////////////////////////
    public void broadcastMessage(String message, String time) {
        /////////////////////////////////////////
        // 1. PREPARATION DU MESSAGE A ENVOYER //
        /////////////////////////////////////////
        String message_broadcast = "[BROADCAST] " + time + " " + message + "\n";
        System.out.print(message_broadcast);

        //////////////////////////////////////////////////////
        // 2. ENVOI DU MESSAGE A TOUS LES CLIENTS CONNECTES //
        //////////////////////////////////////////////////////
        for(int i = 0; i < arrayListClientHandler.size(); i++) {
            ClientHandler clientHandler = arrayListClientHandler.get(i);
            // SI message pas envoye ALORS signifie que user(i) n'est plus connecte DONC deconnexion user(i)
            if(!clientHandler.writeMsg(message_broadcast)) {
                arrayListClientHandler.remove(i);
                display(clientHandler.getUsername() + " est deconnecte. Il est supprime de la liste.");
            }
        }
    }


    ////////////////////////////////////////////////////////
    // remove() : SUPPRESSION DE L'UTILISATEUR DU SERVEUR //
    ////////////////////////////////////////////////////////
    public synchronized void remove(int id, ClientHandler clientHandler) {
        String supprimerClient = "";
        for (int i = 0; i < arrayListClientHandler.size(); ++i) {
            ClientHandler clientHandlerList = arrayListClientHandler.get(i);
            if (clientHandlerList.id == id) {
            	supprimerClient = clientHandlerList.getUsername();
                arrayListClientHandler.remove(i);
                break;
            }
        }
        broadcast("*** " + clientAccount.getUsername() + " a quitte le chat room. ***", this);
    }
    //////////////////////////////////
    // display() : AFFICHER MESSAGE //
    //////////////////////////////////
    public void display(String msg) {
        String time = simpleDateFormat.format(new Date()) + " " + msg;
        System.out.println(time);
    }

    /*----------------------------------------------------------------------
                              GETTERS / SETTERS
    -----------------------------------------------------------------------*/
    public String getUsername() { return username; }
    
    public String getTopic_name() { return this.topic_name; }
    public void setTopic_name(String topic_name) { this.topic_name = topic_name; }
    
}
