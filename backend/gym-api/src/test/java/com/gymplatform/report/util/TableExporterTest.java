package com.gymplatform.report.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class TableExporterTest {

    private final TableExporter exporter = new TableExporter();

    @Test
    void generatesNonEmptyExcelWorkbook() {
        byte[] bytes = exporter.toExcel("Sheet1", List.of("Name", "Amount"),
                List.of(List.of("Alice", 100), List.of("Bob", 50)));

        assertThat(bytes).isNotEmpty();
        // ZIP-based XLSX files start with the "PK" magic bytes.
        assertThat(bytes[0]).isEqualTo((byte) 'P');
        assertThat(bytes[1]).isEqualTo((byte) 'K');
    }

    @Test
    void generatesNonEmptyExcelWithNoRows() {
        byte[] bytes = exporter.toExcel("Sheet1", List.of("Name"), List.of());

        assertThat(bytes).isNotEmpty();
    }

    @Test
    void generatesNonEmptyPdfDocument() {
        byte[] bytes = exporter.toPdf("Report Title", List.of("Name", "Amount"),
                List.of(List.of("Alice", 100), List.of("Bob", 50)));

        assertThat(bytes).isNotEmpty();
        // PDF files start with "%PDF".
        assertThat(new String(bytes, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    void generatesNonEmptyPdfWithNoRows() {
        byte[] bytes = exporter.toPdf("Empty Report", List.of("Name"), List.of());

        assertThat(bytes).isNotEmpty();
    }
}
