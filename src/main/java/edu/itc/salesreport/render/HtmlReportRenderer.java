package edu.itc.salesreport.render;

import edu.itc.salesreport.model.BranchSummary;
import edu.itc.salesreport.model.MonthlyReport;

import java.nio.charset.StandardCharsets;

/**
 * Renders an HTML table report with one row per branch and the chain summary.
 */
public final class HtmlReportRenderer implements ReportRenderer {

    @Override
    public String extension() {
        return "html";
    }

    @Override
    public byte[] render(MonthlyReport report) {
        var sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html>\n<head><meta charset=\"UTF-8\"><title>Sales Report ")
          .append(escape(report.month().toString()))
          .append("</title></head>\n<body>\n");
        sb.append("<h1>Angkor Mart monthly sales ")
          .append(escape(report.month().toString()))
          .append("</h1>\n");
        sb.append("<table>\n<thead>\n<tr>")
          .append("<th>Branch</th><th>Revenue</th><th>Receipts</th><th>Avg basket</th>")
          .append("</tr>\n</thead>\n<tbody>\n");
        for (BranchSummary b : report.branches()) {
            row(sb, b);
        }
        row(sb, report.chain());
        sb.append("</tbody>\n</table>\n</body>\n</html>\n");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void row(StringBuilder sb, BranchSummary b) {
        sb.append("<tr><td>").append(escape(b.branch()))
          .append("</td><td>").append(b.revenue())
          .append("</td><td>").append(b.receipts())
          .append("</td><td>").append(b.averageBasket())
          .append("</td></tr>\n");
    }

    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
