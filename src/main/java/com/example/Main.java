package com.example;
import javax.sql.DataSource;
import java.sql.*;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";


    static void main(String[] args) {
        if (isDevMode(args)) {
            DevDatabaseInitializer.start();
        }
        new Main().run();
    }

    public void run() {
        // Resolve DB settings with precedence: System properties -> Environment variables
        String jdbcUrl = resolveConfig("APP_JDBC_URL", "APP_JDBC_URL");
        String dbUser = resolveConfig("APP_DB_USER", "APP_DB_USER");
        String dbPass = resolveConfig("APP_DB_PASS", "APP_DB_PASS");

        if (jdbcUrl == null || dbUser == null || dbPass == null) {
            throw new IllegalStateException(
                    "Missing DB configuration. Provide APP_JDBC_URL, APP_DB_USER, APP_DB_PASS " +
                            "as system properties (-Dkey=value) or environment variables.");
        }

        DataSource ds = new SimpleDriverManagerDataSource(jdbcUrl, dbUser, dbPass);
        AccountRepository accountRepo = new JdbcAccountRepository(ds);
        MoonMissionRepository missionRepo = new JdbcMoonMissionRepository(ds);

        //Todo: Starting point for your code
        Scanner sc = new Scanner(System.in);

        // Sign in
        System.out.print("Enter username: ");
        String username = sc.nextLine();
        System.out.println("Enter password: ");
        String password = sc.nextLine();

        boolean validLogin = accountRepo.login(username, password);

        if (!validLogin) {
            System.out.println("Invalid username or password");
            return;
        }

        System.out.println("Login successful!");

        // Main menu
        boolean isRunning = true;

        while (isRunning) {
            printMenu();
            System.out.print("> ");
            String option = sc.nextLine();

            switch (option) {
                case "1":
                    missionRepo.listSpacecraft().forEach(System.out::println);
                    break;

                case "2":
                    System.out.print("Enter ID: ");
                    long id = Long.parseLong(sc.nextLine());
                    Mission m = missionRepo.getMissionById(id);
                    if (m == null) {
                        System.out.println("Mission not found");
                    } else {
                        System.out.println("Mission ID: " + m.missionId);
                        System.out.println("Spacecraft: " + m.spacecraft);
                    }
                    break;

                case "3":
                    System.out.print("Enter year: ");
                    int year = Integer.parseInt(sc.nextLine());
                    int count = missionRepo.countByYear(year);
                    System.out.println(count + " missions in year " + year);
                    break;

                case "4":
                    System.out.println("Enter your firstname: ");
                    String first = sc.nextLine();
                    System.out.println("Enter your lastname: ");
                    String last = sc.nextLine();
                    System.out.println("Enter your SSN: ");
                    String ssn = sc.nextLine();
                    System.out.println("Enter your password: ");
                    String pw = sc.nextLine();

                    long newId = accountRepo.create(first, last, ssn, pw);
                    System.out.println("Account created successfully! User ID: " + newId);
                    break;

                case "5":
                    System.out.print("Enter your user ID: ");
                    int uid = Integer.parseInt(sc.nextLine());

                    // Check if account exists
                    if (!accountRepo.exists(uid)) {
                        System.out.println("Account not found!");
                        break;
                    }

                    // If exists --> create new passwrod
                    System.out.println("Enter your new password: ");
                    String newPw = sc.nextLine();

                    boolean updated = accountRepo.updatePassword(uid, newPw);
                    System.out.println(updated ? "Account updated!" : "Account not found!");

                    break;

                case "6":
                    System.out.print("Enter your user ID: ");
                    int delId = Integer.parseInt(sc.nextLine());
                    boolean deleted = accountRepo.delete(delId);
                    System.out.println(deleted ? "Account deleted!" : "No account deleted.");
                    break;

                case "0":
                    isRunning = false;
                    break;

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }


    private void printMenu() {
        System.out.println(BLUE + " " + RESET);
        System.out.println(BLUE + "------MAIN MENU------" + RESET);
        System.out.println(BLUE + "1. List moon missions" + RESET);
        System.out.println(BLUE + "2. Get mission by ID" + RESET);
        System.out.println(BLUE + "3. Count missions by year" + RESET);
        System.out.println(BLUE + "4. Create account" + RESET);
        System.out.println(BLUE + "5. Update account password" + RESET);
        System.out.println(BLUE + "6. Delete account" + RESET);
        System.out.println(BLUE + "0. Exit" + RESET);
    }




    /**
     * Determines if the application is running in development mode based on system properties,
     * environment variables, or command-line arguments.
     *
     * @param args an array of command-line arguments
     * @return {@code true} if the application is in development mode; {@code false} otherwise
     */
    private static boolean isDevMode(String[] args) {
        if (Boolean.getBoolean("devMode"))  //Add VM option -DdevMode=true
            return true;
        if ("true".equalsIgnoreCase(System.getenv("DEV_MODE")))  //Environment variable DEV_MODE=true
            return true;
        return Arrays.asList(args).contains("--dev"); //Argument --dev
    }

    /**
     * Reads configuration with precedence: Java system property first, then environment variable.
     * Returns trimmed value or null if neither source provides a non-empty value.
     */
    private static String resolveConfig(String propertyKey, String envKey) {
        String v = System.getProperty(propertyKey);
        if (v == null || v.trim().isEmpty()) {
            v = System.getenv(envKey);
        }
        return (v == null || v.trim().isEmpty()) ? null : v.trim();
    }
}
