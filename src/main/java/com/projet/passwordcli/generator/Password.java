package com.projet.passwordcli.generator;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Générateur de mots de passe aléatoires.
public class Password {

    private static final String MAJUSCULES = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String MINUSCULES = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHIFFRES = "0123456789";
    private static final String SYMBOLES = "!@#$%^&*()-_=+[]{}|;:,.<>?/~`'\"\\";

    private static final int LONGUEUR_MIN = 4;
    private static final int LONGUEUR_MAX = 128;

    // SecureRandom utiliser pour le tirage 
    private final SecureRandom random = new SecureRandom();

    public String generatePassword(
            int longueur,
            boolean avecMajuscules,
            boolean avecMinuscules,
            boolean avecChiffres,
            boolean avecSymboles
    ) {
        // Vérification des bornes
        if (longueur < LONGUEUR_MIN || longueur > LONGUEUR_MAX) {
            throw new IllegalArgumentException(
                    "La longueur doit être comprise entre " + LONGUEUR_MIN + " et " + LONGUEUR_MAX + "."
            );
        }

        // la liste des jeux sélectionnés
        List<String> jeuxChoisis = new ArrayList<>();
        if (avecMajuscules) jeuxChoisis.add(MAJUSCULES);
        if (avecMinuscules) jeuxChoisis.add(MINUSCULES);
        if (avecChiffres)  jeuxChoisis.add(CHIFFRES);
        if (avecSymboles)  jeuxChoisis.add(SYMBOLES);

        if (jeuxChoisis.isEmpty()) {
            throw new IllegalArgumentException(
                    "Au moins un type de caractère doit être sélectionné."
            );
        }
        if (longueur < jeuxChoisis.size()) {
            throw new IllegalArgumentException(
                    "La longueur (" + longueur + ") est trop courte pour inclure les "
                    + jeuxChoisis.size() + " types de caractères demandés."
            );
        }
        // Fusion de tous les jeux pour le remplissage aléatoire
        String tousCaracteres = String.join("", jeuxChoisis);
        char[] motDePasse = new char[longueur];
        
        // Liste des positions, mélangée pour éviter un motif prévisible
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < longueur; i++) {
            positions.add(i);
        }
        Collections.shuffle(positions, random);

        // choix de chaque type de caractère du mot de passe et remplissage
        for (int i = 0; i < jeuxChoisis.size(); i++) {
            String jeu = jeuxChoisis.get(i);
            int pos = positions.get(i);
            motDePasse[pos] = jeu.charAt(random.nextInt(jeu.length()));
        }
        for (int i = jeuxChoisis.size(); i < longueur; i++) {
            int pos = positions.get(i);
            motDePasse[pos] = tousCaracteres.charAt(random.nextInt(tousCaracteres.length()));
        }
        return new String(motDePasse);
    }
}