package pd.santos.asseteventmonitor.controller;

import com.opencsv.exceptions.CsvValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pd.santos.asseteventmonitor.model.AssetEvent;
import pd.santos.asseteventmonitor.model.EventType;
import pd.santos.asseteventmonitor.service.CsvParserService;
import pd.santos.asseteventmonitor.service.KafkaProducerService;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AssetEventControllerTest {

    @Mock
    private CsvParserService csvParserService;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private AssetEventController assetEventController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(assetEventController).build();
    }

    @Test
    void importEvents_shouldReturnSuccess_whenValidCsvFile() throws Exception {
        // Prepare test data
        String csvContent = "ticker,eventType,amount,date\nEQIX,PRICE_UPDATE,165.75,2025-06-01";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "events.csv",
                MediaType.TEXT_PLAIN_VALUE,
                csvContent.getBytes()
        );

        AssetEvent event = AssetEvent.builder()
                .eventId(UUID.randomUUID())
                .ticker("EQIX")
                .eventType(EventType.PRICE_UPDATE)
                .amount(new BigDecimal("165.75"))
                .date(LocalDate.of(2025, 6, 1))
                .build();

        // Mock service behavior
        when(csvParserService.parseAssetEvents(any())).thenReturn(List.of(event));
        doNothing().when(kafkaProducerService).publishEvents(any());

        // Perform request and verify
        mockMvc.perform(multipart("/api/asset-events/import")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully imported 1 events"));

        verify(csvParserService, times(1)).parseAssetEvents(any());
        verify(kafkaProducerService, times(1)).publishEvents(any());
    }

    @Test
    void importEvents_shouldReturnBadRequest_whenEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.csv",
                MediaType.TEXT_PLAIN_VALUE,
                new byte[0]
        );

        mockMvc.perform(multipart("/api/asset-events/import")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Please upload a non-empty file"));

        verify(csvParserService, never()).parseAssetEvents(any());
        verify(kafkaProducerService, never()).publishEvents(any());
    }

    @Test
    void importEvents_shouldReturnBadRequest_whenNonCsvFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "data.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "some content".getBytes()
        );

        mockMvc.perform(multipart("/api/asset-events/import")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Please upload a CSV file"));

        verify(csvParserService, never()).parseAssetEvents(any());
        verify(kafkaProducerService, never()).publishEvents(any());
    }

    @Test
    void importEvents_shouldReturnBadRequest_whenNoEventsFound() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "events.csv",
                MediaType.TEXT_PLAIN_VALUE,
                "ticker,eventType,amount,date".getBytes()
        );

        when(csvParserService.parseAssetEvents(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(multipart("/api/asset-events/import")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No valid events found in the CSV file"));

        verify(csvParserService, times(1)).parseAssetEvents(any());
        verify(kafkaProducerService, never()).publishEvents(any());
    }

    @Test
    void importEvents_shouldReturnBadRequest_whenIOException() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "events.csv",
                MediaType.TEXT_PLAIN_VALUE,
                "ticker,eventType,amount,date\nEQIX,PRICE_UPDATE,165.75,2025-06-01".getBytes()
        );

        when(csvParserService.parseAssetEvents(any())).thenThrow(new IOException("Test IO exception"));

        mockMvc.perform(multipart("/api/asset-events/import")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error reading CSV file: Test IO exception"));

        verify(csvParserService, times(1)).parseAssetEvents(any());
        verify(kafkaProducerService, never()).publishEvents(any());
    }

    @Test
    void importEvents_shouldReturnBadRequest_whenCsvValidationException() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "events.csv",
                MediaType.TEXT_PLAIN_VALUE,
                "ticker,eventType,amount,date\nEQIX,INVALID_TYPE,165.75,2025-06-01".getBytes()
        );

        when(csvParserService.parseAssetEvents(any())).thenThrow(new CsvValidationException("Test CSV validation exception"));

        mockMvc.perform(multipart("/api/asset-events/import")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error parsing CSV file: Test CSV validation exception"));

        verify(csvParserService, times(1)).parseAssetEvents(any());
        verify(kafkaProducerService, never()).publishEvents(any());
    }
}
