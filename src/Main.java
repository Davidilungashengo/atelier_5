
import objet.*;
import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;git
import java.util.*;

class ParcVehicules {
    private HashMap<Integer, Vehicule> parc = new HashMap<>();
    private DatabaseConnection dbConnection;

    public ParcVehicules(String dbUrl, String dbUser, String dbPassword) throws SQLException {
        this.dbConnection = DatabaseConnection.getInstance(dbUrl, dbUser, dbPassword);
    }

    public void ajouterVehicule(Vehicule v) throws SQLException {
        // Check if the ID already exists
        if (rechercherVehiculeParId(v.getId()) != null) {
            System.out.println("Un véhicule avec cet ID existe déjà.");
            return;
        }
        // Insert the vehicle into the database
        String insertQuery = "INSERT INTO vehicules (id, nom, marque, annee, type) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(insertQuery)) {
            stmt.setInt(1, v.getId());
            stmt.setString(2, v.getNom());
            stmt.setString(3, v.getMarque());
            stmt.setInt(4, v.getAnnee());
            stmt.setString(5, v.getClass().getSimpleName()); // Get the type of the vehicle
            stmt.executeUpdate();
        }
        // Add the vehicle to the in-memory HashMap
        parc.put(v.getId(), v);
    }

    public void supprimerVehicule(int id) throws SQLException {
        // Delete the vehicle from the database
        String deleteQuery = "DELETE FROM vehicules WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(deleteQuery)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
        // Remove the vehicle from the in-memory HashMap
        parc.remove(id);
    }

    public void modifierVehicule(int id, Vehicule v) throws SQLException {
        // Update the vehicle in the database
        String updateQuery = "UPDATE vehicules SET nom = ?, marque = ?, annee = ? WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(updateQuery)) {
            stmt.setString(1, v.getNom());
            stmt.setString(2, v.getMarque());
            stmt.setInt(3, v.getAnnee());
            stmt.setInt(4, id);
            stmt.executeUpdate();
        }
        // Update the vehicle in the in-memory HashMap
        parc.put(id, v);
    }

    public Vehicule rechercherVehiculeParNom(String nom) throws SQLException {
        String selectQuery = "SELECT * FROM vehicules WHERE nom = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(selectQuery)) {
            stmt.setString(1, nom);
            ResultSet rs = stmt.executeQuery();
            if (((ResultSet) rs).next()) {
                int id = rs.getInt("id");
                String marque = rs.getString("marque");
                int annee = rs.getInt("annee");
                String type = rs.getString("type");
                Vehicule v = createVehiculeInstance(id, nom, marque, annee, type);
                return v;
            }
        }
        return null;
    }

    public Vehicule rechercherVehiculeParId(int id) throws SQLException {
        String selectQuery = "SELECT * FROM vehicules WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(selectQuery)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String nom = rs.getString("nom");
                String marque = rs.getString("marque");
                int annee = rs.getInt("annee");
                String type = rs.getString("type");
                Vehicule v = createVehiculeInstance(id, nom, marque, annee, type);
                return v;
            }
        }
        return null;
    }

    public List<Vehicule> listerVehiculesParLettre(char lettre) throws SQLException {
        List<Vehicule> result = new ArrayList<>();
        String selectQuery = "SELECT * FROM vehicules WHERE nom LIKE ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(selectQuery)) {
            stmt.setString(1, lettre + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String nom = rs.getString("nom");
                String marque = rs.getString("marque");
                int annee = rs.getInt("annee");
                String type = rs.getString("type");
                Vehicule v = createVehiculeInstance(id, nom, marque, annee, type);
                result.add(v);
            }
        }
        return result;
    }

    public int afficherNombreDeVehicules() throws SQLException {
        String selectQuery = "SELECT COUNT(*) FROM vehicules";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(selectQuery)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public void mettreAJourPartiellementVehicule(int id, Map<String, String> attributs) throws SQLException {
        Vehicule v = rechercherVehiculeParId(id);
        if (v != null) {
            String updateQuery = "UPDATE vehicules SET ";
            List<String> setClauses = new ArrayList<>();
            for (Map.Entry<String, String> entry : attributs.entrySet()) {
                switch (entry.getKey().toLowerCase()) {
                    case "nom":
                        setClauses.add("nom = '" + entry.getValue() + "'");
                        v.setNom(entry.getValue());
                        break;
                    case "marque":
                        setClauses.add("marque = '" + entry.getValue() + "'");
                        v.setMarque(entry.getValue());
                        break;
                    case "annee":
                        setClauses.add("annee = " + entry.getValue());
                        v.setAnnee(Integer.parseInt(entry.getValue()));
                        break;
                    default:
                        break;
                }
            }
            updateQuery += String.join(", ", setClauses) + " WHERE id = " + id;
            try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(updateQuery)) {
                stmt.executeUpdate();
            }
        }
    }

    public void chargerDonnees() throws SQLException {
        String selectQuery = "SELECT * FROM vehicules";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(selectQuery)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String nom = rs.getString("nom");
                String marque = rs.getString("marque");
                int annee = rs.getInt("annee");
                String type = rs.getString("type");
                Vehicule v = createVehiculeInstance(id, nom, marque, annee, type);
                parc.put(id, v);
            }
        }
    }

    private Vehicule createVehiculeInstance(int id, String nom, String marque, int annee, String type) throws SQLException {
        switch (type) {
            case "Voiture":
                String selectQuery = "SELECT nombreDePortes FROM voitures WHERE id = ?";
                try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(selectQuery)) {
                    stmt.setInt(1, id);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        int nombreDePortes = rs.getInt("nombreDePortes");
                        return new Voiture(id, nom, marque, annee, nombreDePortes);
                    }
                }
                break;
            case "Camion":
                String selectQuery1 = "SELECT capaciteDeCharge FROM camions WHERE id = ?";
                try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(selectQuery1)) {
                    stmt.setInt(1, id);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        int capaciteDeCharge = rs.getInt("capaciteDeCharge");
                        return new Camion(id, nom, marque, annee, capaciteDeCharge);
                    }
                }
                break;
            case "Moto":
                String selectQuery2 = "SELECT cylindree FROM motos WHERE id = ?";
                try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(selectQuery2)) {
                    stmt.setInt(1, id);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        int cylindree = rs.getInt("cylindree");
                        return new Moto(id, nom, marque, annee, cylindree);
                    }
                }
                break;
        }
        return null;
    }

    public void close() throws SQLException {
        dbConnection.close();
    }
}

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static ParcVehicules parc;

    public static void main(String[] args) {
        try {
            // Replace with your actual database credentials
            String dbUrl = "jdbc:mysql://localhost:3306/atelier";
            String dbUser = "root";
            String dbPassword = "";
            parc = new ParcVehicules(dbUrl, dbUser, dbPassword);
            parc.chargerDonnees();
            while (true) {
                afficherMenu();
                int choix = Integer.parseInt(scanner.nextLine());
                switch (choix) {
                    case 1:
                        ajouterVehicule();
                        break;
                    case 2:
                        supprimerVehicule();
                        break;
                    case 3:
                        modifierVehicule();
                        break;
                    case 4:
                        rechercherVehiculeParNom();
                        break;
                    case 5:
                        rechercherVehiculeParId();
                        break;
                    case 6:
                        listerVehiculesParLettre();
                        break;
                    case 7:
                        afficherNombreDeVehicules();
                        break;
                    case 8:
                        System.out.println("Au revoir!");
                        parc.close();
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Choix invalide. Veuillez réessayer.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void afficherMenu() {
        System.out.println("Menu:");
        System.out.println("1. Ajouter un véhicule");
        System.out.println("2. Supprimer un véhicule");
        System.out.println("3. Modifier un véhicule");
        System.out.println("4. Rechercher un véhicule par nom");
        System.out.println("5. Rechercher un véhicule par identifiant");
        System.out.println("6. Lister les véhicules par lettre");
        System.out.println("7. Afficher le nombre de véhicules");
        System.out.println("8. Quitter");
        System.out.print("Choisissez une option: ");
    }

    private static void ajouterVehicule() throws SQLException {
        System.out.print("Entrez le type de véhicule (voiture, camion, moto): ");
        String type = scanner.nextLine().toLowerCase();
        System.out.print("Entrez l'ID: ");
        int id = Integer.parseInt(scanner.nextLine());
        System.out.print("Entrez le nom: ");
        String nom = scanner.nextLine();
        System.out.print("Entrez la marque: ");
        String marque = scanner.nextLine();
        System.out.print("Entrez l'année: ");
        int annee = Integer.parseInt(scanner.nextLine());

        switch (type) {
            case "voiture":
                System.out.print("Entrez le nombre de portes: ");
                int nombreDePortes = Integer.parseInt(scanner.nextLine());
                parc.ajouterVehicule(new Voiture(id, nom, marque, annee, nombreDePortes));
                break;
            case "camion":
                System.out.print("Entrez la capacité de charge (kg): ");
                int capaciteDeCharge = Integer.parseInt(scanner.nextLine());
                parc.ajouterVehicule(new Camion(id, nom, marque, annee, capaciteDeCharge));
                break;
            case "moto":
                System.out.print("Entrez la cylindrée (cc): ");
                int cylindree = Integer.parseInt(scanner.nextLine());
                parc.ajouterVehicule(new Moto(id, nom, marque, annee, cylindree));
                break;
            default:
                System.out.println("Type de véhicule invalide.");
        }
    }

    private static void supprimerVehicule() throws SQLException {
        System.out.print("Entrez l'ID du véhicule à supprimer: ");
        int id = Integer.parseInt(scanner.nextLine());
        parc.supprimerVehicule(id);
    }

    private static void modifierVehicule() throws SQLException {
        System.out.print("Entrez l'ID du véhicule à modifier: ");
        int id = Integer.parseInt(scanner.nextLine());
        Vehicule v = parc.rechercherVehiculeParId(id);
        if (v != null) {
            System.out.print("Entrez le nouveau nom (laissez vide pour ne pas changer): ");
            String nom = scanner.nextLine();
            if (!nom.isEmpty()) {
                v.setNom(nom);
            }
            System.out.print("Entrez la nouvelle marque (laissez vide pour ne pas changer): ");
            String marque = scanner.nextLine();
            if (!marque.isEmpty()) {
                v.setMarque(marque);
            }
            System.out.print("Entrez la nouvelle année (laissez vide pour ne pas changer): ");
            String anneeStr = scanner.nextLine();
            if (!anneeStr.isEmpty()) {
                int annee = Integer.parseInt(anneeStr);
                v.setAnnee(annee);
            }
            parc.modifierVehicule(id, v);
        } else {
            System.out.println("Véhicule non trouvé.");
        }
    }

    private static void rechercherVehiculeParNom() throws SQLException {
        System.out.print("Entrez le nom du véhicule: ");
        String nom = scanner.nextLine();
        Vehicule v = parc.rechercherVehiculeParNom(nom);
        if (v != null) {
            System.out.println(v);
        } else {
            System.out.println("Véhicule non trouvé.");
        }
    }

    private static void rechercherVehiculeParId() throws SQLException {
        System.out.print("Entrez l'ID du véhicule: ");
        int id = Integer.parseInt(scanner.nextLine());
        Vehicule v = parc.rechercherVehiculeParId(id);
        if (v != null) {
            System.out.println(v);
        } else {
            System.out.println("Véhicule non trouvé.");
        }
    }

    private static void listerVehiculesParLettre() throws SQLException {
        System.out.print("Entrez la lettre: ");
        char lettre = scanner.nextLine().charAt(0);
        List<Vehicule> vehicules = parc.listerVehiculesParLettre(lettre);
        for (Vehicule v : vehicules) {
            System.out.println(v);
        }
    }

    private static void afficherNombreDeVehicules() throws SQLException {
        System.out.println("Nombre de véhicules: " + parc.afficherNombreDeVehicules());
    }
}