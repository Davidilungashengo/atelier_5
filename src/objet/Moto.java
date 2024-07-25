package objet;

public class Moto extends Vehicule {
    private int cylindree;

    public Moto(int id, String nom, String marque, int annee, int cylindree) {
        super(id, nom, marque, annee);
        this.cylindree = cylindree;
    }

    public int getCylindree() { return cylindree; }
    public void setCylindree(int cylindree) { this.cylindree = cylindree; }

    @Override
    public String toString() {
        return super.toString() + ", Cylindrée: " + cylindree + "cc";
    }
}