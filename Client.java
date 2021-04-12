import java.net.DatagramPacket;
import java.net.DatagramSocket;

import java.net.InetAddress;

/** 
	Classe Client
	* @author 	: -
	* @date  	: 03/06/2020
	* @version 	: 1.0 
*/


public class Client
{
	private int 	port;
	private String 	adresse;
	private int 	nbProposition; // numéro d'essai initilisé à 10

	// numéro séquentiel auto-incrémenté pour connaître le numéro du client
	private static int nbClient;
	private int numClient;

	public Client(String adresse, int port) // on donne l'adresse et port du serveur
	{
		this.port 			= port;
		this.adresse 		= adresse;

		this.nbProposition 	= 10; // on suppose que au début le client a 10 essais pour trouver le chiffre

		this.numClient = ++Client.nbClient;
	}

	/*-----------*/
	/* Acesseurs */
	/*-----------*/
	public int 		getPort()						{ return this.port; 			}
	public String 	getAdresse() 					{ return this.adresse; 			}
	public int 		getNbProposition()				{ return this.nbProposition; 	}

	public int getNumClient() 						{ return this.numClient; 		}

	/*---------------*/
	/* Modificateurs */
	/*---------------*/
	public void decrementeNbProposition()			{ this.nbProposition--; 			}


	/*-----------*/
	/* Affichage */
	/*-----------*/
	public String toString()
	{
		String sRet = "";

		sRet += "num client : " + this.numClient;
		sRet += " port : " + this.port + "\n";
		sRet += " ip : " + this.port + "\n";
		return sRet;
	}



}