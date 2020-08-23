package fr.diginamic.offi;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import fr.diginamic.offi.entity.Additif;
import fr.diginamic.offi.entity.Allergene;
import fr.diginamic.offi.entity.Categorie;
import fr.diginamic.offi.entity.Ingredient;
import fr.diginamic.offi.entity.Marque;
import fr.diginamic.offi.entity.Produit;
import fr.diginamic.offi.exception.ExceptionTech;
import fr.diginamic.offi.utils.Convertisseur;
import fr.diginamic.offi.utils.StringUtils;

/**
 * Application principale
 * 
 * @author RichardBONNAMY
 *
 */
public class IntegrationApp {

	/**
	 * Point d'entrée
	 * 
	 * @param args non utilisés ici
	 */
	public static void main(String[] args) {

		List<String> lignes = null;

		String fileName = "C:/Temp/open-food-facts.csv";

		try {
			lignes = FileUtils.readLines(new File(fileName), "UTF-8");
		} catch (IOException e) {
			throw new ExceptionTech("Fichier " + fileName + " introuvable.");
		}

		// On supprime la ligne d'entête
		lignes.remove(0);

		// On traite toutes les lignes 1 par 1 et on les transforme en produits
		ArrayList<Produit> produits = new ArrayList<>();
		for (String ligne : lignes) {
			Produit produit = tranformeLigneEnProduit(ligne);
			produits.add(produit);
		}

		// Ouverture de la connexion
		Connection connexion = null;
		try {
			connexion = DriverManager
					.getConnection("jdbc:mysql://localhost:3306/openfoodfacts?serverTimezone=Europe/Paris", "root", "");
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			throw new ExceptionTech(
					"Impossible de créer une connexion. Veuillez vérifier les paramètres de connexion dans le fichier de configuration.");
		}

		// Traitement de la liste de produits

		for (Produit produit : produits) {
			insertionCategorie(connexion, produit.getCategorie());
			insertionMarque(connexion, produit.getMarque());

			insertionProduit(connexion, produit);

			// On insère chaque ingrédient du produit en base de données
			for (Ingredient ingredient : produit.getIngredients()) {
				insertionIngredient(connexion, produit, ingredient);
			}

			// On insère chaque additif du produit en base de données
			for (Additif additif : produit.getAdditifs()) {
				insertionAdditif(connexion, produit, additif);
			}

			// On insère chaque allergène du produit en base de données
			for (Allergene allergene : produit.getAllergenes()) {
				insertionAllergene(connexion, produit, allergene);
			}
		}
	}

	/**
	 * Transforme une ligne du fichier en un Produit
	 * 
	 * @param ligne ligne
	 * @return Produit
	 */
	private static Produit tranformeLigneEnProduit(String ligne) {
		String[] morceaux = ligne.split("\\|", -1);

		// System.out.println(nbMorceaux);

		String nomCategorie = morceaux[0];
		String nomMarque = morceaux[1];
		String nomProduit = morceaux[2];
		String nutritionGradeFr = morceaux[3];

		String ingredientsStr = morceaux[4];

		Double energie100g = Convertisseur.toDouble(morceaux[5]);
		Double graisse100g = Convertisseur.toDouble(morceaux[6]);
		Double sucres100g = Convertisseur.toDouble(morceaux[7]);
		Double fibres100g = Convertisseur.toDouble(morceaux[8]);
		Double proteines100g = Convertisseur.toDouble(morceaux[9]);
		Double sel100g = Convertisseur.toDouble(morceaux[10]);
		Double vitA100g = Convertisseur.toDouble(morceaux[11]);
		Double vitD100g = Convertisseur.toDouble(morceaux[12]);
		Double vitE100g = Convertisseur.toDouble(morceaux[13]);
		Double vitK100g = Convertisseur.toDouble(morceaux[14]);
		Double vitC100g = Convertisseur.toDouble(morceaux[15]);
		Double vitB1100g = Convertisseur.toDouble(morceaux[16]);
		Double vitB2100g = Convertisseur.toDouble(morceaux[17]);
		Double vitPP100g = Convertisseur.toDouble(morceaux[18]);
		Double vitB6100g = Convertisseur.toDouble(morceaux[19]);
		Double vitB9100g = Convertisseur.toDouble(morceaux[20]);
		Double vitB12100g = Convertisseur.toDouble(morceaux[21]);
		Double calcium100g = Convertisseur.toDouble(morceaux[22]);
		Double magnesium100g = Convertisseur.toDouble(morceaux[23]);
		Double iron100g = Convertisseur.toDouble(morceaux[24]);
		Double fer100g = Convertisseur.toDouble(morceaux[25]);
		Double betaCarotene100g = Convertisseur.toDouble(morceaux[26]);
		String presenceHuilePalme = morceaux[27];
		String allergenesStr = morceaux[28];
		String additifStr = morceaux[29];

		Categorie categorie = new Categorie(nomCategorie);
		Marque marque = new Marque(nomMarque);

		Produit produit = new Produit(nomProduit);
		produit.setCategorie(categorie);
		produit.setMarque(marque);

		produit.setGrade(nutritionGradeFr);
		produit.setIngredients(tranformeChaineEnIngredients(ingredientsStr));
		produit.setAdditifs(tranformeChaineEnAdditifs(additifStr));
		produit.setAllergenes(transformeChaineEnAllergenes(allergenesStr));

		produit.setBetaCarotene100g(betaCarotene100g);
		produit.setCalcium100g(calcium100g);
		produit.setEnergie100g(energie100g);
		produit.setFer100g(fer100g);
		produit.setFibres100g(fibres100g);
		produit.setGraisse100g(graisse100g);
		produit.setIron100g(iron100g);
		produit.setMagnesium100g(magnesium100g);
		produit.setPresenceHuilePalme(presenceHuilePalme);
		produit.setProteines100g(proteines100g);
		produit.setSel100g(sel100g);
		produit.setSucres100g(sucres100g);
		produit.setVitA100g(vitA100g);
		produit.setVitB1100g(vitB1100g);
		produit.setVitB12100g(vitB12100g);
		produit.setVitB2100g(vitB2100g);
		produit.setVitB6100g(vitB6100g);
		produit.setVitB9100g(vitB9100g);
		produit.setVitC100g(vitC100g);
		produit.setVitD100g(vitD100g);
		produit.setVitE100g(vitE100g);
		produit.setVitK100g(vitK100g);
		produit.setVitPP100g(vitPP100g);

		return produit;
	}

	/**
	 * Transforme une chaine de caractères contenant tous les ingrédients en liste
	 * d'ingrédients. Cette méthode applique avant le découpage un ensemble de
	 * traitements afin de supprimer de la chaine les données indésirables.
	 * 
	 * @param chaine chaine contenant la liste d'ingrédients.
	 * @return List de Ingredient
	 */
	private static List<Ingredient> tranformeChaineEnIngredients(String chaine) {

		List<String> morceaux = StringUtils.splitChaine(chaine);
		List<Ingredient> ingredients = new ArrayList<>();
		morceaux.forEach(nom -> ingredients.add(new Ingredient(nom)));
		return ingredients;
	}

	/**
	 * Transforme une chaine de caractères contenant tous les allergènes en liste
	 * d'allergènes. Cette méthode applique avant le découpage un ensemble de
	 * traitements afin de supprimer de la chaine les données indésirables.
	 * 
	 * @param chaine chaine contenant la liste d'allergènes.
	 * @return List de Allergene
	 */
	private static List<Allergene> transformeChaineEnAllergenes(String chaine) {

		List<String> morceaux = StringUtils.splitChaine(chaine);
		List<Allergene> allergenes = new ArrayList<>();
		morceaux.forEach(nom -> allergenes.add(new Allergene(nom)));
		return allergenes;
	}

	/**
	 * Transforme une chaine de caractères contenant tous les additifs en liste
	 * d'additifs. Cette méthode applique avant le découpage un ensemble de
	 * traitements afin de supprimer de la chaine les données indésirables.
	 * 
	 * @param chaine chaine contenant la liste d'additifs.
	 * @return List de Additif
	 */
	private static List<Additif> tranformeChaineEnAdditifs(String chaine) {

		List<String> morceaux = StringUtils.splitChaine(chaine);
		List<Additif> additifs = new ArrayList<>();
		morceaux.forEach(nom -> additifs.add(new Additif(nom)));
		return additifs;
	}

	/**
	 * Insère la catégorie en base de données
	 * 
	 * @param conn      connexion à la base de données
	 * @param categorie catégorie à insérer
	 */
	private static void insertionCategorie(Connection conn, Categorie categorie) {

		String nomCategorie = categorie.getNom().replace("'", "''");

		try {
			Statement stat = conn.createStatement();
			ResultSet res1 = stat.executeQuery("SELECT * FROM CATEGORIE WHERE NOM='" + nomCategorie + "'");
			// Si la catégorie existe en base de données on se content de mettre à jour
			// l'identifiant de la catégorie
			if (res1.next()) {
				categorie.setId(res1.getLong("id"));
			}
			// Sinon on créé la catégorie
			else {
				stat.executeUpdate("INSERT INTO CATEGORIE (NOM) VALUES ('" + nomCategorie + "')");

				// Une fois la catégorie créée, on doit récupérer son identifiant en base de
				// données
				ResultSet res2 = stat.executeQuery("SELECT * FROM CATEGORIE WHERE NOM='" + nomCategorie + "'");
				if (res2.next()) {
					categorie.setId(res2.getLong("id"));
				}
				res2.close();
			}
			res1.close();
			stat.close();
		} catch (SQLException e) {
			throw new ExceptionTech(e.getMessage());
		}
	}

	/**
	 * Insère la marque en base de données
	 * 
	 * @param conn   connexion à la base de données
	 * @param marque marque à insérer
	 */
	private static void insertionMarque(Connection conn, Marque marque) {

		String nomMarque = marque.getNom().replace("'", "''");

		try {
			Statement stat = conn.createStatement();
			ResultSet res1 = stat.executeQuery("SELECT * FROM MARQUE WHERE NOM='" + nomMarque + "'");
			// Si la marque existe en base de données on se contente de mettre à jour
			// l'identifiant de la marque
			if (res1.next()) {
				marque.setId(res1.getLong("id"));
			}
			// Sinon on créé la marque
			else {
				stat.executeUpdate("INSERT INTO MARQUE (NOM) VALUES ('" + nomMarque + "')");

				// Une fois la marque créée, on doit récupérer son identifiant en base de
				// données
				ResultSet res2 = stat.executeQuery("SELECT * FROM MARQUE WHERE NOM='" + nomMarque + "'");
				if (res2.next()) {
					marque.setId(res2.getLong("id"));
				}
				res2.close();
			}
			res1.close();
			stat.close();
		} catch (SQLException e) {
			throw new ExceptionTech(e.getMessage());
		}
	}

	/**
	 * Insère le produit en base de données
	 * 
	 * @param conn    connexion à la base de données
	 * @param produit produit à insérer
	 */
	private static void insertionProduit(Connection conn, Produit produit) {
		String nom = produit.getNom().replace("\\", "");
		produit.setNom(nom);

		try {
			PreparedStatement statRecherche = conn.prepareStatement("SELECT * FROM PRODUIT WHERE NOM=?");
			statRecherche.setString(1, produit.getNom());
			ResultSet res1 = statRecherche.executeQuery();

			// Si la produit existe en base de données on se contente de mettre à jour
			// l'identifiant du produit passé en paramètre de la méthode
			if (res1.next()) {
				produit.setId(res1.getLong("id"));
			}
			// Sinon on créé le produit avec une requête préparée
			else {
				PreparedStatement statInsertion = conn.prepareStatement(
						"INSERT INTO Produit (NOM, GRADE, ID_MRQ, ID_CAT, betaCarotene100g, calcium100g, "
								+ "energie100g, fer100g, fibres100g, graisse100g, iron100g, magnesium100g, "
								+ "presenceHuilePalme, proteines100g, sel100g, sucres100g, vitA100g, "
								+ "vitB1100g, vitB12100g, vitB2100g, vitB6100g, vitB9100g, vitC100g, "
								+ "vitD100g, vitE100g, vitK100g,vitPP100g) "
								+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

				statInsertion.setString(1, produit.getNom());
				statInsertion.setString(2, produit.getGrade());
				statInsertion.setLong(3, produit.getMarque().getId());
				statInsertion.setLong(4, produit.getCategorie().getId());

				statInsertion.setObject(5, produit.getBetaCarotene100g(), Types.DOUBLE);
				statInsertion.setObject(6, produit.getCalcium100g(), Types.DOUBLE);
				statInsertion.setObject(7, produit.getEnergie100g(), Types.DOUBLE);
				statInsertion.setObject(8, produit.getFer100g(), Types.DOUBLE);
				statInsertion.setObject(9, produit.getFibres100g(), Types.DOUBLE);
				statInsertion.setObject(10, produit.getGraisse100g(), Types.DOUBLE);
				statInsertion.setObject(11, produit.getIron100g(), Types.DOUBLE);
				statInsertion.setObject(12, produit.getMagnesium100g(), Types.DOUBLE);
				statInsertion.setString(13, produit.getPresenceHuilePalme());
				statInsertion.setObject(14, produit.getProteines100g(), Types.DOUBLE);
				statInsertion.setObject(15, produit.getSel100g(), Types.DOUBLE);
				statInsertion.setObject(16, produit.getSucres100g(), Types.DOUBLE);
				statInsertion.setObject(17, produit.getVitA100g(), Types.DOUBLE);
				statInsertion.setObject(18, produit.getVitB1100g(), Types.DOUBLE);
				statInsertion.setObject(19, produit.getVitB12100g(), Types.DOUBLE);
				statInsertion.setObject(20, produit.getVitB2100g(), Types.DOUBLE);
				statInsertion.setObject(21, produit.getVitB6100g(), Types.DOUBLE);
				statInsertion.setObject(22, produit.getVitB9100g(), Types.DOUBLE);
				statInsertion.setObject(23, produit.getVitC100g(), Types.DOUBLE);
				statInsertion.setObject(24, produit.getVitD100g(), Types.DOUBLE);
				statInsertion.setObject(25, produit.getVitE100g(), Types.DOUBLE);
				statInsertion.setObject(26, produit.getVitK100g(), Types.DOUBLE);
				statInsertion.setObject(27, produit.getVitPP100g(), Types.DOUBLE);
				statInsertion.executeUpdate();

				// Une fois le produit créé, on doit récupérer son identifiant en base de
				// données
				statRecherche.setString(1, produit.getNom());
				ResultSet res2 = statRecherche.executeQuery();
				if (res2.next()) {
					produit.setId(res2.getLong("id"));
				}
				res2.close();
			}
			res1.close();
			statRecherche.close();
		} catch (SQLException e) {
			System.err.println("Erreur sur le produit suivant:" + produit);
			throw new ExceptionTech(e.getMessage());
		}
	}

	/**
	 * Insère un ingrédient en base de données pour le produit passé en paramètre
	 * 
	 * @param conn       connexion à la base de données
	 * @param produit    produit
	 * @param ingredient ingrédient à insérer
	 */
	private static void insertionIngredient(Connection conn, Produit produit, Ingredient ingredient) {

		String nom = ingredient.getNom().replace("'", "''");

		try {
			Statement stat = conn.createStatement();
			ResultSet res1 = stat.executeQuery("SELECT * FROM INGREDIENT WHERE NOM='" + nom + "'");

			// Si l'ingrédient existe en base de données on se contente de mettre à jour
			// l'identifiant de l'ingrédient passé en paramètre de la méthode
			if (res1.next()) {
				ingredient.setId(res1.getLong("id"));
			}
			// Sinon on créé l'ingrédient
			else {
				stat.executeUpdate("INSERT INTO INGREDIENT (NOM) VALUES ('" + nom + "')");

				// Une fois l'ingrédient créé, on doit récupérer son identifiant en base de
				// données
				ResultSet res2 = stat.executeQuery("SELECT * FROM INGREDIENT WHERE NOM='" + nom + "'");
				if (res2.next()) {
					ingredient.setId(res2.getLong("id"));
				}
				res2.close();
			}
			res1.close();
			stat.close();
		} catch (SQLException e) {
			throw new ExceptionTech(e.getMessage());
		}

		// Mise à jour du lien entre le produit et l'ingrédient
		try {
			Statement stat = conn.createStatement();
			ResultSet res1 = stat.executeQuery("SELECT * FROM COMPOSITION_ING WHERE ID_PRD=" + produit.getId()
					+ " AND ID_ING=" + ingredient.getId());

			// Si le lien n'existe pas on le créé
			if (!res1.next()) {
				stat.executeUpdate("INSERT INTO COMPOSITION_ING (ID_PRD, ID_ING) VALUES (" + produit.getId() + ", "
						+ ingredient.getId() + ")");
			}
			res1.close();
			stat.close();
		} catch (SQLException e) {
			System.err.println("Erreur sur le produit suivant:" + produit);
			throw new ExceptionTech(e.getMessage());
		}
	}

	/**
	 * Insère un allergène en base de données pour le produit passé en paramètre
	 * 
	 * @param conn      connexion à la base de données
	 * @param produit   produit
	 * @param allergene allergène à insérer
	 */
	private static void insertionAllergene(Connection conn, Produit produit, Allergene allergene) {
		String nom = allergene.getNom().replace("'", "''");

		try {
			Statement stat = conn.createStatement();
			ResultSet res1 = stat.executeQuery("SELECT * FROM ALLERGENE WHERE NOM='" + nom + "'");

			// Si l'allergène existe en base de données on se contente de mettre à jour
			// l'identifiant de l'allergène passé en paramètre de la méthode
			if (res1.next()) {
				allergene.setId(res1.getLong("id"));
			}
			// Sinon on créé l'allergène
			else {
				stat.executeUpdate("INSERT INTO ALLERGENE (NOM) VALUES ('" + nom + "')");

				// Une fois l'allergène créé, on doit récupérer son identifiant en base de
				// données
				ResultSet res2 = stat.executeQuery("SELECT * FROM ALLERGENE WHERE NOM='" + nom + "'");
				if (res2.next()) {
					allergene.setId(res2.getLong("id"));
				}
				res2.close();
			}
			res1.close();
			stat.close();
		} catch (SQLException e) {
			throw new ExceptionTech(e.getMessage());
		}

		// Mise à jour du lien entre le produit et l'allergène
		try {
			Statement stat = conn.createStatement();
			ResultSet res1 = stat.executeQuery("SELECT * FROM COMPOSITION_ALL WHERE ID_PRD=" + produit.getId()
					+ " AND ID_ALL=" + allergene.getId());

			// Si le lien n'existe pas on le créé
			if (!res1.next()) {
				stat.executeUpdate("INSERT INTO COMPOSITION_ALL (ID_PRD, ID_ALL) VALUES (" + produit.getId() + ", "
						+ allergene.getId() + ")");
			}
			res1.close();
			stat.close();
		} catch (SQLException e) {
			System.err.println("Erreur sur le produit suivant:" + produit);
			throw new ExceptionTech(e.getMessage());
		}
	}

	/**
	 * Insère un additif en base de données pour le produit passé en paramètre
	 * 
	 * @param conn    connexion à la base de données
	 * @param produit produit
	 * @param additif ingrédient à insérer
	 */
	private static void insertionAdditif(Connection conn, Produit produit, Additif additif) {
		String nom = additif.getNom().replace("'", "''");

		try {
			Statement stat = conn.createStatement();
			ResultSet res1 = stat.executeQuery("SELECT * FROM ADDITIF WHERE NOM='" + nom + "'");

			// Si l'additif existe en base de données on se contente de mettre à jour
			// l'identifiant de l'additif passé en paramètre de la méthode
			if (res1.next()) {
				additif.setId(res1.getLong("id"));
			}
			// Sinon on créé l'additif
			else {
				stat.executeUpdate("INSERT INTO ADDITIF (NOM) VALUES ('" + nom + "')");

				// Une fois l'additif créé, on doit récupérer son identifiant en base de
				// données
				ResultSet res2 = stat.executeQuery("SELECT * FROM ADDITIF WHERE NOM='" + nom + "'");
				if (res2.next()) {
					additif.setId(res2.getLong("id"));
				}
				res2.close();
			}
			res1.close();
			stat.close();
		} catch (SQLException e) {
			throw new ExceptionTech(e.getMessage());
		}

		// Mise à jour du lien entre le produit et l'additif
		try {
			Statement stat = conn.createStatement();
			ResultSet res1 = stat.executeQuery(
					"SELECT * FROM COMPOSITION_ADD WHERE ID_PRD=" + produit.getId() + " AND ID_ADD=" + additif.getId());

			// Si le lien n'existe pas on le créé
			if (!res1.next()) {
				stat.executeUpdate("INSERT INTO COMPOSITION_ADD (ID_PRD, ID_ADD) VALUES (" + produit.getId() + ", "
						+ additif.getId() + ")");
			}
			res1.close();
			stat.close();
		} catch (SQLException e) {
			System.err.println("Erreur sur le produit suivant:" + produit);
			throw new ExceptionTech(e.getMessage());
		}
	}
}
