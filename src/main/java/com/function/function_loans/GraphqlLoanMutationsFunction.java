package com.function.function_loans;

import com.function.function_loans.graphql.JsonUtils;
import com.function.function_loans.graphql.LoanGraphqlProvider;
import com.function.events.EventGridPublisher;
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
 * Azure Function para ejecutar MUTACIONES GraphQL sobre préstamos (CREATE,
 * UPDATE, DELETE).
 * Proporciona un endpoint HTTP que acepta mutaciones GraphQL en formato POST.
 * 
 * Mutations soportadas:
 * - createLoan(input): Crea un nuevo préstamo
 * - updateLoan(id, input): Actualiza un préstamo existente
 * - returnBook(id): Registra la devolución de un libro
 * - deleteLoan(id): Elimina un préstamo
 * 
 * Endpoint: POST /api/graphql/loans/mutations
 */
public class GraphqlLoanMutationsFunction {
    private static final Pattern QUERY_PATTERN = Pattern.compile(
            "\"query\"\\s*:\\s*\"((?:\\\\.|[^\"])*+)\"",
            Pattern.DOTALL);

    // Configuración de Event Grid (idealmente desde variables de entorno)
    private static final String EVENT_GRID_ENDPOINT = "https://dcnii-eventgrid.eastus-1.eventgrid.azure.net/api/events";
    private static final String EVENT_GRID_KEY = "CcGZdBEIzkgkgTDCWPrYHt3jZ665H82Se8L6oeykof2PJiqsE41tJQQJ99CDACYeBjFXJ3w3AAABAZEG9KAn";

    /**
     * Función Azure que ejecuta mutaciones GraphQL sobre préstamos.
     * 
     * @param request solicitud HTTP con la mutación GraphQL
     * @param context contexto de ejecución de Azure Functions
     * @return respuesta HTTP con el resultado de la mutación GraphQL
     */
    @FunctionName("GraphqlLoanMutations")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS, route = "graphql/loans/mutations") HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Executing GraphQL Loan Mutations");

        // Crear EventGridPublisher
        EventGridPublisher eventGridPublisher = new EventGridPublisher(EVENT_GRID_ENDPOINT, EVENT_GRID_KEY);

        // Crear LoanGraphqlProvider con EventGridPublisher
        LoanGraphqlProvider graphQLProvider = new LoanGraphqlProvider(eventGridPublisher, context.getLogger());

        String query = getQuery(request);

        if (query == null || query.trim().isEmpty()) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", "You must send a GraphQL mutation in the 'query' parameter or in the body.");
            errorMap.put("ejemplo_createLoan",
                    "mutation { createLoan(input: { bookId: \"1\", userId: \"1\" }) { loan { id status } success message } }");
            errorMap.put("ejemplo_updateLoan",
                    "mutation { updateLoan(id: \"1\", input: { expectedReturnDate: \"2026-05-01\" }) { loan { id expectedReturnDate } success message } }");
            errorMap.put("ejemplo_returnBook",
                    "mutation { returnBook(id: \"1\") { loan { id status actualReturnDate } success message } }");
            errorMap.put("ejemplo_deleteLoan",
                    "mutation { deleteLoan(id: \"1\") { success message } }");
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
     * @return consulta o mutación GraphQL extraída
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
     * Extrae la consulta o mutación GraphQL del cuerpo JSON.
     * Soporta consultas directas y consultas envueltas en JSON.
     * 
     * @param body contenido del cuerpo HTTP
     * @return consulta o mutación GraphQL extraída
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
