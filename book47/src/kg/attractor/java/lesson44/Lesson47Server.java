package kg.attractor.java.lesson44;

import com.sun.net.httpserver.HttpExchange;
import kg.attractor.java.server.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Lesson47Server extends Lesson46Server {
    EmployeeDataModel employeeDataModel=new EmployeeDataModel();
    public Lesson47Server(String host, int port) throws IOException {
        super(host, port);
        registerGet("/query", this::handleQueryRequest);
        registerGet("/eInfo",this::handleEmployeeInfo);
    }

    private void handleEmployeeInfo(HttpExchange exchange) {
        EmployeeDataModel.Employee employee=new EmployeeDataModel.Employee(" "," "," "," "," "," ",null,null," ");
        String queryParams = getQueryParams(exchange);
        Map<String, String> params = Utils.parseUrlEncoded(queryParams, "&");
        String email = params.getOrDefault("email", "null");
        for (int i = 0; i <employeeDataModel.getEmployees().size(); i++) {
            if (employeeDataModel.getEmployees().get(i).getEmail().equals(email)) {
               employee=employeeDataModel.getEmployees().get(i);
                break;
            }
        }

        renderTemplate(exchange, "infoEmployee.html", employee);
    }

    private void handleQueryRequest(HttpExchange exchange) {
        // вытаскиваем параметры из запроса
        String queryParams = getQueryParams(exchange);
        // преобразуем в пары ключ-значение
        Map<String, String> params = Utils.parseUrlEncoded(queryParams, "&");
        // мы можем теперь пользоваться этими данными как
        // нам нравится, но мы просто
        // отображаем их в шаблоне
        Map<String, Object> data = new HashMap<>();
        data.put("params", params);
        renderTemplate(exchange, "query.html", data);
    }
}
