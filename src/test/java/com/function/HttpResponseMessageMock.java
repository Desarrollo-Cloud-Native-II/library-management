package com.function;

import com.microsoft.azure.functions.*;

import java.util.Map;
import java.util.HashMap;

/**
 * Mock de HttpResponseMessage para pruebas unitarias.
 * Simula la respuesta HTTP de Azure Functions para verificar el comportamiento
 * de las funciones.
 */
public class HttpResponseMessageMock implements HttpResponseMessage {
    private int httpStatusCode;
    private HttpStatusType httpStatus;
    private Object body;
    private Map<String, String> headers;

    /**
     * Constructor del mock de respuesta HTTP.
     * 
     * @param status  estado HTTP de la respuesta
     * @param headers encabezados de la respuesta
     * @param body    cuerpo de la respuesta
     */
    public HttpResponseMessageMock(HttpStatusType status, Map<String, String> headers, Object body) {
        this.httpStatus = status;
        this.httpStatusCode = status.value();
        this.headers = headers;
        this.body = body;
    }

    @Override
    public HttpStatusType getStatus() {
        return this.httpStatus;
    }

    @Override
    public int getStatusCode() {
        return httpStatusCode;
    }

    @Override
    public String getHeader(String key) {
        return this.headers.get(key);
    }

    @Override
    public Object getBody() {
        return this.body;
    }

    /**
     * Builder mock para construir respuestas HTTP de prueba.
     */
    public static class HttpResponseMessageBuilderMock implements HttpResponseMessage.Builder {
        private Object body;
        private int httpStatusCode;
        private Map<String, String> headers = new HashMap<>();
        private HttpStatusType httpStatus;

        public Builder status(HttpStatus status) {
            this.httpStatusCode = status.value();
            this.httpStatus = status;
            return this;
        }

        @Override
        public Builder status(HttpStatusType httpStatusType) {
            this.httpStatusCode = httpStatusType.value();
            this.httpStatus = httpStatusType;
            return this;
        }

        @Override
        public HttpResponseMessage.Builder header(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        @Override
        public HttpResponseMessage.Builder body(Object body) {
            this.body = body;
            return this;
        }

        @Override
        public HttpResponseMessage build() {
            return new HttpResponseMessageMock(this.httpStatus, this.headers, this.body);
        }
    }
}
