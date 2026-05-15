import java.sql.*;

public class FindFKs {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://43.204.216.74:3306/trego_db_2?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        Connection conn = DriverManager.getConnection(url, "root", "123456789");
        
        ResultSet rs = conn.createStatement().executeQuery(
            "SELECT TABLE_NAME, COLUMN_NAME, CONSTRAINT_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME " +
            "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
            "WHERE REFERENCED_TABLE_SCHEMA = 'trego_db_2' AND REFERENCED_TABLE_NAME = 'vendor_informations'"
        );
        
        while (rs.next()) {
            System.out.println("Table: " + rs.getString("TABLE_NAME") + 
                               ", Column: " + rs.getString("COLUMN_NAME") + 
                               ", Constraint: " + rs.getString("CONSTRAINT_NAME"));
        }
        conn.close();
    }
}
