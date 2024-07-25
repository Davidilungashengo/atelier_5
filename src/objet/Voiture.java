package objet;

public class Voiture extends Vehicule {
    private int nombreDePortes;

    public Voiture(int id, String nom, String marque, int annee, int nombreDePortes) {
        super(id, nom, marque, annee);
        this.nombreDePortes = nombreDePortes;
    }

    public int getNombreDePortes() { return nombreDePortes; }
    public void setNombreDePortes(int nombreDePortes) { this.nombreDePortes = nombreDePortes; }

    @Override
    public String toString() {
        return super.toString() + ", Nombre de portes: " + nombreDePortes;
    }
}