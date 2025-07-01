package pd.santos.asseteventmonitor.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pd.santos.asseteventmonitor.model.AssetEvent;
import pd.santos.asseteventmonitor.model.EventType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import pd.santos.asseteventmonitor.exception.CsvRowParseException;

/**
 * Service for parsing CSV files into AssetEvent objects.
 */
@Service
public class CsvParserService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;

    /**
     * Parse a CSV file into a list of AssetEvent objects.
     *
     * @param file The CSV file to parse
     * @return A list of AssetEvent objects
     * @throws IOException If there is an error reading the file
     * @throws CsvValidationException If there is an error parsing the CSV
     */
    public List<AssetEvent> parseAssetEvents(MultipartFile file) throws IOException, CsvValidationException {
        List<AssetEvent> events = new ArrayList<>();

        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVReader csvReader = new CSVReader(reader)) {

            // Read header row
            String[] header = csvReader.readNext();
            if (header == null) {
                throw new IOException("CSV file is empty");
            }

            // Create a mapping of column names to indices
            int tickerIndex = -1;
            int eventTypeIndex = -1;
            int amountIndex = -1;
            int dateIndex = -1;

            for (int i = 0; i < header.length; i++) {
                String columnName = header[i].trim().toLowerCase();
                switch (columnName) {
                    case "ticker":
                        tickerIndex = i;
                        break;
                    case "eventtype":
                    case "event_type":
                    case "event type":
                        eventTypeIndex = i;
                        break;
                    case "amount":
                        amountIndex = i;
                        break;
                    case "date":
                        dateIndex = i;
                        break;
                }
            }

            // Validate that all required columns are present
            if (tickerIndex == -1 || eventTypeIndex == -1 || amountIndex == -1 || dateIndex == -1) {
                throw new IOException("CSV file is missing required columns. Required: ticker, eventType, amount, date");
            }

            // Read data rows
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                if (line.length <= Math.max(Math.max(tickerIndex, eventTypeIndex), Math.max(amountIndex, dateIndex))) {
                    throw new CsvRowParseException("Error parsing row: insufficient columns: " + String.join(",", line), line, new IllegalArgumentException("Insufficient columns"));
                }

                try {
                    AssetEvent event = AssetEvent.builder()
                            .ticker(line[tickerIndex])
                            .eventType(EventType.valueOf(line[eventTypeIndex]))
                            .amount(new BigDecimal(line[amountIndex]))
                            .date(LocalDate.parse(line[dateIndex], DATE_FORMATTER))
                            .build();

                    events.add(event);
                } catch (Exception e) {
                    // Throw exception with details about the row that couldn't be parsed
                    throw new CsvRowParseException("Error parsing row: " + String.join(",", line), line, e);
                }
            }
        }

        return events;
    }
}
