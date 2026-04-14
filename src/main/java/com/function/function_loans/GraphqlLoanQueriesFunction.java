package com.function.function_loans;

import com.function.function_loans.graphql.JsonUtils;
import com.function.function_loans.graphql.LoanGraphqlProvider;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import graphql.ExecutionResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Azure Function para ejecutar CONSULTAS GraphQL sobre préstamos (solo READ).
 * Proporciona un endpoint HTTP que acepta consultas GraphQL en formato GET o
 * POST.
 * 
 * Queries soportadas:
 * - loans: Obtiene todos los préstamos
 * - loan(id): Obtiene un préstamo por ID
 * - activeLoans: Obtiene préstamos activos
 * - overdueLoans: Obtiene préstamos vencidos
 * - loansByUserId(userId): Obtiene préstamos de un usuario
 * - loansByBookId(bookId): Obtiene préstamos de un libro
 * 
 * Endpoint: GET/POST /api/graphql/loans/queries
 */
public class GraphqlLoanQueriesFunction {
    private static final Pattern QUERY_PATTERN = Pattern.compile(
            "\"query\"\\s*:\\s*\"((?:\\\\.|[^\"])*+)\"",
            Pattern.DOTALL);

    private final LoanGraphqlProvider graphQLProvider = new LoanGraphqlProvider();

    /**
     * Función Azure que ejecuta consultas (queries) GraphQL sobre préstamos.
     * 
     * @param request solicitud HTTP con la consulta GraphQL
     * @param context contexto de ejecución de Azure Functions
     * @return respuesta HTTP con el resultado de la consulta GraphQL
     */
    @FunctionName("GraphqlLoanQueries")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = { HttpMethod.GET,
                    HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS, route = "graphql/loans/queries") HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Executing GraphQL Loan Queries");

        String query = getQuery(request);

        if (query == null || query.trim().isEmpty()) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", "You must send a GraphQL query in the 'query' parameter or in the body.");
            errorMap.put("ejemplo_loans", "{ loans { id bookId userId loanDate expectedReturnDate status } }");
            errorMap.put("ejemplo_loan", "{ loan(id: \"1\") { id bookId userId status } }");
            errorMap.put("ejemplo_activeLoans", "{ activeLoans { id bookId userId loanDate } }");
            errorMap.put("ejemplo_overdueLoans", "{ overdueLoans { id bookId userId expectedReturnDate } }");
            errorMap.put("ejemplo_loansByUserId", "{ loansByUserId(userId: \"1\") { id bookId loanDate } }");
            errorMap.put("ejemplo_loansByBookId", "{ loansByBookId(bookId: \"1\") { id userId loanDate } }");
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body(JsonUtils.toJson(errorMap))
                    .build();
        }

        ExecutionResult result = graphQLProvider.execute(query);
        HttpStatus status = result.getErrors().isEmpty() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;

        return request.createResponseBuilder(status)
                .header("Content-Type", "application/json")
                .body(JsonUtils.toJson(result.toSpecification()))
                .build();
    }

    /**
     * Extrae la consulta GraphQL de los parámetros URL o del cuerpo de la petición.
     * 
     * @param request petición HTTP
     * @return consulta GraphQL extraída
     */
    private String getQuery(HttpRequestMessage<Optional<String>> request) {
        String queryParam = request.getQueryParameters().get("query");
        if (queryParam != null && !queryParam.trim().isEmpty()) {
            return queryParam.trim();
        }

        return request.getBody()
                .map(String::trim)
                .filter(body -> !body.isEmpty())
                .map(this::extractQueryFromBody)
                .orElse("");
    }

    /**
     * Extrae la consulta GraphQL del cuerpo JSON.
     * Soporta consultas directas y consultas envueltas en JSON.
     * 
     * @param body contenido del cuerpo HTTP
     * @return consulta GraphQL extraída
     */
    private String extractQueryFromBody(String body) {
        if (!body.startsWith("{")) {
            return body;
        }

        Matcher matcher = QUERY_PATTERN.matcher(body);
        if (!matcher.find()) {
            return "";
        }

        return unescapeJson(matcher.group(1)).trim();
    }

    /**
     * Remueve caracteres de escape JSON.
     * 
     * @param value texto escapado JSON
     * @return texto sin escapar
     */
    private String unescapeJson(String value) {
        StringBuilder result = new StringBuilder();
        int index = 0;
        while (index < value.length()) {
            char current = value.charAt(index);
            if (current == '\\' && index + 1 < value.length()) {
                char next = value.charAt(index + 1);
                switch (next) {
                    case 'n':
                        result.append('\n');
                        break;
                    case 'r':
                        result.append('\r');
                        break;
                    case 't':
                        result.append('\t');
                        break;
                    case 'b':
                        result.append('\b');
                        break;
                    case 'f':
                        result.append('\f');
                        break;
                    case '"':
                        result.append('"');
                        break;
                    case '\\':
                        result.append('\\');
                        break;
                    case '/':
                        result.append('/');
                        break;
                    default:
                        result.append(next);
                        break;
                }
                index += 2;
            } else {
                result.append(current);
                index++;
            }
        }
        return result.toString();
    }
}
