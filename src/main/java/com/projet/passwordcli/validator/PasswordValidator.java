package com.projet.passwordcli.validator;

import org.json.JSONObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

// Client HTTP pour interroger le conteneur Docker zxcvbn
public class PasswordValidator {

    private static final String URL_VALIDATEUR = "http://localhost:3000/zxcvbn";

    /**
     * Évalue la robustesse d'un mot de passe via le conteneur Docker.
     * Retourne TRES_FAIBLE, FAIBLE, MOYEN, FORT, TRES_FORT ou un message d'erreur.
     */
    public String evaluateStrength(String motDePasse) {
        try {
            HttpClient clientHttp = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            // Construction du JSON pour éviter de casser la requête
            String corpsJson = "{\"password\":\"" + echapperJson(motDePasse) + "\"}";

            HttpRequest requete = HttpRequest.newBuilder()
                    .uri(URI.create(URL_VALIDATEUR))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(corpsJson))
                    .build();

            HttpResponse<String> reponse = clientHttp.send(requete,
                    HttpResponse.BodyHandlers.ofString());

            if (reponse.statusCode() == 200) {
                return extraireScore(reponse.body());
            } else {
                return "ERREUR_API (Code: " + reponse.statusCode() + ")";
            }
        } catch (Exception e) {
            return "CONTENEUR_INDISPONIBLE (" + e.getMessage() + ")";
        }
    }

    // Extrait le score du JSON pour ca conersion. 
    private String extraireScore(String json) {
        JSONObject obj = new JSONObject(json);
        int score = obj.getInt("score");
        return switch (score) {
            case 0 -> "TRES_FAIBLE";
            case 1 -> "FAIBLE";
            case 2 -> "MOYEN";
            case 3 -> "FORT";
            case 4 -> "TRES_FORT";
            default -> "INCONNU";
        };
    }

    // Échappe les guillemets, antislashs et retours à la ligne pour un JSON.
    private String echapperJson(String chaine) {
        return chaine.replace("\\", "\\\\")
                     .replace("\"", "\\\"")
                     .replace("\n", "\\n")
                     .replace("\r", "\\r")
                     .replace("\t", "\\t");
    }
}