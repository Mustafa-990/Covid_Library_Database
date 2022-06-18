import java.io.PrintStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import org.apache.derby.jdbc.EmbeddedDriver;

//This project was completed as a team project. The members of the team are Rana Muhammad Hamza and Taha Babar.

public class Main {
	private static final Scanner in = new Scanner(System.in);
	private static final PrintStream out = System.out;

	public static void main(String[] args) {
		try {
			Driver d = new EmbeddedDriver();
			Connection conn = d.connect("jdbc:derby:LibraryDB;create=true", null);
			createTables(conn);
			resetTables(conn);
			displayMenu();
			loop: while (true) {
				switch (requestString("Selection (0 to quit, 9 for menu)? ")) {
				case "0": // Quit
					conn.close();
					break loop;

				case "1": // Reset
					resetTables(conn);
					break;

				case "2": // List all books available
					listAllBooksInDatabase(conn);
					break;

				case "3": // Selects a book to checkout
					selectBookToCheckout(conn);
					break;

				case "4": // Displays checked out books
					displayCheckoutList(conn);
					break;

				case "5": // Add a book to the database
					addBook(conn);
					break;

				case "6": //  List all books of a specific department
					displayBooksOfDepartment(conn);
					break;
					
				case "7": // Displays books issued by a specific student
					displayBooksIssuedByAStudent(conn);
					break;
				
				case "8": // Removes a checked out entry
					deleteCheckOutEntry(conn);
					break;

				default:
					displayMenu();
					break;
				}
			}
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		out.println("Done");
	}
	
	
	private static void displayMenu() {
		out.println("0: Quit");
		out.println("1: Reset tables");
		out.println("2: List all books available");
		out.println("3: Select a book to checkout");
		out.println("4: Display checked out books");
		out.println("5: Add a book to the database");
		out.println("6: List all books of a specific department");
		out.println("7: Display books issued by a specific student");
		out.println("8: Remove a checked out entry");
	}
	
	private static String requestString(String prompt) {
		out.print(prompt);
		out.flush();
		return in.nextLine();
	}

	private static void createTables(Connection conn) {
		// First clean up from previous runs, if any
		dropTables(conn);

		// Now create the schema
		addTables(conn);
	}

	private static void doUpdate(Connection conn, String statement, String message) {
		try (Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(statement);
			System.out.println(message);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void doUpdateNoError(Connection conn, String statement, String message) {
		try (Statement stmt = conn.createStatement()) {
			stmt.executeUpdate(statement);
			System.out.println(message);
		} catch (SQLException e) {
			// Ignore error
		}
	}
	
	private static void addTables(Connection conn) {
		StringBuilder sb = new StringBuilder();
		sb.append("create table DEPARTMENT(");
		sb.append("  dName varchar(255) not null,");
		sb.append("  chairName varchar(255) not null,");
		sb.append("  location varchar(255) not null,");
		sb.append("  primary key(dName)");
		sb.append(")");
		doUpdate(conn, sb.toString(), "Table DEPARTMENT created.");
		
		sb = new StringBuilder();
		sb.append("create table STUDENT(");
		sb.append("  sName varchar(255) not null,");
		sb.append("  sId integer not null,");
		sb.append("  major varchar(255) not null,");
		sb.append("  primary key(sId),");
		sb.append("  foreign key (major) references DEPARTMENT on delete no action");		
		sb.append(")");
		doUpdate(conn, sb.toString(), "Table STUDENT created.");

		sb = new StringBuilder();
		sb.append("create table BOOK(");
		sb.append("  bName varchar(255) not null,");
		sb.append("  author varchar(255) not null,");
		sb.append("  bookId integer not null,");
		sb.append("  pickupLibrary varchar(255) not null,");
		sb.append("  pickupShelf varchar(15) not null,");
		sb.append("  department varchar(255) not null,");
		sb.append("  primary key(bookId),");
		sb.append("  foreign key (department) references DEPARTMENT on delete no action");		
		sb.append(")");
		doUpdate(conn, sb.toString(), "Table BOOK created.");

		sb = new StringBuilder();
		sb.append("create table CHECKOUT(");
		sb.append("  book integer not null,");
		sb.append("  student integer not null,");
		sb.append("  foreign key (book) references BOOK on delete no action,");
		sb.append("  foreign key (student) references STUDENT on delete no action");
		sb.append(")");
		doUpdate(conn, sb.toString(), "Table CHECKOUT created.");
	}

	private static void dropTables(Connection conn) {
		doUpdateNoError(conn, "drop table CHECKOUT", "Table CHECKOUT dropped.");
		doUpdateNoError(conn, "drop table BOOK", "Table BOOK dropped.");
		doUpdateNoError(conn, "drop table STUDENT", "Table STUDENT dropped.");
		doUpdateNoError(conn, "drop table DEPARTMENT", "Table DEPARTMENT dropped.");
		
	}

	private static void resetTables(Connection conn) {
		try (Statement stmt = conn.createStatement()) {
			int count = 0;
			count += stmt.executeUpdate("delete from CHECKOUT");
			count += stmt.executeUpdate("delete from BOOK");
			count += stmt.executeUpdate("delete from STUDENT");
			count += stmt.executeUpdate("delete from DEPARTMENT");
			System.out.println(count + " records deleted");

			String[] deptvals = {
					"('compsci', 'thede', 'julian')", "('physics', 'kertzmann', 'julian')", "('english', 'white', 'asbury')"
			};
			count = 0;
			for (String val : deptvals) {
				count += stmt.executeUpdate("insert into DEPARTMENT(dName, chairName, location) values " + val);
			}
			System.out.println(count + " DEPARTMENT records inserted.");
			
			String[] studVals = {
					"('Taha Rabar', 1, 'compsci')",
					"('Vidit Khandelwal', 2, 'english')",
					"('Kavya Shrivastava', 3, 'physics')",
			};
			count = 0;
			for (String val : studVals) {
				count += stmt.executeUpdate("insert into STUDENT(sName, sId, major) values " + val);
			}
			System.out.println(count + " STUDENT records inserted.");
			
			String[] bookvals = {
					"('Java Fundamentals', 'Lori White', 1, 'Prevo', '24J', 'compsci')",
					"('C++ Fundamentals', 'Taha Babar', 2, 'Prevo', '19K', 'compsci')",
					"('Python Fundamentals', 'Brian Howard', 3, 'Prevo', '54F', 'compsci')",
					"('Quantam Mechanics', 'Muhammad Omer Sajid', 4, 'Prevo', '25J', 'physics')",
					"('Nucleur Physics', 'Kristen Wig', 5, 'Prevo', '99K', 'physics')",
					"('Magnetism', 'Isaac Newton', 6, 'Prevo', '1I', 'physics')",
					"('How to Become a Better Writer?', 'Bloomington Squirrel', 7, 'Roy O West', '66X', 'english')",
					"('Grammar 101', 'Harrison Ford', 8, 'Roy O West', '13K', 'english')",
					"('Harry Potter and the Goblet of Fire', 'J.K. Rowling', 9, 'Roy O West', '55G', 'english')",
			};
			count = 0;
			for (String val : bookvals) {
				count += stmt.executeUpdate("insert into BOOK(bName, author, bookId, pickupLibrary, pickupShelf, department) values " + val);
			}
			System.out.println(count + " BOOK records inserted.");

			String[] covals = {
					//"(3, 1)",
			};
			count = 0;
			for (String val : covals) {
				count += stmt.executeUpdate("insert into CHECKOUT(book, student) values " + val);
			}
			System.out.println(count + " CHECKOUT records inserted.");

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private static void listAllBooksInDatabase(Connection conn) {
		StringBuilder query = new StringBuilder();
		query.append("select b.bName, b.author, b.bookId, b.pickupLibrary, b.pickupShelf, b.department");
		query.append("  from BOOK b");

		try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query.toString())) {
			out.printf("%-40s %-30s %-4s %-20s %-20s %-12s\n", "Book Name", "Author", "ID", "Pickup Library", "Pickup Shelf", "Department");
			out.println("----------------------------");
			while (rs.next()) {
				String bName = rs.getString("bName");
				String author = rs.getString("author");
				int bookId = rs.getInt("bookId");
				String pickupLibrary = rs.getString("pickupLibrary");
				String pickupShelf = rs.getString("pickupShelf");
				String department = rs.getString("department");

				out.printf("%-40s %-30s %-4s %-20s %-20s %-12s\n", bName, author, bookId, pickupLibrary, pickupShelf, department);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	private static void addBook(Connection conn) {
		String bName = requestString("Book name? ");
		String author = requestString("Book author? ");
		String bookId = requestString("Book ID? ");
		String pickupLibrary = requestString("Pickup Library? ");
		String pickupShelf = requestString("Pickup Shelf? ");
		String department = requestString("Department? ");
		
		

		StringBuilder command = new StringBuilder();
		command.append("insert into BOOK(bName, author, bookId, pickupLibrary, pickupShelf, department) values (?,?,?,?,?,?)");

		try (PreparedStatement pstmt = conn.prepareStatement(command.toString())) {
			pstmt.setString(1, bName);
			pstmt.setString(2, author);
			pstmt.setString(3, bookId);
			pstmt.setString(4, pickupLibrary);
			pstmt.setString(5, pickupShelf);
			pstmt.setString(6, department);
			int count = pstmt.executeUpdate();

			out.println(count + " book(s) inserted");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private static void displayCheckoutList(Connection conn) {
		StringBuilder query = new StringBuilder();
		//query.append("select b.book, b.student");
		query.append("select b.book, d.bName, c.sName");
		query.append("  from CHECKOUT b, STUDENT c, BOOK d");
		query.append("  where b.student = c.sId and b.book = d.bookId");
		//query.append("  from CHECKOUT b, STUDENT c");

		try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query.toString())) {
			out.printf("%-20s %-20s %-20s\n","Book Id", "Book Name", "Student Name");
			out.println("-------------------------------------------------------------");
			while (rs.next()) {
				String book = rs.getString("book");
				String bookName = rs.getString("bName");
				String student = rs.getString("sName");

				out.printf("%-20s %-20s %-20s\n", book, bookName, student);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	private static void deleteCheckOutEntry(Connection conn) {
		String book = requestString("Enter Book Id? ");  //its better to ask for id rather than name because what if there are more books with same name

		StringBuilder command = new StringBuilder();
		command.append("delete from CHECKOUT");
		command.append("  where book = ?");

		try (PreparedStatement pstmt = conn.prepareStatement(command.toString())) {
			pstmt.setString(1, book);
			int count = pstmt.executeUpdate();

			out.println(count + " record(s) deleted");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
	}
	
	private static void selectBookToCheckout(Connection conn) {
		
		String book = requestString("Book ID? "); //its better to ask for id rather than name because what if there are more books with same name
		String student = requestString("Student ID? "); //its better to ask for id rather than name because what if there are more students with same name

		StringBuilder command = new StringBuilder();
		command.append("insert into CHECKOUT(book, student) values (?,?)");

		try (PreparedStatement pstmt = conn.prepareStatement(command.toString())) {
			pstmt.setString(1, book);
			pstmt.setString(2, student);
			int count = pstmt.executeUpdate();

			out.println(count + " book(s) checked out");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
	}
	
	private static void displayBooksOfDepartment(Connection conn) {
		String department = requestString("Department Name? ");
		
		StringBuilder query = new StringBuilder();
		query.append("select b.bName, b.department");
		query.append("  from BOOK b");
		query.append("  where department = ?");

		try (PreparedStatement pstmt = conn.prepareStatement(query.toString())) {
			pstmt.setString(1, department);
			ResultSet rs = pstmt.executeQuery();
			out.printf("%-20s %-20s\n","Book Name", "Department Name");
			out.println("----------------------------");
			while (rs.next()) {
				String book = rs.getString("bName");
				String departmentName = rs.getString("department");

				out.printf("%-20s %-20s\n", book, departmentName);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	private static void displayBooksIssuedByAStudent(Connection conn) {
		String student = requestString("Student ID? "); //its better to ask for id rather than name because what if there are more students with same name
		
		StringBuilder query = new StringBuilder();
		query.append("select b.book, d.bName, c.sName, c.sId");
		query.append("  from CHECKOUT b, STUDENT c, BOOK d");
		query.append("  where b.student = ? and b.book = d.bookId and b.student = c.sId");
		
		try (PreparedStatement pstmt = conn.prepareStatement(query.toString())) {
			pstmt.setString(1, student);
			ResultSet rs = pstmt.executeQuery();
			out.printf("%-20s %-20s %-20s %-20s\n","Book ID", "Book Name", "Student Name", "Student ID");
			out.println("-----------------------------------------------------------------------");
			while (rs.next()) {
				String book = rs.getString("book");
				String bName = rs.getString("bName");
				String studentName = rs.getString("sName");
				String studentId = rs.getString("sId");

				out.printf("%-20s %-20s %-20s %-20s\n", book, bName, studentName, studentId);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
}