package com.airmazing.pollutionApp.scraper.objects;

import com.airmazing.pollutionApp.scraper.PgConn;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringEscapeUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrei on 27/10/15.
 */
public class Entries {

    //the number of entries to insert at once into the db
    private static final int BULK_SIZE = 30000;

    public static Entry getEntry(List<Entry> entries, String key, String value) {
        for (Entry entry : entries) {
            if (entry.getAttribute(key).equals(value)) {
                return entry;
            }
        }
        return null;
    }

    public static List<String> getCommonAttributeKeys(List<Entry> entries) {
        List<String> commonKeys = new ArrayList<String>();
        for (Entry entry : entries) {
            if (entry != null && !entry.equals("")) {
                List<String> properties = entry.getAttributes();
                for (String obj : properties) {
                    if (commonKeys.contains(obj)) {
                    } else {
                        commonKeys.add(obj);
                    }
                }
            }
        }
        return commonKeys;
    }

    public static List<Entry> inputAttributesFromCsv(String folderPath, String fileName, Character delimiter) {

        FileReader fileReader = null;
        CSVParser csvFileParser = null;
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader().withRecordSeparator('\n').withDelimiter(delimiter);
        try {
            File csvFile = new File(folderPath, fileName);
            List<Entry> entries= new ArrayList<Entry>();
            fileReader = new FileReader(csvFile);
            csvFileParser = new CSVParser(fileReader, csvFileFormat);
            List<CSVRecord> csvRecords = csvFileParser.getRecords();
            for (int i = 1; i < csvRecords.size(); i++) {

                CSVRecord record = csvRecords.get(i);
                Entry entry = new Entry();
                for (Object key : record.toMap().keySet()) {
                    if (record.toMap().get(key) != null && !record.toMap().get(key).equals("")) {
                        entry.setAttribute(key.toString(), record.toMap().get(key).toString());
                    }
                }
                entries.add(entry);
            }
            return entries;
        } catch (Exception e) {
            System.out.println("Error in CsvFileReader !!!");
            e.printStackTrace();
            return null;
        } finally {
            try {
                fileReader.close();
                csvFileParser.close();
            } catch(IOException e) {
                System.out.println("Error while closing fileReader/csvFileParser !!!");
                e.printStackTrace();
            }
        }


    }

    public static void outputAttributesToCsv(List<Entry> entries, String folderPath, String title) {

        String NEW_LINE_SEPARATOR = "\n";

        List<String> attributeKeys = Entries.getCommonAttributeKeys(entries);

        String [] FILE_HEADER = attributeKeys.toArray(new String[attributeKeys.size()]);

        System.out.println(FILE_HEADER);

        FileWriter fileWriter = null;
        CSVPrinter csvFilePrinter = null;

        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);

        try {

            String fileName = title + ".csv";

            Path localPath = Paths.get(folderPath);
            if (!Files.exists(localPath)) {
                Files.createDirectories(localPath);
            }

            File csv = new File(folderPath, fileName);

            fileWriter = new FileWriter(csv);


            csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
            csvFilePrinter.printRecord((Object[])FILE_HEADER);

            System.out.println("Creating CSV file...");

            for (Entry photo : entries) {
                csvFilePrinter.printRecord(photo.getValues(FILE_HEADER));
            }

            System.out.println("CSV file was created successfully!");

        }
        catch (Exception e) {
            System.out.println("Error in CsvFileWriter!");
            e.printStackTrace();
        }
        finally {
            try {
                fileWriter.flush();
                fileWriter.close();
                csvFilePrinter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter/csvPrinter!");
                e.printStackTrace();
            }
        }
    }

    public static List<String> getDifferentAttributeValues(String key, List<Entry> entries) {
        List<String> values = new ArrayList<String>();

        for (Entry entry : entries) {
            String value = entry.getAttribute(key);
            if (!values.contains(value)) {
                values.add(value);
            }
        }
        return values;
    }

    public static List<Entry> getMatchingEntries(String key, String value, List<Entry> entries) {
        List<Entry> matchList = new ArrayList<Entry>();

        for (Entry entry : entries) {

            String valA = entry.getAttribute(key);
            String valB = value;
            if (entry.getAttribute(key) != null && valA.equals(valB)) {
                matchList.add(entry);
            }
        }
        return matchList;
    }

    public static String getEntriesColumns(List<String> commonKeys) {

        StringBuilder sbCols = new StringBuilder();
        sbCols.append("(");
        for (String key : commonKeys) {
            sbCols.append(key + ",");
        }
        sbCols.setLength(sbCols.length() - 1);
        sbCols.append(")");
        return sbCols.toString();
    }

    public static String getEntryValues(Entry entry, List<String> commonKeys) {
        StringBuilder row = new StringBuilder();
        row.append("(");
        for (String key : commonKeys) {
            String ek = entry.getAttribute(key);
            ek = StringEscapeUtils.escapeSql(ek);
            if (ek != null && !ek.equals("")) {
                row.append("'" + ek + "'" + ",");
            } else {
                row.append("NULL,");
            }
        }
        row.setLength(row.length() - 1);
        row.append(")");
        return row.toString();
    }

    public static String getEntriesValues(List<String> entriesValues) {
        StringBuilder sbRows = new StringBuilder();
        for (String entryValue : entriesValues) {
            sbRows.append(entryValue + ",");
        }
        sbRows.setLength(sbRows.length() - 1);
        return sbRows.toString();
    }

    private static void insertEntries(List<Entry> entries, List<String> pks, String db, String table, Statement st) throws SQLException {


        List<String> commonKeys = getCommonAttributeKeys(entries);
        String columns = getEntriesColumns(commonKeys);

        List<String> rowsValues = new ArrayList<String>();
        //System.out.println(sbCols.toString());

        int length = entries.size();
        int i = (int)Math.ceil((double)length / (double) BULK_SIZE);
        int r = length % BULK_SIZE;
        int j = 0;

        for (Entry entry : entries) {
            j++;
            String entryValues = getEntryValues(entry, commonKeys);
            rowsValues.add(entryValues);


            if (j >= BULK_SIZE || (j == r && i == 1)) {
                i--;
                j = 0;
                String values = getEntriesValues(rowsValues);
                rowsValues = new ArrayList<String>();
                //System.out.println(sbRows.toString());

                String query = "INSERT INTO " + table + columns + " " + " VALUES " + values + ";";

                boolean b = true;

                for (String pk : pks) {
                    if (entries.get(0).getAttribute(pk) == null) {
                        b = false;
                    }
                }

                if (!pks.isEmpty() && b) {
                    String pkMatch = "";
                    boolean check = false;
                    for (String pk : pks) {
                        if (check) {
                            pkMatch += " AND ";
                        }
                        pkMatch += "v." + pk + "=" + "t." + pk;
                        check = true;
                    }

                    String pkNull = "";
                    check = false;
                    for (String pk : pks) {
                        if (check) {
                            pkNull += " AND ";
                        }
                        pkNull += "t." + pk + " IS NULL ";
                        check = true;
                    }

                    String query1 =
                            " SELECT v.* FROM (VALUES " + values + ") AS v" + columns + " " +
                                    " LEFT JOIN " + table + " t ON " + pkMatch + " WHERE " + pkNull + ";";

                    List<Entry> entriesToInsert = Entries.inputEntriesFromPostgres(db, query1);

                    if (entriesToInsert.size() > 0) {
                        for (Entry entryToInsert : entriesToInsert) {
                            rowsValues.add(getEntryValues(entryToInsert,commonKeys));
                        }

                        String valuesToInsert = getEntriesValues(rowsValues);

                        query = "INSERT INTO " + table + columns + " " +
                                "VALUES " +  valuesToInsert;
                    } else {
                        query = null;
                    }


                }
                //System.out.println(query);
                if(query != null) {
                    st.executeUpdate(query);
                }
            }
        }
    }

    public static void outputEntriesToPostgres(List<Entry> entries, String db, String table) {

        String[] credentials = PgConn.getCredentials();

        String url = credentials[0] + db;
        String user = credentials[1];
        String password = credentials[2];

        Connection con = null;
        Statement st = null;

        try {
            con = DriverManager.getConnection(url, user, password);
            st = con.createStatement();
            System.out.println("Inserting " + entries.size() + " rows into " + db + "/" + table + "...");


            String primaryKeysQuery =
                    "SELECT a.attname as pk " +
                            "FROM   pg_index i " +
                            "JOIN   pg_attribute a ON a.attrelid = i.indrelid " +
                            "AND a.attnum = ANY(i.indkey) " +
                            "WHERE  i.indrelid = '" + table + "'::regclass " +
                            "AND    i.indisprimary;";
            List<Entry> primaryKeys = inputEntriesFromPostgres(db, primaryKeysQuery);
            List<String> pks = getDifferentAttributeValues("pk", primaryKeys);

            insertEntries(entries, pks, db, table, st);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (st != null) {
                    st.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static List<Entry> procQuery(Connection con, String query) throws SQLException {
        List<Entry> entries = new ArrayList<Entry>();

        //System.out.println(query);

        PreparedStatement pst = con.prepareStatement(query);
        ResultSet rs = pst.executeQuery();

        ResultSetMetaData rsmd = rs.getMetaData();
        List<String> columns = new ArrayList<String>();
        List<String> labels = new ArrayList<String>();

        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            String column = rsmd.getColumnName(i);
            columns.add(column);
            String label = rsmd.getColumnLabel(i);
            if (label != null) {
                if (!label.equals("")) {
                    if (!label.equals(column)) {
                        labels.add(label);
                    }
                }
            }
        }

        while (rs.next()) {
            Entry entry = new Entry();
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                String column = rsmd.getColumnName(i);
                String label = rsmd.getColumnLabel(i);

                if (label != null) {
                    if (!label.equals("")) {
                        if (!label.equals(column)) {
                            entry.setAttribute(label, rs.getObject(column).toString());
                        } else {
                            entry.setAttribute(column, rs.getObject(column).toString());
                        }
                    }
                }
            }
            entries.add(entry);
        }
        return entries;
    }

    public static List<Entry> inputEntriesFromPostgres(String db, String query) {

        String[] credentials = PgConn.getCredentials();

        String url = credentials[0];
        String user = credentials[1];
        String password = credentials[2];

        Connection con = null;
        PreparedStatement pst = null;

        try {
            con = DriverManager.getConnection(url + db, user, password);
            List<Entry> entries = procQuery(con, query);
            return entries;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            try {
                if (pst != null) {
                    pst.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

}