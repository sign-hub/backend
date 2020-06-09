package notifiers;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;
import org.apache.log4j.spi.LoggingEvent;

import play.Logger;
import play.Play;
import play.mvc.Mailer;
import utils.StringUtil;
import utils.cfg.CfgUtil;

public class Mails extends Mailer {

	static class MailUtil {
		public static void begin() {
			Map<String, Object> map = Mails.infos.get();
			List<String> recipientsList = (List<String>) map.get("recipients");
			if (recipientsList != null && !recipientsList.isEmpty()) {
				recipientsList.clear();
				map.put("recipients", recipientsList);
			}

			List<String> bccsList = (List<String>) map.get("bccs");
			if (bccsList != null && !bccsList.isEmpty()) {
				bccsList.clear();
				map.put("bccs", bccsList);
			}

			List<String> ccsList = (List<String>) map.get("ccs");
			if (ccsList != null && !ccsList.isEmpty()) {
				ccsList.clear();
				map.put("ccs", ccsList);
			}
			List<EmailAttachment> attachmentsList = (List<EmailAttachment>) map.get("attachments");
			if (attachmentsList != null && !attachmentsList.isEmpty()) {
				attachmentsList.clear();
				map.put("attachments", attachmentsList);
			}
			map.put("subject", "");
			map.put("from", "");
			Mails.infos.set(map);
		}

		public static void setFrom(String fullName, String email) {
			Mailer.setFrom(createRecipient(fullName, email));
		}

		public static void addTo(String fullName, String email) {
			Mailer.addRecipient(createRecipient(fullName, email));
		}

		public static String createRecipient(String fullName, String email) {
			return fullName + " <" + email + ">";
		}

		public static void end() {

		}
	}

	public static void sendVerificationCode(String fullName, String username, String verificationCode) {
		MailUtil.begin();
		MailUtil.setFrom(CfgUtil.getString("mail.smtp.fromName", "Sign-Hub"),
				CfgUtil.getString("mail.smtp.user", "cinibackend@gmail.com"));
		String name;
		if (StringUtil.isNil(fullName)) {
			name = username;
		} else {
			name = fullName;
		}
		MailUtil.addTo(name, username);
		setSubject("CINI - Conferma la tua mail");
		send(name, verificationCode);
	}
	
	/*public static boolean sendEmail(List<String> toList, List<String> ccList, String subject, String body, File attachment) {
		boolean emailSent = false;

		try {
			HtmlEmail email = new HtmlEmail();
			for(String to : toList)
				email.addTo(to);
			if(ccList!= null) {
				for(String to : ccList)
					email.addCc(to);
			}
			email.setFrom(Play.configuration.getProperty("mail.default.from", "admin@sign-hub.eu"));
			email.setSubject(subject);
			// embed the image and get the content id
			//URL url = new URL("http://www.zenexity.fr/wp-content/themes/images/logo.png");
			//String cid = email.embed(url, "Zenexity logo");
			// set the html message
			email.setHtmlMsg(body);
			// set the alternative message
			//email.setTextMsg("Your email client does not support HTML, too bad :(");
			if (attachment != null && attachment.exists()) {
				EmailAttachment att = new EmailAttachment();
				att.setDescription(attachment.getName());
				att.setPath(attachment.getPath());
				att.setName(attachment.getName());
				att.setDisposition(EmailAttachment.ATTACHMENT);
				email.attach(att);
			} 
			email.setHostName(Play.configuration.getProperty("mail.smtp.host", "smtp.gmail.com"));
			email.send();
		} catch (Exception ex) {
			Logger.error("Exception during sendMail to " + toList);
			ex.printStackTrace();
			emailSent = false;
		} finally {
			return emailSent;
		}
	}*/
	
	public static void sendEmail(List<String> toList, List<String> ccList, String subject, String body, File att) {
		MailUtil.begin();
        MailUtil.setFrom("Sign-Hub", Play.configuration.getProperty("mail.default.from", "cinibackend@gmail.com"));
        
        if (att != null && att.exists()) {
        	EmailAttachment attachment = new EmailAttachment();
            attachment.setDescription("Attachment");
            attachment.setPath(att.getAbsolutePath());
            if (attachment != null && attachment.getPath() != null && att.exists())
                Mailer.addAttachment(attachment);
        }
        
        setSubject(subject);
        for (String emailTo : toList) {
            MailUtil.addTo(emailTo, emailTo);
        }
        send(body);
  	}

	/**
     * Email per la comunicazione dei log
     * 
     * @param eventsList
     *            - lista degli errori
     * @param mode
     *            - dev o prod
     */
    public static void logEvent(ArrayList<LoggingEvent> eventsList, String mode) {
        if (eventsList != null && !eventsList.isEmpty()) {
            MailUtil.begin();
            MailUtil.setFrom("Sign-Hub", Play.configuration.getProperty("mail.default.from", "cinibackend@gmail.com"));
            EmailAttachment attachment = new EmailAttachment();
            attachment.setDescription("Last Error Log");
            String path = Play.configuration.getProperty("edicola.logs.folder",
                    "/var/log/edicola/application.log");
            File f = null;
            if (path.startsWith("/"))
                attachment.setPath(path);
            else
                attachment.setPath(Play.getFile(path).getPath());
            f = new File(attachment.getPath());
            if (attachment != null && attachment.getPath() != null && f.exists())
                Mailer.addAttachment(attachment);
            
            setSubject(Play.configuration.getProperty("envVar", "null??") + " CINI " + mode + " error " + new Date());
            
            String emails[] = Play.configuration
                    .getProperty("log.emailTo", "francesco.sessa@eclettica.net")
                    .split(";");
            for (String emailTo : emails) {
                MailUtil.addTo("admin", emailTo);
            }
            send(eventsList);
        }
    }

}