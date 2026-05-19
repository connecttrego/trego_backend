import java.sql.*;
public class DiagnosticQuery {
    public static void main(String[] a) throws Exception {
        String url = "jdbc:mysql://43.204.216.74:3306/trego_db_2?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        Connection conn = DriverManager.getConnection(url, "root", "123456789");
        
        System.out.println("--- PRINTING ALL order_items ROWS ---");
        ResultSet rs = conn.createStatement().executeQuery(
            "SELECT id, order_id, medicine_id, vendor_medicine_id, qty, amount FROM order_items"
        );
        while(rs.next()) {
            System.out.println("id: " + rs.getLong("id") + 
                               " | order_id: " + rs.getLong("order_id") + 
                               " | medicine_id: " + rs.getLong("medicine_id") + 
                               " | vendor_medicine_id: " + rs.getLong("vendor_medicine_id") + 
                               " | qty: " + rs.getInt("qty") + 
                               " | amount: " + rs.getDouble("amount"));
        }
        
        conn.close();
    }
}
