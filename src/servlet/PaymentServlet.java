package servlet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import resources.Item;
import resources.User;
import common.RedisUtil;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.time.YearMonth;
import java.util.HashMap;


@WebServlet(name = "servlet.PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {
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

    private boolean checkCard(String inputNumber, String inputFirst, String inputLast, String inputExpiration) {
        if (inputNumber == null || inputFirst == null || inputLast == null || inputExpiration == null) {
            return false;
        }

        inputNumber = inputNumber.trim();
        inputFirst = inputFirst.trim();
        inputLast = inputLast.trim();
        YearMonth ym;
        try {
            ym = YearMonth.parse(inputExpiration);
        } catch (Exception e) {
            return false;
        }

        String query = "SELECT 1 FROM creditcards WHERE id = ? AND firstName = ? AND lastName = ? AND YEAR(expiration) = ? AND MONTH(expiration) = ? LIMIT 1";
        try (Connection conn = dataSourceSlave.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, inputNumber);
            statement.setString(2, inputFirst);
            statement.setString(3, inputLast);
            statement.setInt(4, ym.getYear());
            statement.setInt(5, ym.getMonthValue());
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String inputNumber = request.getParameter("number");
        String inputFirst = request.getParameter("first");
        String inputLast = request.getParameter("last");
        String inputExpiration = request.getParameter("expiration");
        JsonObject responseJsonObject = new JsonObject();
        //User user = (User) request.getSession().getAttribute("user");
        String userId = (String) request.getAttribute("userId");	
	HashMap<String, Item> cartItems = RedisUtil.getHashMap("items");

        if (checkCard(inputNumber, inputFirst, inputLast, inputExpiration) && cartItems != null && !cartItems.isEmpty()) {
            String query = "INSERT INTO sales (customerId, movieId, saleDate) VALUES (?, ?, CURDATE())";
            try (Connection conn = dataSourceMaster.getConnection()) {
                JsonArray saleIds = new JsonArray();
                PreparedStatement statement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                statement.setInt(1, Integer.parseInt(userId));
                for (Item item : cartItems.values()) {
                    statement.setString(2, item.getId());   // movieId
                    //statement.setInt(3, item.getQuantity());   // quantity might be removed since it poses issue when loading data
                    statement.executeUpdate();

                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (keys.next()) {
                            saleIds.add(keys.getInt(1)); // the generated sales.id
                        }
                    }
                }
                cartItems.clear();
		RedisUtil.setHashMap("items", cartItems, SESSION_TTL_SECONDS);
                statement.close();
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");
                responseJsonObject.add("saleIds", saleIds);
                responseJsonObject.add("total", saleIds);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }
        else {
            responseJsonObject.addProperty("status", "fail");
            request.getServletContext().log("Payment failed");
            if (cartItems == null || cartItems.isEmpty()) {
                responseJsonObject.addProperty("message", "Cart is empty");
            }
            else {
                responseJsonObject.addProperty("message", "Invalid credit card information");
            }
        }

        response.getWriter().write(responseJsonObject.toString());
    }
}
