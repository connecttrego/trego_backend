import java.sql.*;
public class DBInfo {
    public static void main(String[] a) throws Exception {
        String url = "jdbc:mysql://43.204.216.74:3306/trego_db_2?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        Connection conn = DriverManager.getConnection(url, "root", "123456789");
        
        String[] tables = {"vendor_signup", "vendor_informations", "vendor_medicine_price"};
        for (String table : tables) {
            System.out.println("--- DESCRIBE " + table + " ---");
            try {
                ResultSet rs = conn.createStatement().executeQuery("DESC " + table);
                while(rs.next()) {
                    System.out.println(rs.getString("Field") + " : " + rs.getString("Type"));
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
            System.out.println();
        }
        
        System.out.println("--- SAMPLE VENDOR SIGNUP ---");
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM vendor_signup LIMIT 2");
            ResultSetMetaData md = rs.getMetaData();
            while(rs.next()) {
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    System.out.print(md.getColumnName(i) + ": " + rs.getObject(i) + " | ");
                }
                System.out.println();
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        System.out.println("\n--- SAMPLE VENDOR INFORMATIONS ---");
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM vendor_informations LIMIT 2");
            ResultSetMetaData md = rs.getMetaData();
            while(rs.next()) {
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    System.out.print(md.getColumnName(i) + ": " + rs.getObject(i) + " | ");
                }
                System.out.println();
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        conn.close();
    }
}
