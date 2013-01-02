package bs.edu.gdb.entity;

import edu.fhge.gdb.entity.Student;

/**
 * Ein Objekt dieser Klasse repräsentiert einen Studenten.
 * 
 * @author agribu
 * @version 20121220v0.2rc
 */
public class StudentImpl implements Student {

    /* Matrikelnummer des Studenten */
    private String matrikel;
    
    /* Vorname des Studenten */
    private String vorname;
    
    /* Nachname des Studenten */
    private String name;
    
    /* Adresse des Studenten */
    private String adresse;
    
    /* Kürzel der Studienrichtung des Studenten */
    private String kuerzel;

    /**
     * Erstellt ein neues Studenten-Objekt.
     * 
     * @param adresse   Adresse des Studenten
     * @param matrikel  Matrikelnummer des Studenten
     * @param name      Nachname des Studenten
     * @param kuerzel   Kürzel der Studienrichtung des Studenten
     * @param vorname   Vorname des Studenten
     */
    public StudentImpl(String adresse, String matrikel, String name, String kuerzel, String vorname) {
        
        this.adresse = adresse;
        this.matrikel = matrikel;
        this.name = name;
        this.kuerzel = kuerzel;
        this.vorname = vorname;
    
    }

    /**
     * Gibt die Adresse zurück.
     * 
     * @return Adresse
     */
    @Override
    public String getAdresse() {
    
        return this.adresse;
    
    }

    /**
     * Gibt die Matrikelnummer zurück.
     * 
     * @return Matrikelnummer
     */
    @Override
    
    public String getMatrikel() {
    
        return this.matrikel;
    
    }

    /**
     * Gibt den Nachname zurück.
     * 
     * @return Nachname
     */
    @Override
    public String getName() {
        
        return this.name;
    
    }

    /**
     * Gibt den Kürzel der Studienrichtung zurück.
     * 
     * @return Kürzel der Studienrichtung
     */
    @Override
    public String getStudienrichtungKuerzel() {
    
        return this.kuerzel;
    
    }

    /**
     * Gibt den Vornamen zurück.
     * 
     * @return Vorname
     */
    @Override
    public String getVorname() {
    
        return this.vorname;
    
    }

    /**
     * Vergleicht die Indentitäten zweier Klassenobjekte.
     * 
     * @param obj Übergebenes Klassenobjekt, welches verglichen werden soll.
     * @return TRUE bei gleichen Objektidentitäten, FALSE bei Unterscheidung.
     */
    @Override
    public boolean equals(Object obj) {
    
        return obj instanceof Student && this.matrikel.equals(((Student) obj).getMatrikel());
    
    }

    /**
     * Gibt den Hashcode des Klassenobjekts zurück.
     * 
     * @return Hashcode des Klassenobjekts.
     */
    @Override
    public int hashCode() {
    
        return this.matrikel.hashCode();
    
    }

    /**
     * Gibt die Identität des Studenten zurück.
     * 
     * @return Identität des Studenten. (Name, Vorname, Matrikelnummer)
     */
    @Override
    public String toString() {
    
        return this.name + ", " + this.vorname + " (" + this.matrikel + ")";

    }

}
