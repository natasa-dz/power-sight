package com.example.epsnwtbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    
    public void sendActivationEmail(String to, String activationLink) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        String emailContent = """
        <html>
        <body>
            <h2>Welcome to Electro Power!</h2>
            <p>To complete your registration and access your energy management dashboard, please activate your account.</p>
            <a href="%s" style="display: inline-block; padding: 10px 20px; color: white; background-color: #007bff; text-decoration: none;">Activate Your Account</a>
            <p>If you have any questions or need assistance, feel free to reach out to our support team.</p>
            <p>Best Regards,<br>Electro Power Support Team</p>
        </body>
        </html>
        """.formatted(activationLink);

        helper.setTo(to);
        helper.setSubject("Electro Power - Activate Your Account");
        helper.setText(emailContent, true); // true for HTML
        mailSender.send(message);
    }

    public void sendApprovalEmail(String emailTo) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        String emailContent = """
        <html>
        <body>
            <h2>Ownership Request Approved</h2>
            <p>We are pleased to inform you that your ownership verification request for your household has been approved.</p>
            <p>You can now manage your household's energy consumption through the Electro Power dashboard.</p>
            <p>Thank you for choosing Electro Power!</p>
            <p>Warm Regards,<br>Electro Power Support Team</p>
        </body>
        </html>
        """;

        helper.setTo(emailTo);
        helper.setSubject("Electro Power - Ownership Request Approved");
        helper.setText(emailContent, true);
        mailSender.send(message);
    }

    public void sendRejectionEmail(String emailTo, String rejectionReason) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        String emailContent = """
        <html>
        <body>
            <h2>Ownership Request Rejected</h2>
            <p>We regret to inform you that your ownership verification request for your household was not approved.</p>
            <p><strong>Reason for rejection:</strong> %s</p>
            <p>If you have any questions or would like to discuss this further, please contact our support team for assistance.</p>
            <p>Best Regards,<br>Electro Power Support Team</p>
        </body>
        </html>
        """.formatted(rejectionReason);

        helper.setTo(emailTo);
        helper.setSubject("Electro Power - Ownership Request Rejected");
        helper.setText(emailContent, true);
        mailSender.send(message);
    }

    public void sendRegistrationRequestEmail(String emailTo, String note, Boolean approved) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(emailTo);

        String emailContentApproved = """
                <html>
                <body>
                    <p>Dear User,</p>
                    <br/>
                    <p>We are pleased to inform you that your request to register your real estate property in the Electro Distribution System has been <strong>approved</strong>.</p>
                    <p>Your property is now successfully registered in our system, allowing you to manage and monitor its energy distribution through the <em>Electro Power</em> dashboard.</p>
                    <br/>
                    <p>Thank you for trusting Electro Power to support your energy needs.</p>
                    <br/>
                    <p><strong>Warm Regards,</strong><br/><em>Electro Power Support Team</em></p>
                </body>
                </html>
                """;

        String emailContentDenied = """
                <html>
                <body>
                    <p>Dear User,</p>
                    <br/>
                    <p>We regret to inform you that your request to register your real estate property in the Electro Distribution System has been <strong>denied</strong>.</p>
                    <br/>
                    <p><strong>Reason for denial:</strong> <em>%s</em></p>
                    <br/>
                    <p>Please review the provided reason and ensure that all necessary information and requirements are fulfilled before resubmitting your request.</p>
                    <p>If you have any questions or need further assistance, do not hesitate to contact our support team.</p>
                    <br/>
                    <p><strong>Warm Regards,</strong><br/><em>Electro Power Support Team</em></p>
                </body>
                </html>
                """.formatted(note);

        if (approved) {
            helper.setSubject("Electro Power - Approval of Your Real Estate Registration Request");
            helper.setText(emailContentApproved, true);
        } else {
            helper.setSubject("Electro Power - Notification of Real Estate Registration Request Decision");
            helper.setText(emailContentDenied, true);
        }

        mailSender.send(message);
    }

}
