package server;

import java.net.*;
import java.io.*;
import java.text.*;
import java.util.*;

import shared.Message;

public class Server {
    /*----------------------------------------------------------------------
                               VARIABLES SERVER
    -----------------------------------------------------------------------*/
    private static String SERVER_HOST = "127.0.0.1";
    private static int SERVER_PORT = 3000;
    
    private ArrayList<ClientHandler> arrayListClientHandler;
    private ArrayList<String> arrayListNameTopic;

    private SimpleDateFormat simpleDateFormat;
    
    private boolean isRunning;
    
    private Message message;

    /*----------------------------------------------------------------------
                                  FUNCTIONS
    -----------------------------------------------------------------------*/
    ////////////////////////////////////////
    // Server() : CONSTRUCTEUR PAR DEFAUT //
    ////////////////////////////////////////
    public Server() {
        // display hh:mm:ss
        simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        arrayListClientHandler = new ArrayList<ClientHandler>();
        arrayListNameTopic = new ArrayList<String>();
    }

    ////////////////////////////////////
    // start() : DEMARRAGE DU SERVEUR //
    ////////////////////////////////////
    public void start() {
        isRunning = true;
        boolean isFound = false;
        
        try {
        	///////////////////////////////////////////
            // 1. CREATION DU SOCKET POUR LE SERVEUR //
        	///////////////////////////////////////////
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT, 10, InetAddress.getByName(SERVER_HOST));

            ///////////////////////////////////////////////////////
            // 2. BOUCLE INFINIE EN ATTENTE DE CONNEXION CLIENTS //
            ///////////////////////////////////////////////////////
            while (isRunning) {
            	// Serveur en ecoute et accepte les connexions users
            	display("\tLe serveur est en ecoute sur " + SERVER_HOST + ":" +SERVER_PORT);
                Socket socket = serverSocket.accept();
                
                // Arret sir serveur eteind
                if(!isRunning) {
                    break;
                }

                // Quand un client se connecte -> Creation de son thread
                ClientHandler clientHandler = new ClientHandler(socket, arrayListClientHandler, arrayListNameTopic, message);

                // Ajout du client a la liste des threads (clients connectes)
                arrayListClientHandler.add(clientHandler);

                // Lecture de la liste des topics et comparaison avec le topic du client
                for (String topic: arrayListNameTopic) {
                    if (topic.equals(clientHandler.topic_name)) {
                        isFound = true;
                        break;
                    }
                }
                // SI topic non trouve ALORS ajout du topic a la liste
                if (isFound == false) {
                    arrayListNameTopic.add(clientHandler.topic_name);
                }

                // Start thread client
                clientHandler.start();
            }

            /////////////////////////////////////////////////////////////////////////////////
            // 3. isRunning = false ? -> ARRET DU SERVEUR et FERMETURE DES THREADS/STREAMS //
            /////////////////////////////////////////////////////////////////////////////////
            try {
            	// Arret du serveur
                serverSocket.close();
                
                // Fermeture de tous les I/O Streams et Socket des utilisateurs connectes
                for (int i = 0; i < arrayListClientHandler.size(); ++i) {
                    ClientHandler tc = arrayListClientHandler.get(i);
                    try {
                        tc.objectInputStream.close();
                        tc.objectOutputStream.close();
                        tc.socket.close();
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch(Exception x) {
            	message.display("Exception a la fermeture du serveur et des utilisateurs : " + x);
            }
        } catch (IOException y) {
            String msg = simpleDateFormat.format(new Date()) + " Exception sur le nouveau ServerSocket : " + y + "\n";
            message.display(msg);
        }
    }

    ////////////////////////////////
    // start() : ARRET DU SERVEUR //
    ////////////////////////////////
    public void stop() {
        isRunning = false;
        try {
            new Socket(SERVER_HOST, SERVER_PORT);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////
    // remove() : SUPPRESSION D'UN UTILISATEUR DU SERVEUR //
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
        display("*** " + supprimerClient + " a quitte le chat room. ***");
    }

    //////////////////////////////////
    // display() : AFFICHER MESSAGE //
    //////////////////////////////////
    public void display(String msg) {
        String time = simpleDateFormat.format(new Date()) + " " + msg;
        System.out.println(time);
    }
}