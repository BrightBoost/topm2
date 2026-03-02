package nl.topicus.jdbc;

public class Main {
    public static void main(String[] args) {
        printHeader("part 1: JDBC Introductie & Connectie");
        new nl.topicus.jdbc.part1intro.ConnectionDemo().runDemo();

        printHeader("part 2: Basis Queries met Statement");
        new nl.topicus.jdbc.part2queries.BasicQueriesDemo().runDemo();

        printHeader("part 3a: SQL Injection Demo");
        new nl.topicus.jdbc.part3preparedstatements.SqlInjectionDemo().runDemo();

        printHeader("part 3b: PreparedStatement Demo");
        new nl.topicus.jdbc.part3preparedstatements.PreparedStatementDemo().runDemo();

        printHeader("part 4: ResultSet Mapping & DAO Pattern");
        new nl.topicus.jdbc.part4mapping.MappingDemo().runDemo();

        printHeader("part 5: DataSource & Connection Management");
        new nl.topicus.jdbc.part5datasource.DataSourceDemo().runDemo();
    }

    private static void printHeader(String title) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  " + title);
        System.out.println("=".repeat(60));
    }
}
