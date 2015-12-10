package com.airmazing.pollutionApp.scraper;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Created by andrei on 02/11/15.
 */
public class PgConn {

    public static String[] getCredentials() {
        Properties props = new Properties();
        FileInputStream in = null;

        try {
            in = new FileInputStream(System.getProperty("user.dir") + "/database.properties");
            props.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String[] credentials = new String[3];

        credentials[0] = props.getProperty("url");
        credentials[1] = props.getProperty("user");
        credentials[2] = props.getProperty("passwd");

        return credentials;
    }

    public static void executeInputQuery(String db, String query) {

        String[] credentials = getCredentials();

        String url = credentials[0] + db;
        String user = credentials[1];
        String password = credentials[2];

        Connection con = null;
        Statement st = null;

        try {
            con = DriverManager.getConnection(url, user, password);
            st = con.createStatement();
            st.executeUpdate(query);
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

}
