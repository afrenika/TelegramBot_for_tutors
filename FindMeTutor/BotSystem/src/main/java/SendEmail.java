import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class SendEmail {

    private static final String username = "";
    private static final String password = "";


    public static String sendPassword(String email){
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); //TLS

        Session session = Session.getInstance(prop,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(email)
            );

            String newPassword = MailData.getPassword();


            message.setSubject("Смена пароля FindMeTutor"); // subject line
            String string = "<div><div style = 'font-size: 16px;line-height: 24px;margin-bottom: 20px;'><b>Здравствуйте!</b><br>Мы заметили, что вы запросили восстановление пароля для вашей учетной записи FindMeTutor. <br>Новый временный пароль указан ниже:<br> <div style = 'background-color: #e1f5fe;color: #ff0000; padding: 20px; margin-bottom: 20px;font-weight: bold;text-align: center;  margin: 0 auto; width: 80%;display: flex;'>"+newPassword+"</div><br> Если вы не запрашивали восстановление пароля, пожалуйста, обратитесь в нашу службу поддержки.<br> Это автоматическое сообщение. Пожалуйста, не отвечайте на него. <br><div style ='text-align: right;'> С уважением,<br> Команда поддержки<br></div></div>";
            message.setContent(string, "text/html; charset=utf-8");
            Transport.send(message);

            return newPassword;
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return "";
    }
}
