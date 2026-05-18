import java.sql.*;
public class DiagnosticQuery {
    public static void main(String[] a) throws Exception {
        String url = "jdbc:mysql://43.204.216.74:3306/trego_db_2?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        Connection conn = DriverManager.getConnection(url, "root", "123456789");
        
        System.out.println("--- TESTING NEW NATIVE QUERY ---");
        long medicineId = 203;
        int vendorUserId = 16;
        int externalVendorId = 115690;
        
        String sql = "SELECT * FROM vendor_medicine_price " +
                     "WHERE (vendor_medicine_id = ? OR vendor_medicine_id IN " +
                     "(SELECT vendor_medicine_id FROM vendor_medicine WHERE medicine_id = ? AND (vendor_id = ? OR vendor_id = ?))) " +
                     "AND (vendor_id = ? OR vendor_id = ?)";
                     
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setLong(1, medicineId);
        ps.setLong(2, medicineId);
        ps.setInt(3, vendorUserId);
        ps.setInt(4, externalVendorId);
        ps.setInt(5, vendorUserId);
        ps.setInt(6, externalVendorId);
        
        ResultSet rs = ps.executeQuery();
        while(rs.next()) {
            System.out.println("price_id: " + rs.getInt("price_id") + 
                               " | vendor_medicine_id: " + rs.getLong("vendor_medicine_id") + 
                               " | vendor_id: " + rs.getInt("vendor_id") + 
                               " | mrp: " + rs.getDouble("mrp") + 
                               " | quantity: " + rs.getInt("quantity"));
        }
        
        conn.close();
    }
}
