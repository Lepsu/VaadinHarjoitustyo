package com.example.history;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HistoryEntry {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private final int revision;
    private final LocalDateTime timestamp;
    private final String operation;
    private final String eventName;
    private final String description;
    private final String modifiedBy;

    public HistoryEntry(int revision, LocalDateTime timestamp,
                        String operation, String eventName,
                        String description, String modifiedBy) {
        this.revision = revision;
        this.timestamp = timestamp;
        this.operation = operation;
        this.eventName = eventName;
        this.description = description;
        this.modifiedBy = modifiedBy;
    }

    public int getRevision() { return revision; }
    public String getTimestamp() {
        return timestamp.format(FORMATTER);
    }
    public String getOperation() { return operation; }
    public String getEventName() { return eventName; }
    public String getDescription() { return description; }
    public String getModifiedBy() { return modifiedBy; }
}