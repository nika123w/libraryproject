import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

@WebServlet("/books")
public class BookServlet extends HttpServlet {
    private Connection connection;

    @Override
    public void init() {
        connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        out.println("<h1>Books</h1>");
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM Books")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                out.println("<p>[" + rs.getString("code") + "] " + rs.getString("title") + " - " + rs.getString("author") + "</p>");
            }
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }
        out.println("<h2>Add Book</h2>");
        out.println("<form method='POST'><input name='code' placeholder='Code'><input name='title' placeholder='Title'><input name='author' placeholder='Author'><button type='submit'>Add</button></form>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String code = req.getParameter("code");
        String title = req.getParameter("title");
        String author = req.getParameter("author");


        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO Books (code, title, author) VALUES (?, ?, ?)")) {
            stmt.setString(1, code);
            stmt.setString(2, title);
            stmt.setString(3, author);
            stmt.executeUpdate();
            resp.sendRedirect("/books");
        } catch (SQLException e) {

            resp.sendError(422, "Book code must be unique.");
        }
    }

}
