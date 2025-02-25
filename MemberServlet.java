import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/members")
public class MemberServlet extends HttpServlet {
    private Connection connection;

    @Override
    public void init() {
        connection = DatabaseConnection.getInstance().getConnection();
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        out.println("<h1>Members</h1>");
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM Members")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                out.println("<p>ID: " + rs.getInt("id") + " - " + rs.getString("name") + " (" + rs.getString("email") + ")</p>");
            }
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }
        out.println("<h2>Add Member</h2>");
        out.println("<form method='POST'><input name='name' placeholder='Name'><input name='email' placeholder='Email'><button type='submit'>Add</button></form>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = req.getParameter("name");
        String email = req.getParameter("email");

        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO Members (name, email) VALUES (?, ?)")) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.executeUpdate();
            resp.sendRedirect("/members");
        } catch (SQLException e) {
            resp.sendError(422, "Email must be unique.");
        }

    }

}
