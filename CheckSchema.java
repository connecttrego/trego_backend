import java.sql.*;
public class CheckSchema {
    public static void main(String[] a) throws Exception {
        String url = "jdbc:mysql://43.204.216.74:3306/trego_db_2?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        Connection conn = DriverManager.getConnection(url, "root", "123456789");
        
        System.out.println("--- vendor_medicine ---");
        ResultSet rs1 = conn.createStatement().executeQuery("DESC vendor_medicine");
        while(rs1.next()) System.out.println(rs1.getString("Field") + " : " + rs1.getString("Type"));
        
        System.out.println("\n--- vendor_medicine_information ---");
        ResultSet rs2 = conn.createStatement().executeQuery("DESC vendor_medicine_information");
        while(rs2.next()) System.out.println(rs2.getString("Field") + " : " + rs2.getString("Type"));
        
        conn.close();
    }
}
