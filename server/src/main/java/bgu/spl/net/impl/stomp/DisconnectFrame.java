package bgu.spl.net.impl.stomp;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import bgu.spl.net.impl.stomp.Controller;

public class DisconnectFrame extends Frame {

    public DisconnectFrame(String msg, String[] lines) {
        System.out.println("banay disconnect");
        this.msg = msg;
        this.expectedHeaders=new HashSet<>();
        Map<String, String> headers = parse(lines, msg, expectedHeaders);
        if (out == null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (key.equals("receipt"))
                    receipt = value;
                else
                    try {// probably redundant
                        Field field = this.getClass().getDeclaredField(key);
                        field.set(this, value);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                    }
            }
        }
    }

    //  executes the DISCONNECT frame logic return true if no errors were found
    public boolean execute( String userLogin,Integer connectionId) {
        System.out.println("execute disconnect");
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
        myUser.disconnectClient();// changes isActive in User to false
        Collection<String> values=myUser.removeAllGames();
        System.out.println(values.contains("germany_spain"));
        for(String game:values){// needs sync probably
            Controller.getInstance().games.get(game).remove(connectionId);// removes the users from all the games he was subscibed to subscriptions list
            System.out.println("ze game"+Controller.getInstance().games.get(game).remove(connectionId));
        }
        out = "RECEIPT\n" +
                "receipt - id:"+ receipt +"\n" +
                "\u0000";
        receipt=null;
     return true;
    }
}

