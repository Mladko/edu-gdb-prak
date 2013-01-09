package bs.edu.gdb;

import bs.edu.gdb.entity.ModulImpl;
import bs.edu.gdb.entity.PraktikumsteilnahmeImpl;
import bs.edu.gdb.entity.StudentImpl;
import bs.edu.gdb.entity.StudienrichtungImpl;

import edu.fhge.gdb.ApplicationException;
import edu.fhge.gdb.DataAccessObject;
import edu.fhge.gdb.entity.Modul;
import edu.fhge.gdb.entity.Praktikumsteilnahme;
import edu.fhge.gdb.entity.Student;
import edu.fhge.gdb.entity.Studienrichtung;

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.jdbc.JDBCPieDataset;

/**
 * Ein Objekt dieser Klasse dient der Bindung zwischen der GUI und der Derby DB.
 * 
 * @author agribu
 * @version 20121230v0.1rc
 */
public class DataAccessObjectImpl implements DataAccessObject {

    /** Datenbankverbindung */
    private Connection con;
    /** JDBC-Treiber-Pfad */
    private static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    /** Pfad zur Datenbank */
    private static final String URL = "jdbc:derby:dist/gdb-praktikum";

    /**
     * Erstellt ein DataAccessObjectImpl-Objekt
     */
    public DataAccessObjectImpl() {

        /* Laden des JDBC-Treibers */
        try {

            Class.forName(DRIVER).newInstance();

        } catch (Exception ex) {

            System.err.println("Der JDBC.Treiber konnte nicht geladen werden.");

        }

        /* Verbindung zur Datenbank herstellen */
        try {

            this.con = DriverManager.getConnection(URL);

        } catch (SQLException ex) {

            System.out.println("Konstruktor: DataAccessObjectImpl");
            System.err.println("Die Verbindung zur Datenbank konnte nicht hergestellt werden.\n"
                    + "Die Fehlermeldung lautet: " + ex.getMessage());
            ex.printStackTrace();

        }

    }

    /**
     * Fügt einen neuen Studenten in die Datenbank hinzu
     * 
     * @param adresse                   Adresse des Studenten
     * @param matrikel                  Matrikelnummer des Studenten
     * @param name                      Nachname des Studenten
     * @param kuerzel                   Kürzel der Studienrichtung des Studenten
     * @param vorname                   Vorname des Studenten
     * @throws ApplicationException     Student konnte nicht hinzugefügt werden.
     */
    @Override
    public void addStudent(String matrikel, String name, String vorname, String adresse, String kuerzel)
            throws ApplicationException {

        try {

            /* Vorbereitung der SQL Abfrage */
            String sqlQuery = "select matrikel from student where matrikel = ?";
            PreparedStatement preStmt = this.con.prepareStatement(sqlQuery);
            preStmt.setString(1, matrikel);

            /* Hinzufügen des Studenten */
            ResultSet rs = preStmt.executeQuery();
            /* 
             * next() ist true, wenn mehr als eine Zeile existiert.
             * D.h. es existiert schon mindestens ein Eintrag mit derselben Matrikelnummer.
             */
            if (rs.next()) {

                rs.close();
                throw new ApplicationException("Die Matrikelnummer existiert bereits.");

            }
            rs.close();
            preStmt.close();

            /* Hinzufügen des Studenten in die Datenbank */
            sqlQuery = "insert into student(matrikel, name, vorname, adresse, skuerzel) "
                    + "values(?, ?, ?, ?, ?)";
            preStmt = this.con.prepareStatement(sqlQuery);
            preStmt.setString(1, matrikel);     //Matrikelnummer des Studenten
            preStmt.setString(2, name);         //Nachname des Studenten
            preStmt.setString(3, vorname);      //Vorname des Studenten
            preStmt.setString(4, adresse);      //Adresse des Studenten
            preStmt.setString(5, kuerzel);      //Studienrichtungskürzel des Studenten
            
            preStmt.execute();
            preStmt.close();

        } catch (SQLException ex) {

            System.out.println("Methode: addStudent");
            System.err.println(ex.getLocalizedMessage());
            ex.printStackTrace();

        }

    }

    /**
     * Einfügen einer Praktikumsteilnahme und ggf. eines neuen Studenten in die Datenbank.
     * 
     * @param matrikel                  Matrikelnummer des Studenten
     * @param name                      Nachname des Studenten
     * @param vorname                   Vorname des Studenten
     * @param adresse                   Adresse des Studenten
     * @param srKuerzel                 Studienrichtungskürzel des Studenten
     * @param modul                     Modulbezeichnung
     * @param semester                  Aktuelles Semester (WS/SS)
     * @return                          TRUE wenn neuer Student hinzugefügt wurde, FALSE wenn Student bereits vorhanden war
     * @throws ApplicationException     1) Aktuelles Modul nicht Bestandteil der Studienrichtung des Studenten
     *                                  2) Modul beinhaltet kein Praktikum
     *                                  3) Anmeldung ist bereits erfolgt
     *                                  4) Keine oder eine nicht erfasste Studienrichtung angegeben
     */
    @Override
    public boolean announce(String matrikel, String name, String vorname,
            String adresse, String srKuerzel, Modul modul, String semester)
            throws ApplicationException {

        /* Info, ob ein neuer Student hinzugefügt wurde */
        boolean studentAdded = true;
        
        /* Errorcode zur Fehlerbehandlung */
        int errcode = -1;

        try {

            /* Änderungen sollen erst übergeben werden, wenn alles erfolgreich war */
            this.con.setAutoCommit(false);

            try {
                /* Erstelle ggf. neuen Studenten, wenn existiert Fehlermeldung abfangen */
                try {

                    this.addStudent(matrikel, name, vorname, adresse, srKuerzel);

                } catch (ApplicationException appex) {

                    /* Student existiert bereits */
                    studentAdded = false;

                }

                /*
                 * Prüfe, ob Modul Bestandteil der Studienrichtung und dieses ein Praktikum beinhaltet.
                 */

                /* Vorbereitung der SQL Abfrage */
                String sqlQuery = "select distinct * from kategorieumfang where skuerzel = ?";
                PreparedStatement preStmt = this.con.prepareStatement(sqlQuery);
                preStmt.setString(1, srKuerzel);

                /* Durchführung der SQL Abfrage */
                ResultSet rs = preStmt.executeQuery();

                /* Info, ob Modul Bestandteil der Studienrichtung */
                boolean istEnthalten = false;

                while (rs.next() && !istEnthalten) {

                    istEnthalten = rs.getString("mkuerzel").equals(modul.getKuerzel());

                }
                if (!istEnthalten) {

                    errcode = 0;

                }

                /* Info, ob Modul ein Praktikum beinhaltet */
                if (modul.getPraktikum() == 0) {

                    errcode = 1;

                }

                rs.close();
                preStmt.close();

                /*
                 * Prüfe, ob Student bereits angemeldet wurde.
                 */

                /* Vorbereitung der SQL Abfrage */
                sqlQuery = "select * from praktikumsteilnahme"
                        + " where matrikel = ?"
                        + " and mkuerzel = ?"
                        + " and semester = ?";
                preStmt = this.con.prepareStatement(sqlQuery);
                preStmt.setString(1, matrikel);
                preStmt.setString(2, modul.getKuerzel());
                preStmt.setString(3, semester);

                /* Durchführung der SQL Abfrage */
                rs = preStmt.executeQuery();

                /* next() ist true, wenn mehr als eine Zeile existiert. */
                if (rs.next()) {

                    errcode = 2;

                }

                rs.close();
                preStmt.close();

                /*
                 * Prüfe, ob keine oder eine nicht erfasste Studienrichtung angegeben wurde.
                 */

                /* Vorbereitung der SQL Abfrage */
                sqlQuery = "select * from studienrichtung where skuerzel = ?";
                preStmt = this.con.prepareStatement(sqlQuery);
                preStmt.setString(1, srKuerzel);

                /* Durchführung der SQL Abfrage */
                rs = preStmt.executeQuery();

                /* Prüfe, ob Einträge vorhanden sind */
                if (!rs.next()) {

                    errcode = 3;

                }

                rs.close();
                preStmt.close();
                
                /* Fehlerbehandlung */
                if (errcode != -1) {
                    
                    /* Fehlermeldung */
                    String errMsg = "";
                    
                    /* Verwerfe Änderungen */
                    try {

                        this.con.rollback();

                    } catch (SQLException sqlex1) {

                        System.out.println("Methode: announce");
                        System.err.println(sqlex1.getLocalizedMessage());
                        sqlex1.printStackTrace();

                    }
                    
                    this.con.setAutoCommit(true);
 
                    /* Ausgabe: Fehlermeldung und Abbruch des Programms */
                    switch (errcode) {
                        
                        case 0: {
                            
                            errMsg = "Das übergebene Modul ist nicht Bestandteil der Studienrichtung des anzumeldenden Studenten!";
                            System.out.println("Errcode: 0: " + errMsg);
                            throw new ApplicationException(errMsg);
                            
                        }
                            
                        case 1: {
                            
                            errMsg = "Das übergebene Modul sieht kein Praktikum vor!";
                            System.out.println("Errcode: 1: " + errMsg);
                            throw new ApplicationException(errMsg);
                            
                        }
                            
                        case 2: {
                            
                            errMsg = "Es existiert bereits eine Anmeldung des übergebenen Studenten am übergebenen Modul im übergebenen Semester.";
                            System.out.println("Errcode: 2: " + errMsg);
                            throw new ApplicationException(errMsg);
                            
                        }
                            
                        case 3: {
                            
                            errMsg = "Es wurde keine oder eine nicht erfasste Studienrichtung für den neu anzulegenden Studenten angegeben.";
                            System.out.println("Errcode: 3: " + errMsg);
                            throw new ApplicationException(errMsg);
                            
                        }
                        
                        default: {

                            errMsg = "Ein Fehler zur Anmeldung einer Praktikumsteilnahme ist aufgetreten.";
                            System.out.println("Errcode: default: " + errMsg);
                            throw new ApplicationException(errMsg);

                        }     
                        
                    }
                    
                    return;
                    
                }

                /*
                 * Wenn alle Prüfungen erfolgreich, pflege neue Daten in die Datenbank ein.
                 */

                /* Vorbereitung der SQL Abfrage */
                sqlQuery = "insert into praktikumsteilnahme(matrikel, mkuerzel, semester)"
                        + " values(?, ?, ?)";
                preStmt = this.con.prepareStatement(sqlQuery);
                preStmt.setString(1, matrikel);
                preStmt.setString(2, modul.getKuerzel());
                preStmt.setString(3, semester);

                /* Durchführung der SQL Abfrage */
                System.out.println(preStmt.executeUpdate());

                preStmt.close();

                /* Einpflegen der Änderungen in die Datenbank */
                this.con.commit();

            } catch (SQLException sqlex) {

                /* Verwerfe Änderungen */
                try {

                    this.con.rollback();

                } catch (SQLException sqlex1) {

                    System.out.println("Methode: announce");
                    System.err.println(sqlex1.getLocalizedMessage());
                    sqlex1.printStackTrace();

                }

                System.out.println("Methode: announce");
                System.err.println(sqlex.getLocalizedMessage());
                sqlex.printStackTrace();

            }

            /* Zurücksetzen von AutoCommit */
            this.con.setAutoCommit(true);

        } catch (SQLException sqlex2) {

            System.out.println("Methode: announce");
            System.err.println(sqlex2.getLocalizedMessage());
            sqlex2.printStackTrace();

        }

        return studentAdded;

    }

    /**
     * Pflegt alle Testate in die Datenbank ein.
     * 
     * @param testate Collection aller Testate
     */
    @Override
    public void setTestate(Collection<Praktikumsteilnahme> testate) {

        try {

            /* Änderungen sollen erst übergeben werden, wenn alles erfolgreich war */
            this.con.setAutoCommit(false);

            PreparedStatement preStmt;

            for (Praktikumsteilnahme testat : testate) {

                /* Vorbereitung der SQL Abfrage */
                String sqlQuery = "update praktikumsteilnahme set testat = ?"
                        + " where matrikel = ? and semester = ? and mkuerzel = ?";
                preStmt = this.con.prepareStatement(sqlQuery);

                /* Hinzufügen des Testats */
                preStmt.setBoolean(1, testat.isTestat());
                preStmt.setString(2, testat.getStudent().getMatrikel());
                preStmt.setString(3, testat.getSemester());
                preStmt.setString(4, testat.getModul().getKuerzel());

                preStmt.executeUpdate();
                preStmt.close();
            }

            /* Übergeben der Änderungen an die Datenbank */
            this.con.commit();

        } catch (SQLException ex) {

            /* Falls Exception, verwerfe Änderungen */
            try {

                this.con.rollback();

            } catch (SQLException exl) {

                System.out.println("Methode: setTestate");
                System.err.println(exl.getLocalizedMessage());
                exl.printStackTrace();


            }

            System.out.println("Methode: setTestate");
            System.err.println(ex.getLocalizedMessage());
            ex.printStackTrace();


        }

        try {

            /* Zurücksetzen von AutoCommit */
            this.con.setAutoCommit(true);

        } catch (SQLException ex) {

            System.out.println("Methode: setTestate");
            System.err.println(ex.getLocalizedMessage());
            ex.printStackTrace();

        }

    }

    /** 
     * Gibt den Studienverlaufsplan einer Studienrichtung als Liste zurück.
     * 
     * @param sr    Studienrichtung
     * @return      Studienverlaufsplan einer Studienrichtung
     * @throws ApplicationException     Fehler zur Ermittlung des Studienverlaufsplan.
     */
    @Override
    public List<List<String>> getStudienverlaufsplan(Studienrichtung sr)
            throws ApplicationException {

        /* Liste des Studienverlaufsplan */
        List<List<String>> sVerlPlan = new ArrayList<List<String>>();

        /* --- Header --- 
         * <editor-fold defaultstate="collapsed" desc="Header"> 
         */

        /* -- Beschriftung der 1. Reihe -- */
        List<String> hl0 = new ArrayList<String>();

        /* Überschrift */
        hl0.add(0,
                "Studienverlaufsplan\n"
                + sr.getName()
                + " (" + sr.getKuerzel() + ")");

        /* Beschriftungen der Semester */
        for (int i = 1; i <= 6; i++) {

            hl0.add(i, i + "." + "Semester");

        }
        hl0.add(7, "");
        sVerlPlan.add(hl0);

        /* -- Beschriftung der 2. Reihe -- */
        List<String> hl1 = new ArrayList<String>();

        hl1.add(0, "Nr  Kategorie");
        for (int i = 1; i <= 6; i++) {

            hl1.add(i, "Mod  V  Ü  P  Cr");

        }
        hl1.add(7, "Summe");
        sVerlPlan.add(hl1);

        // </editor-fold>

        /* --- Content ---
         * <editor-fold defaultstate="collapsed" desc="Content"> 
         */
        int maxNumOfKategories = getMaxNumOfKategories(sr);
        List<String> cnt;
        int[] summeSWS = new int[7];

        try {

            /* Iteration über Kategorien */
            for (int lfdnr = 1; lfdnr <= maxNumOfKategories; lfdnr++) {
                
                cnt = new ArrayList<String>();

                /* -- Erste Spalte -- */

                /* Vorbereiten des SQL-Abfrage */
                String sqlcurKat = "select distinct name"
                        + " from kategorie"
                        + " where lfdnr = ?"
                        + " and skuerzel = ?";
                PreparedStatement preStmtCurKat = this.con.prepareStatement(sqlcurKat);
                preStmtCurKat.setInt(1, lfdnr);
                preStmtCurKat.setString(2, sr.getKuerzel());

                /* Durchführung der SQL-Abfrage */
                ResultSet rsCurKat = preStmtCurKat.executeQuery();
                rsCurKat.next();

                cnt.add(lfdnr + "   " + rsCurKat.getString("name"));

                rsCurKat.close();
                preStmtCurKat.close();

                /* Vorbereiten des SQL-Abfrage */
                /* Inhalt aller Werte für Semester-Spalten */
                String sqlSemCnt = "select m.mkuerzel, m.vl, m.ub, m.pr, m.credits as cr, ku.sem"
                        + " from kategorie k, kategorieumfang ku, modul m"
                        + " where k.skuerzel = ? and k.lfdnr = ?"
                        + " and k.skuerzel = ku.skuerzel"
                        + " and k.lfdnr = ku.lfdnr"
                        + " and ku.mkuerzel = m.mkuerzel"
                        + " and sem = ?"
                        + " order by mkuerzel";
                
                int summe = 0;

                /* -- Semester-Spalten -- */
                for (int semNum = 1; semNum <= 6; semNum++) {

                    PreparedStatement preStmtSemCnt = this.con.prepareStatement(sqlSemCnt);
                    preStmtSemCnt.setString(1, sr.getKuerzel());
                    preStmtSemCnt.setInt(2, lfdnr);
                    preStmtSemCnt.setInt(3, semNum);

                    ResultSet rsSemCnt = preStmtSemCnt.executeQuery();
                    StringBuilder sb = new StringBuilder();

                    while (rsSemCnt.next()) {
                        
                        sb.append(rsSemCnt.getString("mkuerzel"));  //Modulkürzel
                        /* Leerzeichen */
                        for (int i = 4 - rsSemCnt.getString("mkuerzel").length(); i >= 0; i--) {
                            
                            sb.append(" ");
                            
                        }
                        sb.append(rsSemCnt.getInt("vl") + "  ");    //Vorlesungsstunden  
                        sb.append(rsSemCnt.getInt("ub") + "  ");    //Übungsstunden
                        sb.append(rsSemCnt.getInt("pr") + "  ");    //Praktikumsstunden
                        sb.append(rsSemCnt.getInt("cr") + "\n");    //Credits
                        
                        summe += rsSemCnt.getInt("vl") + rsSemCnt.getInt("ub") + rsSemCnt.getInt("pr");
                        summeSWS[semNum] += rsSemCnt.getInt("vl") + rsSemCnt.getInt("ub") + rsSemCnt.getInt("pr");
                        
                    }
                    
                    cnt.add(semNum, sb.toString());
                    
                    if (rsSemCnt.wasNull()) {
                        
                        cnt.add(semNum, "");
                        
                    }

                    rsSemCnt.close();
                    preStmtSemCnt.close();

                }
                
                /* -- Summe -- */
                cnt.add(7, ("" + summe));
                
                sVerlPlan.add(cnt);

            }
            
            

        } catch (SQLException ex) {

            System.out.println("Methode: getStudienverlaufsplan");
            System.err.println(ex.getLocalizedMessage());
            ex.printStackTrace();

        }
        
        // </editor-fold>

        /* --- Footer --- 
         * <editor-fold defaultstate="collapsed" desc="Footer"> 
         */

        List<String> footer = new ArrayList<String>();
        int gesamt = 0;
        
        footer.add(0, "Summe SWS");

        for (int semNum = 1; semNum <= 6; semNum++) {

            footer.add(semNum, "" + summeSWS[semNum]);
            gesamt += summeSWS[semNum];

        }
        
        footer.add(7, "" + gesamt);

        sVerlPlan.add(footer);
        // </editor-fold>

        return sVerlPlan;

    }

    /**
     * Gibt die Anzahl aller Kategorien einer Studienrichtung zurück.
     * 
     * @param sr Studienrichtung
     * @return Anzahl aller Kategorien
     */
    public int getMaxNumOfKategories(Studienrichtung sr) {

        int maxKategories = -1;

        /* Vorbereitung der SQL-Abfrage */
        /* Anzahl aller Kategorien einer Studienrichtung */
        String sqlMaxKat = "select max(lfdnr) as maxKat"
                + " from kategorie"
                + " where skuerzel = ?";
        try {

            PreparedStatement psMaxKat = this.con.prepareStatement(sqlMaxKat);
            psMaxKat.setString(1, sr.getKuerzel());

            /* Durchführung der SQL-Abfrage */
            ResultSet rsMaxKat = psMaxKat.executeQuery();

            rsMaxKat.next();
            maxKategories = rsMaxKat.getInt("maxKat");

            rsMaxKat.close();
            psMaxKat.close();

        } catch (SQLException ex) {

            System.out.println("Methode: getMaxNumOfKategories");
            System.err.println(ex.getLocalizedMessage());
            ex.printStackTrace();

        }

        return maxKategories;

    }

    /**
     * Liefert eine Liste aller Studenten.
     * 
     * @return Liste aller Studenten
     */
    @Override
    public Collection<Student> getAllStudent() {

        /* Vorbereitung der SQL Abfrage */
        String sqlQuery = "select * from student";

        /* Liste aller Module */
        Collection<Student> allStudents = new ArrayList<Student>();

        try {

            /* Durchführung der SQL Abfrage */
            ResultSet rs = this.con.createStatement().executeQuery(sqlQuery);

            /* Hinzufügen aller Studenten zur Liste allStudents */
            while (rs.next()) {

                allStudents.add(
                        new StudentImpl(
                        rs.getString("adresse"), //Kürzel
                        rs.getString("matrikel"), //Matrikelnummer
                        rs.getString("name"), //Nachname
                        rs.getString("skuerzel"), //Kürzel der Studienrichtung
                        rs.getString("vorname") //Vorname
                        ));

            }

            rs.close();

        } catch (SQLException ex) {

            System.out.println("Methode: getAllStudent");
            System.err.println(ex.getLocalizedMessage());
            ex.printStackTrace();

        }

        return allStudents;

    }

    /**
     * Liefert eine Liste aller Studienrichtungen.
     * 
     * @return Liste aller Studienrichtungen
     */
    @Override
    public Collection<Studienrichtung> getAllStudienrichtung() {

        /* Vorbereitung der SQL Abfrage */
        String sqlQuery = "select skuerzel, name from studienrichtung";

        /* Liste aller Studienrichtungen */
        Collection<Studienrichtung> allStudienrichtung = new ArrayList<Studienrichtung>();

        try {

            /* Durchführung der SQL Abfrage */
            ResultSet rs = this.con.createStatement().executeQuery(sqlQuery);

            /* Hinzufügen aller Studienrichtungen zur Liste allStudienrichtung */
            while (rs.next()) {

                allStudienrichtung.add(
                        new StudienrichtungImpl(
                        rs.getString("skuerzel"), //Kürzel der Studienrichtung
                        rs.getString("name") //Nachname
                        ));

            }

            rs.close();

        } catch (SQLException ex) {

            System.out.println("Methode: getAllStudienrichtung");
            System.err.println(ex.getLocalizedMessage());
            ex.printStackTrace();

        }

        return allStudienrichtung;

    }

    /**
     * Liefert eine Liste aller Module.
     * 
     * @return Liste aller Module
     */
    @Override
    public Collection<Modul> getAllModul() {

        /* Vorbereitung der SQL Abfrage */
        String sqlQuery = "select * from modul";
        /* Liste aller Module */
        Collection<Modul> allModules = new ArrayList<Modul>();

        try {

            /* Durchführung der SQL Abfrage */
            ResultSet rs = this.con.createStatement().executeQuery(sqlQuery);

            /* Hinzufügen aller Module zur Liste allModul */
            while (rs.next()) {

                allModules.add(
                        new ModulImpl(
                        rs.getString("mkuerzel"), //Kürzel
                        rs.getString("modulname"), //Modulname
                        rs.getInt("pr"), //Praktikumsstunden
                        rs.getInt("ub"), //Übungsstunden
                        rs.getInt("vl"), //Vorlesungsstunden
                        rs.getInt("credits") //Credits
                        ));

            }

            rs.close();

        } catch (SQLException ex) {

            System.out.println("Methode: getAllModul");
            System.err.println(ex.getLocalizedMessage());
            ex.printStackTrace();

        }

        return allModules;

    }

    /**
     * Liefert eine Liste aller Praktikumsteilnehmer.
     * 
     * @param modul       Studienmodul
     * @param semester    Studiensemester
     * @return            Liste aller Praktikumsteilnehmer
     */
    @Override
    public Collection<Praktikumsteilnahme> getAllPraktikumsteilnahme(Modul modul, String semester) {

        /* Liste aller Praktikumsteilnehmer */
        Collection<Praktikumsteilnahme> allMembers = new ArrayList<Praktikumsteilnahme>();

        if (modul != null && !semester.isEmpty()) {
            /* Vorbereitung der SQL Abfrage */
            String sqlQuery =
                    "select *"
                    + "from praktikumsteilnahme pt, student s "
                    + "where pt.matrikel = s.matrikel "
                    + "and pt.mkuerzel = '" + modul.getKuerzel() + "' "
                    + "and pt.semester = '" + semester + "'";

            try {

                /* Durchführung der SQL Abfrage */
                ResultSet rs = this.con.createStatement().executeQuery(sqlQuery);

                /* Hinzufügen aller Praktikumsteilnehmer zur Liste allMembers */
                while (rs.next()) {

                    allMembers.add(
                            new PraktikumsteilnahmeImpl(
                            new StudentImpl(
                            rs.getString("adresse"), //Adresse
                            rs.getString("matrikel"), //Matrikelnummer
                            rs.getString("name"), //Nachname
                            rs.getString("skuerzel"), //Kürzel der Studienrichtung
                            rs.getString("vorname")), //Vorname
                            modul,
                            semester,
                            rs.getBoolean("testat")));

                }

                rs.close();

            } catch (SQLException ex) {

                System.out.println("Methode: getAllPraktikumsteilnahme");
                System.err.println(ex.getLocalizedMessage());
                ex.printStackTrace();

            }

        }

        return allMembers;

    }

    /**
     * Dient der Visualisierung verschiedener Diagrammtypen
     * 
     * @param type      Diagrammtyp
     * @param param1    Erster Parameter
     * @param param2    Zweiter Paramter
     * @return          Darzustellendes Diagramm
     * @throws ApplicationException     Ungültiger Visualisierungstyp. 
     */
    @Override
    public JPanel getChart(int type, Object param1, Object param2)
            throws ApplicationException {

        JFreeChart chart = null;
        PreparedStatement preStmtSRichtungen;
        PreparedStatement preStmtTestabnahmen;
        PreparedStatement preStmtTestatvergaben;
        PreparedStatement preStmtAnmeldungen;
        PreparedStatement preStmtBestandeneTestatvergaben;

        try {

            /* Änderungen sollen erst übergeben werden, wenn alles erfolgreich war */
            this.con.setAutoCommit(false);

            try {

                switch (type) {

                    // <editor-fold defaultstate="collapsed" desc="VISUALISIERUNG_ANTEIL_TESTATABNAHMEN">
                    case VISUALISIERUNG_ANTEIL_TESTATABNAHMEN: {

                        /* Fehlerbehandlung für ungültige Parameter */
                        if (!(param1 instanceof String)) {

                            throw new ApplicationException("Falscher Parametertyp. Erwartet: String.");

                        } else if (param2 != null) {

                            throw new ApplicationException("Falscher Parametertyp. Erwartet: Leeres Objekt.");

                        }

                        /* Semesterangabe */
                        String sem = (String) param1;

                        /* Tabelle aller Studienrichtungen */
                        ResultSet rsSRichtungen;

                        /* Tabelle aller bestandene Testabnahmen */
                        ResultSet rsTestabnahmen;

                        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

                        /* Iteration über alle Studienrichtungen */
                        for (Studienrichtung sr : this.getAllStudienrichtung()) {

                            /* Vorbereitung der SQL Abfrage */
                            String sqlQuerySRichtungen =
                                    "select m.mkuerzel as modul, count(*) as anmeldungen "
                                    + "from modul as m, praktikumsteilnahme as p, student as s "
                                    + "where m.pr > 0 "
                                    + "and m.mkuerzel = p.mkuerzel "
                                    + "and s.matrikel = p.matrikel "
                                    + "and s.skuerzel = ?"
                                    + "and p.semester = ?"
                                    + "group by m.mkuerzel";
                            preStmtSRichtungen = this.con.prepareStatement(sqlQuerySRichtungen);
                            preStmtSRichtungen.setString(1, sr.getKuerzel());
                            preStmtSRichtungen.setString(2, sem);

                            /* Durchführung der SQL Abfrage */
                            rsSRichtungen = preStmtSRichtungen.executeQuery();

                            /* Alle bestandenen Testabnahmen */
                            while (rsSRichtungen.next()) {

                                /* Modulkürzel */
                                String mKuerzel = rsSRichtungen.getString("modul");

                                /* Anzahl Praktikumsanmeldungen */
                                int anzahl = rsSRichtungen.getInt("anmeldungen");

                                /* Vorbereitung der SQL Abfrage */
                                String sqlQueryTestabnahmen =
                                        "select count(m.mkuerzel) as bestanden "
                                        + "from modul as m, praktikumsteilnahme as p, student as s "
                                        + "where m.pr > 0 "
                                        + "and m.mkuerzel = p.mkuerzel "
                                        + "and s.matrikel = p.matrikel "
                                        + "and p.testat = true "
                                        + "and s.skuerzel = ?"
                                        + "and m.mkuerzel = ?"
                                        + "and p.semester = ?";
                                preStmtTestabnahmen = this.con.prepareStatement(sqlQueryTestabnahmen);
                                preStmtTestabnahmen.setString(1, sr.getKuerzel());
                                preStmtTestabnahmen.setString(2, mKuerzel);
                                preStmtTestabnahmen.setString(3, sem);

                                /* Durchführung der SQL Abfrage */
                                rsTestabnahmen = preStmtTestabnahmen.executeQuery();
                                rsTestabnahmen.next();

                                /* Anzahl bestandener Testabnahmen */
                                int bestanden = rsTestabnahmen.getInt("bestanden");

                                rsTestabnahmen.close();
                                preStmtTestabnahmen.close();

                                /* Ermittle Prozentsatz bestandener Testabnahmen */
                                double gesamt = (bestanden == 0 ? 0 : ((double) bestanden / (double) anzahl) * 100.0);

                                dataset.addValue(gesamt, mKuerzel, sr.getKuerzel());
                            }

                            rsSRichtungen.close();
                            preStmtSRichtungen.close();

                        }

                        /* Erstellen des Balkendiagramms */
                        chart = ChartFactory.createBarChart(sem,
                                "Praktikumsmodule nach Studienrichtung",
                                "Erfolgreiche Teilnahme in %",
                                dataset,
                                PlotOrientation.VERTICAL,
                                true,
                                true,
                                false);

                    }

                    break;
                    // </editor-fold>

                    //<editor-fold defaultstate="collapsed" desc="VISUALISIERUNG_AUFTEILUNG_ANMELDUNGEN">
                    case VISUALISIERUNG_AUFTEILUNG_ANMELDUNGEN: {

                        /* Fehlerbehandlung für ungültige Parameter */
                        if (!(param1 instanceof Studienrichtung)) {

                            throw new ApplicationException("Falscher Parametertyp. Erwartet: Studienrichtung.");

                        } else if (!(param2 instanceof String)) {

                            throw new ApplicationException("Falscher Parametertyp. Erwartet: String.");

                        }

                        Studienrichtung sr = (Studienrichtung) param1;
                        String sem = (String) param2;

                        /* Vorbereitung der SQL Abfrage */
                        String sqlQuery = "select mkuerzel as modul, count(*) as anmeldungen "
                                + "from praktikumsteilnahme p, student s "
                                + "where p.matrikel = s.matrikel "
                                + "and s.skuerzel ='" + sr.getKuerzel() + "' "
                                + "and semester = '" + sem + "' "
                                + "group by p.mkuerzel";

                        PieDataset dataset = new JDBCPieDataset(this.con, sqlQuery);

                        /* Erstellen des Kuchendiagramms */
                        chart = ChartFactory.createPieChart(
                                sr.getName() + " (" + sr.getKuerzel() + ") " + sem,
                                dataset,
                                true,
                                true,
                                false);
                        ((PiePlot) chart.getPlot()).setLabelGenerator(
                                new StandardPieSectionLabelGenerator("{0}: {1} Anmeldungen"));

                    }

                    break;
                    // </editor-fold>
                        
                    //<editor-fold defaultstate="collapsed" desc="VISUALISIERUNG_ENTWICKLUNG_ANMELDUNGEN">
                    case VISUALISIERUNG_ENTWICKLUNG_ANMELDUNGEN: {

                        /* Fehlerbehandlung für ungültige Parameter */
                        if (!(param1 instanceof Modul)) {

                            throw new ApplicationException("Falscher Parametertyp. Erwartet: Modul.");

                        } else if (param2 != null) {

                            throw new ApplicationException("Falscher Parametertyp. Erwartet: Leeres Objekt.");

                        }

                        Modul modul = (Modul) param1;
                        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

                        /* Tabelle aller Testatvergaben */
                        ResultSet rsTestatvergaben;

                        String sqlQueryTestatvergaben =
                                "select semester, count(*) as anmeldungen"
                                + " from praktikumsteilnahme"
                                + " where mkuerzel = ?"
                                + " group by semester";
                        preStmtTestatvergaben = this.con.prepareStatement(sqlQueryTestatvergaben);
                        preStmtTestatvergaben.setString(1, modul.getKuerzel());

                        /* Durchführung der SQL Abfrage */
                        rsTestatvergaben = preStmtTestatvergaben.executeQuery();


                        while (rsTestatvergaben.next()) {

                            dataset.addValue(
                                    rsTestatvergaben.getInt("anmeldungen"),
                                    "Anmeldungen",
                                    rsTestatvergaben.getString("semester"));
                            dataset.addValue(
                                    0,
                                    "Testatvergaben",
                                    rsTestatvergaben.getString("semester"));

                        }

                        rsTestatvergaben.close();
                        preStmtTestatvergaben.close();

                        /* Tabelle aller bestandenen Testatvergaben */
                        ResultSet rsBestandeneTestatvergaben;

                        /* Vorbereitung der SQL Abfrage */
                        String sqlQueryBestandeneTestatvergaben =
                                "select semester, count(*) as vergaben "
                                + "from praktikumsteilnahme"
                                + " where mkuerzel = ?"
                                + " and testat = true"
                                + " group by semester";
                        preStmtBestandeneTestatvergaben = this.con.prepareStatement(sqlQueryBestandeneTestatvergaben);
                        preStmtBestandeneTestatvergaben.setString(1, modul.getKuerzel());

                        /* Durchführung der SQL Abfrage */
                        rsBestandeneTestatvergaben = preStmtBestandeneTestatvergaben.executeQuery();

                        while (rsBestandeneTestatvergaben.next()) {

                            dataset.addValue(
                                    rsBestandeneTestatvergaben.getInt("vergaben"),
                                    "Testatvergaben",
                                    rsBestandeneTestatvergaben.getString("semester"));

                        }

                        rsBestandeneTestatvergaben.close();
                        preStmtTestatvergaben.close();

                        /* Erstellen des Liniendiagramms */
                        chart = ChartFactory.createLineChart(modul.getName()
                                + " (" + modul.getKuerzel() + ")",
                                "Semester",
                                "Studierende",
                                dataset,
                                PlotOrientation.VERTICAL,
                                true,
                                true,
                                false);

                    }

                    break;
                    // </editor-fold>

                    //<editor-fold defaultstate="collapsed" desc="VISUALISIERUNG_ANMELDUNGEN_TESTATE">
                    case VISUALISIERUNG_ANMELDUNGEN_TESTATE: {

                        /* Fehlerbehandlung für ungültige Parameter */
                        if (!(param1 instanceof Studienrichtung)) {

                            throw new ApplicationException("Falscher Parametertyp. Erwartet: Studienrichtung.");

                        } else if (!(param2 instanceof String)) {

                            throw new ApplicationException("Falscher Parametertyp. Erwartet: String.");

                        }

                        Studienrichtung sr = (Studienrichtung) param1;
                        String sem = (String) param2;
                        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

                        /* Tabelle aller Anmeldungen zu Praktika */
                        ResultSet rsAnmeldungen;

                        /* Tabelle aller Anmeldungen zu Praktika mit Testatvergabe */
                        ResultSet rsTestatvergaben;

                        /* Anmeldungen zu Praktika */
                        /* Vorbereitung der SQL Abfrage */
                        String sqlQueryAnmeldungen = "select p.mkuerzel as modul ,count(p.mkuerzel) as anmeldungen"
                                + " from praktikumsteilnahme as p, student as s "
                                + " where p.semester = ?"
                                + " and p.matrikel = s.matrikel"
                                + " and s.skuerzel = ?"
                                + " group by p.mkuerzel";
                        preStmtAnmeldungen = this.con.prepareStatement(sqlQueryAnmeldungen);
                        preStmtAnmeldungen.setString(1, sem);
                        preStmtAnmeldungen.setString(2, sr.getKuerzel());

                        /* Durchführung der SQL Abfrage */
                        rsAnmeldungen = preStmtAnmeldungen.executeQuery();

                        /* Alle Praktikumsanmeldungen */
                        while (rsAnmeldungen.next()) {

                            /* Modulkürzel */
                            String mKuerzel = rsAnmeldungen.getString("modul");

                            /* Anzahl Praktikumsanmeldungen */
                            int anmeldungen = rsAnmeldungen.getInt("anmeldungen");

                            dataset.addValue(anmeldungen, "Anmeldungen", mKuerzel);

                            /* Vorbereitung der SQL Abfrage */
                            String sqlQueryTestatvergaben = "select count(p.mkuerzel) as anzahl"
                                    + " from praktikumsteilnahme as p, student as S"
                                    + " where p.semester = ?"
                                    + " and p.matrikel = s.matrikel"
                                    + " and s.skuerzel = ?"
                                    + " and p.mkuerzel = ?"
                                    + " and p.testat = true"
                                    + " group by p.mkuerzel";
                            preStmtTestatvergaben = this.con.prepareStatement(sqlQueryTestatvergaben);
                            preStmtTestatvergaben.setString(1, sem);
                            preStmtTestatvergaben.setString(2, sr.getKuerzel());
                            preStmtTestatvergaben.setString(3, mKuerzel);

                            /* Durchführung der SQL Abfrage */
                            rsTestatvergaben = preStmtTestatvergaben.executeQuery();

                            /* Alle Testatvergaben */
                            while (rsTestatvergaben.next()) {
                                dataset.addValue(rsTestatvergaben.getInt("anzahl"),
                                        "Testatvergabe",
                                        mKuerzel);
                            }

                            rsTestatvergaben.close();
                            preStmtTestatvergaben.close();

                        }

                        rsAnmeldungen.close();
                        preStmtAnmeldungen.close();

                        /* Erstellen des Balkendiagramms */
                        chart = ChartFactory.createBarChart(sr.getName()
                                + " " + sr.getKuerzel() + " - " + sem,
                                "Module",
                                "Studierende",
                                dataset,
                                PlotOrientation.VERTICAL,
                                true,
                                true,
                                false);

                    }

                    break;
                    // </editor-fold>

                    default: {

                        throw new ApplicationException("Visualisierungstyp nicht vorhanden");

                    }

                }

            } catch (SQLException sqlex) {

                System.out.println("Methode: getChart");
                System.err.println(sqlex.getLocalizedMessage());
                sqlex.printStackTrace();

            }

            /* Zurücksetzen von AutoCommit */
            this.con.setAutoCommit(true);

        } catch (SQLException sqlex2) {

            System.out.println("Methode: getChart");
            System.err.println(sqlex2.getLocalizedMessage());
            sqlex2.printStackTrace();

        }

        return new ChartPanel(chart);

    }

    /**
     * FIXME: Die Implementierung dieser Methode war nicht Bestandteil des GDB Praktikums.
     * Diese Funktion dient dem Exportieren der Datensätze im PDF-Format.
     * 
     * @param out       PDF-Datei
     * @param string    Dateiname
     */
    @Override
    public void exportAnnouncementList(OutputStream out, String string) {

        System.out.println("Methode: exportAnnouncementList");
        throw new UnsupportedOperationException("Not supported yet.");

    }

    /**
     * Beendet die Verbindung zur Datenbank
     * 
     * @throws ApplicationException     Verbindung zur Datenbank konnte nicht beendet werden
     */
    @Override
    public void close() throws ApplicationException {

        try {

            this.con.close();

        } catch (SQLException ex) {

            System.out.println("Methode: close");
            System.err.println("Fehler beim Schließen der Verbindung:\n"
                    + ex.getLocalizedMessage());
            ex.printStackTrace();

        }

    }
}
