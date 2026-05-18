import java.sql.*;
public class DiagnosticQuery {
    public static void main(String[] a) throws Exception {
        String url = "jdbc:mysql://43.204.216.74:3306/trego_db_2?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        Connection conn = DriverManager.getConnection(url, "root", "123456789");
        
        System.out.println("--- STOCKS FOR MEDICINE 203 ---");
        ResultSet rs2 = conn.createStatement().executeQuery(
            "SELECT p.price_id, p.vendor_medicine_id, p.vendor_id, p.mrp, p.quantity " +
            "FROM vendor_medicine_price p " +
            "JOIN vendor_medicine m ON m.vendor_medicine_id = p.vendor_medicine_id " +
            "WHERE m.medicine_id = 203"
        );
        while(rs2.next()) {
            int vendorIdInStockTable = rs2.getInt("vendor_id");
            System.out.println("price_id: " + rs2.getInt("price_id") + 
                               " | vendor_medicine_id: " + rs2.getLong("vendor_medicine_id") + 
                               " | vendor_id (in price table): " + vendorIdInStockTable + 
                               " | mrp: " + rs2.getDouble("mrp") + 
                               " | quantity: " + rs2.getInt("quantity"));
            
            // Query matching vendor from vendor_informations
            PreparedStatement ps = conn.prepareStatement(
                "SELECT vendor_user_id, vendor_id, ref_name FROM vendor_informations WHERE vendor_user_id = ? OR vendor_id = ?"
            );
            ps.setInt(1, vendorIdInStockTable);
            ps.setInt(2, vendorIdInStockTable);
            ResultSet rsVendor = ps.executeQuery();
            if (rsVendor.next()) {
                System.out.println("   -> MATCHED VENDOR: vendor_user_id=" + rsVendor.getInt("vendor_user_id") + 
                                   " | vendor_id=" + rsVendor.getInt("vendor_id") + 
                                   " | ref_name=" + rsVendor.getString("ref_name"));
            } else {
                System.out.println("   -> MATCHED VENDOR: NONE found!");
            }
        }
        
        conn.close();
    }
}
