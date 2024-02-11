package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.Connections;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import bgu.spl.net.impl.stomp.Controller;

public class ConnectFrame extends Frame {
    private String version;
    private String host;
    private String login;
    private String passcode;

    public ConnectFrame(String msg,String[] lines){
        expectedHeaders= new HashSet<>(Arrays.asList("accept-version", "host","login","passcode"));
        this.msg=msg;
        Map<String, String> headers=parse(lines,msg,expectedHeaders);
        if(out==null){
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if(key.equals("accept-version"))//we could not name the field with -
                    version=value;
                else {
                    if(key.equals("receipt"))
                        receipt=value;
                    else
                    try {
                        Field field = this.getClass().getDeclaredField(key);
                        field.set(this, value);

                    } catch (NoSuchFieldException | IllegalAccessException e) {
                    }
                }
            }
        }
     }
     public String getLogin(){
      return login;
    }
    //  executes the CONNECT frame logic return true if no errors were found
     public boolean execute(String login,Integer connectionId)   {
        if(out!=null){
            return false;
        }
        if(!version.equals("1.2")){
            out = "ERROR\n" +
                    "message: malformed frame received\n" +
                    "The message:\n" +
                    "-----\n" +
                    msg+ "\n" +
                    "-----\n" +
                    version+ " is not a legal version\n" +
                    "\u0000";
            return false;
        } else if (!host.equals("stomp.cs.bgu.ac.il")) {
                     out = " ERROR\n" +
                    "message: malformed frame received\n" +
                    "The message:\n" +
                    "-----\n" +
                    msg+ "\n" +
                    "-----\n" +
                    host+ " is not a legal host\n" +
                    "\u0000";
            return false;
        } else {
            User temp=new User(login, passcode);
            User ogUser=Controller.getInstance().users.putIfAbsent(login,temp);
            if (ogUser != null) {//when it was already in users
                if (ogUser.isActive()) {//the user is  logged in
                    out = "ERROR\n" +
                            "message: malformed frame received\n" +
                            "The message:\n" +
                            "-----\n" +
                            msg + "\n" +
                            "-----\n" +
                            login + " is already logged in\n" +
                            "\u0000";
                    return false;
                }else if (!passcode.equals(ogUser.getPassword())) {
                    System.out.println("hamekory "+ogUser.getPassword());
                    System.out.println("hachadash "+passcode);
                    out = "ERROR\n" +
                            "message: malformed frame received\n" +
                            "The message:\n" +
                            "-----\n" +
                            msg + "\n" +
                            "-----\n" +
                            passcode + " passcode is wrong\n" +
                            "\u0000";
                    return false;
                }
                }
        }
         out="CONNECTED"+"\n"+
                 "version:"+this.version+"\n"+
                 "\u0000";
         User myUser=Controller.getInstance().users.get(login);
         myUser.isActive=true;
         return true;

     }
    }


//    public static void main(String[] args) {
//        String s = "<StompCommand>\n<HeaderName1>: <HeaderValue1>\n<HeaderName2>: <HeaderValue2>\n\n<FrameBody>\n^@";
//        System.out.println(s);    }
//
//}
//    String s = "<StompCommand>\n<HeaderName1>: <HeaderValue1>\n<HeaderName2>: <HeaderValue2>\n\n<FrameBody>\n^@";
//
//    // Split the string into lines
//    String[] lines = s.split("\n");
//
//    // Extract the STOMP command from the first line
//    String command = lines[0].substring(1, lines[0].length() - 1);  // Remove the angle brackets
//
//    // Create a map to hold the header name-value pairs
//    Map<String, String> headers = new HashMap<>();
//
//// Extract the header name-value pairs from the remaining lines
//for (int i = 1; i < lines.length; i++) {
//        // Check for an empty line, which indicates the end of the headers
//        if (lines[i].isEmpty()) {
//        break;
//        }
//        // Split the line into a name-value pair and add it to the map
//        String[] pair = lines[i].split(": ");
//        String name = pair[0].substring(1, pair[0].length() - 1);  // Remove the angle brackets
//        String value = pair[1].substring(1, pair[1].length() - 1);  // Remove the angle brackets
//        headers.put(name, value);
//        }
//
//// Extract the frame body from the remaining lines
//        String body = "";
//        for (int i = 1; i < lines.length; i++) {
//        // Check for an empty line, which indicates the end of the headers
//        if (lines[i].isEmpty()) {
//        i++;
//        break;
//        }
//        }
//        for (int i = i; i < lines.length; i++) {
//        body += lines[i] + "\n";
//        }
//
//// Create a StompFrame object using the extracted values
//        StompFrame frame = new StompFrame(command, headers, body);
