package com.example.tradingapp.service;

import com.example.tradingapp.persistence.DataSnapshot;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

public class PersistenceService {
    private static final Path FILE = Path.of("data/snapshot.json");
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public void save(DataSnapshot snap) throws IOException {
        Files.createDirectories(FILE.getParent());
        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(FILE.toFile(), snap);
    }

    public DataSnapshot load() throws IOException {
        if (Files.notExists(FILE)) {
            return new DataSnapshot();
        }
        try (var r = Files.newBufferedReader(FILE)) {
            return mapper.readValue(r, DataSnapshot.class);
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            System.err.println("✘ Corrupted snapshot.json — starting fresh:\n" + ex);
            return new DataSnapshot();
        }
    }
}
