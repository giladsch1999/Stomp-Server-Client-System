package bgu.spl.net.impl.stomp;

import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Connections;

import java.util.Collection;

public class StompMessagingProtocolImp<T> implements StompMessagingProtocol<T> {

    private boolean shouldTerminate = false;
    private int connectionId;
    private ConnectionsImp<String> connections;
    private String UserLogin;

    public  void start(int connectionId, Connections<T> connections) {
        System.out.println("we are in start");
        this.connectionId = connectionId;
        this.connections = (ConnectionsImp) connections;
    }

    @Override
    public void process(T frame) {
        if(frame==null){
            connections.send(connectionId, "ERROR" +"\n" +
                    "message: malformed frame received +\n" +"\n"+
                    "ilegal frame sent"+"\n"+
                    "\u0000");
            connections.disconnect(connectionId);
            return;
        }
        if(frame instanceof ConnectFrame){
            UserLogin=((ConnectFrame) frame).getLogin();
        }
        System.out.println(UserLogin);
        if(UserLogin!=null) {//ignore this if the user is not connected
            boolean legalFrame = ((Frame) frame).execute(UserLogin, connectionId);
            if (!legalFrame ) {
                System.out.println("illegal frame");
                connections.send(connectionId, ((Frame) frame).getOut());
                User myUser=Controller.getInstance().users.get(UserLogin);
                myUser.disconnectClient();// changes isActive in User to false
                Collection<String> values=myUser.removeAllGames();
                for(String game:values){// needs sync probably
                    Controller.getInstance().games.get(game).remove(connectionId);// removes the users from all the games he was subscibed to subscriptions list
                }
                connections.disconnect(connectionId);
                if(Controller.getInstance().users.get(UserLogin)!=null)
                    Controller.getInstance().users.get(UserLogin).isActive = false;
            }
            else
            if (frame instanceof SendFrame && legalFrame) {
                connections.send(((SendFrame) frame).getDestination(), ((SendFrame) frame).getOut());
            } else {
                if(((Frame) frame).getOut()!=null){
                    connections.send(connectionId, ((Frame) frame).getOut());
                }
            }
            if (((Frame) frame).getReceipt() != null) {
                System.out.println("receipt sent");
                connections.send(connectionId, "RECEIPT"+"\n" + "receipt-id:" + ((Frame) frame).getReceipt() + "\n" + "\u0000");
            }
        }
        else
        {
            connections.send(connectionId, "ERROR" +"\n" +
                    "message: malformed frame received +\n" +"\n"+
                    "ilegal frame sent"+"\n"+
                    "\u0000");
            connections.disconnect(connectionId);
            return;
        }
    }

    /**
     * @return true if the connection should be terminated
     */
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    public void terminate(){
        this.shouldTerminate=true;
    }
}
