package com.workerconnect.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.workerconnect.model.Agreement;
import com.workerconnect.model.Booking;
import com.workerconnect.model.Payment;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGeneratorService {

    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, new BaseColor(37, 99, 235));
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, BaseColor.DARK_GRAY);
    private static final Font LABEL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.DARK_GRAY);
    private static final Font VALUE_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
    private static final Font SMALL_FONT = new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public byte[] generateInvoice(Booking booking, Payment payment) throws DocumentException {
        Document doc = new Document(PageSize.A4, 50, 50, 60, 60);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, baos);
        doc.open();

        // Header
        addHeader(doc, "PAYMENT INVOICE");

        // Invoice details table
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingBefore(10);
        addTableRow(infoTable, "Invoice No:", "INV-" + booking.getBookingNumber());
        addTableRow(infoTable, "Booking No:", booking.getBookingNumber());
        addTableRow(infoTable, "Payment Date:", payment.getPaymentDate() != null
                ? payment.getPaymentDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")) : LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));
        addTableRow(infoTable, "Payment Method:", payment.getPaymentMethod());
        addTableRow(infoTable, "Transaction ID:", payment.getTransactionId() != null ? payment.getTransactionId() : "N/A");
        doc.add(infoTable);

        doc.add(new Chunk(new LineSeparator()));
        doc.add(Chunk.NEWLINE);

        // Customer & Worker Info
        Paragraph section = new Paragraph("Customer & Service Details", HEADER_FONT);
        section.setSpacingBefore(10);
        doc.add(section);

        PdfPTable detailTable = new PdfPTable(2);
        detailTable.setWidthPercentage(100);
        detailTable.setSpacingBefore(8);
        addTableRow(detailTable, "Customer Name:", booking.getUser().getFullName());
        addTableRow(detailTable, "Customer Email:", booking.getUser().getEmail());
        addTableRow(detailTable, "Worker Name:", booking.getWorker().getFullName());
        addTableRow(detailTable, "Profession:", booking.getWorker().getProfession());
        addTableRow(detailTable, "Service Type:", booking.getServiceType());
        addTableRow(detailTable, "Work Description:", booking.getWorkDescription());
        addTableRow(detailTable, "Start Date:", booking.getStartDate() != null ? booking.getStartDate().format(DATE_FMT) : "N/A");
        addTableRow(detailTable, "Completion Date:", booking.getCompletionDate() != null ? booking.getCompletionDate().format(DATE_FMT) : "N/A");
        doc.add(detailTable);

        doc.add(Chunk.NEWLINE);
        doc.add(new Chunk(new LineSeparator()));

        // Amount
        Paragraph amountTitle = new Paragraph("Payment Summary", HEADER_FONT);
        amountTitle.setSpacingBefore(10);
        doc.add(amountTitle);

        PdfPTable amountTable = new PdfPTable(2);
        amountTable.setWidthPercentage(60);
        amountTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        amountTable.setSpacingBefore(8);

        PdfPCell labelCell = new PdfPCell(new Phrase("Total Amount", LABEL_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        PdfPCell valueCell = new PdfPCell(new Phrase("₹ " + payment.getAmount(), new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, new BaseColor(37, 99, 235))));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        amountTable.addCell(labelCell);
        amountTable.addCell(valueCell);

        PdfPCell statusLabel = new PdfPCell(new Phrase("Status", LABEL_FONT));
        statusLabel.setBorder(Rectangle.NO_BORDER);
        PdfPCell statusValue = new PdfPCell(new Phrase(payment.getStatus().name(), new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, new BaseColor(22, 163, 74))));
        statusValue.setBorder(Rectangle.NO_BORDER);
        statusValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        amountTable.addCell(statusLabel);
        amountTable.addCell(statusValue);
        doc.add(amountTable);

        // Footer
        doc.add(Chunk.NEWLINE);
        doc.add(new Chunk(new LineSeparator()));
        Paragraph footer = new Paragraph("Thank you for using WorkerConnect. For support contact: support@workerconnect.com", SMALL_FONT);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(10);
        doc.add(footer);

        doc.close();
        return baos.toByteArray();
    }

    public byte[] generateAgreement(Agreement agreement, Booking booking) throws DocumentException {
        Document doc = new Document(PageSize.A4, 50, 50, 60, 60);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, baos);
        doc.open();

        addHeader(doc, "SERVICE AGREEMENT");

        Paragraph intro = new Paragraph(
            "This Service Agreement is entered into between the Customer (User) and the Service Provider (Worker) "
            + "as described below, facilitated by WorkerConnect platform.", VALUE_FONT);
        intro.setSpacingBefore(15);
        intro.setSpacingAfter(10);
        doc.add(intro);

        doc.add(new Chunk(new LineSeparator()));

        // Party Info
        Paragraph partyTitle = new Paragraph("PARTIES INVOLVED", HEADER_FONT);
        partyTitle.setSpacingBefore(12);
        doc.add(partyTitle);

        PdfPTable partyTable = new PdfPTable(2);
        partyTable.setWidthPercentage(100);
        partyTable.setSpacingBefore(8);
        addTableRow(partyTable, "Customer Name:", agreement.getUserName());
        addTableRow(partyTable, "Customer Email:", agreement.getUserEmail());
        addTableRow(partyTable, "Customer Phone:", agreement.getUserPhone());
        addTableRow(partyTable, "Customer Address:", agreement.getUserAddress() != null ? agreement.getUserAddress() : "N/A");
        addTableRow(partyTable, "Worker Name:", agreement.getWorkerName());
        addTableRow(partyTable, "Worker Email:", agreement.getWorkerEmail());
        addTableRow(partyTable, "Worker Phone:", agreement.getWorkerPhone());
        addTableRow(partyTable, "Profession:", agreement.getWorkerProfession());
        doc.add(partyTable);

        doc.add(Chunk.NEWLINE);
        doc.add(new Chunk(new LineSeparator()));

        // Service Details
        Paragraph serviceTitle = new Paragraph("SERVICE DETAILS", HEADER_FONT);
        serviceTitle.setSpacingBefore(12);
        doc.add(serviceTitle);

        PdfPTable serviceTable = new PdfPTable(2);
        serviceTable.setWidthPercentage(100);
        serviceTable.setSpacingBefore(8);
        addTableRow(serviceTable, "Booking Number:", booking.getBookingNumber());
        addTableRow(serviceTable, "Service Description:", agreement.getServiceDescription());
        addTableRow(serviceTable, "Start Date:", agreement.getStartDate() != null ? agreement.getStartDate().format(DATE_FMT) : "N/A");
        addTableRow(serviceTable, "Completion Date:", agreement.getCompletionDate() != null ? agreement.getCompletionDate().format(DATE_FMT) : "N/A");
        addTableRow(serviceTable, "Agreed Amount:", "₹ " + agreement.getAmount());
        doc.add(serviceTable);

        doc.add(Chunk.NEWLINE);
        doc.add(new Chunk(new LineSeparator()));

        // Terms
        Paragraph termsTitle = new Paragraph("TERMS & CONDITIONS", HEADER_FONT);
        termsTitle.setSpacingBefore(12);
        doc.add(termsTitle);

        String terms = agreement.getTermsAndConditions() != null ? agreement.getTermsAndConditions() :
            "1. The Worker agrees to complete the specified service professionally.\n" +
            "2. Payment will be made upon successful completion as agreed.\n" +
            "3. Any changes to scope must be mutually agreed in writing.\n" +
            "4. Either party may cancel with 24 hours notice prior to start date.\n" +
            "5. Disputes will be resolved through WorkerConnect platform.\n" +
            "6. The Worker is responsible for bringing necessary tools/materials.\n" +
            "7. WorkerConnect is not liable for damages beyond platform policy.";
        Paragraph termsPara = new Paragraph(terms, VALUE_FONT);
        termsPara.setSpacingBefore(8);
        termsPara.setLeading(18);
        doc.add(termsPara);

        doc.add(Chunk.NEWLINE);
        doc.add(new Chunk(new LineSeparator()));

        // Signatures
        Paragraph sigTitle = new Paragraph("DIGITAL SIGNATURES", HEADER_FONT);
        sigTitle.setSpacingBefore(12);
        doc.add(sigTitle);

        PdfPTable sigTable = new PdfPTable(2);
        sigTable.setWidthPercentage(100);
        sigTable.setSpacingBefore(10);

        PdfPCell userSigCell = new PdfPCell();
        userSigCell.setPadding(10);
        userSigCell.addElement(new Phrase("Customer Signature", LABEL_FONT));
        userSigCell.addElement(new Phrase(agreement.isUserSigned()
            ? "✓ Signed by: " + agreement.getUserName() + "\n" + agreement.getUserSignedAt()
            : "Not yet signed", VALUE_FONT));
        sigTable.addCell(userSigCell);

        PdfPCell workerSigCell = new PdfPCell();
        workerSigCell.setPadding(10);
        workerSigCell.addElement(new Phrase("Worker Signature", LABEL_FONT));
        workerSigCell.addElement(new Phrase(agreement.isWorkerSigned()
            ? "✓ Signed by: " + agreement.getWorkerName() + "\n" + agreement.getWorkerSignedAt()
            : "Not yet signed", VALUE_FONT));
        sigTable.addCell(workerSigCell);
        doc.add(sigTable);

        // Footer
        Paragraph footer = new Paragraph(
            "Generated by WorkerConnect | Agreement ID: AGR-" + agreement.getId() + 
            " | " + (agreement.getCreatedAt() != null ? agreement.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) : ""),
            SMALL_FONT);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(20);
        doc.add(footer);

        doc.close();
        return baos.toByteArray();
    }

    private void addHeader(Document doc, String title) throws DocumentException {
        Paragraph header = new Paragraph("WorkerConnect", TITLE_FONT);
        header.setAlignment(Element.ALIGN_CENTER);
        doc.add(header);

        Paragraph titlePara = new Paragraph(title, new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.DARK_GRAY));
        titlePara.setAlignment(Element.ALIGN_CENTER);
        titlePara.setSpacingBefore(4);
        doc.add(titlePara);
    }

    private void addTableRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, LABEL_FONT));
        labelCell.setBorder(Rectangle.BOTTOM);
        labelCell.setBorderColor(new BaseColor(229, 231, 235));
        labelCell.setPadding(6);
        labelCell.setBackgroundColor(new BaseColor(249, 250, 251));

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "N/A", VALUE_FONT));
        valueCell.setBorder(Rectangle.BOTTOM);
        valueCell.setBorderColor(new BaseColor(229, 231, 235));
        valueCell.setPadding(6);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}
