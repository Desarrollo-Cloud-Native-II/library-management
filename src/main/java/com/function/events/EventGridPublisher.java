package com.function.events;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.UUID;

/**
 * Clase utilitaria para publicar eventos a Azure Event Grid.
 * Maneja la conexión con Event Grid y la serialización de eventos.
 */
public class EventGridPublisher {

    private final EventGridPublisherClient<EventGridEvent> client;
    private final ObjectMapper objectMapper;
    private final String eventSource = "LibrarySystem";

    /**
     * Constructor que inicializa el cliente de Event Grid.
     * 
     * @param endpoint URL del endpoint de Event Grid
     * @param key      Clave de acceso para autenticación
     */
    public EventGridPublisher(String endpoint, String key) {
        this.client = new EventGridPublisherClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(key))
                .buildEventGridEventPublisherClient();

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Publica un evento de préstamo creado a Event Grid.
     * 
     * @param event  Evento de préstamo creado
     * @param logger Logger para registrar información
     */
    public void publishLoanCreatedEvent(LoanCreatedEvent event, java.util.logging.Logger logger) {
        try {
            String jsonData = objectMapper.writeValueAsString(event);

            EventGridEvent eventGridEvent = new EventGridEvent(
                    eventSource,
                    "Library.Loan.Created",
                    BinaryData.fromString(jsonData),
                    "1.0")
                    .setId(UUID.randomUUID().toString())
                    .setEventTime(OffsetDateTime.now());

            client.sendEvents(Collections.singletonList(eventGridEvent));

            logger.info("Evento LoanCreated publicado exitosamente: " + event.getLoanId());
            logger.info(
                    "Notificacion enviada para usuario: " + event.getUserName() + " (" + event.getUserEmail() + ")");
            logger.info("Libro: " + event.getBookTitle());
            logger.info("Fecha de devolucion: " + event.getExpectedReturnDate());

        } catch (Exception e) {
            logger.severe("Error al publicar evento a Event Grid: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Publica cualquier evento genérico a Event Grid.
     * 
     * @param eventType Tipo de evento (ej: "Library.Book.Deleted")
     * @param eventData Datos del evento
     * @param logger    Logger para registrar información
     */
    public void publishEvent(String eventType, Object eventData, java.util.logging.Logger logger) {
        try {
            String jsonData = objectMapper.writeValueAsString(eventData);

            EventGridEvent eventGridEvent = new EventGridEvent(
                    eventSource,
                    eventType,
                    BinaryData.fromString(jsonData),
                    "1.0")
                    .setId(UUID.randomUUID().toString())
                    .setEventTime(OffsetDateTime.now());

            client.sendEvents(Collections.singletonList(eventGridEvent));

            logger.info("Evento publicado exitosamente: " + eventType);

        } catch (Exception e) {
            logger.severe("Error al publicar evento a Event Grid: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
