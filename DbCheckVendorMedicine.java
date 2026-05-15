import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbCheckVendorMedicine {
    public static void main(String[] args) {
        String url = "jdbc:mysql://43.204.216.74:3306/trego_db_2?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String user = "root";
        String password = "123456789";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("DESCRIBE vendor_medicine");
            while (rs.next()) {
                System.out.println(rs.getString("Field") + " - " + rs.getString("Type"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
