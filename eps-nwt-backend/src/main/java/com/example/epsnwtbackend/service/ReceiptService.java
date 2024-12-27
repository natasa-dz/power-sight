package com.example.epsnwtbackend.service;

import com.example.epsnwtbackend.model.Household;
import com.example.epsnwtbackend.model.PriceList;
import com.example.epsnwtbackend.model.Receipt;
import com.example.epsnwtbackend.repository.CreatedReceiptsRepository;
import com.example.epsnwtbackend.repository.ReceiptRepository;
import com.example.epsnwtbackend.utils.QRCodeGenerator;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class ReceiptService {

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private CreatedReceiptsRepository createdReceiptsRepository;

    @Autowired EmailService emailService;

    @Autowired private HouseholdService householdService;

    @Autowired private PriceListService priceListService;

    @Autowired private InfluxService influxService;

    public Receipt getReceipt(Long receiptId) {
        return receiptRepository.getReferenceById(receiptId);
    }

    public List<Receipt> getAllReceiptsForOwner(Long ownerId) {
        return receiptRepository.getAllByHousehold_Owner_Id(ownerId);
    }

    public void createReceipts(String month, int year) throws Exception {
        List<Household> households = householdService.getAll();
        for(Household household : households) {
            Receipt receipt = new Receipt();
            receipt.setHousehold(household);
            receipt.setPaid(false);
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
            receipt.setPriceList(current);
            Double price = calculatePrice(current, household.getId(), monthNum, year);
            receipt.setPrice(price);
            receiptRepository.save(receipt);

            //create pdf
            String paymentUrl = "http://localhost:4200//add-price-list";        //TODO: change
            byte[] pdf = generateReceiptPDF(receipt, month, year, paymentUrl);

            //save pdf
            String path = "resources/data/receipts/" + household.getId();
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
    }

    private Double calculatePrice(PriceList current, Long id, Integer monthNum, Integer year) {
        Date startDate = new Date(year, monthNum, 1);
        LocalDateTime localDateTimeStart = startDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        LocalDateTime lastDayOfMonth = localDateTimeStart.withDayOfMonth(localDateTimeStart.toLocalDate().lengthOfMonth());
        Double totalConsumption = influxService.getConsumptionByDateRange(id, localDateTimeStart, lastDayOfMonth);

        Double cenaKwhZeleneZone = current.getGreenZone();
        Double cenaKwhPlaveZone = current.getBlueZone();
        Double cenaKwhCrveneZone = current.getRedZone();
        Double basePrice = current.getBasePrice();
        Double pdvPercentage = current.getPdvPercentage();

        Double greenZoneConsumption = Math.min(totalConsumption, 350.0);
        Double blueZoneConsumption = Math.min(Math.max(totalConsumption - 350, 0), 1250.0);
        Double redZoneConsumption = Math.max(totalConsumption - 1600, 0);

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
        Document document = new Document(pdf);

        // Add Receipt Header
        document.add(new Paragraph("Receipt for " + month + " " + year));
        document.add(new Paragraph("Household: " + receipt.getHousehold().getRealEstate().getAddress() + ", " + receipt.getHousehold().getApartmentNumber()));
        document.add(new Paragraph("Owner: " + receipt.getHousehold().getOwner().getUsername()));
        document.add(new Paragraph("Amount Due: " + receipt.getPrice()));

        // Generate QR Code
        BufferedImage qrCodeImage = QRCodeGenerator.generateQRCodeImage(paymentUrl);
        com.itextpdf.layout.element.Image qrImage = new com.itextpdf.layout.element.Image(com.itextpdf.io.image.ImageDataFactory.create(qrCodeImage, null));
        document.add(qrImage);  // Add QR code to PDF

        // Add payment details
        document.add(new Paragraph("Scan the QR code to pay."));

        // Finalize document
        document.close();

        return byteArrayOutputStream.toByteArray(); // Return PDF as byte array
    }
}
