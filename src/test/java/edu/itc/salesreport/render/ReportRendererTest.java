package edu.itc.salesreport.render;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ReportRendererTest {

    @ParameterizedTest
    @ValueSource(strings = {"txt", "html", "csv"})
    void rendersAllBranchesAndChainTotal(String ext) {
        var renderer = ReportRenderer.forExtension(ext);
        var report = TestReports.september();
        var content = new String(renderer.render(report), StandardCharsets.UTF_8);

        assertTrue(content.contains("PNH"), () -> ext + " missing PNH");
        assertTrue(content.contains("REP"), () -> ext + " missing REP");
        assertTrue(content.contains("BTB"), () -> ext + " missing BTB");
        assertTrue(content.contains("2300.50"), () -> ext + " missing chain total");
    }

    @Test
    void csvRendersExactLine() {
        var renderer = ReportRenderer.forExtension("csv");
        var report = TestReports.september();
        var content = new String(renderer.render(report), StandardCharsets.UTF_8);

        assertTrue(content.contains("2026-09,REP,800.50,0.00,50,16.01"));
    }

    @Test
    void rejectsUnsupportedExtension() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> ReportRenderer.forExtension("pdf"));
        assertTrue(ex.getMessage().contains(".pdf"));
    }

    @Test
    void exhaustiveSwitchOverPermittedRenderers() {
        ReportRenderer r = new TextReportRenderer();
        String result = switch (r) {
            case TextReportRenderer txt -> "text";
            case HtmlReportRenderer html -> "html";
            case CsvReportRenderer csv -> "csv";
        };
        assertEquals("text", result);
    }
}
