/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package executables;

import cronMail.CreateMail;
import dataaccess.OracleConnector;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 *
 * @author pabodhar
 */
public class CEBMailer {

    static public String _strDate = "";
    String _strIpAddress = "";

    public void dayEndMail(String prm_strTxnDate) {
        try {
//            OracleConnector con = new OracleConnector("192.168.1.27", "RPD2", "bview", new jText.TextUti().getText("bview"));
            OracleConnector con = new OracleConnector("192.168.1.27", "RPD2", "TMARKSYS", "TMARKSYS");
            if (prm_strTxnDate.equals("")) {

                System.out.println("");
            }

            String m_strcomcod = "CEB";
            String m_strSBuCode = "830";
            Statement stmnt = con.getConn().createStatement();
            ResultSet rs = null;
            String m_strHeader = "";
            String m_strControlFileName = "";
            String m_strDataFileName = "";
            boolean m_bolDataFound = false;
            String m_strSql = "select 'ARPC'||'100'||max(to_char(to_date(trandt),'MM'))||max(to_char(to_date(trandt),'DD'))||'.SDF'||"
                    + "'ARPC'||'100'||max(to_char(to_date(trandt),'YYYY/MM/DD'))||max(to_char(to_date(trandt),'YYYY/MM/DD'))||"
                    + "lpad(count(*),5,'0')||lpad(replace(trim(TO_CHAR(sum(amount),9999999.99)),'.',''),12,'0') as detail,"
                    + "case when count(*)>0 then 'ARPC'||'100'||max(to_char(to_date('" + prm_strTxnDate + "'),'MM'))||max(to_char(to_date('" + prm_strTxnDate + "'),'DD'))||'.SDF'||'ARPC'||'100'||"
                    + "max(to_char(to_date('" + prm_strTxnDate + "'),'YYYY/MM/DD'))||max(to_char(to_date('" + prm_strTxnDate + "'),'YYYY/MM/DD'))||"
                    + "lpad(count(*),5,'0')||lpad(replace(trim(TO_CHAR(sum(amount),9999999.99)),'.',''),12,'0') else '0' end as data,"
                    + "'C'||'ARPC'||'100'||max(to_char(to_date(trandt),'MM'))||max(to_char(to_date(trandt),'DD'))||'.SDF' as file_ext_cont,"
                    + "'ARPC'||'100'||max(to_char(to_date(trandt),'MM'))||max(to_char(to_date(trandt),'DD'))||'.SDF' as file_ext_data,"
                    + " max(to_char(to_date(trandt),'MM'))||max(to_char(to_date(trandt),'DD')) filedate"
                    + " from RMIS_RMS_POS_BC_TXN_MAS where sbucod='" + m_strSBuCode + "' and comcod='" + m_strcomcod + "'"
                    + " and to_date(trandt)='" + prm_strTxnDate + "'"
                    + " and Transd='T' and uptdcd='1'";
            rs = stmnt.executeQuery(m_strSql);
            if (rs.next()) {
                if (!rs.getString("data").equals("0")) {
                    m_bolDataFound = true;
                    m_strHeader = rs.getString("data");
                    m_strControlFileName = rs.getString("file_ext_cont");
                    m_strDataFileName = rs.getString("file_ext_data");
                }
            } else {
                throw new Exception("CEB Data not found for control file.");
            }
            rs.close();
            stmnt.close();
            rs = null;
            stmnt = null;

            if (m_bolDataFound) {
                File infile = new File(m_strControlFileName);
                FileOutputStream fos = new FileOutputStream(infile);
                PrintWriter out = new PrintWriter(fos);

                File infiledata = new File(m_strDataFileName);
                FileOutputStream fosdata = new FileOutputStream(infiledata);
                PrintWriter outdata = new PrintWriter(fosdata);
                m_strSql = "";
                m_strSql = "select sbucod,loccod,'ARPC' as bank_code,'100' as branch_code,reftx1 as account_no,"
                        + "lpad(replace(trim(TO_CHAR(amount,9999999.99)),'.',''),12,'0') amount,to_char(txndat,'YYYY/MM/DD') payment_date,"
                        + "to_char(trandt,'YYYY-MM-DD') cash_date,'c' as paymode,"
                        + "'ARPC'||'100'||reftx1||lpad(replace(trim(TO_CHAR(amount,9999999.99)),'.',''),12,'0')"
                        + "||to_char(txndat,'YYYY/MM/DD')||to_char(to_date('" + prm_strTxnDate + "'),'YYYY/MM/DD')||'C' as data"
                        + " from RMIS_RMS_POS_BC_TXN_MAS where sbucod='" + m_strSBuCode + "' and comcod='" + m_strcomcod + "'"
                        + " and to_date(trandt)='" + prm_strTxnDate + "' and Transd='T' and uptdcd='1'";
                Statement stmntdata = con.getConn().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet rsdata = null;
                rsdata = stmntdata.executeQuery(m_strSql);
                if (!rsdata.next()) {
                    throw new Exception("CEB Data not found for data file.");
                } else {
                    rsdata.beforeFirst();
                    while (rsdata.next()) {
                        outdata.println(rsdata.getString("data"));
                    }
                    outdata.flush();
                    outdata.close();

                    out.println(m_strHeader);
                    out.flush();
                    out.close();
                    System.out.println("Writting control file Completed..");
                    System.out.println("Writting data file Completed..");

                }
                rsdata.close();
                stmntdata.close();
                rsdata = null;
                stmntdata = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void dayEndMail() {
        try {
            OracleConnector con = new OracleConnector("192.168.1.27", "RPD2", "bview", new jText.TextUti().getText("bview"));
//            OracleConnector con = new OracleConnector("192.168.1.27", "RPD2", "TMARKSYS", "TMARKSYS");

            String m_strcomcod = "CEB";
            String m_strSBuCode = "830";
            Statement stmnt = con.getConn().createStatement();
            ResultSet rs = null;
            String m_strHeader = "";
            String m_strControlFileName = "";
            String m_strDataFileName = "";
            String m_fileAttachment1 = "";
            String m_fileAttachment2 = "";
            boolean m_bolDataFound = false;
            String m_strSql = "select 'ARPC'||'100'||max(to_char(to_date(trandt),'MM'))||max(to_char(to_date(trandt),'DD'))||'.SDF'||"
                    + "'ARPC'||'100'||max(to_char(to_date(trandt),'YYYY/MM/DD'))||max(to_char(to_date(trandt),'YYYY/MM/DD'))||"
                    + "lpad(count(*),5,'0')||lpad(replace(trim(TO_CHAR(sum(amount),9999999.99)),'.',''),12,'0') as detail,"
                    + "case when count(*)>0 then 'ARPC'||'100'||max(to_char(to_date(sysdate),'MM'))||max(to_char(to_date(sysdate),'DD'))||'.SDF'||'ARPC'||'100'||"
                    + "max(to_char(to_date(sysdate),'YYYY/MM/DD'))||max(to_char(to_date(sysdate),'YYYY/MM/DD'))||"
                    + "lpad(count(*),5,'0')||lpad(replace(trim(TO_CHAR(sum(amount),9999999.99)),'.',''),12,'0') else '0' end as data,"
                    + "'C'||'ARPC'||'100'||max(to_char(to_date(trandt),'MM'))||max(to_char(to_date(trandt),'DD'))||'.SDF' as file_ext_cont,"
                    + "'ARPC'||'100'||max(to_char(to_date(trandt),'MM'))||max(to_char(to_date(trandt),'DD'))||'.SDF' as file_ext_data,"
                    + " max(to_char(to_date(trandt),'MM'))||max(to_char(to_date(trandt),'DD')) filedate"
                    + " from RMIS_RMS_POS_BC_TXN_MAS where sbucod='" + m_strSBuCode + "' and comcod='" + m_strcomcod + "'"
                    + " and to_date(trandt)=to_char(sysdate,'DD-Mon-YYYY')"
                    + " and Transd='T' and uptdcd='1'";
            rs = stmnt.executeQuery(m_strSql);
            if (rs.next()) {
                if (!rs.getString("data").equals("0")) {
                    m_bolDataFound = true;
                    m_strHeader = rs.getString("data");
                    m_strControlFileName = rs.getString("file_ext_cont");
                    m_strDataFileName = rs.getString("file_ext_data");
                    setStrDate(rs.getString("filedate"));
                }
            } else {
                throw new Exception("CEB Data not found for control file.");
            }
            if (rs != null) {
                rs.close();
                rs = null;
            }
            if (stmnt != null) {
                stmnt.close();
                stmnt = null;
            }

            if (m_bolDataFound) {
                m_strSql = "";
                m_strSql = "select sbucod,loccod,'ARPC' as bank_code,'100' as branch_code,reftx1 as account_no,"
                        + "lpad(replace(trim(TO_CHAR(amount,9999999.99)),'.',''),12,'0') amount,to_char(txndat,'YYYY/MM/DD') payment_date,"
                        + "to_char(trandt,'YYYY-MM-DD') cash_date,'c' as paymode,"
                        + "'ARPC'||'100'||reftx1||lpad(replace(trim(TO_CHAR(amount,9999999.99)),'.',''),12,'0')"
                        + "||to_char(txndat,'YYYY/MM/DD')||to_char(to_date(sysdate),'YYYY/MM/DD')||'C' as data"
                        + " from RMIS_RMS_POS_BC_TXN_MAS where sbucod='" + m_strSBuCode + "' and comcod='" + m_strcomcod + "'"
                        + " and to_date(trandt)=to_char(sysdate,'DD-Mon-YYYY') and Transd='T' and uptdcd='1'";
                Statement stmntdata = con.getConn().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet rsdata = null;
                rsdata = stmntdata.executeQuery(m_strSql);
                boolean m_boldetailFound = false;
                if (rsdata.next()) {
                    m_boldetailFound = true;
                    FileWriter aWriterdetail;
                    aWriterdetail = new FileWriter(".//" + m_strDataFileName);
                    rsdata.beforeFirst();
                    while (rsdata.next()) {
                        aWriterdetail.write(rsdata.getString("data") + System.getProperty("line.separator"));
                    }
                    aWriterdetail.flush();
                    aWriterdetail.close();
                    System.out.println("Writting data file Completed..");
                    m_fileAttachment1 = ".//" + m_strDataFileName;

                    if (m_boldetailFound) {
                        FileWriter aWriterHeader;
                        aWriterHeader = new FileWriter(".//" + m_strControlFileName);
                        aWriterHeader.write(m_strHeader);
                        aWriterHeader.flush();
                        aWriterHeader.close();
                        System.out.println("Writting control file Completed..");
                        m_fileAttachment2 = ".//" + m_strControlFileName;
                    }
                    ResultSet _rsetautomail = null;
                    Statement stmntmail = con.getConn().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    String m_strQry = "select a.*"
                            + " from smautomail a where a.sbucod='830' "
                            + " and a.loccod='100' and a.progid='CEBMailer'";
                    _rsetautomail = stmntmail.executeQuery(m_strQry);
                    if (_rsetautomail.next()) {
                        String host = _rsetautomail.getString("mailhs") == null ? "" : _rsetautomail.getString("mailhs");
                        String from = _rsetautomail.getString("frmadd") == null ? "" : _rsetautomail.getString("frmadd");
                        String tempto = _rsetautomail.getString("toaddr") == null ? "" : _rsetautomail.getString("toaddr");
                        String tempCC = _rsetautomail.getString("carcpy") == null ? "" : _rsetautomail.getString("carcpy");
                        String tempBCC = _rsetautomail.getString("bcccpy") == null ? "" : _rsetautomail.getString("bcccpy");
                        String m_strSubject = _rsetautomail.getString("subjet") == null ? "" : _rsetautomail.getString("subjet");
                        String m_strBody = _rsetautomail.getClob("mssage").getSubString(1, (int) _rsetautomail.getClob("mssage").length()) == null ? "" : _rsetautomail.getClob("mssage").getSubString(1, (int) _rsetautomail.getClob("mssage").length());
                        String temp_Attachments = _rsetautomail.getString("attach") == null ? "" : _rsetautomail.getString("attach");
                        String m_strProgrammID = _rsetautomail.getString("progid") == null ? "" : _rsetautomail.getString("progid");
                        String[] to = tempto.split(";");
                        String[] cc = tempCC.split(";");
                        String[] bcc = tempBCC.split(";");

                        Properties props = System.getProperties();
                        props.put("mail.smtp.host", host);
                        System.out.println("Mail Host : " + host);

                        Session session = Session.getInstance(props, null);

                        MimeMessage message = new MimeMessage(session);
                        message.setFrom(new InternetAddress(from));
                        System.out.println("Masge From : " + from);

                        for (int i = 0; i < to.length; i++) {
                            if (!to[i].trim().equalsIgnoreCase("")) {
                                message.addRecipient(Message.RecipientType.TO, new InternetAddress(to[i]));
                                System.out.println("Masge sent to : " + to[i]);
                            }
                        }

                        for (int i = 0; i < cc.length; i++) {
                            if (!cc[i].trim().equalsIgnoreCase("")) {
                                message.addRecipient(Message.RecipientType.CC, new InternetAddress(cc[i]));
                                System.out.println("Masge sent to CC : " + cc[i]);
                            }
                        }

                        for (int i = 0; i < bcc.length; i++) {
                            if (!bcc[i].trim().equalsIgnoreCase("")) {
                                message.addRecipient(Message.RecipientType.BCC, new InternetAddress(bcc[i]));
                                System.out.println("Masge sent to BCC : " + bcc[i]);
                            }
                        }

                        message.setSubject(m_strSubject);
                        MimeBodyPart messageBodyPart1 = null;
                        MimeBodyPart messageBodyPart2 = null;
                        MimeBodyPart messageBodyPart3 = null;
                        if (!m_fileAttachment1.trim().equals("")) {
                            messageBodyPart1 = new MimeBodyPart();
                            messageBodyPart1.attachFile(m_fileAttachment1);
                        }
                        if (!m_fileAttachment2.trim().equals("")) {
                            messageBodyPart2 = new MimeBodyPart();
                            messageBodyPart2.attachFile(m_fileAttachment2);
                        }
                        Multipart multipart = new MimeMultipart();
                        MimeBodyPart textPart = new MimeBodyPart();
                        textPart.setContent(m_strBody, "text/html");
                        multipart.addBodyPart(textPart);
                        if (!m_fileAttachment1.trim().equals("")) {
                            multipart.addBodyPart(messageBodyPart1);
                        }
                        if (!m_fileAttachment2.trim().equals("")) {
                            multipart.addBodyPart(messageBodyPart2);
                        }
                        message.setContent(multipart);
                        File file1 = new File(m_fileAttachment1);
                        File file2 = new File(m_fileAttachment2);
                        if (file1.exists() && file2.exists()) {
                            double bytes = file1.length();
                            double bytes1 = file2.length();
                            if (bytes > 0 && bytes1 > 0) {
                                Transport.send(message);
                            }else{
                                throw new Exception("File Sizes are zero...");
                            }

                        }

                    }
                } else {
                    throw new Exception("CEB Data not found for detail file.");
                }
                if (rsdata != null) {
                    rsdata.close();
                    rsdata = null;
                }
                if (stmntdata != null) {
                    stmntdata.close();
                    stmntdata = null;
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @return the _strDate
     */
    public String getStrDate() {
        return _strDate;
    }

    /**
     * @param _strDate the _strDate to set
     */
    public void setStrDate(String _strDate) {
        this._strDate = _strDate;
    }

    public boolean CreateMail(String strtoAddress, String prm_strFromAddress, String prm_strCCAddress,
            String prm_strHeader, String strFilename1, String strFilename2, String strFilename3,
            String prm_strboodyText1, String prm_strboodyText2, String prm_strbodytext3,
            String prm_strbodytext4) throws Exception {
        boolean result = false;
        try {
            GetIP();
            String from = prm_strFromAddress;
            String[] to = strtoAddress.split(",");
            String[] cc = prm_strCCAddress.split(",");
            String fileAttachment1 = strFilename1;
            String fileAttachment2 = strFilename2;
            String fileAttachment3 = strFilename3;
            String bodytext1 = prm_strboodyText1;
            String bodytext2 = prm_strboodyText2;
            String bodytext3 = prm_strbodytext3;//"This is System Generated email.For any queries please contact the sender of this mail.<br><br>Regards,<br><br>";
            String bodytext4 = prm_strbodytext4;//_audit.getStrUserName();
            Properties props = System.getProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.host", "192.168.1.19");
            props.put("mail.smtp.localhost", _strIpAddress);

            Session session = Session.getInstance(props);
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));

            for (int i = 0; i < cc.length; i++) {
                if (!cc[i].equals("")) {
                    message.addRecipient(Message.RecipientType.CC, new InternetAddress(cc[i]));
                }
            }
            for (int i = 0; i < to.length; i++) {
                if (!to[i].equals("")) {
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(to[i]));
                }
            }
            message.setSubject(prm_strHeader);
            MimeBodyPart messageBodyPart1 = null;
            MimeBodyPart messageBodyPart2 = null;
            MimeBodyPart messageBodyPart3 = null;
            if (!fileAttachment1.trim().equals("")) {
                messageBodyPart1 = new MimeBodyPart();
                messageBodyPart1.attachFile(fileAttachment1);
            }
            if (!fileAttachment2.trim().equals("")) {
                messageBodyPart2 = new MimeBodyPart();
                messageBodyPart2.attachFile(fileAttachment2);
            }
            if (!fileAttachment3.trim().equals("")) {
                messageBodyPart3 = new MimeBodyPart();
                messageBodyPart3.attachFile(fileAttachment3);
            }

            Multipart multipart = new MimeMultipart();
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setContent(bodytext1 + bodytext2 + bodytext3 + bodytext4, "text/html");
            multipart.addBodyPart(textPart);
            if (!fileAttachment1.trim().equals("")) {
                multipart.addBodyPart(messageBodyPart1);
            }
            if (!fileAttachment2.trim().equals("")) {
                multipart.addBodyPart(messageBodyPart2);
            }
            if (!fileAttachment3.trim().equals("")) {
                multipart.addBodyPart(messageBodyPart3);
            }
            message.setContent(multipart);
            Transport.send(message);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void GetIP() {
        try {
            if (System.getProperty("os.name").toLowerCase().equalsIgnoreCase("linux")) {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface nif = interfaces.nextElement();
                    Enumeration<InetAddress> inetAddresses = nif.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddr = inetAddresses.nextElement();
                        if (inetAddr instanceof Inet4Address) {
                            String address = inetAddr.getCanonicalHostName().trim();
                            if (address.trim().substring(0, 3).equals("192")) {
                                _strIpAddress = address;
                            }
                        }
                    }
                }
            } else {
                _strIpAddress = InetAddress.getLocalHost().getHostAddress();
            }
        } catch (Exception e) {
        }
    }

}
