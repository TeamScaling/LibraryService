package com.scaling.libraryservice.dataPipe.libraryCatalog;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

public class CsvUtils {

    private static final int BUFFER_SIZE = 8192;

    public static CSVParser getCsvParser(Path filePath) throws IOException {
        Reader reader = Files.newBufferedReader(filePath);

        return CSVFormat.DEFAULT
            .withFirstRecordAsHeader()
            .parse(reader);
    }

    public static CSVParser getCsvParser(Path filePath,Reader reader) throws IOException {

        return CSVFormat.DEFAULT
            .withFirstRecordAsHeader()
            .parse(reader);
    }

    public static BufferedWriter getBufferedWriter(String outPutNm) throws IOException {
        return Files.newBufferedWriter(
            Paths.get(outPutNm),
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE
        );
    }

    public static String buildCsvLine(String... args) {
        return String.join(",", args);
    }
}
