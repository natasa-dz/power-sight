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

    public void sendActivationEmail(String to, String activationLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Electro Power - Activate Your Account");

        String emailContent = """
            Dear User,
            
            Welcome to Electro Power! To complete your registration and access your energy management dashboard, please activate your account.
            
            Click the link below to activate your account:
            %s

            If you have any questions or need assistance, feel free to reach out to our support team.

            Best Regards,
            Electro Power Support Team
            """.formatted(activationLink);

        message.setText(emailContent);
        mailSender.send(message);
    }

    public void sendApprovalEmail(String emailTo) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailTo);
        message.setSubject("Electro Power - Ownership Request Approved");

        String emailContent = """
            Dear User,
            
            We are pleased to inform you that your ownership verification request for your household has been approved. 
            
            You can now manage your household's energy consumption through the Electro Power dashboard.

            Thank you for choosing Electro Power!

            Warm Regards,
            Electro Power Support Team
            """;

        message.setText(emailContent);
        mailSender.send(message);
    }

    public void sendRejectionEmail(String emailTo, String rejectionReason) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailTo);
        message.setSubject("Electro Power - Ownership Request Rejected");

        String emailContent = """
            Dear User,
            
            We regret to inform you that your ownership verification request for your household was not approved. 
            
            Reason for rejection: %s
            
            If you have any questions or would like to discuss this further, please contact our support team for assistance.

            Best Regards,
            Electro Power Support Team
            """.formatted(rejectionReason);

        message.setText(emailContent);
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
