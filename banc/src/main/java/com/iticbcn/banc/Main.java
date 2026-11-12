package com.iticbcn.banc;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool.PoolInitializationException;

public class Main {

    public static void main(String[] args) {
        Properties p = new Properties();
        try (InputStream in = Main.class.getResourceAsStream("/conn.properties")) {
            if (in == null) {
                System.out.println("No es troba conn.properties");
                return;
            }
            p.load(in);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        HikariConfig hkcfg = new HikariConfig();
        hkcfg.setJdbcUrl(p.getProperty("DB_URL"));
        hkcfg.setUsername(p.getProperty("DB_USER"));
        hkcfg.setPassword(p.getProperty("DB_PASSWORD"));
        hkcfg.setMaximumPoolSize(Integer.parseInt(p.getProperty("DB_MAX_POOL", "5")));

        try (HikariDataSource hkds = new HikariDataSource(hkcfg);
             Connection cnn = hkds.getConnection()) {

            // Pas 4: tots els moviments
            for (String resultat : readAllStatements(cnn)) {
                System.out.println("moviment amb id: " + resultat);
            }

            // Exercici 1
            System.out.println("\n--- Moviments amb import >= 1000 ---");
            readStatementsMinImport(cnn, 1000);

            // Exercici 2
            Scanner sc = new Scanner(System.in);
            List<String> dades = new ArrayList<>();
            System.out.print("Nom: ");
            dades.add(sc.nextLine());
            System.out.print("Cognoms: ");
            dades.add(sc.nextLine());
            System.out.print("Id fiscal: ");
            dades.add(sc.nextLine());
            insertClient(cnn, dades);

        } catch (PoolInitializationException e) {
            System.out.println("No s'ha pogut connectar a la base de dades: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Error de base de dades");
            e.printStackTrace();
        }
    }

    public static List<String> readAllStatements(Connection conn) throws SQLException {
        List<String> resultatQuery = new ArrayList<>();
        String query = "SELECT * FROM statements";
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultatQuery.add(String.join(";",
                        rs.getString("id_statement"),
                        rs.getString("tipus_moviment"),
                        rs.getString("import"),
                        rs.getString("divisa")));
            }
        }
        return resultatQuery;
    }

    public static void readStatementsMinImport(Connection conn, double quantitat) throws SQLException {
        String query = "SELECT * FROM statements WHERE import >= ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setDouble(1, quantitat);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                String header = String.join(";",
                        meta.getColumnName(1),
                        meta.getColumnName(3),
                        meta.getColumnName(8),
                        meta.getColumnName(10));
                System.out.println(header);
                while (rs.next()) {
                    System.out.println(String.join(";",
                            rs.getString("id_statement"),
                            rs.getString("tipus_moviment"),
                            rs.getString("import"),
                            rs.getString("divisa")));
                }
            }
        }
    }

    public static boolean clientExists(Connection conn, String idFiscal) throws SQLException {
        String query = "SELECT 1 FROM clients WHERE id_fiscal = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, idFiscal);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static void insertClient(Connection conn, List<String> dades) throws SQLException {
        if (clientExists(conn, dades.get(2))) {
            System.out.println("El client ja existeix, no s'insereix.");
            return;
        }

        String insert = "INSERT INTO clients (nom,cognoms,id_fiscal) VALUES (?,?,?)";
        conn.setAutoCommit(false);
        try (PreparedStatement ps = conn.prepareStatement(insert)) {
            ps.setString(1, dades.get(0));
            ps.setString(2, dades.get(1));
            ps.setString(3, dades.get(2));
            ps.executeUpdate();
            conn.commit();
            System.out.println("Client inserit correctament.");
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("Error en la inserció, s'ha fet rollback.");
            throw e;
        }
    }
}