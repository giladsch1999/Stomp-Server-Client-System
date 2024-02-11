package bgu.spl.net.impl.stomp;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Controller {
   protected   ConcurrentHashMap<String,User> users;// mapping  username to user
    protected ConcurrentHashMap<String, CopyOnWriteArrayList<Integer>> games;// mapping games to all fllowing users
    protected ConnectionsImp connections;
    private static class SingletonHolder{
        private static Controller UC_instance = new Controller();
    }
    public  Controller(){
        users= new ConcurrentHashMap<>();
        games=new  ConcurrentHashMap<>();
        connections=ConnectionsImp.getInstance();
    }
    public static Controller getInstance(){
        return SingletonHolder.UC_instance;
    }

}
