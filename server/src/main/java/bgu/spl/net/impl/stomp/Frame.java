package bgu.spl.net.impl.stomp;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Frame {

    protected String out;
    protected String receipt=null;
    protected String msg;
    protected Set<String> expectedHeaders ;

    public Map<String, String> parse(String[] lines ,String msg,Set<String> expectedHeaders){
        int start=0;

    // Create a map to hold the header name-value pairs
    Map<String, String> headers = new HashMap<>();

// Extract the header name-value pairs from the remaining lines
for (int i = 1; i < lines.length; i++) {
        // Check for an empty line, which indicates the end of the headers
        if (lines[i].isEmpty()) {
            start=i+1;
        break;
        }
        // Split the line into a name-value pair and add it to the map
        String[] pair = lines[i].split(":");
        String header = pair[0];
        String value = pair[1];
        if(headers.put(header, value)!=null || (!expectedHeaders.contains(header) && !header.equals("receipt"))){
            out = "ERROR\n" +
                    "message: malformed frame received\n" +
                    "The message:\n" +
                    "-----\n" +
                    msg+ "\n" +
                    "-----\n" +
                  header+ " is not a legal header\n" +
                    "\u0000";
            System.out.println("lo huki in frame"+header);
            break;

        }
        }

// Extract the frame body from the remaining lines //ba banay
     return headers;
    }

    abstract boolean execute(String login,Integer connectionId) ;        //צריך לשמור גם איכשהו בקונקשיין הנדלר את הלוגין אם הוא נכון

    public String getOut(){
        return out;
    }
    public String getReceipt(){
        return receipt;
    }
}
