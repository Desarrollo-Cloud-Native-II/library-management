package com.function.function_loans.graphql;

import java.util.Iterator;
import java.util.Map;

/**
 * Utilidades para convertir objetos Java a formato JSON.
 * Proporciona una implementación ligera para serialización JSON sin
 * dependencias externas.
 * Soporta conversión de tipos primitivos, Maps, Iterables y valores null.
 */
public final class JsonUtils {

    /**
     * Constructor privado para prevenir instanciación.
     */
    private JsonUtils() {
    }

    /**
     * Convierte un objeto Java a su representación JSON como String.
     * 
     * @param value objeto a convertir (String, Number, Boolean, Map, Iterable, o
     *              null)
     * @return representación JSON del objeto
     */
    public static String toJson(Object value) {
        StringBuilder builder = new StringBuilder();
        appendJson(builder, value);
        return builder.toString();
    }

    private static void appendJson(StringBuilder builder, Object value) {
        if (value == null) {
            builder.append("null");
            return;
        }

        if (value instanceof String text) {
            builder.append('"').append(escape(text)).append('"');
            return;
        }

        if (value instanceof Number || value instanceof Boolean) {
            builder.append(value);
            return;
        }

        if (value instanceof Map<?, ?> map) {
            appendMapJson(builder, map);
            return;
        }

        if (value instanceof Iterable<?> iterable) {
            appendIterableJson(builder, iterable);
            return;
        }

        builder.append('"').append(escape(String.valueOf(value))).append('"');
    }

    private static void appendMapJson(StringBuilder builder, Map<?, ?> map) {
        builder.append('{');
        Iterator<? extends Map.Entry<?, ?>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<?, ?> entry = iterator.next();
            builder.append('"')
                    .append(escape(String.valueOf(entry.getKey())))
                    .append("\":");
            appendJson(builder, entry.getValue());
            if (iterator.hasNext()) {
                builder.append(',');
            }
        }
        builder.append('}');
    }

    private static void appendIterableJson(StringBuilder builder, Iterable<?> iterable) {
        builder.append('[');
        Iterator<?> iterator = iterable.iterator();
        while (iterator.hasNext()) {
            appendJson(builder, iterator.next());
            if (iterator.hasNext()) {
                builder.append(',');
            }
        }
        builder.append(']');
    }

    /**
     * Escapa caracteres especiales para formato JSON.
     * 
     * @param text texto a escapar
     * @return texto con caracteres especiales escapados
     */
    private static String escape(String text) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < text.length(); index++) {
            char current = text.charAt(index);
            switch (current) {
                case '"':
                    builder.append("\\\"");
                    break;
                case '\\':
                    builder.append("\\\\");
                    break;
                case '\b':
                    builder.append("\\b");
                    break;
                case '\f':
                    builder.append("\\f");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\t':
                    builder.append("\\t");
                    break;
                default:
                    if (current < 0x20) {
                        builder.append(String.format("\\u%04x", (int) current));
                    } else {
                        builder.append(current);
                    }
                    break;
            }
        }
        return builder.toString();
    }
}
