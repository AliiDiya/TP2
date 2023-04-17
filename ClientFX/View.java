package ClientFX;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import server.models.Course;

/**
 * Cette classe représente l'interface utilisateur de l'application client_fx
 */
public class View extends Parent {
    /**
     * Conteneur principal de la vue.
     */
    public HBox root = new HBox();
    /**
     * Liste déroulante pour choisir la session.
     */
    public ChoiceBox<String> choixSession;
    /**
     * Bouton pour charger les cours de la session sélectionnée.
     */
    public Button charger;
    /**
     * Tableau pour afficher les cours disponibles.
     */
    public TableView<Course> tableCourse;
    /**
     * Bouton pour envoyer le formulaire d'inscription.
     */
    public Button envoyer;
    /**
     * Champ de texte pour entrer le prénom de l'étudiant.
     */
    public TextField prenomTextField;
    /**
     * Champ de texte pour entrer le nom de l'étudiant.
     */
    public TextField nomTextField;
    /**
     * Champ de texte pour entrer l'email de l'étudiant.
     */
    public TextField emailTextField;
    /**
     * Champ de texte pour entrer le matricule de l'étudiant.
     */
    public TextField matriculeTextField;

    /**
     * Constructeur de la classe View.
     * Initialise les éléments de l'interface utilisateur.
     */
    public View() {
        //créer scene et root(qui va être HBox pour la partie gauche et droite)
        int width = 600;

        //créer les côtés gauche et droite en VBox
        int halfWidth = width/2;
        VBox left = new VBox();
        left.setMinWidth(halfWidth);
        left.setAlignment(Pos.TOP_CENTER);
        VBox right = new VBox();
        right.setMinWidth(halfWidth);
        right.setSpacing(10);
        right.setAlignment(Pos.TOP_CENTER);

        //ajouter les côtés gauche et droite au root et ajouter le séparateur au milieu
        root.getChildren().add(left);
        Separator mainSeparator = new Separator();
        mainSeparator.setOrientation(Orientation.VERTICAL);
        root.getChildren().add(mainSeparator);
        root.getChildren().add(right);

        //SECTION GAUCHE DE L'APPLICATION
        //Créer le titre pour la section gauche qui est allouée aux cours
        Text titreCours = new Text("Liste des cours");
        titreCours.setFont(Font.font("Arial", 20));
        left.getChildren().add(titreCours);

        //Créer la partie qui affichera le tableau des cours dans la partie gauche
        VBox table = new VBox();
        table.setPadding(new Insets(10,10,10,10));

        //Créer le tableau avec le model Course
        tableCourse = new TableView<>();
        TableColumn<Course, String> codeColumn = new TableColumn<>("Code");
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));

        TableColumn<Course, String> courseColumn = new TableColumn<>("Cours");
        courseColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        //Définition de la cellFactory pour la colonne du code
        codeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(item);
                }
            }
        });

        //Définition de la cellFactory pour la colonne du cours
        courseColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(item);
                }
            }
        });

        //Rajouter les colonnes au tableau
        tableCourse.getColumns().add(codeColumn);
        tableCourse.getColumns().add(courseColumn);
        tableCourse.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableCourse.setMaxHeight(300);

        //Rajouter le tableau dans la partie gauche du programme
        table.getChildren().add(tableCourse);
        left.getChildren().add(table);

        //les boutons de la partie gauche, le choiceBox et le bouton "charger"
        HBox boutons = new HBox();
        boutons.setAlignment(Pos.CENTER);
        boutons.setSpacing(40);

        choixSession = new ChoiceBox<>();
        choixSession.getItems().addAll("Automne", "Hiver", "Ete");
        choixSession.setValue("Automne");
        charger = new Button("charger");

        boutons.getChildren().addAll(choixSession, charger);
        left.getChildren().add(boutons);


        //SECTION DROITE DE L'APPLICATION
        //Créer le titre pour la section droite qui est allouée au formulaire d'inscription
        Text titreInscription = new Text("Formulaire d'inscription");
        titreInscription.setFont(Font.font("Arial", 20));
        right.getChildren().add(titreInscription);

        //Créer le champ à remplir pour le prénom
        HBox prenomChamp = new HBox();
        prenomChamp.setSpacing(10);
        prenomChamp.setAlignment(Pos.CENTER);

        Label prenom = new Label("Prénom");

        prenomTextField = new TextField();

        prenomChamp.getChildren().addAll(prenom, prenomTextField);
        right.getChildren().add(prenomChamp);

        //Créer le champ à remplir pour le nom
        HBox nomChamp = new HBox();
        nomChamp.setSpacing(25);
        nomChamp.setAlignment(Pos.CENTER);

        Label nom = new Label("Nom");

        nomTextField = new TextField();

        nomChamp.getChildren().addAll(nom, nomTextField);
        right.getChildren().add(nomChamp);

        //Créer le champ à remplir pour le nom
        HBox emailChamp = new HBox();
        emailChamp.setSpacing(25);
        emailChamp.setAlignment(Pos.CENTER);

        Label email = new Label("Email");

        emailTextField = new TextField();

        emailChamp.getChildren().addAll(email, emailTextField);
        right.getChildren().add(emailChamp);

        //Créer le champ à remplir pour le matricule
        HBox matriculeChamp = new HBox();
        matriculeChamp.setSpacing(8);
        matriculeChamp.setAlignment(Pos.CENTER);

        Label matricule = new Label("Matricule");

        matriculeTextField = new TextField();

        matriculeChamp.getChildren().addAll(matricule, matriculeTextField);
        right.getChildren().add(matriculeChamp);

        //Créer le bouton "envoyer"
        envoyer = new Button("envoyer");
        right.getChildren().add(envoyer);
    }
}
