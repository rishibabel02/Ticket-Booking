package ticket.booking.util;

import org.mindrot.jbcrypt.BCrypt;


public class UserServiceUtil {
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static boolean checkPassword(String password, String hashedPassword) {
        try {
            return BCrypt.checkpw(password, hashedPassword);
        } catch (Exception e) {
            System.err.println("Password check failed: " + e.getMessage());
            return false;
        }
    }
}
