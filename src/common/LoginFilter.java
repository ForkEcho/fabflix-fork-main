package common;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Servlet Filter implementation class filter.LoginFilter
 */
@WebFilter(filterName = "filter.LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();
    private static final int SESSION_TTL_SECONDS = 24 * 60 * 60;
    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        System.out.println("filter.LoginFilter: " + httpRequest.getRequestURI());

        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }


	if(!httpRequest.getRequestURI().contains("dashboard")){
	    String sessionId = getCookieValue(httpRequest, "redisSessionId");
            if (sessionId == null) {
                httpResponse.sendRedirect("login.html");
                return;
            }   
        
            String sessionKey = "session:" + sessionId;
            String sessionJson = RedisUtil.get(sessionKey);
            if (sessionJson == null || sessionJson.isEmpty()) {
            	httpResponse.sendRedirect("login.html");
            	return;
 	    }
		
            JsonObject sessionObject = JsonParser.parseString(sessionJson).getAsJsonObject();
	    String username = sessionObject.get("user").getAsString();
            String loginTime = sessionObject.get("loginTime").getAsString();
	    String userId = sessionObject.get("userId").getAsString();
	    RedisUtil.set(sessionKey, sessionJson, SESSION_TTL_SECONDS);
	    httpRequest.setAttribute("user", username);
	    httpRequest.setAttribute("userId", userId);
            httpRequest.setAttribute("loginTime", loginTime);

            chain.doFilter(request, response);
	} else {
	   //JsonObject sessionObject = JsonParser.parseString(sessionJson).getAsJsonObject();
           String employee = RedisUtil.get("employee");
	   if(employee == null){
		httpResponse.sendRedirect(httpRequest.getContextPath() + "/_dashboard/login.html");
	   } else {
                chain.doFilter(request, response);
            }
	}

        /*if (httpRequest.getRequestURI().contains("cart") ||  httpRequest.getRequestURI().contains("payment")) {
            if (httpRequest.getSession().getAttribute("user") == null) {
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.html");
            } else {
                chain.doFilter(request, response);
            }
        }
        else if (httpRequest.getRequestURI().contains("dashboard")) {
            // Redirect to login page if the "employee" attribute doesn't exist in session
            if (httpRequest.getSession().getAttribute("employee") == null) {
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/_dashboard/login.html");
            } else {
                chain.doFilter(request, response);
            }
        }
        else {
            // Redirect to login page if the "user" attribute doesn't exist in session
            if (httpRequest.getSession().getAttribute("user") == null && httpRequest.getSession().getAttribute("employee") == null) {
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.html");
            } else {
                chain.doFilter(request, response);
            }
        }*/

    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        /*
         Setup your own rules here to allow accessing some resources without logging in
         Always allow your own login related requests(html, js, servlet, etc..)
         You might also want to allow some CSS files, etc..
         */
        return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    public void init(FilterConfig fConfig) {
        allowedURIs.add("login.html");
        allowedURIs.add("login.css");
        allowedURIs.add("login.js");
        allowedURIs.add("api/login");
        allowedURIs.add("_dashboard/login.html");
        allowedURIs.add("_dashboard/login.js");
    	RedisUtil.init();
    }

    public void destroy() {
        // ignored.
    }

    private String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
