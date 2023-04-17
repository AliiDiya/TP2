package server;

import javafx.util.Pair;
import server.models.Course;
import server.models.RegistrationForm;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Classe Server qui représente un serveur d'inscription aux cours.
 * Le serveur écoute en continu sur un port spécifié et traite les commandes envoyées par les clients.
 * Il utilise des objets de la classe EventHandler pour gérer les événements liés aux commandes.
 */

public class Server {

    /**
     * Commande d'inscription.
     */
    public final static String REGISTER_COMMAND = "INSCRIRE";

    /**
     * Commande de chargement des cours.
     */
    public final static String LOAD_COMMAND = "CHARGER";
    private final ServerSocket server;
    private Socket client;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private final ArrayList<EventHandler> handlers;

    /**
     * Constructeur de la classe Server.
     * @param port Le port sur lequel le serveur écoute.
     * @throws IOException En cas d'erreur lors de la création du serveur socket.
     */
    public Server(int port) throws IOException {
        this.server = new ServerSocket(port, 1);
        this.handlers = new ArrayList<EventHandler>();
        this.addEventHandler(this::handleEvents);
    }

    /**
     * Ajoute un EventHandler pour gérer les événements liés aux commandes.
     * @param h L'objet EventHandler à ajouter.
     */
    public void addEventHandler(EventHandler h) {
        this.handlers.add(h);
    }

    /**
     * Alerte les EventHandlers en appelant leur méthode handle avec les arguments spécifiés.
     * @param cmd La commande à traiter.
     * @param arg L'argument associé à la commande.
     */
    private void alertHandlers(String cmd, String arg) {
        for (EventHandler h : this.handlers) {
            h.handle(cmd, arg);
        }
    }

    /**
     * Méthode principale pour exécuter le serveur.
     * Le serveur écoute en continu sur le port spécifié et traite les commandes des clients.
     */
    public void run() {
        while (true) {
            try {
                client = server.accept();
                System.out.println("Connecté au client: " + client);
                objectInputStream = new ObjectInputStream(client.getInputStream());
                objectOutputStream = new ObjectOutputStream(client.getOutputStream());
                listen();
                disconnect();
                System.out.println("Client déconnecté!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Méthode pour écouter les commandes des clients et les traiter.
     * @throws IOException En cas d'erreur lors de la lecture ou de l'écriture des objets.
     * @throws ClassNotFoundException En cas d'erreur de classe non trouvée lors de la désérialisation.
     */
    public void listen() throws IOException, ClassNotFoundException {
        String line;
        if ((line = this.objectInputStream.readObject().toString()) != null) {
            Pair<String, String> parts = processCommandLine(line);
            String cmd = parts.getKey();
            String arg = parts.getValue();
            this.alertHandlers(cmd, arg);
        }
    }

    /**
     * Méthode pour traiter une ligne de commande reçue du client et la diviser en commande et argument.
     * @param line La ligne de commande à traiter.
     * @return Un objet Pair contenant la commande et l'argument.
     */
    public Pair<String, String> processCommandLine(String line) {
        String[] parts = line.split(" ");
        String cmd = parts[0];
        String args = String.join(" ", Arrays.asList(parts).subList(1, parts.length));
        return new Pair<>(cmd, args);
    }

    /**
     * Ferme les flux de sortie et d'entrée ainsi que le socket client.
     * La méthode gère les exceptions si une erreur se produit lors de la fermeture des flux ou du socket.
     * @throws IOException si une erreur se produit lors de la fermeture des flux ou du socket
     */
    public void disconnect() throws IOException {
        objectOutputStream.close();
        objectInputStream.close();
        client.close();
    }

    /**
     * Gère les événements reçus du client en fonction de la commande (cmd) et de l'argument (arg) reçus.
     * Si la commande est REGISTER_COMMAND, appelle la méthode handleRegistration().
     * Si la commande est LOAD_COMMAND, appelle la méthode handleLoadCourses() avec l'argument arg.
     * @param cmd la commande reçue du client
     * @param arg l'argument reçu du client
     */
    public void handleEvents(String cmd, String arg) {
        if (cmd.equals(REGISTER_COMMAND)) {
            handleRegistration();
        } else if (cmd.equals(LOAD_COMMAND)) {
            handleLoadCourses(arg);
        }
    }

    /**
     Lire un fichier texte contenant des informations sur les cours et les transofmer en liste d'objets 'Course'.
     La méthode filtre les cours par la session spécifiée en argument.
     Ensuite, elle renvoie la liste des cours pour une session au client en utilisant l'objet 'objectOutputStream'.
     La méthode gère les exceptions si une erreur se produit lors de la lecture du fichier ou de l'écriture de l'objet dans le flux.
     @param arg la session pour laquelle on veut récupérer la liste des cours
     */
    public void handleLoadCourses(String arg){
        try {
            // Lecture du fichier cours.txt dans le dossier "data"
            BufferedReader reader = new BufferedReader(new FileReader("src/main/java/server/data/cours.txt"));
            String line;
            ArrayList<Course> courses = new ArrayList<>();

            // Parcours du fichier ligne par ligne
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                String code = parts[0];
                String name = parts[1];
                String session = parts[2];

                // Filtrage des cours selon la session spécifiée en argument
                if (session.equalsIgnoreCase(arg)) {
                    Course course = new Course(name, code, session);
                    courses.add(course);
                }
            }

            reader.close();

            // Envoi de la liste des cours au client via objectOutputStream
            objectOutputStream.writeObject(courses);
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     Récupérer l'objet 'RegistrationForm' envoyé par le client en utilisant 'objectInputStream', l'enregistrer dans un fichier texte
     et renvoyer un message de confirmation au client.
     La méthode gère les exceptions si une erreur se produit lors de la lecture de l'objet, l'écriture dans un fichier ou dans le flux de sortie.
     */
    public void handleRegistration() {
        try {
            // Récupération de l'objet 'RegistrationForm' envoyé par le client en utilisant 'objectInputStream'
            RegistrationForm form = (RegistrationForm) objectInputStream.readObject();

            // Création d'une chaîne de caractères contenant les informations de l'objet 'RegistrationForm'
            String output = form.getCourse().getSession() + "\t" +
                    form.getCourse().getCode() + "\t" +
                    form.getMatricule() + "\t" +
                    form.getPrenom() + "\t" +
                    form.getNom() + "\t" +
                    form.getEmail();

            // Création d'un objet 'PrintWriter' pour écrire dans un fichier texte
            // Le paramètre 'true' dans le constructeur indique que le fichier sera ouvert en mode append
            PrintWriter pw = new PrintWriter(new FileWriter("src/main/java/server/data/inscription.txt", true));

            // Écriture de la chaîne de caractères dans le fichier
            pw.println(output);

            // Fermeture du 'PrintWriter'
            pw.close();

            objectOutputStream.writeObject("Félicitations! Inscription réussie de " + form.getPrenom() + " au cours " + form.getCourse().getCode() + ".");

        } catch (IOException e) {
            // Gestion de l'exception IOException en affichant la trace de la pile d'exécution
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // Gestion de l'exception ClassNotFoundException en affichant un message d'erreur
            System.out.println("Classe n'a pas été trouvée");
        }
    }
}

