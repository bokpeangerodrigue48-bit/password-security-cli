package com.projet.passwordcli;

import com.projet.passwordcli.generator.Password;
import com.projet.passwordcli.validator.PasswordValidator;
import java.util.Scanner;

// Interface CLI pour le générateur de mots de passe
public class Main {

    public static void main(String[] args) {
        Scanner lecteur = new Scanner(System.in);

        System.out.println("    BIENVENUE SUR PASSWORD SECURITY CLI (Java 21) ");

        // Vérification du conteneur Docker
        PasswordValidator validateur = new PasswordValidator();
        String test = validateur.evaluateStrength("test");
        if (test.startsWith("CONTENEUR_INDISPONIBLE")) {
            System.out.println("ATTENTION : Le conteneur Docker de validation n'est pas joignable.");
            System.out.println("Lancez-le avec : docker run -d --name zxcvbn-api -p 3000:3000 password-checker");
            lecteur.close();
            return;
        } else if (test.startsWith("ERREUR_API")) {
            System.out.println("Le service de validation a répondu avec une erreur : " + test);
            System.out.println("Vérifiez les logs du conteneur avec : docker logs zxcvbn-api");
            lecteur.close();
            return;
        }
        System.out.println("Service de validation Docker : OK");

        // 1. Choix de la longueur
        int longueur = 12;
        System.out.print("Entrez la longueur du mot de passe (min 4, défaut 12) : ");
        String saisieLongueur = lecteur.nextLine().trim();
        if (!saisieLongueur.isEmpty()) {
            try {
                longueur = Integer.parseInt(saisieLongueur);
                if (longueur < 4) {
                    System.out.println("Saisie trop courte. Sécurité minimale fixée à 4.");
                    longueur = 4;
                }
            } catch (NumberFormatException e) {
                System.out.println("Saisie invalide. Utilisation de la valeur par défaut (12).");
            }
        }
        // 2. Types de caractères
        System.out.print("Inclure des MAJUSCULES ? (O/n) : ");
        boolean avecMajuscules = !lecteur.nextLine().trim().equalsIgnoreCase("n");

        System.out.print("Inclure des minuscules ? (O/n) : ");
        boolean avecMinuscules = !lecteur.nextLine().trim().equalsIgnoreCase("n");

        System.out.print("Inclure des chiffres ? (O/n) : ");
        boolean avecChiffres = !lecteur.nextLine().trim().equalsIgnoreCase("n");

        System.out.print("Inclure des symboles ? (O/n) : ");
        boolean avecSymboles = !lecteur.nextLine().trim().equalsIgnoreCase("n");

        if (!avecMajuscules && !avecMinuscules && !avecChiffres && !avecSymboles) {
            System.out.println("Aucun type choisi. Activation par défaut des minuscules et majuscules.");
            avecMinuscules = true;
            avecMajuscules = true;
        }
        // 3. Mode rafale
        int nbMotsDePasse = 1;
        System.out.print("Combien de mots de passe générer (Mode Rafale, défaut 1) : ");
        String saisieNb = lecteur.nextLine().trim();
        if (!saisieNb.isEmpty()) {
            try {
                nbMotsDePasse = Integer.parseInt(saisieNb);
                if (nbMotsDePasse < 1) nbMotsDePasse = 1;
            } catch (NumberFormatException e) {
                System.out.println("Saisie invalide. Génération d'un seul mot de passe.");
            }
        }
        System.out.println("\nRÉSULTAT DU MODE RAFALE (" + nbMotsDePasse + " mot(s) de passe) :");

        // 4. Génération et évaluation
        Password generateur = new Password();
        for (int i = 1; i <= nbMotsDePasse; i++) {
            String motDePasse;
            try {
                // Appel au générateur 
                motDePasse = generateur.generatePassword(longueur, avecMajuscules, avecMinuscules, avecChiffres, avecSymboles);
            } catch (IllegalArgumentException e) {
                System.out.printf("[%d] Erreur de génération : %s%n", i, e.getMessage());
                continue;
            }
            String robustesse = validateur.evaluateStrength(motDePasse);

            System.out.printf("[%d] Mot de passe : %s%n", i, motDePasse);
            System.out.printf("    Robustesse  : %s%n", robustesse);
        }
        lecteur.close();
        System.out.println("\nMerci d'avoir utilisé Password Security CLI ! À bientôt.");
    }
}