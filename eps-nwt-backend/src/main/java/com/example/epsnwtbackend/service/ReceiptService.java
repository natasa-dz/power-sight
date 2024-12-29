package com.example.epsnwtbackend.service;

import com.example.epsnwtbackend.dto.PaymentSlipDTO;
import com.example.epsnwtbackend.dto.ReceiptDTO;
import com.example.epsnwtbackend.model.CreatedReceipts;
import com.example.epsnwtbackend.model.Household;
import com.example.epsnwtbackend.model.PriceList;
import com.example.epsnwtbackend.model.Receipt;
import com.example.epsnwtbackend.repository.CitizenRepository;
import com.example.epsnwtbackend.repository.CreatedReceiptsRepository;
import com.example.epsnwtbackend.repository.ReceiptRepository;
import com.example.epsnwtbackend.utils.QRCodeGenerator;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import org.springframework.web.client.HttpClientErrorException;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Optional;

import javax.imageio.ImageIO;

@Service
public class ReceiptService {

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private CreatedReceiptsRepository createdReceiptsRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private HouseholdService householdService;

    @Autowired
    private PriceListService priceListService;

    @Autowired
    private InfluxService influxService;

    @Autowired
    private CitizenRepository citizenRepository;



    public ReceiptDTO getReceipt(Long receiptId) {
        return ReceiptDTO.toDTO(receiptRepository.getReferenceById(receiptId));
    }

    public List<ReceiptDTO> getAllReceiptsForOwner(Long ownerId) {
        List<Receipt> result = new ArrayList<>();
        List<ReceiptDTO> receipts = new ArrayList<>();
        result.addAll(receiptRepository.getAllByHousehold_Owner_Id(ownerId));
        result.addAll(receiptRepository.getAllByHousehold_AccessGranted_CitizenId(citizenRepository.findByUserId(ownerId).getId()));
        for(Receipt receipt : result){
            receipts.add(ReceiptDTO.toDTO(receipt));
        }
        return receipts;
    }

    public List<ReceiptDTO> getAllReceiptsForHousehold(Long householdId) {
        List<ReceiptDTO> receipts = new ArrayList<>();
        List<Receipt> result = receiptRepository.getAllByHousehold_Id(householdId);
        for(Receipt receipt : result){
            receipts.add(ReceiptDTO.toDTO(receipt));
        }
        return receipts;
    }

    public void createReceipts(String month, int year) throws Exception {
        boolean isReceiptCreated = checkIsReceiptCreated(month, year);
        if (isReceiptCreated) {
            throw new Exception("Receipt already exists");
        }
        int monthNum = 0;
        switch (month) {
            case "Jan":
                monthNum = 1;
                break;
            case "Feb":
                monthNum = 2;
                break;
            case "Mar":
                monthNum = 3;
                break;
            case "Apr":
                monthNum = 4;
                break;
            case "May":
                monthNum = 5;
                break;
            case "Jun":
                monthNum = 6;
                break;
            case "Jul":
                monthNum = 7;
                break;
            case "Aug":
                monthNum = 8;
                break;
            case "Sep":
                monthNum = 9;
                break;
            case "Oct":
                monthNum = 10;
                break;
            case "Nov":
                monthNum = 11;
                break;
            case "Dec":
                monthNum = 12;
                break;
            default:
                break;
        }
        PriceList current = priceListService.findForDate(new Date(year, monthNum, 1));

        List<Household> households = householdService.getAll();
        for(Household household : households) {
            Receipt receipt = new Receipt();
            receipt.setHousehold(household);
            receipt.setPaid(false);
            receipt.setPriceList(current);
            Double price = calculatePrice(current, household.getId(), monthNum, year, receipt);
            receipt.setPrice(price);
            receipt.setMonth(month);
            receipt.setYear(year);
            receipt.setPaymentDate(null);
            receiptRepository.save(receipt);

            //create pdf
            String paymentUrl = "http://localhost:4200/add-price-list";        //TODO: change
            byte[] pdf = generateReceiptPDF(receipt, month, year, paymentUrl);

            //save pdf
            String path = "src/main/resources/data/receipts/" + household.getId();
            Path folder = Paths.get(path);
            try {
                Files.createDirectories(folder);
                String fileName = year + "-" + month + ".pdf";
                Path filePath = folder.resolve(fileName);
                Files.write(filePath, pdf);
            } catch (IOException e) {
                throw new RuntimeException("File upload failed for household: " + household.getId(), e);
            }
            receipt.setPath(folder.toString());

            emailService.sendReceipt(receipt.getHousehold().getOwner().getUsername(), month, year, pdf);
        }

        //save that the receipts have been created
        CreatedReceipts createdReceipts = new CreatedReceipts();
        createdReceipts.setMonth(month);
        createdReceipts.setYear(year);
        createdReceiptsRepository.save(createdReceipts);
    }

    private boolean checkIsReceiptCreated(String month, int year) {
        Optional<CreatedReceipts> receipts = createdReceiptsRepository.findByYearAndMonth(year, month);
        return receipts.isPresent();
    }

    private Double calculatePrice(PriceList current, Long id, Integer monthNum, Integer year, Receipt receipt) {
        LocalDate localDateStart = LocalDate.of(year, monthNum, 1);
        LocalDateTime localDateTimeStart = localDateStart.atStartOfDay();
        LocalDateTime lastDayOfMonth = localDateStart.withDayOfMonth(localDateStart.lengthOfMonth()).atStartOfDay();

        Double totalConsumption = 0d;
        try {
            totalConsumption = influxService.getConsumptionByDateRange(id, localDateTimeStart, lastDayOfMonth);
            if(totalConsumption == null) {
                totalConsumption = 0d;
            }
        } catch (Exception e) {
            totalConsumption = 0d;
        }

        Double cenaKwhZeleneZone = current.getGreenZone();
        Double cenaKwhPlaveZone = current.getBlueZone();
        Double cenaKwhCrveneZone = current.getRedZone();
        Double basePrice = current.getBasePrice();
        Double pdvPercentage = current.getPdvPercentage();

        Double greenZoneConsumption = Math.min(totalConsumption, 350.0);
        Double blueZoneConsumption = Math.min(Math.max(totalConsumption - 350, 0), 1250.0);
        Double redZoneConsumption = Math.max(totalConsumption - 1600, 0);

        receipt.setGreenZoneConsumption(greenZoneConsumption);
        receipt.setBlueZoneConsumption(blueZoneConsumption);
        receipt.setRedZoneConsumption(redZoneConsumption);

        Double greenZoneCost = greenZoneConsumption * cenaKwhZeleneZone;
        Double blueZoneCost = blueZoneConsumption * cenaKwhPlaveZone;
        Double redZoneCost = redZoneConsumption * cenaKwhCrveneZone;

        Double calculationPowerCost = 7 * basePrice;

        Double withoutPdv = calculationPowerCost + greenZoneCost + blueZoneCost + redZoneCost;

        Double finalCost = withoutPdv + withoutPdv * pdvPercentage;
        return finalCost;
    }

    public static byte[] generateReceiptPDF(Receipt receipt, String month, int year, String paymentUrl) throws Exception {
        // Prepare PDF
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(byteArrayOutputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(20, 20, 20, 20);

        // Header Section
        Paragraph header = new Paragraph("Receipt")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(header);

        Paragraph subHeader = new Paragraph("For the month of " + month + " " + year)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(subHeader);

        document.add(new Paragraph(" ")); // Spacer

        // Household and Owner Information
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}));
        infoTable.setWidth(UnitValue.createPercentValue(100));
        infoTable.addCell(new Cell().add(new Paragraph("Household:").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
        infoTable.addCell(new Cell().add(new Paragraph(receipt.getHousehold().getRealEstate().getAddress() + ", " + receipt.getHousehold().getApartmentNumber())));
        infoTable.addCell(new Cell().add(new Paragraph("Owner:").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
        infoTable.addCell(new Cell().add(new Paragraph(receipt.getHousehold().getOwner().getUsername())));
        document.add(infoTable);

        document.add(new Paragraph(" ")); // Spacer

        // Consumption by Zone Section
        Double greenZoneConsumption = receipt.getGreenZoneConsumption();
        Double blueZoneConsumption = receipt.getBlueZoneConsumption();
        Double redZoneConsumption = receipt.getRedZoneConsumption();

        Double greenZonePrice = receipt.getPriceList().getGreenZone();
        Double blueZonePrice = receipt.getPriceList().getBlueZone();
        Double redZonePrice = receipt.getPriceList().getRedZone();

        Table zoneConsumptionTable = new Table(UnitValue.createPercentArray(new float[]{3, 2, 3}));
        zoneConsumptionTable.setWidth(UnitValue.createPercentValue(100));

        zoneConsumptionTable.addCell(new Cell().add(new Paragraph("Green Zone Consumption:").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
        zoneConsumptionTable.addCell(new Cell().add(new Paragraph(greenZoneConsumption + " kWh")));
        zoneConsumptionTable.addCell(new Cell().add(new Paragraph(greenZonePrice + " RSD/kWh")));

        zoneConsumptionTable.addCell(new Cell().add(new Paragraph("Blue Zone Consumption:").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
        zoneConsumptionTable.addCell(new Cell().add(new Paragraph(blueZoneConsumption + " kWh")));
        zoneConsumptionTable.addCell(new Cell().add(new Paragraph(blueZonePrice + " RSD/kWh")));

        zoneConsumptionTable.addCell(new Cell().add(new Paragraph("Red Zone Consumption:").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
        zoneConsumptionTable.addCell(new Cell().add(new Paragraph(redZoneConsumption + " kWh")));
        zoneConsumptionTable.addCell(new Cell().add(new Paragraph(redZonePrice + " RSD/kWh")));

        document.add(zoneConsumptionTable);

        document.add(new Paragraph(" ")); // Spacer

        // Amount Due Section
        BigDecimal price = BigDecimal.valueOf(receipt.getPrice());
        BigDecimal roundedValue = price.setScale(2, RoundingMode.HALF_UP);

        Table amountTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}));
        amountTable.setWidth(UnitValue.createPercentValue(100));
        amountTable.addCell(new Cell(1, 2).add(new Paragraph("Amount Due").setBold().setTextAlignment(TextAlignment.CENTER)).setBackgroundColor(ColorConstants.LIGHT_GRAY));
        amountTable.addCell(new Cell().add(new Paragraph("Amount: ").setBold()));
        amountTable.addCell(new Cell().add(new Paragraph(roundedValue.toString() + " RSD")));
        document.add(amountTable);

        document.add(new Paragraph(" ")); // Spacer

        // QR Code Section
        BufferedImage qrCodeImage = QRCodeGenerator.generateQRCodeImage(paymentUrl);
        ByteArrayOutputStream qrCodeOutputStream = new ByteArrayOutputStream();
        ImageIO.write(qrCodeImage, "PNG", qrCodeOutputStream);
        qrCodeOutputStream.flush();

        byte[] qrCodeBytes = qrCodeOutputStream.toByteArray();
        com.itextpdf.layout.element.Image qrImage = new com.itextpdf.layout.element.Image(ImageDataFactory.create(qrCodeBytes));
        qrImage.setHorizontalAlignment(HorizontalAlignment.CENTER);

        document.add(new Paragraph("Scan the QR code to pay.").setTextAlignment(TextAlignment.CENTER));
        document.add(qrImage);

        document.add(new Paragraph(" ")); // Spacer

        // Footer Section
        LineSeparator separator = new LineSeparator(new SolidLine());
        separator.setMarginTop(10);
        separator.setMarginBottom(10);
        document.add(separator);
        Paragraph footer = new Paragraph("Thank you for your payment.")
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(footer);

        // Finalize document
        document.close();

        return byteArrayOutputStream.toByteArray(); // Return PDF as byte array
    }

    public void payment(Long receiptId, PaymentSlipDTO paymentSlip) throws Exception {
        Receipt receipt = receiptRepository.getReferenceById(receiptId);
        if (receipt != null ){
            receipt.setPaid(true);
            receipt.setPaymentDate(new Date());
            receiptRepository.save(receipt);
            byte[] pdf = generatePaymentSlipPDF(paymentSlip);
            String path = "src/main/resources/data/paymentSlips";
            Path folder = Paths.get(path);
            try {
                Files.createDirectories(folder);
                String fileName = receipt.getMonth() + "_" + receipt.getYear() + ".pdf";
                Path filePath = folder.resolve(fileName);
                Files.write(filePath, pdf);
            } catch (IOException e) {
                throw new RuntimeException("File upload failed for receipt: " + receiptId, e);
            }
            emailService.sendPaymentSlip(receipt.getHousehold().getOwner().getUsername(), pdf, receipt);
        }
        else{
            throw new Exception("Receipt not found");
        }
    }

    public static byte[] generatePaymentSlipPDF(PaymentSlipDTO paymentSlip) throws Exception {
        // Prepare PDF
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(byteArrayOutputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(20, 20, 20, 20);

        // Header Section
        Paragraph header = new Paragraph("Payment Slip")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(header);

        document.add(new Paragraph(" ")); // Spacer

        // Customer and Recipient Information
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}));
        infoTable.setWidth(UnitValue.createPercentValue(100));

        infoTable.addCell(new Cell().add(new Paragraph("Customer:").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
        infoTable.addCell(new Cell().add(new Paragraph(paymentSlip.getCustomerName() + ", " + paymentSlip.getCustomerAddress())));

        infoTable.addCell(new Cell().add(new Paragraph("Recipient:").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
        infoTable.addCell(new Cell().add(new Paragraph(paymentSlip.getRecipientName())));

        infoTable.addCell(new Cell().add(new Paragraph("Purpose:").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
        infoTable.addCell(new Cell().add(new Paragraph(paymentSlip.getPurpose())));

        infoTable.addCell(new Cell().add(new Paragraph("Recipient Account:").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
        infoTable.addCell(new Cell().add(new Paragraph(paymentSlip.getRecipientAccount())));

        document.add(infoTable);

        document.add(new Paragraph(" ")); // Spacer

        // Payment Details
        Table paymentTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}));
        paymentTable.setWidth(UnitValue.createPercentValue(100));

        paymentTable.addCell(new Cell().add(new Paragraph("Model:").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
        paymentTable.addCell(new Cell().add(new Paragraph(String.valueOf(paymentSlip.getModel()))));

        paymentTable.addCell(new Cell().add(new Paragraph("Reference Number:").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
        paymentTable.addCell(new Cell().add(new Paragraph(paymentSlip.getReferenceNumber())));

        paymentTable.addCell(new Cell().add(new Paragraph("Amount:").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
        BigDecimal roundedAmount = BigDecimal.valueOf(paymentSlip.getAmount()).setScale(2, RoundingMode.HALF_UP);
        paymentTable.addCell(new Cell().add(new Paragraph(roundedAmount + " RSD")));

        paymentTable.addCell(new Cell().add(new Paragraph("Date:").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
        paymentTable.addCell(new Cell().add(new Paragraph(String.valueOf(new Date()))));

        document.add(paymentTable);

        document.add(new Paragraph(" ")); // Spacer

        // Footer Section
        Paragraph footer = new Paragraph("This document is automatically generated.")
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(footer);

        // Finalize document
        document.close();

        return byteArrayOutputStream.toByteArray(); // Return PDF as byte array
    }

}
