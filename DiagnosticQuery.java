import java.sql.*;
public class DiagnosticQuery {
    public static void main(String[] a) throws Exception {
        String url = "jdbc:mysql://43.204.216.74:3306/trego_db_2?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        Connection conn = DriverManager.getConnection(url, "root", "123456789");
        
        System.out.println("--- DROPPING INCORRECT LEGACY CONSTRAINT FKg9h7rx5ml7y47afcha8tiftsk ON order_items ---");
        try {
            conn.createStatement().execute("ALTER TABLE order_items DROP FOREIGN KEY FKg9h7rx5ml7y47afcha8tiftsk");
            System.out.println("SUCCESS: Dropped incorrect foreign key constraint successfully!");
        } catch (Exception e) {
            System.out.println("ERROR dropping constraint: " + e.getMessage());
        }
        
        System.out.println("\n--- CREATING CORRECT CONSTRAINT ON medicine_master_db_table ---");
        try {
            conn.createStatement().execute(
                "ALTER TABLE order_items ADD CONSTRAINT fk_order_items_master_medicine " +
                "FOREIGN KEY (medicine_id) REFERENCES medicine_master_db_table(medicine_id)"
            );
            System.out.println("SUCCESS: Added correct foreign key constraint to medicine_master_db_table successfully!");
        } catch (Exception e) {
            System.out.println("ERROR adding correct constraint: " + e.getMessage());
        }
        
        conn.close();
    }
}
