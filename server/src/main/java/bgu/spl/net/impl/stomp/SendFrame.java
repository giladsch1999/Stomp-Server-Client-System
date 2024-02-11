package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.Connections;
//import com.sun.org.apache.bcel.internal.generic.PUSH;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SendFrame extends Frame {
   private String destination;
    private String body;

    public SendFrame(String msg,String[] lines) {
        System.out.println("banay send");
        expectedHeaders= new HashSet<>(Arrays.asList("destination"));
        this.msg=msg;
        Map<String, String> headers=parse(lines,msg,expectedHeaders);
        if(out==null){
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                    if(key.equals("receipt"))
                        receipt=value;
                    else
                        destination=value;

               body=parseBody(headers.size()+2,lines);//1 line for name of the frame and another for the empty line
            }
        }
    }
    boolean execute(String userLogin,Integer connectionId) {
        System.out.println("execute send");
        if(out!=null)
            return false;
        User myUser=Controller.getInstance().users.get(userLogin);
        if(!myUser.isActive){
            out="ERROR\n" +
                    "message: malformed frame received\n" +
                    "The message:\n" +
                    "-----\n" +
                    msg+ "\n" +
                    "-----\n" +
                    userLogin+ " is not connected "+"\n" +
                    "\u0000";
            return false;
        }
        if(!myUser.checkIfSubscribed(destination))//if the user didnt subscribe to the game before unsubscribing
         {
             System.out.println("error send");
            out=  "ERROR\n" +
                    "message: malformed frame received\n" +
                    "The message:\n" +
                    "-----\n" +
                    msg+ "\n" +
                    "-----\n" +
                     " this user is not subscribed to this topic\n" +
                    "\u0000";
            return false;
        }
        else
            out="MESSAGE\n"+
                    "subscription:"+ myUser.gameToSub(destination)+"\n"+
                    "message-id:"+"$\n"+
                    "destination:"+destination+"\n"+
                    "\n"+
                    body+"\n"+
                    "\u0000";
            return true;
    }

    public String parseBody(Integer numOfHeaders,String[] lines){
        String body = "";
        for (int j = numOfHeaders; j < lines.length; j++) {
        body += lines[j] + "\n";
        }
        return body;
    }
    public String getDestination(){
        return destination;
    }
    }
