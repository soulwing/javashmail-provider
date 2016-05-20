package org.soulwing.mail.transport;

import java.util.Properties;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

public class FileTransportDemo {

  public static void main(String[] args) throws Exception {
    Properties properties = new Properties();
    properties.setProperty("mail.transport.protocol", "file");
    properties.setProperty("mail.file.path", "/tmp/mail.txt");
    properties.setProperty("mail.file.append", "true");
    
    Session session = Session.getInstance(properties);
    
    MimeMessage message = new MimeMessage(session);
    message.setFrom(new InternetAddress("nobody@nowhere.net"));
    message.addRecipient(RecipientType.TO, 
        new InternetAddress("somebody@somewhere.net"));
    message.setSubject("Hello!");
    message.setText("A message from JavaShmail.");
    
    Transport transport = session.getTransport();
    transport.connect();
    transport.sendMessage(message, message.getAllRecipients());
  }  

}
