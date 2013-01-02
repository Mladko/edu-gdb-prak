package bs.edu.gdb.entity;

import edu.fhge.gdb.entity.Modul;
import edu.fhge.gdb.entity.Praktikumsteilnahme;
import edu.fhge.gdb.entity.Student;

/**
 * Ein Objekt dieser Klasse repräsentiert den Umfang einer Praktikumsteilnahme.
 * 
 * @author agribu
 * @version 20121220v0.2rc
 */
public class PraktikumsteilnahmeImpl implements Praktikumsteilnahme {

    /** Studienmodul */
    private Modul mod;
    
    /** Studiensemester (SS/WS) */
    private String sem;
    
    /** Teilnehmender Student */
    private Student stud;
    
    /** Info, ob das Testat erfolgreich war */
    private boolean testat;

    /**
     * Erstellt ein neues Praktikumsteilnahme-Objekt
     * 
     * @param stud      Teilnehmender Student
     * @param mod       Studienmodul
     * @param sem       Studiensemester
     * @param testat    Info, ob das Testat erfolgreich war
     */
    public PraktikumsteilnahmeImpl(Student stud, Modul mod, String sem, boolean testat) {
        
        this.stud = stud;
        this.mod = mod;
        this.sem = sem;
        this.testat = testat;
        
    }

    /**
     * Liefert den teilnehmenden Studenten.
     * 
     * @return Teilnehmender Student
     */
    @Override
    public Student getStudent() {
        
        return this.stud;
        
    }

    /**
     * Liefert das Studienmodul.
     * 
     * @return Studienmodul
     */
    @Override
    public Modul getModul() {
        
        return this.mod;
        
    }

    /**
     * Liefert das Studiensemester (SS/WS)
     * 
     * @return Studiensemester
     */
    @Override
    public String getSemester() {
        
        return this.sem;
        
    }

    /**
     * Liefert die Info, ob das Testat erfolgreich war.
     * 
     * @return TRUE Testat war erfolreich, FALSE Testat fehlgeschlagen
     */
    @Override
    public boolean isTestat() {
        
        return this.testat;
        
    }

    /**
     * Setzte das Ergebnis zum Testat.
     * 
     * @param testat Ergebnis zum Testat
     */
    @Override
    public void setTestat(boolean testat) {
        
        this.testat = testat;
        
    }

    /**
     * Vergleicht die Indentitäten zweier Klassenobjekte.
     * 
     * @param obj Übergebenes Klassenobjekt, welches verglichen werden soll.
     * @return TRUE bei gleichen Objektidentitäten, FALSE bei Unterscheidung.
     */
    @Override
    public boolean equals(Object obj) {
        
        return obj instanceof Praktikumsteilnahme
                && this.stud.equals(((Praktikumsteilnahme) obj).getStudent())
                && this.mod.equals(((Praktikumsteilnahme) obj).getModul())
                && this.sem.equals(((Praktikumsteilnahme) obj).getSemester());
        
    }

    /**
     * Gibt den Hashcode des Klassenobjekts zurück.
     * 
     * @return Hashcode des Klassenobjekts.
     */
    @Override
    public int hashCode() {
        
        return this.stud.hashCode() * this.mod.hashCode() * this.sem.hashCode();
        
    }
  
    /**
     * Gibt das Objekt als eindeutig indentifizierbaren String zurück.
     * 
     * @return Indentität des Objekts als String
     */
    @Override
    public String toString() {
        
        return this.mod + ", " + this.sem + ", " + this.stud + ", " + this.stud;
        
    }
    
}