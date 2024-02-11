package bgu.spl.net.impl.stomp;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UnsubscribeFrame extends Frame {

    private String id;

    public UnsubscribeFrame(String msg, String[] lines) {
        System.out.println("banay unsbc");
        expectedHeaders = new HashSet<>(Arrays.asList("id"));
        this.msg = msg;
        Map<String, String> headers = parse(lines, msg, expectedHeaders);
        if (out == null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (key.equals("receipt"))
                    receipt = value;
                else
                    try {
                        Field field = this.getClass().getDeclaredField(key);
                        field.set(this, value);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                    }
            }
        }
    }
    //  executes the UNSUBSCRIBE frame logic return true if no errors were found
    public boolean execute( String userLogin,Integer connectionId) {
        System.out.println("execute unsbc");
        if(out!=null)
            return false;
        User myUser=Controller.getInstance().users.get(userLogin);
        String gameToUnsub=myUser.getGame(Integer.parseInt(id));
        Controller.getInstance().games.get(gameToUnsub).remove(connectionId);

        if((gameToUnsub==null)||!myUser.checkIfSubscribed(gameToUnsub)) {//if the user didnt subscribe to the game before unsubscribing
            out=  "ERROR\n" +
                    "message: malformed frame received\n" +
                    "The message:\n" +
                    "-----\n" +
                    msg+ "\n" +
                    "-----\n" +
                    id+ " is not subscribed to this topic\n" +
                    "\u0000";
            return false;
        }
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
        myUser.exitGame(Integer.parseInt(id));
        // removes this user from the list of the game followers
        return true;
    }
}
