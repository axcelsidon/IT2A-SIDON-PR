package property_rental2a;

import java.sql.*;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.InputMismatchException;

public class Property_Rental2A {
    private static final String DATABASE_URL = "jdbc:sqlite:PropertyRental.db";
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try (Connection conn = connect()) {
            while (true) {
                displayMenu();
                System.out.print("Select an option: ");
                int choice = getIntInput();

                if (choice == 1) {
                    viewAvailableProperties(conn);
                } else if (choice == 2) {
                    rentProperty(conn);
                } else if (choice == 3) {
                    viewRentalHistory(conn);
                } else if (choice == 4) {
                    addProperty(conn);
                } else if (choice == 5) {
                    updatePropertyDetails(conn);
                } else if (choice == 6) {
                    deleteProperty(conn);
                } else if (choice == 7) {
                    System.out.println("Exiting...");
                    break;
                } else {
                    System.out.println("Invalid choice. Try again.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
    }

    private static void displayMenu() {
        System.out.println("\nProperty Rental System");
        System.out.println("1. View Available Properties");
        System.out.println("2. Rent Property");
        System.out.println("3. View Rental History");
        System.out.println("4. Add Property");
        System.out.println("5. Update Property Details");
        System.out.println("6. Delete Property");
        System.out.println("7. Exit");
    }

    private static Connection connect() {
        try {
            Connection conn = DriverManager.getConnection(DATABASE_URL);
            System.out.println("Connected to SQLite.");
            return conn;
        } catch (SQLException e) {
            throw new RuntimeException("Connection failed: " + e.getMessage());
        }
    }

    private static int getIntInput() {
        while (true) {
            try {
                return scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                scanner.next(); // Clear invalid input
            }
        }
    }

   private static void viewAvailableProperties(Connection conn) throws SQLException {
    String query = "SELECT id, name, type, location, rent FROM properties WHERE available = 1";
    try (PreparedStatement stmt = conn.prepareStatement(query);
         ResultSet rs = stmt.executeQuery()) {

       
        System.out.println("\nAvailable Properties:");
        System.out.println("+-----+----------------------+---------------+----------------------+----------+");
        System.out.printf("| %-3s | %-20s | %-13s | %-20s | %-8s |%n", 
                          "ID", "Property Name", "Type", "Location", "Rent ($)");
        System.out.println("+-----+----------------------+---------------+----------------------+----------+");

        
        boolean hasResults = false;
        while (rs.next()) {
            hasResults = true;
            System.out.printf("| %-3d | %-20s | %-13s | %-20s | %-8d |%n",
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("type"),
                    rs.getString("location"),
                    rs.getInt("rent"));
        }

        
        if (!hasResults) {
            System.out.println("|                No available properties at the moment.                 |");
        }

       
        System.out.println("+-----+----------------------+---------------+----------------------+----------+");
    } catch (SQLException e) {
        System.out.println("Error fetching available properties: " + e.getMessage());
    }
}



    private static void rentProperty(Connection conn) throws SQLException {
        System.out.print("Enter the ID of the property you want to rent: ");
        int propertyId = getIntInput();

        String checkQuery = "SELECT name, available FROM properties WHERE id = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, propertyId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt("available") == 1) {
                String propertyName = rs.getString("name");
                System.out.print("Enter renter's name: ");
                scanner.nextLine();  // Consume newline
                String renterName = scanner.nextLine();

                System.out.print("Enter renter's contact number (e.g., 123-456-7890): ");
                String contactNumber = scanner.nextLine();

                // Insert rental record
                String insertQuery = "INSERT INTO rental_history (property_id, renter_name, renter_contact, rental_date) VALUES (?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                    insertStmt.setInt(1, propertyId);
                    insertStmt.setString(2, renterName);
                    insertStmt.setString(3, contactNumber);
                    insertStmt.setString(4, java.time.LocalDate.now().toString());
                    insertStmt.executeUpdate();
                }

                // Update property status
                String updateQuery = "UPDATE properties SET available = 0 WHERE id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                    updateStmt.setInt(1, propertyId);
                    updateStmt.executeUpdate();
                }
                System.out.println("Property rented successfully!");
            } else {
                System.out.println("Property not available.");
            }
        }
    }

    private static void viewRentalHistory(Connection conn) throws SQLException {
        String query = "SELECT rental_id, property_id, renter_name, rental_date FROM rental_history";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            System.out.println("\nRental History:");
            while (rs.next()) {
                System.out.printf("Rental ID: %d, Property ID: %d, Renter: %s, Date: %s%n",
                        rs.getInt("rental_id"),
                        rs.getInt("property_id"),
                        rs.getString("renter_name"),
                        rs.getString("rental_date"));
            }
        }
    }

    private static void addProperty(Connection conn) throws SQLException {
        System.out.print("Enter property name: ");
        scanner.nextLine();  // Consume newline
        String name = scanner.nextLine();

        System.out.print("Enter property type: ");
        String type = scanner.nextLine();

        System.out.print("Enter property location: ");
        String location = scanner.nextLine();

        System.out.print("Enter property rent: ");
        int rent = getIntInput();

        String insertQuery = "INSERT INTO properties (name, type, location, rent, available) VALUES (?, ?, ?, ?, 1)";
        try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            stmt.setString(1, name);
            stmt.setString(2, type);
            stmt.setString(3, location);
            stmt.setInt(4, rent);
            stmt.executeUpdate();
            System.out.println("Property added successfully!");
        }
    }

    private static void updatePropertyDetails(Connection conn) throws SQLException {
        System.out.print("Enter the ID of the property to update: ");
        int propertyId = getIntInput();

        System.out.print("Enter new rent: ");
        int newRent = getIntInput();

        String updateQuery = "UPDATE properties SET rent = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
            stmt.setInt(1, newRent);
            stmt.setInt(2, propertyId);
            stmt.executeUpdate();
            System.out.println("Property details updated successfully!");
        }
    }

    private static void deleteProperty(Connection conn) throws SQLException {
        System.out.print("Enter the ID of the property to delete: ");
        int propertyId = getIntInput();

        String deleteQuery = "DELETE FROM properties WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
            stmt.setInt(1, propertyId);
            stmt.executeUpdate();
            System.out.println("Property deleted successfully!");
        }
    }
}
