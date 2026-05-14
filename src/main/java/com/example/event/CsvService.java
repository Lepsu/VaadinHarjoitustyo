package com.example.event;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.*;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvService {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private static final String[] HEADERS = {
            "ID", "Nimi", "Kuvaus", "Alkamisaika",
            "Päättymisaika", "Maks. osallistujat", "Hinta"
    };

    // ---- Vienti ----
    public byte[] exportEventsToCsv(List<Event> events) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(
                     new OutputStreamWriter(out, "UTF-8"));
             CSVPrinter printer = new CSVPrinter(writer,
                     CSVFormat.DEFAULT.builder()
                             .setHeader(HEADERS)
                             .build())) {

            for (Event event : events) {
                printer.printRecord(
                        event.getId(),
                        event.getName(),
                        event.getDescription(),
                        event.getStartTime() != null
                                ? event.getStartTime().format(FORMATTER) : "",
                        event.getEndTime() != null
                                ? event.getEndTime().format(FORMATTER) : "",
                        event.getMaxAttendees(),
                        event.getPrice() != null
                                ? event.getPrice().toString() : "0"
                );
            }
            printer.flush();
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException(
                    "CSV-vienti epäonnistui: " + e.getMessage(), e);
        }
    }

    // ---- Tuonti ----
    public List<Event> importEventsFromCsv(
            InputStream inputStream) throws IOException {

        List<Event> events = new ArrayList<>();

        // BOM-merkki poistetaan lukemalla UTF-8 BOM-aware
        byte[] bytes = inputStream.readAllBytes();
        String content = new String(bytes, "UTF-8");

        // Poista BOM jos löytyy
        if (content.startsWith("\uFEFF")) {
            content = content.substring(1);
        }

        // Tunnista erotin automaattisesti (; tai ,)
        char delimiter = content.contains(";") ? ';' : ',';

        try (StringReader reader = new StringReader(content);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreHeaderCase(true)
                     .setTrim(true)
                     .setDelimiter(delimiter)
                     .build()
                     .parse(reader)) {

            for (CSVRecord record : parser) {
                try {
                    Event event = new Event();
                    event.setName(record.get("Nimi"));
                    event.setDescription(record.get("Kuvaus"));

                    String startStr = record.get("Alkamisaika");
                    if (!startStr.isBlank()) {
                        event.setStartTime(
                                LocalDateTime.parse(startStr, FORMATTER));
                    }

                    String endStr = record.get("Päättymisaika");
                    if (!endStr.isBlank()) {
                        event.setEndTime(
                                LocalDateTime.parse(endStr, FORMATTER));
                    }

                    String maxStr = record.get("Maks. osallistujat");
                    if (!maxStr.isBlank()) {
                        event.setMaxAttendees(
                                Integer.parseInt(maxStr));
                    }

                    String priceStr = record.get("Hinta");
                    if (!priceStr.isBlank()) {
                        event.setPrice(new BigDecimal(priceStr));
                    }

                    events.add(event);
                } catch (Exception e) {
                    System.err.println(
                            "Ohitettu rivi " + record.getRecordNumber()
                                    + ": " + e.getMessage());
                }
            }
        }
        return events;
    }
}