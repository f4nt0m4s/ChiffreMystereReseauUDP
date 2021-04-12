import java.net.DatagramPacket;
import java.net.DatagramSocket;

import java.net.InetAddress;
import java.net.SocketException;


import java.util.Scanner;

import java.util.ArrayList;

import java.util.Random;

/** 
	Classe ServeurChiffreMystere
	* @author 	: -
	* @date  	: 03/06/2020
	* @version 	: 1.0 
*/


public class ServeurChiffreMystere
{
	public static void main(String[] args) 
	{
		int 	portNumber = 9000;
		boolean bOk 		= false; // indique si le numéro mystère a été trouvé

		int chiffreMystere;

		ArrayList<Client> alClient 	= new ArrayList<Client>();

		DatagramSocket dSocket;
		DatagramPacket dPacket;

		Scanner sc;
		boolean bRejouer=true; // indique si le joueur veut rejouer une partie

		try
		{

			do
			{
				// Création socket UDP -> Le serveur se trouve sur le port 9000
				dSocket = new DatagramSocket(portNumber);

				/*-------------------------------------------*/
				/* Initialisation du serveur pour une partie */
				/*-------------------------------------------*/
				byte[] buf = new byte[1024]; 							// Reception message UDP
				dPacket = new DatagramPacket(buf, 1024);				//création paquet vide	

				dSocket.receive(dPacket);								// Reception dans le paquet créé

				// ajout du premier client
				alClient.add( new Client( dPacket.getAddress().getHostAddress(), dPacket.getPort() ) );


				/*------------------------*/
				/* Lancement de la partie */
				/*------------------------*/ 
				bOk = ServeurChiffreMystere.jouerPartie( dSocket, dPacket, alClient );

				/*-------------------------------------------------------------------*/
				/* Fermeture du serveur qui a été ouvert pour que les clients jouent */
				/*-------------------------------------------------------------------*/ 
				dSocket.close();
			
				/*---------------------------*/
				/* Si le client veut rejouer */
				/*---------------------------*/

				if ( bOk)
				{
					System.out.print("Le numero mystere a ete trouve ! Voulez-vous rejouer ? [O/N] O = Oui / N = Non : ");
					sc = new Scanner(System.in);
					char choix = sc.next().charAt(0);
					boolean choixCorrecte = false;
					
					if ( choix == 'O' || choix == 'N')
					{
						while( choixCorrecte )
						{
							System.out.println("Tapez soit O ou N");
							choix = sc.next().charAt(0);
							if ( choix == 'O' || choix == 'N') { choixCorrecte = true; }
						}
					}
					if(choix == 'O')
					{
						bRejouer = true;
						System.out.println("Vous avez decide de relancer une partie !");

					}
					else if ( choix == 'N' )
					{
						bRejouer = false;
						System.out.println("Vous avez decide de quitter le jeu !");
					}
				}

			}while( bRejouer );
			
		}
		catch (Exception e) 		{ e.printStackTrace(); }

	}

	/*-----------------------------------------------*/
	/* Méthode  pour jouer une partie pour un client */
	/*-----------------------------------------------*/
	private static boolean jouerPartie(DatagramSocket dSocket, DatagramPacket dPacket, ArrayList<Client> alClient )
	{
		String msg 					= "";
		String strRecu 				= "";

		int chiffreMystere = (int) (Math.random() * 100) + 1;

		Integer nbPropose 			= new Integer(0);
		String 	strReponse 			= "";

		boolean bOk 				= false;


		try
		{
			/*--------------------------------------------*/
			/* Boucle pour signifier si le client a gagné */
			/*--------------------------------------------*/
			while( ! bOk )
			{

				/*---------------------------------------------------------------------------*/
				/* Boucle pour tester l'entrée clavier et le nombre de coup valide du client */
				/*---------------------------------------------------------------------------*/
				do 
				{
					msg = "Saississez un nombre : ";
					// Envoie message UDP
						// Fabrication paquet UDP
					dPacket = new DatagramPacket(msg.getBytes(), msg.length(), dPacket.getAddress(), dPacket.getPort());
							
					// Envoi
					dSocket.send(dPacket);

					// Reception dans le paquet créé
					dSocket.receive(dPacket);

					for(Client client : alClient)
					{
						if ( client.getNbProposition() < 1 && dPacket.getPort() == client.getPort() )
						{
							boolean bTest = true;
							while( bTest )
							{
								String strError = "Nombre de coup depasse, deconnectez vous ! ";
								DatagramPacket dPError = new DatagramPacket(strError.getBytes(), strError.length(), dPacket.getAddress(), dPacket.getPort());
								dSocket.send(dPError);
								dSocket.receive(dPacket);
								if( dPacket.getPort()!= client.getPort() ) bTest = false;

							}
						}
						
					}


					// Fabrication chaine reçue
					strRecu = new String( dPacket.getData(), 0, dPacket.getLength() );

					/*----------------------------------------*/
					/* Ajout du client dans la base de Client */
					/*----------------------------------------*/

					boolean bTest = false;
					for ( Client client : alClient )
					{

						// si le client qui écrit est deja enregistré dans la base de donnes arraylist client on peut enlever son nb proprop
						if ( dPacket.getPort() == client.getPort() && dPacket.getAddress().getHostAddress().equals(client.getAdresse()))
						{

							client.decrementeNbProposition();
							bTest = true;  // on peut pas ajouté car le port existe deja
						}
					}


					if ( ! bTest )
					{
						alClient.add( new Client( dPacket.getAddress().getHostAddress(), dPacket.getPort() ) );
					}


					/*-------------------------------------------------------------------------------------*/
					/* Lors de chaque saisie par un client, les informations sont affichés dans le serveur */
					/*-------------------------------------------------------------------------------------*/
					ServeurChiffreMystere.afficherInfosServeur(alClient, dPacket, strRecu);
					


				}while( ! ServeurChiffreMystere.estNombre( strRecu.trim()) );

				/*------------------------------------------------*/
				/* Récupération de l'entier proposé par le client */
				/*------------------------------------------------*/
				nbPropose = Integer.parseInt( strRecu.trim() ); // Pour information : trim() retourne une copie de cette chaîne avec un espace blanc de début et de fin supprimé
							
				/*-----------------------*/
				/* Test de validé + ou - */
				/*-----------------------*/
				strReponse = "";

				if( nbPropose < chiffreMystere ) 		{ strReponse += "-" + "\n";  }
				else if( nbPropose > chiffreMystere ) 	{ strReponse += "+" + "\n";  }
				else 
				{
					strReponse += "= BRAVO !" + "\n" + "Fin de partie ! Appuyez sur entree, et rendez-vous dans la partie SERVEUR ! " + "\n";

					bOk = true;
				}
					

				/*------------------------------*/
				/* Envoie du résultat au client */
				/*------------------------------*/
				dPacket = new DatagramPacket(strReponse.getBytes(), strReponse.length(), dPacket.getAddress(), dPacket.getPort());
				dSocket.send(dPacket);
			}
		}
		catch (Exception e) 		{ e.printStackTrace(); }

		return bOk;

	}

	/*---------------------------------------------------------------*/
	/* Méthode  pour savoir si c'est un nombre que le client a entré */
	/*---------------------------------------------------------------*/

	private static boolean estNombre(String s)
	{
		// nulle ou vide
		if ( s == null || s.length() == 0)
		{
			System.out.println("Mauvaise saisie, chaine nulle ou vide !");
			return false;
		}

		for( Character c : s.toCharArray() ) // on met chaque caractère du string dans un tab de Char
		{
			if ( !Character.isDigit(c) ) 
			{
				System.out.println(c + " est pas un nombre ! ");
				return false;
			}
		}
		return true;
	}




	/*------------------------------*/
	/* Informations pour le serveur */
	/*------------------------------*/
	private static void afficherInfosServeur(ArrayList<Client> alClient, DatagramPacket dPacket, String strRecu)
	{
		for( Client client : alClient )
		{
			if ( dPacket.getPort() == client.getPort() && dPacket.getAddress().getHostAddress().equals(client.getAdresse()))
			{
				System.out.println( String.format("%15s %d", "CLIENT NUMERO", client.getNumClient() )  											);
				System.out.println( String.format("%7s %12s %17s %35s" , "IP", "PORT", "NUMERO ENTRE", "NOMBRE PROPOSITIONS RESTANTES") 		);
				System.out.print( String.format("%s" , "+-----------") 		);
				System.out.print( String.format("%s" , "+---------") 		);
				System.out.print( String.format("%s" , "+-------------------+") 		);
				System.out.println( String.format("%s" , "-----------------------------------+") 		);

				// Catégorie IP
				System.out.print( String.format("%s %8s %s", "|", dPacket.getAddress().getHostAddress(), "|") );

				// Catégorie Port
				System.out.print( String.format(" %6d %2s",dPacket.getPort(), "|") );

				// Catégorie Numéro entré
				System.out.print( String.format("%10s %9s", strRecu.trim(), "|") );

				System.out.println( String.format("%18s %17s", client.getNbProposition(), "|") );

				System.out.print( String.format("%s" , "+-----------") 		);
				System.out.print( String.format("%s" , "+---------") 		);
				System.out.print( String.format("%s" , "+-------------------+") 		);
				System.out.println( String.format("%s" , "-----------------------------------+") 		);
					

				System.out.println();
				System.out.println();
			}

		}
	}
}
