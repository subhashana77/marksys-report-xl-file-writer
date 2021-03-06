/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cronjobapplication;

import cronMail.CreateMail;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.mail.internet.ParseException;

/**
 *
 * @author indika.kuruppu
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    private static CreateMail _creMail;
    private static boolean _bolisdate;

    /**
     * This Method is used to execute any classes. and after execute the class
     * system will automatically email according to class settings
     *
     * @param args argument 0 should be classname argument 1 should be
     * methodName argument 2 should be SBU code argument 3 should be Location
     * code after above arguments you can use your own arguments[4,5,.....]
     */
    public static void main(String[] args) {
        String originalClassname = args[0].toString();
        String classname = originalClassname;
        String methodname = args[1].toString();

        if (args.length < 3) {


//            CreateXLFile createXLFile = new CreateXLFile();
//            createXLFile.posTxnFileUpload();


            System.out.println("Invalid number of arguments");
            System.out.println(">java -jarfile <ClassName> <MethodName> <arguments> ...");

        } else {
            try {
                 String methodParameter ="";
                if (args.length == 5) {
                    methodParameter = args[4].toString();
                    DateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
                    Date date = null;
                    String formatedDate = null;
                    try {
                        date = format.parse(methodParameter);
                        formatedDate = format.format(date);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (!methodParameter.equals(formatedDate)) {
                        date = null;
                    }

                    if (date != null) {
                        _bolisdate=true;
                        if (new Main().execute(classname, methodname, methodParameter) == 0) {
                            classname = "executables." + classname;
                            if (new Main().execute(classname, methodname, methodParameter) == 0) {
                                System.exit(0);
                            }
                        }
                    } else {
                        if (new Main().execute(classname, methodname) == 0) {
                            classname = "executables." + classname;
                            if (new Main().execute(classname, methodname) == 0) {
                                System.exit(0);
                            }
                        }
                    }

                } else {

                    if (new Main().execute(classname, methodname) == 0) {
                        classname = "executables." + classname;
                        if (new Main().execute(classname, methodname) == 0) {
                            System.exit(0);
                        }
                    }
                }
                if (args.length <= 5) {
                     if (_bolisdate) {
                         _creMail = new CreateMail(methodParameter);
                     }else{
                          _creMail = new CreateMail();
                     }
//                    _creMail = new CreateMail();
                    _creMail.setStrSBU(args[2].toString());
                    _creMail.setStrLoc(args[3].toString());
                    _creMail.setStrProgId(originalClassname);
                    _creMail.readAutoMail();
                }
//              

                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public int execute(String name, String methodName) {
        Class params[] = {String[].class};
        try {

            Object obj = Class.forName(name).newInstance();
            Class.forName(name).getDeclaredMethod(methodName, null).invoke(obj, null);
            return 1;

        }catch (ClassNotFoundException exception) {
            System.out.println("Class Not Found Ex" + "\n");
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int execute(String name, String methodName, String methodParameter) {
        Class params[] = {String[].class};
        try {

            Object obj = Class.forName(name).newInstance();
            Class.forName(name).getDeclaredMethod(methodName, String.class).invoke(obj, methodParameter);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
