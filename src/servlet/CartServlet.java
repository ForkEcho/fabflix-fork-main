package servlet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import resources.Item;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import common.RedisUtil;
/**
 * This servlet.CartServlet is declared in the web annotation below,
 * which is mapped to the URL pattern /api/cart.
 */
@WebServlet(name = "servlet.CartServlet", urlPatterns = "/api/cart")
public class CartServlet extends HttpServlet {
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

    private JsonObject getItemById(String itemId) {
        String query = "SELECT * FROM movies WHERE id = ?";
        try (Connection conn = dataSourceSlave.getConnection()) {
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, itemId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    JsonObject item = new JsonObject();
                    item.addProperty("title", rs.getString("title"));
                    item.addProperty("price", 4.99);
                    item.addProperty("quantity", 1);
                    return item;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;

    }

    /**
     * handles GET requests to store session information
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        JsonObject responseJsonObject = new JsonObject();
	//HashMap<String, Item> cartItems = (HashMap<String, Item>) session.getAttribute("cartItems");
        HashMap<String, Item> cartItems = (HashMap<String, Item>) RedisUtil.getHashMap("items");
        if (cartItems == null) {
            cartItems = new HashMap<>();
	    //session.setAttribute("cartItems", cartItems);
            RedisUtil.setHashMap("items", cartItems, SESSION_TTL_SECONDS);
        }

        JsonArray cartItemsJsonArray = new JsonArray();
        double totalPrice = 0;

        request.getServletContext().log("getting " + cartItems.size() + " items");
        for (Item item : cartItems.values()) {
            JsonObject itemJson = new JsonObject();
            //System.out.println("Found ID: ", item.getId());
	    itemJson.addProperty("id", item.getId());
            itemJson.addProperty("title", item.getName());
            itemJson.addProperty("price", item.getPrice());
            itemJson.addProperty("quantity", item.getQuantity());
            itemJson.addProperty("total", item.getTotalPrice());
            cartItemsJsonArray.add(itemJson);
            totalPrice += item.getTotalPrice();
        }

        // Log to localhost log
        responseJsonObject.add("cartItems", cartItemsJsonArray);
        responseJsonObject.addProperty("total", totalPrice);

        // write all the data into the jsonObject
        response.getWriter().write(responseJsonObject.toString());
    }

    /**
     * handles POST requests to add and show the item list information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String itemId = request.getParameter("item");
        String action = request.getParameter("action");
        System.out.println(itemId);
        HttpSession session = request.getSession();

        // get the previous items in a ArrayList
	
        HashMap<String, Item> cartItems = (HashMap<String, Item>) RedisUtil.getHashMap("items");
        //HashMap<String, Item> cartItems = (HashMap<String, Item>) session.getAttribute("cartItems");
        if(cartItems == null){
	    cartItems = new HashMap<>();
	}
	/*if (cartItems == null) {
            cartItems = new HashMap<>();
	    RedisUtil.setHashMap("items", cartItems, SESSION_TTL_SECONDS);
        }*/

        // prevent corrupted states through sharing under multi-threads
        // will only be executed by one thread at a time
        synchronized (cartItems) {
            switch (action) {
                case "add":
                    if (cartItems.containsKey(itemId)) {
                        cartItems.get(itemId).incrementQuantity();
                    }
                    else {
                        JsonObject itemJson = getItemById(itemId);
                        Item item = new Item(itemId, itemJson.get("title").getAsString(), itemJson.get("price").getAsDouble());
                        cartItems.put(itemId, item);
                    }
                    break;
                case "remove":
                    cartItems.remove(itemId);
                    break;
                case "increment":
                    cartItems.get(itemId).incrementQuantity();
                    break;
                case "decrement":
                    cartItems.get(itemId).decrementQuantity();
                    if (cartItems.get(itemId).getQuantity() <= 0) {
                        cartItems.remove(itemId);
                    }
                    break;
            }
        }

        RedisUtil.setHashMap("items", cartItems, SESSION_TTL_SECONDS);
	doGet(request, response);
    }
}
