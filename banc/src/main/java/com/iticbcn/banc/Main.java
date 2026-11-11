package com.iticbcn.banc;
 
 
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
 
 
public class Main {
    public static void main(String[] args) {
        Properties prop = new Properties();
        try {
            prop.load(Files.newBufferedReader(Path.of("banc","src","main","resources","conn.properties")));
  
        } catch (IOException e) {
           e.printStackTrace(); 
        }
        HikariConfig hkcfg = new HikariConfig();
//parametria de la connexió
        hkcfg.setJdbcUrl(prop.getProperty("DB_URL"));
        hkcfg.setUsername(prop.getProperty("DB_USER"));
        hkcfg.setPassword(prop.getProperty("DB_PASSWORD"));
        hkcfg.setMaximumPoolSize(Integer.parseInt(prop.getProperty("DB_MAX_POOL")));
 
 
//iniciem el datasource i controlem excepcions
 
 
 
 
        try (HikariDataSource hkds = new HikariDataSource(hkcfg)) {
//connectem la base de dades
            try {
                Connection cnn = hkds.getConnection();
                System.out.println("Connectat!");
            } catch (SQLException e) {
                System.out.println("No connectat!");
                e.printStackTrace();
            }
 
 
        }
    }
}