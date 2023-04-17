package ClientFX;

import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import server.models.Course;
import server.models.RegistrationForm;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Cette classe représente le client JavaFX pour l'application d'inscription à des cours à l'UDEM.
 * Elle hérite de la classe Application de JavaFX pour gérer l'interface graphique.
 */
public class ClientFX extends Application {
    private View view;
    private Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    /**
     * Commande de chargement des cours.
     */
    public final static String LOAD_COMMAND = "CHARGER";

    /**
     * Commande d'inscription.
     */
    public final static String REGISTER_COMMAND = "INSCRIRE";
    private ArrayList<Course> courses;
    private String session;

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Point d'entrée de l'application JavaFX. Cette méthode est appelée au démarrage de l'application.
     * Elle crée l'interface utilisateur, configure les actions des boutons et affiche la fenêtre principale.
     * @param primaryStage L'objet Stage principal de l'application JavaFX.
     */
    @Override
    public void start(Stage primaryStage) {
        view = new View();

        charger();
        inscription();

        primaryStage.setTitle("Inscription UDEM");
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(view.root,600,400));
        primaryStage.show();
    }

    /**
     * Méthode pour charger les cours à partir du serveur en fonction de la session sélectionnée.
     * Elle configure l'action du bouton "Charger" pour envoyer une demande au serveur et afficher les cours dans le tableau.
     */
    public void charger(){
        // Lorsque l'utilisateur clique sur le bouton "charger"
        view.charger.setOnAction((action) -> {
            try{
                connect();
                view.tableCourse.getItems().clear();
                // Prendre la valeur de la session à partir du choiceBox
                session = view.choixSession.getValue();
                // Envoyer la commande au serveur
                objectOutputStream.writeObject(LOAD_COMMAND + " " + session);
                objectOutputStream.flush();
                // Lire la liste des cours envoyée par le serveur
                courses = (ArrayList<Course>) objectInputStream.readObject();
                // Rajouter les cours au tableau de l'application
                for (Course course : courses) {
                    view.tableCourse.getItems().add(new Course(course.getName(),course.getCode(), session));
                }
                disconnect();
            }catch (Exception e){
                e.printStackTrace();
            }
        });
    }

    /**
     * Méthode pour gérer l'inscription à un cours. Elle configure l'action du bouton "Envoyer" pour envoyer le
     * formulaire d'inscription au serveur.
     * Elle effectue également des vérifications sur les champs du formulaire avant d'envoyer les données au serveur.
     */
    public void inscription(){
        // Lorsque l'utilisateur clique sur le bouton "envoyer"
        view.envoyer.setOnAction((action) -> {
            try{
                // boolean true s'il n'y a aucune erreur dans le formulaire
                boolean noError = true;
                // Créer un tableau pour les erreurs
                List<String> erreurs = new ArrayList<>();

                // Vérifier si aucun cours n'est sélectionné
                if (view.tableCourse.getSelectionModel().isEmpty()) {
                    erreurs.add("Vous devez sélectionner un cours!");
                }

                // Vérifier si le champ prénom est vide
                String prenom = view.prenomTextField.getText();
                if (prenom.isEmpty()) {
                    erreurs.add("Vous devez entrer votre prénom!");
                }

                String nom = view.nomTextField.getText();
                // Vérifier si le champ nom est vide
                if (nom.isEmpty()) {
                    erreurs.add("Vous devez entrer votre nom!");
                }

                String email = view.emailTextField.getText();
                // Vérifier si le champ email est vide ou ne respecte pas la structure d'un email
                if (email.isEmpty() || !email.matches("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}")){
                    erreurs.add("Le champ 'Email' est invalide!");
                }

                String matricule = view.matriculeTextField.getText();
                // Vérifier si le champ matricule est vide ou n'est pas composé de 6 chiffres
                if(matricule.isEmpty() || !matricule.matches("\\d{6}")){
                    erreurs.add("Le champ 'Matricule' est invalide! (doit être composé de 6 chiffres)");
                }

                // Vérifier si la liste d'erreurs n'est pas vide
                if (!erreurs.isEmpty()) {
                    // Afficher les messages d'erreur
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Le formulaire est invalide");
                    alert.setContentText(String.join("\n", erreurs));
                    alert.showAndWait();
                    // S'il y a des erreurs mettre le boolean à false
                    noError = false;
                }

                // Vérifier s'il n'y a aucune erreur, si aucune erreur, on envoie le formulaire au serveur
                if (noError) {
                    // Créer un objet Course selon le cours sélectionné
                    Course selectedCourse = view.tableCourse.getSelectionModel().getSelectedItem();

                    String code = selectedCourse.getCode();
                    String cours = selectedCourse.getName();

                    Course coursInscrit = new Course(cours, code, session);
                    // Créer un objet RegistrationForm avec les informations rempli par l'utilisateur
                    RegistrationForm form = new RegistrationForm(prenom, nom, email, matricule, coursInscrit);

                    // Envoyer la commande et le formulaire au serveur
                    connect();
                    objectOutputStream.writeObject(REGISTER_COMMAND);
                    objectOutputStream.writeObject(form);
                    objectOutputStream.flush();

                    // Lire le message de succès envoyé par le serveur
                    String message = (String) objectInputStream.readObject();

                    Alert alertSuccess = new Alert(Alert.AlertType.INFORMATION);
                    alertSuccess.setTitle("Message");
                    alertSuccess.setHeaderText("Message");
                    alertSuccess.setContentText(message);
                    alertSuccess.showAndWait();

                    disconnect();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        });
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
     * Méthode pour se déconnecter du serveur.
     * @throws IOException En cas d'erreur lors de la déconnexion.
     */
    public void disconnect() throws IOException {
        objectOutputStream.close();
        objectInputStream.close();
        socket.close();
    }
}
