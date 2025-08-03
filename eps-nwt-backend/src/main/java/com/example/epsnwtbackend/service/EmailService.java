package com.example.epsnwtbackend.service;

import com.example.epsnwtbackend.model.Receipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private String generateEmailBody(String title, String contentHtml) {
        return """
            <html>
            <head>
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background-color: #f0f4f8;
                        padding: 20px;
                        color: #37474f;
                    }
                    .email-container {
                        background-color: #ffffff;
                        border-radius: 12px;
                        padding: 30px;
                        max-width: 600px;
                        margin: auto;
                        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
                    }
                    h2 {
                        color: #0d47a1;
                    }
                    p {
                        margin: 16px 0;
                    }
                    .btn {
                        display: inline-block;
                        padding: 12px 24px;
                        background-color: #0288d1;
                        color: #ffffff;
                        text-decoration: none;
                        border-radius: 6px;
                        font-weight: bold;
                        margin-top: 20px;
                    }
                    .footer {
                        margin-top: 40px;
                        font-size: 0.9rem;
                        color: #90a4ae;
                    }
                </style>
            </head>
            <body>
                <div class="email-container">
                    <h2>%s</h2>
                    %s
                    <div class="footer">
                        Electro Power Support Team<br>
                        &copy; 2025 Electro Power
                    </div>
                </div>
            </body>
            </html>
        """.formatted(title, contentHtml);
    }

    public void sendActivationEmail(String to, String activationLink) throws MessagingException {
        String bodyContent = """
            <p>To complete your registration and access your energy management dashboard, please activate your account by clicking the button below.</p>
            <a href="%s" class="btn" style=" color: #ffffff; ">Activate Your Account</a>
            <p>If you have any questions or need assistance, feel free to reach out to our support team.</p>
        """.formatted(activationLink);

        sendHtmlEmail(to, "Electro Power - Activate Your Account", generateEmailBody("Welcome to Electro Power ⚡", bodyContent));
    }

    public void sendApprovalEmail(String emailTo) throws MessagingException {
        String bodyContent = """
            <p>We are pleased to inform you that your ownership verification request for your household has been approved.</p>
            <p>You can now manage your household's energy consumption through the Electro Power dashboard.</p>
            <p>Thank you for choosing Electro Power!</p>
        """;

        sendHtmlEmail(emailTo, "Electro Power - Ownership Request Approved", generateEmailBody("Ownership Request Approved ✅", bodyContent));
    }

    public void sendRejectionEmail(String emailTo, String rejectionReason) throws MessagingException {
        String bodyContent = """
            <p>We regret to inform you that your ownership verification request for your household was not approved.</p>
            <p><strong>Reason for rejection:</strong> %s</p>
            <p>If you have any questions or would like to discuss this further, please contact our support team for assistance.</p>
        """.formatted(rejectionReason);

        sendHtmlEmail(emailTo, "Electro Power - Ownership Request Rejected", generateEmailBody("Ownership Request Rejected ❌", bodyContent));
    }

    public void sendRegistrationRequestEmail(String emailTo, String note, Boolean approved) throws MessagingException {
        String title = approved ? "Registration Approved ✅" : "Registration Denied ❌";
        String subject = approved ? "Electro Power - Approval of Your Real Estate Registration Request"
                : "Electro Power - Notification of Real Estate Registration Request Decision";

        String bodyContent = approved
                ? """
                    <p>We are pleased to inform you that your request to register your real estate property in the Electro Distribution System has been <strong>approved</strong>.</p>
                    <p>Your property is now successfully registered in our system, allowing you to manage and monitor its energy distribution through the <em>Electro Power</em> dashboard.</p>
                    <p>Thank you for trusting Electro Power to support your energy needs.</p>
                  """
                : """
                    <p>We regret to inform you that your request to register your real estate property in the Electro Distribution System has been <strong>denied</strong>.</p>
                    <p><strong>Reason for denial:</strong> <em>%s</em></p>
                    <p>Please review the provided reason and ensure that all necessary information and requirements are fulfilled before resubmitting your request.</p>
                  """.formatted(note);

        sendHtmlEmail(emailTo, subject, generateEmailBody(title, bodyContent));
    }

    public void sendReceipt(String emailTo, String month, int year, byte[] pdfAttachment) throws MessagingException {
        String bodyContent = """
            <p>Thank you for your continued trust in Electro Power.</p>
            <p>Please find attached the receipt for your energy usage for the month of <strong>%s %s</strong>.</p>
            <ul>
                <li>Month: %s</li>
                <li>Year: %s</li>
            </ul>
            <p>If you have any questions or need further assistance, please don’t hesitate to reach out to our support team.</p>
        """.formatted(month, year, month, year);

        String subject = "Electro Power - Receipt for " + month + " " + year;

        sendHtmlEmailWithAttachment(emailTo, subject, generateEmailBody("Your Energy Receipt 📄", bodyContent),
                "Receipt_" + month + "_" + year + ".pdf", pdfAttachment);
    }

    public void sendPaymentSlip(String emailTo, byte[] pdf, Receipt receipt) throws MessagingException {
        String bodyContent = """
            <p>Thank you for your payment.</p>
            <p>Please find attached the payment slip for the paid receipt for the month of <strong>%s %s</strong>.</p>
            <p>If you have any questions or need further assistance, please don’t hesitate to reach out to our support team.</p>
        """.formatted(receipt.getMonth(), receipt.getYear());

        sendHtmlEmailWithAttachment(emailTo, "Electro Power - Payment Slip",
                generateEmailBody("Payment Confirmation ✅", bodyContent),
                "PaymentSlip_" + receipt.getMonth() + "_" + receipt.getYear() + ".pdf", pdf);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

    private void sendHtmlEmailWithAttachment(String to, String subject, String htmlContent, String attachmentName, byte[] attachment) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        helper.addAttachment(attachmentName, new ByteArrayResource(attachment));

        mailSender.send(message);
    }
}