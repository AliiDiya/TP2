package client;

import server.models.Course;
import server.models.RegistrationForm;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * La classe Client représente le client qui se connecte au serveur pour s'inscrire à des cours.
 */
public class Client {
    /**
     * Commande d'inscription.
     */
    public final static String REGISTER_COMMAND = "INSCRIRE";

    /**
     * Commande de chargement des cours.
     */
    public final static String LOAD_COMMAND = "CHARGER";
    private static Client client;
    private Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private final Scanner scanner = new Scanner(System.in);
    private ArrayList<Course> courses;
    private String session;
    private String courseName;

    /**
     * Méthode principale pour lancer le client.
     * @param args Les arguments en ligne de commande.
     * @throws Exception Si une exception survient lors de l'exécution du client.
     */
    public static void main(String[] args) throws Exception {
        client = new Client();
        System.out.println("*** Bienvenue au portail d'inscription de cours de l'UDEM ***");
        client.charger();
        client.inscription();
        client.disconnect();
    }

    /**
     * Établit une connexion avec le serveur.
     */
    public void connect() {
        try{
            socket = new Socket("localhost", 1337);
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Charge la liste des cours offerts pour une session donnée.
     */
    public void charger(){
        try {
            client.connect();
            int choix = 0;
            while (choix < 1 || choix > 3) {
                // Afficher le menu de choix de session
                System.out.println("Veuillez choisir la session pour laquelle vous voulez consulter la liste des cours:");
                System.out.println("1. Automne");
                System.out.println("2. Hiver");
                System.out.println("3. Été");
                System.out.print("> Choix: ");
                choix = scanner.nextInt();

                if (choix < 1 || choix > 3) {
                    System.out.println("Erreur: Choix invalide. Veuillez choisir une valeur entre 1 et 3.");
                }
            }

            // Entrer la bonne valeur dans la variable session selon le choix
            switch (choix) {
                case 1:
                    session = "Automne";
                    break;
                case 2:
                    session = "Hiver";
                    break;
                case 3:
                    session = "Ete";
                    break;
                default:
                    System.out.println("Choix invalide. Fermeture du client.");
                    return;
            }

            // Envoyer une requête pour récupérer la liste des cours en fonction du choix de session
            objectOutputStream.writeObject(LOAD_COMMAND + " " + session);
            objectOutputStream.flush();

            // Lire et affiche la liste des cours envoyée par le serveur
            System.out.println("Les cours offerts pendant la session d'" + session + " sont:");
            courses = (ArrayList<Course>) objectInputStream.readObject();
            int compteur = 1;
            for (Course course : courses) {
                System.out.println(compteur + ". " + course.getCode() + "\t" + course.getName());
                compteur ++;
            }

            client.disconnect();

        } catch (IOException e) {
            // Gestion de l'exception IOException en affichant la trace de la pile d'exécution
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // Gestion de l'exception ClassNotFoundException en affichant un message d'erreur
            System.out.println("Classe n'a pas été trouvée");
        }
    }

    /**
     * Permet à l'utilisateur de s'inscrire à des cours.
     * @throws IOException En cas d'erreur lors de la lecture ou de l'écriture des objets.
     * @throws ClassNotFoundException En cas d'erreur de classe non trouvée lors de la désérialisation.
     */
    public void inscription() throws IOException, ClassNotFoundException {
        // Afficher le menu de choix pour consulter les cours offerts pour une autre session ou pour s'inscrire à un
        // cours dans la session déjà affichée.
        int choix = 0;
        while (choix < 1 || choix > 2) {
            System.out.println("> Choix:");
            System.out.println("1. Consulter les cours offerts pour une autre session");
            System.out.println("2. Inscription à un cours");
            System.out.print("> Choix: ");
            choix = scanner.nextInt();
            scanner.nextLine();
            if (choix < 1 || choix > 2) {
                System.out.println("Erreur: Choix invalide. Veuillez choisir une valeur de 1 ou 2.");
            }
            // Si l'utilisateur veut consulter les cours pour une autre session, on refait appel à la méthode charger()
            if (choix == 1){
                charger();
                // Mettre le choix à 0 pour recommencer la boucle
                choix = 0;
            }
        }

        String prenom = "";
        String nom = "";
        String email = "";
        String matricule = "";
        String code = "";

        // Saisie du prénom avec vérification
        while (prenom.isEmpty()) {
            System.out.print("Veuillez saisir votre prénom: ");
            prenom = scanner.nextLine();
        }

        // Saisie du nom avec vérification
        while (nom.isEmpty()) {
            System.out.print("Veuillez saisir votre nom: ");
            nom = scanner.nextLine();
        }

        // Saisie de l'email avec vérification
        while (email.isEmpty() || !email.matches("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}")) {
            System.out.print("Veuillez saisir votre email: ");
            email = scanner.nextLine();
            if (!email.isEmpty() && !email.matches("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}")) {
                System.out.println("Erreur: L'email n'est pas valide.");
            }
        }

        // Saisie du matricule avec vérification
        while (matricule.isEmpty() || !matricule.matches("\\d{6}")) {
            System.out.print("Veuillez saisir votre matricule (6 chiffres): ");
            matricule = scanner.nextLine();
            if (!matricule.isEmpty() && !matricule.matches("\\d{6}")) {
                System.out.println("Erreur: Le matricule doit contenir 6 chiffres.");
            }
        }

        // Saisie du code du cours avec vérification
        boolean coursExiste = false;
        while (!coursExiste) {
            System.out.print("Veuillez saisir le code du cours: ");
            code = scanner.nextLine();
            for (Course course : courses) {
                if (course.getCode().equals(code)) {
                    coursExiste = true;
                    // Récupérer le nom du cours choisi
                    courseName = course.getName();
                    break;
                }
            }
            if (!coursExiste) {
                System.out.println("Erreur: Le code du cours n'est pas valide.");
            }
        }

        // Créer le cours choisit et le formulaire remplit pour l'envoyer en requête
        Course coursInscrit = new Course(courseName, code, session);
        RegistrationForm form = new RegistrationForm(prenom, nom, email, matricule, coursInscrit);

        // Envoyer une requête d'inscription pour le cours choisi
        client.connect();
        objectOutputStream.writeObject(REGISTER_COMMAND);
        objectOutputStream.writeObject(form);
        objectOutputStream.flush();

        //Avoir la confirmation du serveur
        System.out.println(objectInputStream.readObject());
    }

    /**
     * Méthode pour se déconnecter du serveur.
     * @throws IOException En cas d'erreur lors de la déconnexion.
     */
    public void disconnect() throws IOException{
        objectOutputStream.close();
        objectInputStream.close();
        socket.close();
        }
    }

