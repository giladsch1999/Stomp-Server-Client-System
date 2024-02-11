package bgu.spl.net.impl.stomp;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class SubscribeFrame extends Frame {

    private String destination;
    private String id;


    public SubscribeFrame(String msg, String[] lines) {
        expectedHeaders = new HashSet<>(Arrays.asList("destination", "id"));
        this.msg = msg;
        Map<String, String> headers = parse(lines, msg, expectedHeaders);
        if (out == null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (key.equals("receipt")){
                    receipt = value;
                }
                else
                    try {
                        Field field = this.getClass().getDeclaredField(key);
                        field.set(this, value);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                    }
            }
        }
    }
    //  executes the SUBSCRIBE frame logic return true if no errors were found
    public boolean execute( String userLogin,Integer connectionId)  {
        if(out!=null)
            return false;
    User myUser=Controller.getInstance().users.get(userLogin);
        Controller.getInstance().games.putIfAbsent(destination,new CopyOnWriteArrayList<>());
       if(!(Controller.getInstance().games.get(destination).addIfAbsent(connectionId))) {
           out="ERROR\n" +
                   "message: malformed frame received\n" +
                   "The message:\n" +
                   "-----\n" +
                   msg+ "\n" +
                   "-----\n" +
                   userLogin+ " already logged to "+destination+"\n" +
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
        myUser.joinGame(destination,Integer.parseInt(id));
        return true;
    }
    }
