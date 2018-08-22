package edu.sjsu.cmpe275.project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
public class NotificationService {
	
	private JavaMailSender javaMailSender;
	
	@Autowired
	public NotificationService(JavaMailSender javaMailSender) throws MailException{
		this.javaMailSender = javaMailSender;
	}

	public void sendNotification(String emailAddress) {
		//send successful registration email
		
		SimpleMailMessage  mail = new SimpleMailMessage();
		mail.setTo(emailAddress);
		mail.setFrom("sjsucmpe275@gmail.com");
		mail.setSubject("Successful registration!");
		mail.setText("Welcome!");
		
		javaMailSender.send(mail);
	}
	
	public void sendVerification(String emailAddress, int code) {
		//send verification email
		
		SimpleMailMessage  mail = new SimpleMailMessage();
		mail.setTo(emailAddress);
		mail.setFrom("sjsucmpe275@gmail.com");
		mail.setSubject("Verification Code");
		mail.setText("Code:" + code);
		
		javaMailSender.send(mail);
	}
	
	public void sendInvitation(String emailAddress, String link) {
		//send invitation email
		
		SimpleMailMessage  mail = new SimpleMailMessage();
		mail.setTo(emailAddress);
		mail.setFrom("sjsucmpe275@gmail.com");
		mail.setSubject("Survey Invitation");
		//mail.setText("Survey Link: http://localhost:8080/survey" + link);
		mail.setText("Survey Link: http://localhost:12345/private/surveys/" + link);
		javaMailSender.send(mail);
	}
}
