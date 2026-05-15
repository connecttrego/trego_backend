import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class AlterAddressTable {
    public static void main(String[] args) {
        String url = "jdbc:mysql://43.204.216.74:3306/trego_db_2?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String user = "root";
        String password = "123456789";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            String sql = "ALTER TABLE address MODIFY COLUMN lat DECIMAL(38,2), MODIFY COLUMN lng DECIMAL(38,2)";
            int rowsAffected = stmt.executeUpdate(sql);
            System.out.println("Altered address table successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
