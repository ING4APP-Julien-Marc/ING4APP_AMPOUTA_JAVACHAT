package server;

import java.io.*;

public class ClientAuthenticationValidation {
    /*----------------------------------------------------------------------
                                VARIABLES
    -----------------------------------------------------------------------*/
    private File loginFile = new File("./login.txt");
    private String username;
    private String password;


    /*----------------------------------------------------------------------
                                FUNCTIONS
    -----------------------------------------------------------------------*/
    ////////////////////////////////////////////////////////////////
    // ClientAuthenticationValidation() : CONSTRUCTEUR PAR DEFAUT //
    ////////////////////////////////////////////////////////////////
    public ClientAuthenticationValidation() { }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ClientAuthenticationValidation() : "CONSTRUCTEUR" AVEC PARAMETRES - Return resultat de la connexion //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public boolean ClientAuthenticationValidation(String user_login, String user_password) throws IOException {
    	if (clientExist(user_login, user_password)) {
    		return true;
    	} else {
    		return false;
    	}
    }

    ///////////////////////////////////////////////
    // createAccount() : CREATION NOUVEAU CLIENT //
    ///////////////////////////////////////////////
    public boolean createAccount(String user_login, String user_password) throws IOException {
    	////////////////////////////////
    	// 1. SI username N'EXIST PAS //
    	////////////////////////////////
    	if (!userLoginExist(user_login)) {
        	///////////////////////////////////////////////////
        	// 2. Ouverture du fichier loginFile en ecriture //
        	///////////////////////////////////////////////////
            FileWriter fileWriter = new FileWriter(loginFile, true);
            // 1. <login>:<password>
            fileWriter.write(user_login + ";" + user_password);
            fileWriter.write("\n");
            fileWriter.close();
            // Enregistrement user et pwd
        	setUsername(user_login);
        	setPassword(user_password);
        	// Return true
        	return true;
    	}
    	return false;
    }

    /////////////////////////////////////////////////////////////////
    // clientExist() : PARCOURT login.txt ET DIT SI USER/PWD EXIST //
    /////////////////////////////////////////////////////////////////
    public boolean clientExist(String user_login, String user_password) throws IOException {
    	//////////////////////////////////////////////////
    	// 1. Ouverture du fichier loginFile en lecture //
    	//////////////////////////////////////////////////
        FileReader fileReader = new FileReader(loginFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        String[] parse;
        
        /////////////////////////////////////
        // 2. Lecture du fichier loginFile //
        /////////////////////////////////////
        while ((line= bufferedReader.readLine()) != null) {
        	// Separation de la ligne courante en 2 parties : login et password
            parse = line.split(";");
            // SI loginLigneCourante=loginSaisie && passwordLigneCourante=passwordSaisie ALORS userTrouve
            if (parse[0].equals(user_login) && parse[1].equals(user_password)) {
            	// Enregistrement user et pwd
            	setUsername(user_login);
            	setPassword(user_password);
            	// RESULTAT : return true
                return true;
            }
        }
        // SINON RESULTAT : return false
        return false;
    }
    
    /////////////////////////////////////////////////////////////////
    // userLoginExist() : PARCOURT login.txt ET DIT SI USER_LOGIN EXIST //
    /////////////////////////////////////////////////////////////////
    public boolean userLoginExist(String user_login) throws IOException {
    	//////////////////////////////////////////////////
    	// 1. Ouverture du fichier loginFile en lecture //
    	//////////////////////////////////////////////////
        FileReader fileReader = new FileReader(loginFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        String[] parse;
        
        /////////////////////////////////////
        // 2. Lecture du fichier loginFile //
        /////////////////////////////////////
        while ((line= bufferedReader.readLine()) != null) {
        	// Separation de la ligne courante en 2 parties : login et password
            parse = line.split(";");
            // SI loginLigneCourante=loginSaisie ALORS loginTrouve
            if (parse[0].equals(user_login)) {
                return true;
            }
        }
        // SINON RESULTAT : return false
        return false;
    }
    
    /*----------------------------------------------------------------------
                              GETTERS / SETTERS
    -----------------------------------------------------------------------*/
	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }

	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }
}
