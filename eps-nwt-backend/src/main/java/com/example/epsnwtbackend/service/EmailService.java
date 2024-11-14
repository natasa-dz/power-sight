package com.example.epsnwtbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

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

}
