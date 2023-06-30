package kg.attractor.java.lesson44;

import com.sun.net.httpserver.HttpExchange;
import kg.attractor.java.server.ContentType;
import kg.attractor.java.server.Cookie;
import kg.attractor.java.server.Utils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static kg.attractor.java.server.Utils.parseUrlEncoded;

public class Lesson46Server extends Lesson44Server {

    private final EmployeeDataModel employeeDataModel;
    EmployeeDataModel.Employee user = null;


    public Lesson46Server(String host, int port) throws IOException {
        super(host, port);
        employeeDataModel = new EmployeeDataModel();

        registerGet("/register", this::handleRegisterGet);
        registerPost("/register", this::handleRegisterPost);
        registerGet("/employee", this::freemarkerSampleHandler);
        registerGet("/cookie", this::cookieHandler);
        registerGet("/login", this::handleLoginGet);
        registerPost("/login", this::handleLoginPost);
        registerGet("/profile", this::handleProfileGet);
        registerGet("/logout", this::handleLogOut);
        registerGet("/book/get", this::handleGetBook);
        registerGet("/book/get/bookname", this::handleAddBook);
        registerGet("/book/return", this::handleReturn);
        registerGet("/book/return/bookname", this::handleReturnBook);
    }

    private void handleReturnBook(HttpExchange exchange) {
        String queryParams = getQueryParams(exchange);
        Map<String, String> params = Utils.parseUrlEncoded(queryParams, "&");
        String bookName = params.getOrDefault("bookName", "null");
        BookDataModel.Book book = null;
        for (int i = 0; i < bookData.getBooks().size(); i++) {
            if (bookData.getBooks().get(i).getName().equals(bookName)) {
                System.out.println("bookName found!");
                book = bookData.getBooks().get(i);
                break;
            }
        }
        employeeDataModel.addTakenBooks(user.getEmail(),book);
        employeeDataModel.removeEmployeeBooks(user.getEmail(),book);
        bookData.changeState(book);
        renderTemplate(exchange,"returned.html",book);

    }

    private void handleReturn(HttpExchange exchange) {
        renderTemplate(exchange, "returnBook.html", user);
    }

    private void freemarkerSampleHandler(HttpExchange exchange) {
        renderTemplate(exchange, "employee.html", getEmployeeDataModel());
    }

    private EmployeeDataModel getEmployeeDataModel() {
        return new EmployeeDataModel();
    }

    private void handleGetBook(HttpExchange exchange) {
        BookDataModel books = new BookDataModel();
        books.setUser(user);
        renderTemplate(exchange, "getBook.html", books);
    }


    private void handleAddBook(HttpExchange exchange) {
        String queryParams = getQueryParams(exchange);
        Map<String, String> params = Utils.parseUrlEncoded(queryParams, "&");
        String bookName = params.getOrDefault("bookName", "null");
        BookDataModel.Book book = null;
        for (int i = 0; i < bookData.getBooks().size(); i++) {
            if (bookData.getBooks().get(i).getName().equals(bookName)) {
                System.out.println("bookName found!");
                book = bookData.getBooks().get(i);
                break;
            }
        }
        int size = employeeDataModel.getEmployeeBookSize(user.getEmail());
        if (size < 2) {
            renderTemplate(exchange, "addBook.html", book);
            employeeDataModel.setEmployeeBooks(user.getEmail(), book);
            for (int i = 0; i < bookData.getBooks().size(); i++) {
                if (bookData.getBooks().get(i).getName().equals(bookName)) {
                    bookData.getBooks().get(i).setState("given");
                    bookData.getBooks().get(i).setBookTaker(user.getEmail());
                    bookData.saveEmployeesToFile();
                    break;
                }
            }
        } else {
            Path path = makeFilePath("bookLimit.html");
            sendFile(exchange, path, ContentType.TEXT_HTML);
        }
    }


    private void handleLogOut(HttpExchange exchange) {
        redirect303(exchange, "/");
        user.setCookieId("0");
        user = null;
    }


    public void handleLoginGet(HttpExchange exchange) {
        Path path = makeFilePath("login.html");
        sendFile(exchange, path, ContentType.TEXT_HTML);
    }

    public void handleLoginPost(HttpExchange exchange) {
        user = null;
        String raw = getBody(exchange);
        Map<String, String> parsed = parseUrlEncoded(raw, "&");

        for (EmployeeDataModel.Employee employee : employeeDataModel.getEmployees()) {
            if (employee.getEmail().equals(parsed.get("email")) && employee.getPassword().equals(parsed.get("user-password"))) {
                user = employee;
                break;
            }
        }


        if (user != null) {
            redirect303(exchange, "/profile");
            employeeDataModel.setEmployeeCookieId(parsed.get("email"));
            employeeDataModel.getEmployeeCookieId(parsed.get("email"));
            Cookie sessionCookieId = Cookie.make("cookieId", user.getCookieId());
            exchange.getResponseHeaders().add("Set-Cookie", sessionCookieId.toString());
            sessionCookieId.setMaxAge(3600);
        } else {
            Path path = makeFilePath("loginError.html");
            sendFile(exchange, path, ContentType.TEXT_HTML);
        }

    }


    public void handleProfileGet(HttpExchange exchange) {

        renderTemplate(exchange, "profile.html", user);
    }


    private void cookieHandler(HttpExchange exchange) {
        if (user != null) {
            Map<String, Object> data = new HashMap<>();
            String name = "times";

            String cookieString = getCookies(exchange);
            Map<String, String> cookies = Cookie.parse(cookieString);

            String cookieValue = cookies.getOrDefault(name, "0");

            int times = Integer.parseInt(cookieValue) + 1;

            Cookie response = new Cookie<>(name, times);
            setCookie(exchange, response);

            data.put(name, times);
            data.put("cookies", cookies);
            Cookie c1 = Cookie.make("userId", user.getCookieId());
            setCookie(exchange, c1);
            renderTemplate(exchange, "cookie.html", data);
        } else {
            Path path = makeFilePath("cookieNull.html");
            sendFile(exchange, path, ContentType.TEXT_HTML);

        }
    }


    private void handleRegisterGet(HttpExchange exchange) {
        renderTemplate(exchange, "register.html", employeeDataModel);
    }

    private void handleRegisterPost(HttpExchange exchange) {
        String raw = getBody(exchange);
        Map<String, String> parsed = parseUrlEncoded(raw, "&");
        System.out.println(parsed);

        for (EmployeeDataModel.Employee employee : employeeDataModel.getEmployees()) {
            if (employee.getEmail().equals(parsed.get("email"))) {
                Path path = makeFilePath("registrationError.html");
                sendFile(exchange, path, ContentType.TEXT_HTML);
                return;
            }
        }

        String email = parsed.get("email");
        String password = parsed.get("password");
        String name = parsed.get("name");
        String lastName = parsed.get("lname");
        if (hasNumber(name) == 1 || hasNumber(lastName) == 1) {
            Path path = makeFilePath("invalid.html");
            sendFile(exchange, path, ContentType.TEXT_HTML);
        } else {
            List<BookDataModel.Book> list = new ArrayList<>();

            EmployeeDataModel.Employee employee = new EmployeeDataModel.Employee(name, lastName, email, null, null, "user.png", list, null, password);
            employeeDataModel.addEmployee(employee);

            renderTemplate(exchange, "newUser.html", employee);
        }

    }

    public static int hasNumber(String input) {
        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                return 1;
            }
        }
        return 0;
    }


}
