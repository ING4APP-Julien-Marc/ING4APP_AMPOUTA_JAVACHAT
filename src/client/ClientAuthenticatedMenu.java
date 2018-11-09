package client;

public class ClientAuthenticatedMenu {
    /*----------------------------------------------------------------------
	    						FUNCTION
	-----------------------------------------------------------------------*/
    ///////////////////////////////////////////////////////////////////
    // ClientAuthenticatedMenu() : Affichage menu au client connecte //
    ///////////////////////////////////////////////////////////////////
	ClientAuthenticatedMenu(String user, String pwd) {
		System.out.println();
		System.out.println("*****************************************************");
		System.out.println("*                 CONNECTE - MENU                   *");
		System.out.println("*****************************************************");
        System.out.println("Utilisateur : " + user + " | Mot de Passe : " + pwd);
		System.out.println("*****************************************************");
        System.out.println("1) MESSAGE A TOUS [BROADCAST] :");
        System.out.println("\t Entrez votre message a envoyer a tous les clients actifs.");
        System.out.println("2) TOPICS :");
        System.out.println("\t Entrez TOPIC pour lister tous les topics actifs.");
        System.out.println("\t Entrez #nomDuTopic pour rejoindre un topic.");
        System.out.println("\t Entrez #nomDuTopic + votreMessage pour envoyer un message sur le topic.");
        System.out.println("3) UTILISATEURS CONNECTES :");
        System.out.println("\t Entrez USERS pour lister les clients actifs.");
        System.out.println("\t Entrez @nomDuClient + votreMessage pour envoyer un message privé.");
        System.out.println("4) QUITTER :");
        System.out.println("\t Entrez EXIT pour quitter le serveur.");
		System.out.println("*****************************************************");
	}
}
