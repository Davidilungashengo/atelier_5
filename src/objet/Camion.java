package objet;

public class Camion extends Vehicule {
    private int capaciteDeCharge;

    public Camion(int id, String nom, String marque, int annee, int capaciteDeCharge) {
        super(id, nom, marque, annee);
        this.capaciteDeCharge = capaciteDeCharge;
    }

    public int getCapaciteDeCharge() { return capaciteDeCharge; }
    public void setCapaciteDeCharge(int capaciteDeCharge) { this.capaciteDeCharge = capaciteDeCharge; }

    @Override
    public String toString() {
        return super.toString() + ", Capacité de charge: " + capaciteDeCharge + "kg";
    }
}