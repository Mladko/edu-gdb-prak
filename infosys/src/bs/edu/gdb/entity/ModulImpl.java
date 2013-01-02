package bs.edu.gdb.entity;

import edu.fhge.gdb.entity.Modul;

/**
 * Ein Objekt dieser Klasse repräsentiert ein Studienmodul.
 * 
 * @author agribu
 * @version 20121220v0.2rc
 */
public class ModulImpl implements Modul {

    /** Modulbezeichnung */
    private String name;
    
    /** Modulkürzel */
    private String kuerzel;
    
    /** Anzahl Praktikumsstunden */
    private int praktikum;
    
    /** Anzahl Übungsstunden */
    private int uebung;
    
    /** Anzahl Vorlesungsstunden */
    private int vorlesung;
    
    /** Anzahl Credits des Moduls */
    private int credits;

    /** 
     * Erstellt ein neues ModulKlassen-Objekt
     * 
     * @param kuerzel   Modulkürzel
     * @param name      Modulbezeichnung
     * @param praktikum Anzahl Praktikumsstunden
     * @param uebung    Anzahl Übungsstunden
     * @param vorlesung Anzahl Vorlesungsstunden
     * @param credits   Anzahl Credits des Moduls
     */
    public ModulImpl(String kuerzel, String name, int praktikum, int uebung, int vorlesung, int credits) {
        
        this.kuerzel = kuerzel;
        this.name = name;
        this.praktikum = praktikum;
        this.uebung = uebung;
        this.vorlesung = vorlesung;
        this.credits = credits;
        
    }

    /**
     * Gibt das Modulkürzel zurück.
     * 
     * @return Modulkürzel
     */
    @Override
    public String getKuerzel() {
        
        return this.kuerzel;
        
    }

    /**
     * Gibt die Modulbezeichnung zurück.
     * 
     * @return Modulbezeichnung 
     */
    @Override
    public String getName() {
        
        return this.name;
        
    }

    /**
     * Gibt die Anzahl an Praktikumsstunden zurück.
     * 
     * @return Anzahl an Praktikumsstunden 
     */
    @Override
    public int getPraktikum() {
        
        return this.praktikum;
        
    }

    /**
     * Gibt die Anahl an Übungsstunden zurück.
     * 
     * @return Anzahl an Übungsstunden
     */
    @Override
    public int getUebung() {
        
        return this.uebung;
        
    }

    /**
     * Gibt die Anahl an Vorlesungsstunden zurück.
     * 
     * @return Anzahl an Vorlesungsstunden
     */
    @Override
    public int getVorlesung() {
        
        return this.vorlesung;
        
    }

    /** 
     * Gibt die Anzahl an Credits des Moduls zurück.
     *
     * @return Anzahl Credits des Moduls
     */
    @Override
    public int getCredits() {
        
        return this.credits;
        
    }

    /**
     * Gibt den Hashcode des Klassenobjekts zurück.
     * 
     * @return Hashcode des Klassenobjekts.
     */
    @Override
    public int hashCode() {
        
        return this.name.hashCode();
        
    }

    /**
     * Vergleicht die Indentitäten zweier Klassenobjekte.
     * 
     * @param obj Übergebenes Klassenobjekt, welches verglichen werden soll.
     * @return TRUE bei gleichen Objektidentitäten, FALSE bei Unterscheidung.
     */
    @Override
    public boolean equals(Object obj) {
        
        return obj instanceof Modul
                && this.kuerzel.equals(((Modul) obj).getKuerzel())
                && this.name.equals(((Modul) obj).getName());
        
    }

    /**
     * Gibt das Objekt als eindeutig indentifizierbaren String zurück.
     * 
     * @return Indentität des Objekts als String
     */
    @Override
    public String toString() {
        
        return this.name;
        
    }

}