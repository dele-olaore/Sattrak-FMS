/**
 *
 */
package com.dexter.common.util;

import javax.naming.Context;
import javax.naming.InitialContext;

import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import javax.activation.*;

/**
 * @author Olaore Oladele
 *
 */
public class Emailer
{	
    /**
     * Sends an email to the specified email address.
     *
     * @param from The from email address.
     * @param to The recipient of the email.
     * @param subject The subject of the email.
     * @param body The message content of the email.
     * */
    public static synchronized String sendEmail(String from, String[] to, String subject,
            String body)
    {
        String ret = null;
        
        Emailer e = new Emailer();
        
        try
        {
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            Session session = (Session) envCtx.lookup("mail/Session");
            
            String smtp_username = (String)envCtx.lookup("smtp.username");
            String smtp_password = (String)envCtx.lookup("smtp.password");
            
            Authenticator auth = e.new SMTPAuthenticator(smtp_username, smtp_password);
            session = Session.getDefaultInstance(session.getProperties(), auth);
            
            session.setDebug(false);
            
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            InternetAddress to_address[] = new InternetAddress[to.length];
            for(int i=0; i<to.length; i++)
            	to_address[i] = new InternetAddress(to[i]);
            message.setRecipients(Message.RecipientType.TO, to_address);
            message.setSubject(subject);
            message.setContent(body, "text/html");
            message.setSentDate(new java.util.Date());
            Transport.send(message);
            
            ret = "success";
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            
            String exStr = ex.getMessage();
            try
            {
                exStr = exStr.substring(exStr.lastIndexOf("]")+1);
            }
            catch(Exception ignore){}
            
            ret = "Error: Could not send the email, Message: " + exStr;
        }
        finally
        {
            //nothing to dispose
        }
        
        return ret;
    }
    
    /**
     * Sends an email to the specified email address with attachment.
     *
     * @param from The from email address.
     * @param to The recipient of the email.
     * @param subject The subject of the email.
     * @param body The message content of the email.
     * @param data The byte array of the file to be attached.
     * */
    public static synchronized String sendEmail(String from, String[] to, String subject,
            String body, byte[] data, String fname)
    {
        String ret = null;
        
        Emailer e = new Emailer();
        
        try
        {
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            Session session = (Session) envCtx.lookup("mail/Session");
            
            String smtp_username = (String)envCtx.lookup("smtp.username");
            String smtp_password = (String)envCtx.lookup("smtp.password");
            
            Authenticator auth = e.new SMTPAuthenticator(smtp_username, smtp_password);
            session = Session.getDefaultInstance(session.getProperties(), auth);
            
            session.setDebug(false);
            
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            InternetAddress to_address[] = new InternetAddress[to.length];
            for(int i=0; i<to.length; i++)
            	to_address[i] = new InternetAddress(to[i]);
            message.setRecipients(Message.RecipientType.TO, to_address);
            message.setSubject(subject);
            
            // create and fill the first message part
            MimeBodyPart mbp1 = new MimeBodyPart();
            mbp1.setContent(body, "text/html");

            // create the second message part
            MimeBodyPart mbp2 = new MimeBodyPart();

            // attach the file to the message
            DataSource source = new ByteArrayDataSource(data, "application/pdf");
            mbp2.setDataHandler(new DataHandler(source));
            mbp2.setFileName(fname);

            // create the Multipart and add its parts to it
            Multipart mp = new MimeMultipart();
            mp.addBodyPart(mbp1);
            mp.addBodyPart(mbp2);

            // add the Multipart to the message
            message.setContent(mp);
            
            message.setSentDate(new java.util.Date());
            
            Transport.send(message);
            
            ret = "success";
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            
            String exStr = ex.getMessage();
            try
            {
                exStr = exStr.substring(exStr.lastIndexOf("]")+1);
            }
            catch(Exception ignore){}
            
            ret = "Error: Could not send the email, Message: " + exStr;
        }
        finally
        {
            //nothing to dispose
        }
        
        return ret;
    }
    
    /**
     * Sends an email to the specified email address with attachment.
     *
     * @param from The from email address.
     * @param to The recipient of the email.
     * @param subject The subject of the email.
     * @param body The message content of the email.
     * @param filename The file to be attached.
     * */
    public static synchronized String sendEmail(String from, String[] to, String subject,
            String body, String filename)
    {
        String ret = null;
        
        Emailer e = new Emailer();
        
        try
        {
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            Session session = (Session) envCtx.lookup("mail/Session");
            
            String smtp_username = (String)envCtx.lookup("smtp.username");
            String smtp_password = (String)envCtx.lookup("smtp.password");
            
            Authenticator auth = e.new SMTPAuthenticator(smtp_username, smtp_password);
            session = Session.getDefaultInstance(session.getProperties(), auth);
            
            session.setDebug(false);
            
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            InternetAddress to_address[] = new InternetAddress[to.length];
            for(int i=0; i<to.length; i++)
            	to_address[i] = new InternetAddress(to[i]);
            message.setRecipients(Message.RecipientType.TO, to_address);
            message.setSubject(subject);
            
            // create and fill the first message part
            MimeBodyPart mbp1 = new MimeBodyPart();
            mbp1.setContent(body, "text/html");

            // create the second message part
            MimeBodyPart mbp2 = new MimeBodyPart();

            // attach the file to the message
            FileDataSource fds = new FileDataSource(filename);
            mbp2.setDataHandler(new DataHandler(fds));
            mbp2.setFileName(fds.getName());

            // create the Multipart and add its parts to it
            Multipart mp = new MimeMultipart();
            mp.addBodyPart(mbp1);
            mp.addBodyPart(mbp2);

            // add the Multipart to the message
            message.setContent(mp);
            
            message.setSentDate(new java.util.Date());
            
            Transport.send(message);
            
            ret = "success";
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            
            String exStr = ex.getMessage();
            try
            {
                exStr = exStr.substring(exStr.lastIndexOf("]")+1);
            }
            catch(Exception ignore){}
            
            ret = "Error: Could not send the email, Message: " + exStr;
        }
        finally
        {
            //nothing to dispose
        }
        
        return ret;
    }
    
    /**
     * SimpleAuthenticator is used to do simple authentication
     * when the SMTP server requires it.
     */
    private class SMTPAuthenticator extends Authenticator
    {
        private String SMTP_AUTH_USER;
        private String SMTP_AUTH_PWD;
        
        public SMTPAuthenticator(String SMTP_AUTH_USER, String SMTP_AUTH_PWD)
        {
            this.SMTP_AUTH_USER = SMTP_AUTH_USER;
            this.SMTP_AUTH_PWD = SMTP_AUTH_PWD;
        }
        
        public PasswordAuthentication getPasswordAuthentication()
        {
            String username = SMTP_AUTH_USER;
            String password = SMTP_AUTH_PWD;
            return new PasswordAuthentication(username, password);
        }
    }
}
