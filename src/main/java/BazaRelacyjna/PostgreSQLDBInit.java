package BazaRelacyjna;

import java.sql.*;
import java.time.LocalDate;
import java.util.Random;

public class PostgreSQLDBInit {

    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/PracaMagisterskaBazaDanych1FB";
        String user = "postgres";
        String password = "Password";

        String[] sponsors = {
                "Red Hawk", "Blue Stone", "Silver Tree", "Golden Fox", "Crimson Flame",
                "Green Power", "Iron Wolf", "Sky Bridge", "Flowers Co.", "TurboMax"
        };
        String[] cities = {
                "Warszawa", "Londyn", "Lyon", "Monachium", "Ankara",
                "Praga", "Barcelona", "Rzym", "Stambuł", "Wiedeń"
        };
        String[] countries = {
                "Polska", "Niemcy", "Francja", "Włochy", "Hiszpania", "Anglia",
                "Holandia", "Portugalia", "Norwegia", "Szwecja",
                "Czechy", "Słowacja", "Austria", "Węgry",
                "Meksyk", "USA", "Kanada", "Brazylia", "Japonia", "Australia"
        };
        String[] stadiums = {
                "Arena K1", "Stadion Centralny", "Złoty Stadion", "SportPark X", "Stadion 3000",
                "Red Arena", "Nowa Arena", "Stadion Wschodni", "Victory Arena", "Techno Arena"
        };
        String[] firstNames = {"Tomasz", "Wojciech", "Jan", "Michał", "Piotr", "John", "David", "James", "Robert", "William"};
        String[] lastNames = {"Kowalski", "Nowak", "Lewandowski", "Smith", "Johnson", "Brown", "Jones", "Garcia", "Lopez", "Young"};

        Random random = new Random();

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            conn.setAutoCommit(false);

            String createLeagueTable = """
                DROP TABLE IF EXISTS league CASCADE;
                CREATE TABLE league (
                    id BIGSERIAL PRIMARY KEY,
                    name TEXT,
                    country TEXT,
                    sponsor TEXT,
                    founded_year SMALLINT
                );
                """;
            conn.createStatement().execute(createLeagueTable);

            String createClubTable = """
                DROP TABLE IF EXISTS club CASCADE;
                CREATE TABLE club (
                    id BIGSERIAL PRIMARY KEY,
                    name TEXT,
                    city TEXT,
                    founded_year SMALLINT,
                    league_id BIGINT,
                    stadium TEXT,
                    capacity INTEGER,
                    budget NUMERIC(15,2),
                    president TEXT,
                    coach TEXT,
                    wins SMALLINT,
                    draws SMALLINT,
                    losses SMALLINT,
                    last_season_position SMALLINT,
                    last_match DATE
                );
                """;
            conn.createStatement().execute(createClubTable);

            System.out.println("Generowanie 400 000 rekordów do league...");
            String leagueSQL = "INSERT INTO league(name, country, sponsor, founded_year) VALUES (?, ?, ?, ?)";
            PreparedStatement leagueStmt = conn.prepareStatement(leagueSQL);

            for (int i = 1; i <= 400_000; i++) {
                leagueStmt.setString(1, "Liga #" + i);
                leagueStmt.setString(2, countries[random.nextInt(countries.length)]);
                leagueStmt.setString(3, sponsors[random.nextInt(sponsors.length)]);
                leagueStmt.setInt(4, 1900 + random.nextInt(121));
                leagueStmt.addBatch();

                if (i % 5000 == 0) {
                    leagueStmt.executeBatch();
                    conn.commit();
                }
            }
            leagueStmt.executeBatch();
            conn.commit();
            System.out.println("Wstawiono 400 000 rekordów do league.");

            System.out.println("Generowanie 12 000 000 losowych rekordów do club...");
            String clubSQL = """
                INSERT INTO club(name, city, founded_year, league_id, stadium, capacity, budget,
                president, coach, wins, draws, losses, last_season_position, last_match)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
            PreparedStatement clubStmt = conn.prepareStatement(clubSQL);

            for (int i = 1; i <= 12_000_000; i++) {
                clubStmt.setString(1, "Klub #" + i);
                clubStmt.setString(2, cities[random.nextInt(cities.length)]);
                clubStmt.setInt(3, 1900 + random.nextInt(121));
                clubStmt.setInt(4, random.nextInt(400_000) + 1);
                clubStmt.setString(5, stadiums[random.nextInt(stadiums.length)]);
                clubStmt.setInt(6, 10000 + random.nextInt(40000));
                clubStmt.setBigDecimal(7, new java.math.BigDecimal(10_000_000 + random.nextInt(90_000_000)));

                String president = firstNames[random.nextInt(firstNames.length)] + " " + lastNames[random.nextInt(lastNames.length)];
                String coach = firstNames[random.nextInt(firstNames.length)] + " " + lastNames[random.nextInt(lastNames.length)];

                clubStmt.setString(8, president);
                clubStmt.setString(9, coach);
                clubStmt.setInt(10, random.nextInt(30));
                clubStmt.setInt(11, random.nextInt(10));
                clubStmt.setInt(12, random.nextInt(10));
                clubStmt.setInt(13, 1 + random.nextInt(20));
                clubStmt.setDate(14, Date.valueOf(LocalDate.now().minusDays(random.nextInt(50))));

                clubStmt.addBatch();

                if (i % 5000 == 0) {
                    clubStmt.executeBatch();
                    conn.commit();
                }
            }
            clubStmt.executeBatch();
            conn.commit();
            System.out.println("Wstawiono 12 000 000 losowych rekordów do club.");

            System.out.println("Generowanie dodatkowych 8 000 000 rekordów dla Barcelony...");
            for (int i = 12_000_001; i <= 20_000_000; i++) {
                clubStmt.setString(1, "Klub #" + i);
                clubStmt.setString(2, "Barcelona");
                clubStmt.setInt(3, 1900 + random.nextInt(121));
                clubStmt.setInt(4, random.nextInt(400_000) + 1);
                clubStmt.setString(5, stadiums[random.nextInt(stadiums.length)]);
                clubStmt.setInt(6, 10000 + random.nextInt(40000));
                clubStmt.setBigDecimal(7, new java.math.BigDecimal(10_000_000 + random.nextInt(90_000_000)));

                String president = firstNames[random.nextInt(firstNames.length)] + " " + lastNames[random.nextInt(lastNames.length)];
                String coach = firstNames[random.nextInt(firstNames.length)] + " " + lastNames[random.nextInt(lastNames.length)];

                clubStmt.setString(8, president);
                clubStmt.setString(9, coach);
                clubStmt.setInt(10, random.nextInt(30));
                clubStmt.setInt(11, random.nextInt(10));
                clubStmt.setInt(12, random.nextInt(10));
                clubStmt.setInt(13, 1 + random.nextInt(20));
                clubStmt.setDate(14, Date.valueOf(LocalDate.now().minusDays(random.nextInt(50))));

                clubStmt.addBatch();

                if (i % 5000 == 0) {
                    clubStmt.executeBatch();
                    conn.commit();
                }
            }
            clubStmt.executeBatch();
            conn.commit();
            System.out.println("Wstawiono dodatkowe 8 000 000 rekordów Barcelony.");
            System.out.println("Łącznie 20 000 000 rekordów w tabeli club.");

        } catch (SQLException e) {
            System.out.println("Błąd: " + e.getMessage());
            e.printStackTrace();
        }
    }
}