package fr.diginamic.offi.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Fournit des services de traitement de chaine de caractères pour le traitement
 * des ingrédients, des additifs et des allergènes
 * 
 * @author RichardBONNAMY
 *
 */
public class StringUtils {

	/**
	 * Découpe une chaine en liste de String. Cette méthode va également supprimer
	 * des données indésirables de la chaine:
	 * <ul>
	 * <li>Les espaces en début et fin de chaine</li>
	 * <li>Les données entre parenthèses</li>
	 * <li>Les parenthèses orphelines</li>
	 * <li>les chiffres avec des pourcentages</li>
	 * <li>les numérotations de produits</li>
	 * <li>les caractères spéciaux comme les points, les tirets, les points
	 * d'interrogation, etc..</li<
	 * </ul>
	 * 
	 * @param chaine
	 * @return
	 */
	public static List<String> splitChaine(String chaine) {

		String chaineTraiter = chaine;
		chaineTraiter = supprimerParentheses(chaineTraiter);
		chaineTraiter = supprimerPourcentages(chaineTraiter);
		chaineTraiter = supprimerNumerotations(chaineTraiter);
		chaineTraiter = supprimerSpecialChars(chaineTraiter);
		return decouper(chaineTraiter);
	}

	/**
	 * Découpe la chaine de caractères passée en paramètre sur la base des
	 * caractères suivants: , ; ou :
	 * 
	 * @param chaine chaine à découper
	 * @return List de String
	 */
	private static List<String> decouper(String chaine) {

		if (chaine == null || chaine.isEmpty()) {
			return new ArrayList<>();
		}

		List<String> liste = new ArrayList<>();
		String[] morceaux = chaine.split("[,;:]");
		for (String m : morceaux) {
			String morceauTraite = m.trim();
			if (!morceauTraite.isEmpty()) {
				morceauTraite = morceauTraite.substring(0, 1).toUpperCase() + morceauTraite.substring(1);

				liste.add(morceauTraite);
			}
		}
		return liste;
	}

	/**
	 * Supprime un certain nombre de caractères spéciaux qui polluent les noms
	 * 
	 * @param chaine chaine à traiter
	 * @return String
	 */
	private static String supprimerSpecialChars(String chaine) {
		return chaine.replaceAll("[_\\*'/\\?\\+=]*", "").trim();

	}

	/**
	 * Supprime toutes les données entre parenthèses, qui sont souvent des
	 * compléments d'informations sur les ingrédients, additifs ou allergènes et
	 * dont on n'a pas besoin.
	 * 
	 * @param chaine chaine à traiter
	 * @return String
	 */
	private static String supprimerParentheses(String chaine) {
		return chaine.replaceAll("\\(.*?\\)", "").replaceAll("[\\(\\)\\[\\]]", "");
	}

	/**
	 * Supprime les pourcentages associés aux ingrédients, additifs ou allergènes et
	 * qu'on ne souhaite pas conserver
	 * 
	 * @param chaine chaine à traiter
	 * @return String
	 */
	private static String supprimerPourcentages(String chaine) {
		return chaine.replaceAll("[0-9]*\\s*%", "").trim();
	}

	/**
	 * Supprime les numérotations qu'on trouve parfois à côté des ingrédients.
	 * 
	 * @param chaine chaine à traiter
	 * @return String
	 */
	private static String supprimerNumerotations(String chaine) {
		return chaine.replaceAll("[0-9]*\\.*", "").trim();
	}
}
