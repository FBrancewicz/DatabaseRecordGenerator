package BazaMySQL;

import java.sql.*;
import java.time.LocalDate;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;


public class BazaMySQLDBInit {

    public static void main(String[] args) {
        String dbName = "PracaMagisterskaMySQL";
        String urlWithoutDb = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String url = "jdbc:mysql://localhost:3306/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String user = "root";
        String password = "root";

        String[] sponsors = {"Red Hawk", "Blue Stone", "Silver Tree", "Golden Fox", "Crimson Flame",
                "Green Power", "Iron Wolf", "Sky Bridge", "Flowers Co.", "TurboMax"};
        String[] cities = {"Warszawa", "Londyn", "Lyon", "Monachium", "Ankara", "Praga", "Barcelona", "Rzym", "Stambuł", "Wiedeń"};
        String[] countries = {"Polska", "Niemcy", "Francja", "Włochy", "Hiszpania", "Anglia", "Holandia", "Portugalia",
                "Norwegia", "Szwecja", "Czechy", "Słowacja", "Austria", "Węgry", "Meksyk", "USA", "Kanada", "Brazylia", "Japonia", "Australia"};
        String[] stadiums = {"Arena K1", "Stadion Centralny", "Złoty Stadion", "SportPark X", "Stadion 3000",
                "Red Arena", "Nowa Arena", "Stadion Wschodni", "Victory Arena", "Techno Arena"};
        String[] firstNames = {"Tomasz", "Wojciech", "Jan", "Michał", "Piotr", "John", "David", "James", "Robert", "William"};
        String[] lastNames = {"Kowalski", "Nowak", "Lewandowski", "Smith", "Johnson", "Brown", "Jones", "Garcia", "Lopez", "Young"};

        Random random = new Random();

        try {
            try (Connection conn = DriverManager.getConnection(urlWithoutDb, user, password)) {
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
                System.out.println("Baza danych utworzona lub już istnieje.");
            }

            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                conn.setAutoCommit(false);

                conn.createStatement().execute("DROP TABLE IF EXISTS club");
                conn.createStatement().execute("DROP TABLE IF EXISTS league");

                String createLeagueTable = """
                        CREATE TABLE league (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(255),
                            country VARCHAR(255),
                            sponsor VARCHAR(255),
                            founded_year SMALLINT
                        ) ENGINE=InnoDB;
                        """;
                conn.createStatement().execute(createLeagueTable);

                String createClubTable = """
                        CREATE TABLE club (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(255),
                            city VARCHAR(255),
                            founded_year SMALLINT,
                            stadium VARCHAR(255),
                            capacity INT,
                            budget DECIMAL(15,2),
                            president VARCHAR(255),
                            coach VARCHAR(255),
                            wins SMALLINT,
                            draws SMALLINT,
                            losses SMALLINT,
                            last_season_position SMALLINT,
                            last_match DATE,
                            league_id BIGINT
                        ) ENGINE=InnoDB;
                        """;
                conn.createStatement().execute(createClubTable);

                String leagueSQL = "INSERT INTO league(name, country, sponsor, founded_year) VALUES (?, ?, ?, ?)";
                PreparedStatement leagueStmt = conn.prepareStatement(leagueSQL, Statement.RETURN_GENERATED_KEYS);

                List<Long> leagueIds = new ArrayList<>();
                System.out.println("Generowanie 400 000 lig...");
                for (int i = 1; i <= 400_000; i++) {
                    leagueStmt.setString(1, "Liga #" + i);
                    leagueStmt.setString(2, countries[random.nextInt(countries.length)]);
                    leagueStmt.setString(3, sponsors[random.nextInt(sponsors.length)]);
                    leagueStmt.setInt(4, 1900 + random.nextInt(121));
                    leagueStmt.addBatch();

                    if (i % 5000 == 0) {
                        leagueStmt.executeBatch();
                        try (ResultSet rs = leagueStmt.getGeneratedKeys()) {
                            while (rs.next()) {
                                leagueIds.add(rs.getLong(1));
                            }
                        }
                        conn.commit();
                    }
                }
                leagueStmt.executeBatch();
                try (ResultSet rs = leagueStmt.getGeneratedKeys()) {
                    while (rs.next()) {
                        leagueIds.add(rs.getLong(1));
                    }
                }
                conn.commit();
                System.out.println("Wstawiono 400 000 lig.");

                String clubSQL = """
                        INSERT INTO club(name, city, founded_year, stadium, capacity, budget,
                        president, coach, wins, draws, losses, last_season_position, last_match, league_id)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """;
                PreparedStatement clubStmt = conn.prepareStatement(clubSQL);

                System.out.println("Generowanie 3 000 000 losowych klubów...");
                for (int i = 1; i <= 3_000_000; i++) {
                    clubStmt.setString(1, "Klub #" + i);
                    clubStmt.setString(2, cities[random.nextInt(cities.length)]);
                    clubStmt.setInt(3, 1900 + random.nextInt(121));
                    clubStmt.setString(4, stadiums[random.nextInt(stadiums.length)]);
                    clubStmt.setInt(5, 10_000 + random.nextInt(40_000));
                    clubStmt.setBigDecimal(6, new java.math.BigDecimal(10_000_000 + random.nextInt(90_000_000)));
                    clubStmt.setString(7, firstNames[random.nextInt(firstNames.length)] + " " +
                            lastNames[random.nextInt(lastNames.length)]);
                    clubStmt.setString(8, firstNames[random.nextInt(firstNames.length)] + " " +
                            lastNames[random.nextInt(lastNames.length)]);
                    clubStmt.setInt(9, random.nextInt(30));
                    clubStmt.setInt(10, random.nextInt(10));
                    clubStmt.setInt(11, random.nextInt(10));
                    clubStmt.setInt(12, 1 + random.nextInt(20));
                    clubStmt.setDate(13, Date.valueOf(LocalDate.now().minusDays(random.nextInt(50))));
                    clubStmt.setLong(14, leagueIds.get(random.nextInt(leagueIds.size())));
                    clubStmt.addBatch();

                    if (i % 10000 == 0) {
                        clubStmt.executeBatch();
                        conn.commit();
                    }
                }
                conn.commit();
                System.out.println("Wstawiono 3 000 000 losowych klubów.");

                System.out.println("Generowanie 2 000 000 klubów z Barcelony");
                for (int i = 3_000_001; i <= 5_000_000; i++) {
                    clubStmt.setString(1, "Klub #" + i);
                    clubStmt.setString(2, "Barcelona");
                    clubStmt.setInt(3, 1900 + random.nextInt(121));
                    clubStmt.setString(4, stadiums[random.nextInt(stadiums.length)]);
                    clubStmt.setInt(5, 10_000 + random.nextInt(40_000));
                    clubStmt.setBigDecimal(6, new java.math.BigDecimal(10_000_000 + random.nextInt(90_000_000)));
                    clubStmt.setString(7, firstNames[random.nextInt(firstNames.length)] + " " +
                            lastNames[random.nextInt(lastNames.length)]);
                    clubStmt.setString(8, firstNames[random.nextInt(firstNames.length)] + " " +
                            lastNames[random.nextInt(lastNames.length)]);
                    clubStmt.setInt(9, random.nextInt(30));
                    clubStmt.setInt(10, random.nextInt(10));
                    clubStmt.setInt(11, random.nextInt(10));
                    clubStmt.setInt(12, 1 + random.nextInt(20));
                    clubStmt.setDate(13, Date.valueOf(LocalDate.now().minusDays(random.nextInt(50))));
                    clubStmt.setLong(14, leagueIds.get(random.nextInt(leagueIds.size())));
                    clubStmt.addBatch();

                    if (i % 10000 == 0) {
                        clubStmt.executeBatch();
                        conn.commit();
                    }
                }
                clubStmt.executeBatch();
                conn.commit();
                System.out.println("Wstawiono 2 000 000 klubów z Barcelony.");
                System.out.println("Łącznie 5 000 000 klubów w tabeli club.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}