
Projet: Password Security CLI

Application en ligne de commande (Java 21) de génération de mots de passe robustes
avec évaluation de leur solidité par un outil externe exécuté dans un conteneur Docker.

Table des matières

- Fonctionnalités
- Architecture et choix techniques
- Analyse fonctionnelle
- Analyse technique
- Prérequis
- Installation et exécution
- Exemple de session

1- Fonctionnalités

- Génération de mots de passe cryptographiquement sûrs (`SecureRandom`).
- Paramétrage complet : longueur (4–128), inclusion des majuscules, minuscules, chiffres et symboles.
- Garantie de la présence d’au moins un caractère de chaque type demandé.
- Mode rafale : génération de plusieurs mots de passe en une seule exécution.
- Indicateur de robustesse à cinq niveaux : Très faible, Faible, Moyen, Fort, Très fort.
- Validation externe via un conteneur Docker exécutant l’algorithme `zxcvbn`.
- Interface interactive simple en français.

2- Architecture et choix techniques

Le projet respecte une séparation stricte entre la génération des mots de passe (code Java)
et l’évaluation de leur robustesse (conteneur Docker), comme l’exige le cahier des charges.

- Java 21 – seul langage autorisé pour l’application.
- Maven – gestion du cycle de vie et empaquetage (JAR exécutable avec dépendances).
- Docker – conteneur autonome embarquant un micro-service Node.js + `zxcvbn`.
- Communication HTTP – le programme Java envoie chaque mot de passe au conteneur
  via une requête `POST` JSON et récupère un score de 0 à 4.
- Bibliothèque `org.json` – seul ajout externe, elle sert uniquement au parsing de la
  réponse JSON du conteneur.

Pourquoi `zxcvbn` ?

- Algorithme réaliste prenant en compte l’entropie, les motifs, les dictionnaires et les répétitions.
- Fournit un score de 0 à 4 directement transposable en cinq niveaux de robustesse.
- il est facilement encapsulable dans un micro-service Docker.

Pourquoi un serveur Node.js maison plutôt qu’une image publique ?

-pour la Maîtrise totale de l’outil et conformité au principe DevOps « construire son propre conteneur ».
- Léger, rapide, et totalement indépendant.

Analyse fonctionnelle

L’application se déroule en quatre étapes :

1. Vérification du service Docker : un appel test est effectué pour s’assurer que le
   conteneur de validation est bien lancé. Si ce n’est pas le cas, l’utilisateur en est averti
   et le programme s’arrête proprement avec la commande à exécuter.

2. Configuration : l’utilisateur choisit la longueur du mot de passe (défaut 12) et les
   types de caractères à inclure (majuscules, minuscules, chiffres, symboles). Par défaut,
   tous les types sont sélectionnés. Si aucun type n’est retenu, les minuscules et majuscules
   sont activées automatiquement.

3. Mode rafale : l’utilisateur indique combien de mots de passe il souhaite générer
   (défaut 1).

4. Génération et évaluation : pour chaque mot de passe demandé, le générateur produit
   une chaîne aléatoire respectant les contraintes, puis le validateur l’envoie au conteneur
   Docker qui retourne un score. Ce score est converti en libellé français et affiché.

 Analyse technique

 Générateur (`Password.java`)

- Utilise `SecureRandom` (non prédictible) pour tous les tirages aléatoires.
- Les ensembles de caractères sont conservés séparément afin de garantir qu’au moins un
  caractère de chaque type demandé figure dans le résultat.
- Les positions des caractères garantis sont mélangées aléatoirement (via `Collections.shuffle`)
  pour éviter un motif prévisible (ex. `Aa1!xxxx`).
- La longueur est limitée à 128 caractères, ce qui couvre tous les besoins réalistes.

Validateur (`PasswordValidator.java`)

- Communique avec le conteneur via `java.net.http.HttpClient` (intégré au JDK).
- Construit un corps JSON en échappant les caractères spéciaux (`escapeJson()`).
- Envoie une requête `POST` sur `http://localhost:3000/zxcvbn` avec un timeout de connexion
  (5 s) et un timeout global (10 s).
- Parse la réponse JSON avec `org.json` pour extraire le score.
- Mappe le score (0-4) vers les libellés français : `TRES_FAIBLE`, `FAIBLE`, `MOYEN`, `FORT`,
  `TRES_FORT`.
- En cas d’erreur (conteneur injoignable, réponse invalide), retourne un message explicite
  (`CONTENEUR_INDISPONIBLE` ou `ERREUR_API`).

Interface CLI (`Main.java`)

- Interaction en ligne de commande avec `Scanner`.
- Vérification préalable de la disponibilité du conteneur avant de commencer la génération.
- Gestion des saisies invalides avec valeurs par défaut et messages d’erreur.
- Capture des éventuelles exceptions du générateur (paramètres incohérents) et poursuite
  du traitement.

Conteneur Docker (`Dockerfile` + `server.js`)

- Image basée sur `node:20-alpine`.
- Le fichier `server.js` crée un serveur HTTP écoutant sur le port 3000.
- Seule la route `/zxcvbn` est acceptée ; toute autre requête reçoit un 404.
- La méthode `zxcvbn()` est appelée sur le mot de passe reçu et le score est renvoyé.
- Construction de l’image en une commande, exécution d’un simple `docker run`.

Prérequis

- Java 21 (JDK)
- Maven 3.6 ou supérieur
- Docker (Desktop ou Engine)

Installation et exécution

1. Construire l’image Docker du validateur

Depuis l'endroit où se trouve le `Dockerfile` :

`bash`
docker build -t password-checker .


2. Lancer le conteneur de validation

`bash`
docker run -d --name zxcvbn-api -p 3000:3000 password-checker


Vérifiez qu’il est bien actif :

`bash`
curl -X POST http://localhost:3000/zxcvbn -H "Content-Type: application/json" -d '{"password":"test"}'


Vous devez recevoir un JSON contenant "score".

3. Compiler et exécuter l’application Java

`bash`
Compilation, tests et empaquetage en un seul JAR exécutable
mvn clean package

Lancement de l'application
java -jar target/password-security-cli-1.0-SNAPSHOT-jar-with-dependencies.jar


4. Arrêter le conteneur (après utilisation)

`bash`
docker stop zxcvbn-api && docker rm zxcvbn-api


Exemple de session


    BIENVENUE SUR PASSWORD SECURITY CLI (Java 21)
Service de validation Docker : OK
Entrez la longueur du mot de passe (min 4, défaut 12) : 16
Inclure des MAJUSCULES ? (O/n) : o
Inclure des minuscules ? (O/n) : o
Inclure des chiffres ? (O/n) : o
Inclure des symboles ? (O/n) : o
Combien de mots de passe générer (Mode Rafale, défaut 1) : 3

RÉSULTAT DU MODE RAFALE (3 mot(s) de passe) :
[1] Mot de passe : G5$kL9@mP2&xY7!v
    Robustesse  : TRES_FORT
[2] Mot de passe : R4#tZ1!vB3$kL9@m
    Robustesse  : TRES_FORT
[3] Mot de passe : P2&xY7!vR4#tZ1!v
    Robustesse  : TRES_FORT

Merci d'avoir utilisé Password Security CLI ! À bientôt.


Auteur : BOKPE NANDJUI ANGE RODRIGUE