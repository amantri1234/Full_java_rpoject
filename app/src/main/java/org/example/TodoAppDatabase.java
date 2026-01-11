package com.example;

import io.javalin.Javalin;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

// Database imports
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TodoAppDatabase {
    private static final String DB_URL = "jdbc:sqlite:todo_app.db";

    public static void main(String[] args) {
        // Initialize database when app starts
        initializeDatabase();

        // Set up Thymeleaf template engine
        TemplateEngine templateEngine = new TemplateEngine();
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        templateEngine.setTemplateResolver(resolver);
        
        Javalin app = Javalin.create();
        app.start(7072);  // Server runs on port 7072 (different from your other app)

        // ✅ Route 1: Home/Dashboard page
        app.get("/", ctx -> {
            Context context = new Context();
            context.setVariable("pageTitle", "Todo App Dashboard");
            context.setVariable("appName", "My Todo Application");
            
            String renderedHtml = templateEngine.process("index", context);
            ctx.html(renderedHtml);
        });

        // ✅ Route 2: User Registration page
        app.get("/register", ctx -> {
            Context context = new Context();
            
            // Get success/error messages from URL parameters
            String msg = ctx.queryParam("msg");
            String error = ctx.queryParam("error");
            
            if (msg != null) context.setVariable("message", msg);
            if (error != null) context.setVariable("error", error);
            
            // Always set form variables to avoid null errors in templates
            context.setVariable("username", "");
            context.setVariable("email", "");
            
            String renderedHtml = templateEngine.process("register", context);
            ctx.html(renderedHtml);
        });

        // ✅ Route 3: Process Registration - FIXED
        app.post("/register", ctx -> {
            String username = ctx.formParam("username");
            String email = ctx.formParam("email");
            String password = ctx.formParam("password");
            String confirmPassword = ctx.formParam("confirmPassword");

            // Validate registration data
            if (username == null || username.trim().isEmpty()) {
                Context context = new Context();
                context.setVariable("error", "Username is required!");
                context.setVariable("username", username != null ? username : "");
                context.setVariable("email", email != null ? email : "");
                
                String renderedHtml = templateEngine.process("register", context);
                ctx.html(renderedHtml);
                return;
            }
            if (!password.equals(confirmPassword)) {
                Context context = new Context();
                context.setVariable("error", "Passwords do not match!");
                context.setVariable("username", username != null ? username : "");
                context.setVariable("email", email != null ? email : "");
                
                String renderedHtml = templateEngine.process("register", context);
                ctx.html(renderedHtml);
                return;
            }

            // Add user to database
            if (addUser(username, email, password)) {
                ctx.redirect("/register?msg=Registration successful! Please login.");
            } else {
                Context context = new Context();
                context.setVariable("error", "Username or email already exists!");
                context.setVariable("username", username != null ? username : "");
                context.setVariable("email", email != null ? email : "");
                
                String renderedHtml = templateEngine.process("register", context);
                ctx.html(renderedHtml);
            }
        });

        // ✅ Route 4: User Login page - FIXED
        app.get("/login", ctx -> {
            Context context = new Context();

            String msg = ctx.queryParam("msg");
            String error = ctx.queryParam("error");

            if (msg != null) context.setVariable("message", msg);
            if (error != null) context.setVariable("error", error);
            
            // Always set username variable to avoid null errors in templates
            context.setVariable("username", "");

            String renderedHtml = templateEngine.process("login", context);
            ctx.html(renderedHtml);
        });

        // ✅ Route 5: Process Login - FIXED
        app.post("/login", ctx -> {
            String username = ctx.formParam("username");
            String password = ctx.formParam("password");

            if (validateUser(username, password)) {
                // Store user session info (simplified)
                ctx.sessionAttribute("username", username);
                ctx.sessionAttribute("isLoggedIn", true);
                ctx.redirect("/dashboard");
            } else {
                // Pass the username back to preserve form data
                Context context = new Context();
                context.setVariable("error", "Invalid username or password!");
                context.setVariable("username", username != null ? username : "");
                
                String renderedHtml = templateEngine.process("login", context);
                ctx.html(renderedHtml);
            }
        });

        // ✅ Route 6: User Dashboard
        // ✅ Route 6: User Dashboard - MODIFIED
        app.get("/dashboard", ctx -> {
            if (ctx.sessionAttribute("isLoggedIn") == null) {
                ctx.redirect("/login");
                return;
            }

            Context context = new Context();
            String username = ctx.sessionAttribute("username");
            List<Task> userTasks = getUserTasks(getUserIdByUsername(username));

            // Calculate counts manually
            int pendingCount = 0;
            int completedCount = 0;
            int inProgressCount = 0;

            if (userTasks != null) {
                for (Task task : userTasks) {
                    if ("pending".equals(task.getStatus())) {
                        pendingCount++;
                    } else if ("completed".equals(task.getStatus())) {
                        completedCount++;
                    } else if ("in_progress".equals(task.getStatus())) {
                        inProgressCount++;
                    }
                }
            }

            context.setVariable("username", username);
            context.setVariable("tasks", userTasks);
            context.setVariable("pendingCount", pendingCount);
            context.setVariable("completedCount", completedCount);
            context.setVariable("inProgressCount", inProgressCount);
            context.setVariable("pageTitle", "Dashboard - " + username);

            String renderedHtml = templateEngine.process("dashboard", context);
            ctx.html(renderedHtml);
        });

        // ✅ Route 7: Show all tasks
        // ✅ Route 7: Show all tasks - MODIFIED
        app.get("/tasks", ctx -> {
            if (ctx.sessionAttribute("isLoggedIn") == null) {
                ctx.redirect("/login");
                return;
            }

            Context context = new Context();
            String username = ctx.sessionAttribute("username");
            List<Task> userTasks = getUserTasks(getUserIdByUsername(username));

            // Calculate task counts in Java
            int pendingCount = 0;
            int completedCount = 0;
            int inProgressCount = 0;
            int totalCount = 0;

            if (userTasks != null) {
                totalCount = userTasks.size();
                for (Task task : userTasks) {
                    if ("pending".equals(task.getStatus())) {
                        pendingCount++;
                    } else if ("completed".equals(task.getStatus())) {
                        completedCount++;
                    } else if ("in_progress".equals(task.getStatus())) {
                        inProgressCount++;
                    }
                }
            }

            context.setVariable("username", username);
            context.setVariable("tasks", userTasks);
            context.setVariable("pendingCount", pendingCount);
            context.setVariable("completedCount", completedCount);
            context.setVariable("inProgressCount", inProgressCount);
            context.setVariable("totalCount", totalCount);
            context.setVariable("pageTitle", "My Tasks");

            String renderedHtml = templateEngine.process("tasks", context);
            ctx.html(renderedHtml);
        });

        // ✅ Route 8: Create new task form - FIXED
        app.get("/tasks/new", ctx -> {
            if (ctx.sessionAttribute("isLoggedIn") == null) {
                ctx.redirect("/login");
                return;
            }

            Context context = new Context();
            context.setVariable("pageTitle", "Create New Task");
            
            // Always set form variables to avoid null errors in templates
            context.setVariable("title", "");
            context.setVariable("description", "");
            context.setVariable("priority", "");
            
            String renderedHtml = templateEngine.process("new-task", context);
            ctx.html(renderedHtml);
        });

        // ✅ Route 9: Process new task creation
        app.post("/tasks", ctx -> {
            if (ctx.sessionAttribute("isLoggedIn") == null) {
                ctx.redirect("/login");
                return;
            }

            String title = ctx.formParam("title");
            String description = ctx.formParam("description");
            String priority = ctx.formParam("priority");
            String username = ctx.sessionAttribute("username");

            if (title != null && !title.trim().isEmpty()) {
                addTask(getUserIdByUsername(username), title, description, priority);
                ctx.redirect("/tasks");
            } else {
                // If title is empty, show form again with error
                Context context = new Context();
                context.setVariable("error", "Task title is required!");
                context.setVariable("title", title != null ? title : "");
                context.setVariable("description", description != null ? description : "");
                context.setVariable("priority", priority != null ? priority : "");
                context.setVariable("pageTitle", "Create New Task");
                
                String renderedHtml = templateEngine.process("new-task", context);
                ctx.html(renderedHtml);
            }
        });

        // ✅ Route 10: Logout
        app.get("/logout", ctx -> {
            ctx.sessionAttribute("isLoggedIn", null);
            ctx.sessionAttribute("username", null);
            ctx.redirect("/?msg=Logged out successfully!");
        });
    }

    // Initialize database tables
    private static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            
            String createUserTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    email TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
            
            String createTasksTable = """
                CREATE TABLE IF NOT EXISTS tasks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT,
                    priority TEXT CHECK(priority IN ('low', 'medium', 'high')) DEFAULT 'medium',
                    status TEXT CHECK(status IN ('pending', 'in_progress', 'completed')) DEFAULT 'pending',
                    parent_task_id INTEGER,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id),
                    FOREIGN KEY (parent_task_id) REFERENCES tasks(id)
                )
                """;
            
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createUserTable);
                stmt.execute(createTasksTable);
            }
            
        } catch (SQLException e) {
            System.out.println("Database initialization failed: " + e.getMessage());
        }
    }

    // Add user to database
    private static boolean addUser(String username, String email, String password) {
        String hashedPassword = hashPassword(password); // Simple hash for now
        String sql = "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, hashedPassword);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Add user failed: " + e.getMessage());
            return false;
        }
    }

    // Validate user credentials
    private static boolean validateUser(String username, String password) {
        String sql = "SELECT password_hash FROM users WHERE username = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                return storedHash.equals(hashPassword(password));
            }
        } catch (SQLException e) {
            System.out.println("Validate user failed: " + e.getMessage());
        }
        return false;
    }

    // Get user ID by username
    private static int getUserIdByUsername(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println("Get user ID failed: " + e.getMessage());
        }
        return -1;
    }

    // Add task to database
    private static void addTask(int userId, String title, String description, String priority) {
        String sql = "INSERT INTO tasks (user_id, title, description, priority) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, title);
            pstmt.setString(3, description);
            pstmt.setString(4, priority != null ? priority : "medium");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Add task failed: " + e.getMessage());
        }
    }

    // Get user's tasks from database
    private static List<Task> getUserTasks(int userId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE user_id = ? ORDER BY created_at DESC LIMIT 10";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Task task = new Task();
                task.setId(rs.getInt("id"));
                task.setTitle(rs.getString("title"));
                task.setDescription(rs.getString("description"));
                task.setPriority(rs.getString("priority"));
                task.setStatus(rs.getString("status"));
                tasks.add(task);
            }
        } catch (SQLException e) {
            System.out.println("Get user tasks failed: " + e.getMessage());
        }
        return tasks;
    }

    // Simple password hashing (in real app, use proper encryption)
    private static String hashPassword(String password) {
        return password; // Simplified for learning
    }
}

// Task class for storing task data
class Task {
    private int id;
    private String title;
    private String description;
    private String priority;
    private String status;

    // Constructors
    public Task() {}

    public Task(String title, String description, String priority, String status) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}