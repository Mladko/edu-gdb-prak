package bs.edu.gdb.entity;

import edu.fhge.gdb.entity.Studienrichtung;

/**
 * Ein Objekt dieser Klasse repräsentiert eine Studienrichtung.
 * 
 * @author agribu
 * @version 20121220v0.2rc
 */
public class StudienrichtungImpl implements Studienrichtung {

    /* Kurzbezeichnung der Studienrichtung */
    private String kuerzel;
    
    /* Vollständige Bezeichnung der Studienrichtung */
    private String name;

    /**
     * Erstellt ein neues Studienrichtung-Objekt.
     * 
     * @param kuerzel   Kurzbezeichnung der Studienrichtung
     * @param name      Vollständige Bezeichnung der Studienrichtung
     */
    public StudienrichtungImpl(String kuerzel, String name) {
        
        this.kuerzel = kuerzel;
        this.name = name;
        
    }

    /**
     * Gibt die Kurzbezeichnung der Studienrichtung zurück.
     * 
     * @return Kurzbezeichnung der Studienrichtung
     */
    @Override
    public String getKuerzel() {
        
        return this.kuerzel;
        
    }

    /**
     * Gibt die vollständige Bezeichnung der Studienrichtung zurück.
     * 
     * @return Vollständige Bezeichnung der Studienrichtung
     */
    @Override
    public String getName() {
        
        return this.name;
        
    }

    /**
     * Gibt das Objekt als eindeutig indentifizierbaren String zurück.
     * 
     * @return Indentität des Objekts als String
     */
    @Override
    public String toString() {
        
        return this.name + " (" + this.kuerzel + ")";
        
    }

    /**
     * Gibt den Hashcode des Klassenobjekts zurück.
     * 
     * @return Hashcode des Klassenobjekts.
     */
    @Override
    public int hashCode() {
        
        return this.toString().hashCode();
        
    }

    /**
     * Vergleicht die Indentitäten zweier Klassenobjekte.
     * 
     * @param obj Übergebenes Klassenobjekt, welches verglichen werden soll.
     * @return TRUE bei gleichen Objektidentitäten, FALSE bei Unterscheidung.
     */
    @Override
    public boolean equals(Object obj) {
        
        return (obj instanceof StudienrichtungImpl
                && this.kuerzel == ((StudienrichtungImpl) obj).kuerzel
                && this.name == ((StudienrichtungImpl) obj).name);
        
    }
    
}
