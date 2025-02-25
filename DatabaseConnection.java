import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/library_db";
    private static final String USER = "your_username";
    private static final String PASSWORD = "your_password";
    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            initializeDatabase();
        } catch (SQLException e) {
            throw new RuntimeException("Error connecting to the database", e);
        }
    }
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private void initializeDatabase() {
        try (Statement stmt = connection.createStatement()) {
            String createBooksTable = "CREATE TABLE IF NOT EXISTS Books (" +
                    "code VARCHAR(50) PRIMARY KEY, " +
                    "title VARCHAR(255) NOT NULL, " +
                    "author VARCHAR(255) NOT NULL);";

            String createMembersTable = "CREATE TABLE IF NOT EXISTS Members (" +
                    "id SERIAL PRIMARY KEY, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "email VARCHAR(255) UNIQUE NOT NULL, " +
                    "join_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";

            String createBorrowingsTable = "CREATE TABLE IF NOT EXISTS Borrowings (" +
                    "book_code VARCHAR(50) REFERENCES Books(code) ON DELETE CASCADE, " +
                    "member_id INT REFERENCES Members(id) ON DELETE CASCADE, " +
                    "borrow_date TIMESTAMP NOT NULL, " +
                    "return_date TIMESTAMP, " +
                    "PRIMARY KEY (book_code, member_id, borrow_date));";

            stmt.executeUpdate(createBooksTable);
            stmt.executeUpdate(createMembersTable);
            stmt.executeUpdate(createBorrowingsTable);
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing database", e);
        }
    }


    public static void main(String[] args) {
        DatabaseConnection dbInstance = DatabaseConnection.getInstance();
        System.out.println("Database initialized successfully.");
    }
}

