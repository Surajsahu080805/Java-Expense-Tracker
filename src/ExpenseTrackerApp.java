import java.io.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ExpenseTrackerApp {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ExpenseManager manager = new ExpenseManager("expenses.csv");

        // Load previous data (if file exists)
        manager.loadFromFile();

        System.out.println("======================================");
        System.out.println("      PERSONAL EXPENSE MANAGER");
        System.out.println("======================================");

        int choice = -1;
        while (choice != 0) {
            printMenu();
            System.out.print("Enter your choice: ");
            if (!scanner.hasNextInt()) {
                System.out.println("Please enter a valid number.");
                scanner.nextLine();
                continue;
            }
            choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    addExpenseFlow(scanner, manager);
                    break;
                case 2:
                    manager.listAllExpenses();
                    break;
                case 3:
                    listByMonthFlow(scanner, manager);
                    break;
                case 4:
                    monthlySummaryFlow(scanner, manager);
                    break;
                case 5:
                    deleteExpenseFlow(scanner, manager);
                    break;
                case 0:
                    System.out.println("Saving data and exiting...");
                    manager.saveToFile();
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }

        scanner.close();
        System.out.println("Goodbye!");
    }

    private static void printMenu() {
        System.out.println("\n----------- MENU -----------");
        System.out.println("1. Add New Expense");
        System.out.println("2. List All Expenses");
        System.out.println("3. List Expenses by Month & Year");
        System.out.println("4. Show Monthly Total Spending");
        System.out.println("5. Delete Expense by ID");
        System.out.println("0. Exit");
        System.out.println("----------------------------");
    }

    private static void addExpenseFlow(Scanner scanner, ExpenseManager manager) {
        try {
            System.out.print("Enter description (e.g., Lunch, Uber, Gym Fees): ");
            String description = scanner.nextLine();

            System.out.print("Enter category (e.g., Food, Travel, Shopping): ");
            String category = scanner.nextLine();

            System.out.print("Enter amount: ");
            double amount = Double.parseDouble(scanner.nextLine());

            System.out.print("Enter date (yyyy-MM-dd): ");
            String dateStr = scanner.nextLine();
            LocalDate date = LocalDate.parse(dateStr);

            manager.addExpense(description, category, amount, date);
            System.out.println("Expense added successfully!");
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. Expense not added.");
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Use yyyy-MM-dd. Expense not added.");
        } catch (Exception e) {
            System.out.println("Something went wrong: " + e.getMessage());
        }
    }

    private static void listByMonthFlow(Scanner scanner, ExpenseManager manager) {
        try {
            System.out.print("Enter year (e.g., 2025): ");
            int year = Integer.parseInt(scanner.nextLine());
            System.out.print("Enter month (1-12): ");
            int month = Integer.parseInt(scanner.nextLine());

            YearMonth ym = YearMonth.of(year, month);
            manager.listExpensesByMonth(ym);
        } catch (Exception e) {
            System.out.println("Invalid year or month.");
        }
    }

    private static void monthlySummaryFlow(Scanner scanner, ExpenseManager manager) {
        try {
            System.out.print("Enter year (e.g., 2025): ");
            int year = Integer.parseInt(scanner.nextLine());
            System.out.print("Enter month (1-12): ");
            int month = Integer.parseInt(scanner.nextLine());

            YearMonth ym = YearMonth.of(year, month);
            double total = manager.getTotalForMonth(ym);
            System.out.println("Total spending in " + ym + " = " + total);
        } catch (Exception e) {
            System.out.println("Invalid year or month.");
        }
    }

    private static void deleteExpenseFlow(Scanner scanner, ExpenseManager manager) {
        try {
            System.out.print("Enter Expense ID to delete: ");
            int id = Integer.parseInt(scanner.nextLine());
            boolean removed = manager.deleteExpenseById(id);
            if (removed) {
                System.out.println("Expense deleted.");
            } else {
                System.out.println("No expense found with ID: " + id);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
        }
    }
}

// =================== Expense CLASS ===================

class Expense {
    private int id;
    private String description;
    private String category;
    private double amount;
    private LocalDate date;

    public Expense(int id, String description, String category, double amount, LocalDate date) {
        this.id = id;
        this.description = description;
        this.category = category;
        this.amount = amount;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public String toCsvLine() {
        // Note: do not use commas in description/category for simplicity
        return id + "," + description + "," + category + "," + amount + "," + date;
    }

    public static Expense fromCsvLine(String line) {
        String[] parts = line.split(",");
        if (parts.length != 5) return null;

        int id = Integer.parseInt(parts[0]);
        String description = parts[1];
        String category = parts[2];
        double amount = Double.parseDouble(parts[3]);
        LocalDate date = LocalDate.parse(parts[4]);
        return new Expense(id, description, category, amount, date);
    }

    @Override
    public String toString() {
        return String.format("ID: %d | %s | %s | %.2f | %s",
                id, description, category, amount, date);
    }
}

// =================== ExpenseManager CLASS ===================

class ExpenseManager {
    private List<Expense> expenses;
    private int nextId;
    private String filePath;

    public ExpenseManager(String filePath) {
        this.expenses = new ArrayList<>();
        this.nextId = 1;
        this.filePath = filePath;
    }

    public void addExpense(String description, String category, double amount, LocalDate date) {
        Expense e = new Expense(nextId++, description, category, amount, date);
        expenses.add(e);
    }

    public void listAllExpenses() {
        if (expenses.isEmpty()) {
            System.out.println("No expenses found.");
            return;
        }
        System.out.println("\nAll Expenses:");
        for (Expense e : expenses) {
            System.out.println(e);
        }
    }

    public void listExpensesByMonth(YearMonth ym) {
        boolean found = false;
        System.out.println("\nExpenses in " + ym + ":");
        for (Expense e : expenses) {
            YearMonth expenseMonth = YearMonth.from(e.getDate());
            if (expenseMonth.equals(ym)) {
                System.out.println(e);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No expenses in this month.");
        }
    }

    public double getTotalForMonth(YearMonth ym) {
        double total = 0.0;
        for (Expense e : expenses) {
            YearMonth expenseMonth = YearMonth.from(e.getDate());
            if (expenseMonth.equals(ym)) {
                total += e.getAmount();
            }
        }
        return total;
    }

    public boolean deleteExpenseById(int id) {
        for (int i = 0; i < expenses.size(); i++) {
            if (expenses.get(i).getId() == id) {
                expenses.remove(i);
                return true;
            }
        }
        return false;
    }

    public void loadFromFile() {
        File file = new File(filePath);
        if (!file.exists()) {
            // No previous data
            return;
        }

        try (Scanner fileScanner = new Scanner(file)) {
            int maxId = 0;
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (line.isEmpty()) continue;

                Expense e = Expense.fromCsvLine(line);
                if (e != null) {
                    expenses.add(e);
                    if (e.getId() > maxId) {
                        maxId = e.getId();
                    }
                }
            }
            nextId = maxId + 1;
            System.out.println("Loaded " + expenses.size() + " expenses from file.");
        } catch (Exception e) {
            System.out.println("Failed to load from file: " + e.getMessage());
        }
    }

    public void saveToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (Expense e : expenses) {
                writer.println(e.toCsvLine());
            }
            System.out.println("Data saved to " + filePath);
        } catch (IOException e) {
            System.out.println("Failed to save to file: " + e.getMessage());
        }
    }
}
