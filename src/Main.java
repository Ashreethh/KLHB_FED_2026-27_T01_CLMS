import java.util.*;
import java.nio.file.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

class Book {
    private String title;
    private int copies;

    public Book(String title, int copies) {
        this.title = title;
        this.copies = copies;
    }
    public String getTitle() { return title; }
    public int getCopies() { return copies; }
    public void setCopies(int copies) { this.copies = copies; }
    public String toString() { return title + " (copies: " + copies + ")"; }
}

class Member {
    private String id;
    private String name;

    public Member(String id, String name) {
        this.id = id;
        this.name = name;
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public String toString() { return id + " - " + name; }
}

class Loan {
    private String memberId;
    private String bookTitle;
    private LocalDate dueDate;

    public Loan(String memberId, String bookTitle, LocalDate dueDate) {
        this.memberId = memberId;
        this.bookTitle = bookTitle;
        this.dueDate = dueDate;
    }
    public String getMemberId() { return memberId; }
    public String getBookTitle() { return bookTitle; }
    public LocalDate getDueDate() { return dueDate; }
}

public class Main {
    static Scanner sc = new Scanner(System.in);
    static ArrayList<Book> books = new ArrayList<>();
    static ArrayList<Member> members = new ArrayList<>();
    static ArrayList<Loan> loans = new ArrayList<>();
    static Path file = Paths.get("library.txt");

    public static void main(String[] args) {
        loadData();
        int choice = 0;
        do {
            System.out.println("\n=== COMMUNITY LIBRARY ===");
            System.out.println("1. Add book  2. View books  3. Search book");
            System.out.println("4. Register member  5. Borrow book  6. Return book");
            System.out.println("7. Overdue report  8. Most borrowed  9. Save and exit");
            System.out.print("Choice: ");
            try {
                choice = Integer.parseInt(sc.nextLine());
                switch (choice) {
                    case 1: addBook(); break;
                    case 2: viewBooks(); break;
                    case 3: searchBook(); break;
                    case 4: registerMember(); break;
                    case 5: borrowBook(); break;
                    case 6: returnBook(); break;
                    case 7: overdueReport(); break;
                    case 8: mostBorrowed(); break;
                    case 9: saveData(); System.out.println("Saved. Goodbye!"); break;
                    default: System.out.println("Choose a number from 1 to 9.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        } while (choice != 9);
    }

    static Book findBook(String title) {
        for (Book b : books)
            if (b.getTitle().equalsIgnoreCase(title)) return b;
        return null;
    }

    static void addBook() {
        System.out.print("Book title: ");
        String title = sc.nextLine().trim();
        if (title.isEmpty()) { System.out.println("Title cannot be empty."); return; }
        System.out.print("Number of copies: ");
