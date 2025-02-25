import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;

@WebServlet("/borrow")
public class BorrowingServlet extends HttpServlet {
    private Connection connection;

    @Override
    public void init() {
        connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        out.println("<h1>Borrowing</h1>");

        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT b.book_code, m.name, br.borrow_date, br.return_date " +
                        "FROM Borrowings br JOIN Members m ON br.member_id = m.id " +
                        "JOIN Books b ON br.book_code = b.code ORDER BY br.borrow_date DESC")) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String returnDate = rs.getTimestamp("return_date") == null ? "Not Returned" : rs.getTimestamp("return_date").toString();
                out.println("<p>[" + rs.getString("book_code") + "] - " + rs.getString("name") +
                        " [" + rs.getTimestamp("borrow_date") + " - " + returnDate + "]</p>");
            }
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }

        out.println("<h2>Borrow Book</h2>");
        out.println("<form method='POST'><input name='bookCode' placeholder='Book Code'><input name='memberId' placeholder='Member ID'><button type='submit'>Borrow</button></form>");

        out.println("<h2>Return Book</h2>");
        out.println("<form method='POST' action='/borrow/return'><input name='bookCode' placeholder='Book Code'><button type='submit'>Return</button></form>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String bookCode = req.getParameter("bookCode");
        String memberId = req.getParameter("memberId");

        try {
            PreparedStatement checkStmt = connection.prepareStatement(
                    "SELECT * FROM Borrowings WHERE book_code = ? AND return_date IS NULL");
            checkStmt.setString(1, bookCode);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                resp.sendError(422, "Book is already borrowed and not returned.");
                return;
            }

            PreparedStatement borrowStmt = connection.prepareStatement(
                    "INSERT INTO Borrowings (book_code, member_id, borrow_date) VALUES (?, ?, ?)");
            borrowStmt.setString(1, bookCode);
            borrowStmt.setInt(2, Integer.parseInt(memberId));
            borrowStmt.setTimestamp(3, Timestamp.from(Instant.now()));
            borrowStmt.executeUpdate();

            resp.sendRedirect("/borrow");
        } catch (SQLException e) {
            resp.sendError(422, "Error borrowing book. Check if book and member exist.");
        }
    }
}

@WebServlet("/borrow/return")
class ReturnBookServlet extends HttpServlet {
    private Connection connection;

    @Override
    public void init() {
        connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String bookCode = req.getParameter("bookCode");
        try {
            PreparedStatement updateStmt = connection.prepareStatement(
                    "UPDATE Borrowings SET return_date = ? WHERE book_code = ? AND return_date IS NULL");
            updateStmt.setTimestamp(1, Timestamp.from(Instant.now()));
            updateStmt.setString(2, bookCode);
            int updatedRows = updateStmt.executeUpdate();

            if (updatedRows == 0) {
                resp.sendError(422, "Book is not currently borrowed or does not exist.");
            } else {
                resp.sendRedirect("/borrow");
            }
        } catch (SQLException e) {
            resp.sendError(422, "Error returning book.");
        }

    }

}
