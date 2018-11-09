package client;

import java.io.IOException;
import java.util.Scanner;

public class ClientAuthenticationForm {
    /*----------------------------------------------------------------------
	    						VARIABLES CLASS
	-----------------------------------------------------------------------*/
	private String user;
	private String password;
	private String choixMenu;

    /*----------------------------------------------------------------------
                                  FUNCTIONS
    -----------------------------------------------------------------------*/
    ///////////////////////////////////////////////////////////////////////////////////////////
    // ClientAuthenticationForm() : CONSTRUCTEUR - AFFICHAGE DU FORMULAIRE POUR LA CONNEXION //
    ///////////////////////////////////////////////////////////////////////////////////////////
	public ClientAuthenticationForm(Client client) throws IOException {
		System.out.println("-----------------------------------------------------");
		System.out.println("-        CONNEXION - MENU    :   SE CONNECTER       -");
		System.out.println("-----------------------------------------------------");
		
		/////////////////////////
		// 1. Afficher le menu //
		/////////////////////////
		System.out.println("1) S'authentifier");
		System.out.println("2) Creer un compte");
		System.out.println();
		System.out.println("-----------------------------------------------------");
		
		//////////////////////////////////////////////////
	    // 2. Saisie tant que choix different de 1 ou 2 //
		//////////////////////////////////////////////////
	    do {
			System.out.print("Choix : ");
		    Scanner sc = new Scanner(System.in);
		    choixMenu = sc.nextLine();
	    } while (!choixMenu.equals("1") && !choixMenu.equals("2"));

		System.out.println("-----------------------------------------------------");
		System.out.println();
		
		///////////////////////////////
		// 3 . Connexion ou Creation //
		///////////////////////////////
	    if (choixMenu.equals("1")) {
			// Saisie des identifiants de connexion existants
			System.out.println("-----------------------------------------------------");
			System.out.print("Identifiant de connexion : ");
		    Scanner userExist = new Scanner(System.in);
			setUser(userExist.nextLine());
			
			System.out.print("Mot de passe : ");
		    Scanner pwdExist = new Scanner(System.in);
			setPassword(pwdExist.nextLine());
			System.out.println("-----------------------------------------------------");
	    } else if (choixMenu.equals("2")) {
			// Saisie des nouveaux identifiants de connexion
			System.out.println("-----------------------------------------------------");
			System.out.print("Identifiant de connexion : ");
		    Scanner userNew = new Scanner(System.in);
			setUser(userNew.nextLine());
			
			System.out.print("Mot de passe : ");
		    Scanner pwdNew = new Scanner(System.in);
			setPassword(pwdNew.nextLine());
			System.out.println("-----------------------------------------------------");
	    }
	}

    /*----------------------------------------------------------------------
                              GETTERS / SETTERS
    -----------------------------------------------------------------------*/
	public String getUser() { return user; }
	public void setUser(String user) { this.user = user; }

	public String getPassword() { return password; }
	public void setPassword(String password) { this.password = password; }

	public String getChoixMenu() { return choixMenu; }
}
