package login;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jasypt.util.password.StrongPasswordEncryptor;
import resources.Employee;
import resources.User;
import resources.RecaptchaVerifyUtils;
import java.util.UUID;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import jakarta.servlet.http.Cookie;
import common.RedisUtil;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


@WebServlet(name = "servlet.LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    private DataSource dataSourceMaster;
    private DataSource dataSourceSlave;
    private static final int SESSION_TTL_SECONDS = 24 * 60 * 60;
    public void init(ServletConfig config) {
        try {
            dataSourceMaster = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/MySQLReadWrite");
            dataSourceSlave = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/MySQLReadOnly");
            RedisUtil.init();
	} catch (NamingException e) {
            e.printStackTrace();
        }
    }

    private User getUser(String inputUsername) {
        String query = "SELECT id, password FROM customers WHERE email = ?";
        try (Connection conn = dataSourceSlave.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, inputUsername);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("id");
                    String userPassword = rs.getString("password");
                    return new User(userId, inputUsername, userPassword);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private Employee getEmployee(String inputUsername) {
        String query = "SELECT fullname, password FROM employees WHERE email = ?";
        try (Connection conn = dataSourceMaster.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, inputUsername);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    String fullName = rs.getString("fullname");
                    String userPassword = rs.getString("password");
                    return new Employee(inputUsername, fullName, userPassword);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String inputRole = request.getParameter("role");
        String inputUsername = request.getParameter("username");
        String inputPassword = request.getParameter("password");
        JsonObject responseJsonObject = new JsonObject();

        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);

        try {
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
        } catch (Exception e) {
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "reCAPTCHA verification failed");
            response.getWriter().write(responseJsonObject.toString());
            return;
        }


        if (inputRole.equals("user")) {
            User user = getUser(inputUsername);
            if (user == null) {
                responseJsonObject.addProperty("status", "fail");
                request.getServletContext().log("Login failed");
                responseJsonObject.addProperty("message", "user " + inputUsername + " doesn't exist");
            }
            else if (!new StrongPasswordEncryptor().checkPassword(inputPassword, user.getPassword())) {
                responseJsonObject.addProperty("status", "fail");
                request.getServletContext().log("Login failed");
                responseJsonObject.addProperty("message", "incorrect password");
            }
            else {
                // Login success: set this user into the session
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            	String loginTime = dateFormat.format(new Date());
            	String sessionId = UUID.randomUUID().toString();
		JsonObject sessionObject = new JsonObject();

		sessionObject.addProperty("user", user.getUsername());
		sessionObject.addProperty("loginTime", loginTime);
		sessionObject.addProperty("userId", user.getId());

		RedisUtil.set("session:" + sessionId, sessionObject.toString(), SESSION_TTL_SECONDS);
		Cookie sessionCookie = new Cookie("redisSessionId", sessionId);
            	sessionCookie.setHttpOnly(true);
            	sessionCookie.setPath("/");
            	sessionCookie.setMaxAge(SESSION_TTL_SECONDS);
            	response.addCookie(sessionCookie);
		//request.getSession().setAttribute("user", user);
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");
            }
        }
        else if (inputRole.equals("employee")) {
            Employee employee = getEmployee(inputUsername);
            if (employee == null) {
                responseJsonObject.addProperty("status", "fail");
                request.getServletContext().log("Login failed");
                responseJsonObject.addProperty("message", "employee " + inputUsername + " doesn't exist");
            }
            else if (!inputPassword.equals(employee.getPassword())) {
                responseJsonObject.addProperty("status", "fail");
                request.getServletContext().log("Login failed");
                responseJsonObject.addProperty("message", "incorrect password");
            }
            else {
                // Login success: set this employee into the session
                //request.getSession().setAttribute("employee", employee);
                //String sessionId = UUID.randomUUID().toString();
		RedisUtil.set("employee", employee.getEmail(), SESSION_TTL_SECONDS);
		responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");
            }
        }
        response.getWriter().write(responseJsonObject.toString());
    }
}
