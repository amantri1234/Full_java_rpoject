package com.example;

import io.javalin.Javalin;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

public class Main {
    public static void main(String[] args) {
        // 🏗️ BACKEND: Set up Thymeleaf for dynamic templates
        TemplateEngine templateEngine = new TemplateEngine();
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        templateEngine.setTemplateResolver(resolver);
        
        Javalin app = Javalin.create();
        app.start(7071);  // Server runs on port 7071

        // ✅ Route 1: Home page
        app.get("/", ctx -> {
            Context context = new Context();  // Fixed: use one Context
            context.setVariable("name", "aman");
            context.setVariable("email", "amantrivedi@gmail.com");
            context.setVariable("location", "India");
            context.setVariable("age", 35);
            context.setVariable("hobby", "Software Engineer");
            context.setVariable("isStudent", false);
            
            String renderedHtml = templateEngine.process("index", context);
            ctx.html(renderedHtml);
        });

        // ✅ Route 2: Contact page
        app.get("/contactUs", ctx -> {
            Context context = new Context();  // Fixed: use one Context
            
            context.setVariable("title", "Contact Us");
            context.setVariable("email", "contact@techsolutions.com");
            context.setVariable("phone", "+1-555-123-4567");
            context.setVariable("address", "123 Tech Street, Silicon Valley");
            
            String renderedHtml = templateEngine.process("contact", context);
            ctx.html(renderedHtml);
        });

        // ✅ Route 3: About page
        app.get("/aboutUs", ctx -> {
            Context context = new Context();  // Fixed: use one Context
            
            context.setVariable("title", "About Us");
            context.setVariable("companyName", "Tech Solutions Inc.");
            context.setVariable("founded", 2020);
            context.setVariable("employees", 50);
            
            String renderedHtml = templateEngine.process("about", context);
            ctx.html(renderedHtml);
        });

        // ✅ Route 4: GET Form (show form)
        app.get("/Form", ctx -> {
            Context context = new Context();  // Fixed: use one Context
            
            // Get success message from URL parameter
            String msg = ctx.queryParam("msg");
            if (msg != null) {
                context.setVariable("message", msg);
            }
            
            // Get error message from URL parameter
            String error = ctx.queryParam("error");
            if (error != null) {
                context.setVariable("error", error);
            }
            
            String renderedHtml = templateEngine.process("form", context);  // Changed to lowercase "form"
            ctx.html(renderedHtml);     
        });

        // ✅ Route 5: POST Form (process form submission)
        app.post("/Form", ctx -> {
            String name = ctx.formParam("name");
            String email = ctx.formParam("email");
            String message = ctx.formParam("message");

            // Validate the data
            if (name == null || name.trim().isEmpty()) {
                // Redirect back with error message
                ctx.redirect("/Form?error=Name is required!");
                return;
            }
            
            // Process the data
            System.out.println("=== FORM SUBMISSION ===");
            System.out.println("Name: " + name);
            System.out.println("Email: " + email);
            System.out.println("Message: " + message);
            System.out.println("=====================");
            
            // Redirect back with success message
            ctx.redirect("/Form?msg=Message sent successfully!");
        });

        app.get("/registration" , ctx ->{
            Context context = new Context();  // Fixed: use one Context
            
            // Get success message from URL parameter
            String msg = ctx.queryParam("msg");
            if (msg != null) {
                context.setVariable("message", msg);
            }
            
            // Get error message from URL parameter
            String error = ctx.queryParam("error");
            if (error != null) {
                context.setVariable("error", error);
            }
            String renderedHtml = templateEngine.process("registration", context);
            ctx.html(renderedHtml);
        });
        app.post("/registration", ctx -> {
            String FirstName = ctx.formParam("firstName");
            String LastName = ctx.formParam("lastName");
            String email = ctx.formParam("email");
            String password = ctx.formParam("password");
            String ConformPassword = ctx.formParam("confirmPassword");
            int age = Integer.parseInt(ctx.formParam("age"));
            String Gender = ctx.formParam("gender");
            String Hobbies = ctx.formParam("hobbies");

            // Validate the data
            if (FirstName == null || FirstName.trim().isEmpty()) {
                // Redirect back with error message
                ctx.redirect("/registration?error=Username is required!");
                return;
            }
            
            // Process the data
            System.out.println("=== REGISTRATION SUBMISSION ===");
            System.out.println("Username: " + FirstName);
            System.out.println("Password: " + password);
            System.out.println("Email: " + email);
            System.out.println("===============================");
            
            // Redirect back with success message
            
            ctx.redirect("/registration?msg=Registration successful!");
        });
    }
}