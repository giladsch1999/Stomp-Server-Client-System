package bgu.spl.net.impl.stomp;


import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class User {
    private String userName;
    private String password;
    protected boolean isActive=false;
    protected Integer activeConnectionId;
    // maps bet sub id to game name
    protected ConcurrentHashMap<Integer,String> userGames;
    // maps bet game name to sub id
    protected ConcurrentHashMap<String,Integer> gamesToSub;


    public User(String userName,String password){
        this.userName=userName;
        this.password=password;
        userGames=new ConcurrentHashMap<>();
        gamesToSub=new ConcurrentHashMap<>();
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public boolean joinGame(String game, Integer subId){
        gamesToSub.putIfAbsent(game,subId);
        return userGames.putIfAbsent(subId,game)==null;
    }

    public String exitGame(Integer subId){
        String game=userGames.remove(subId);
        gamesToSub.remove(game) ;
        return game;
    }
    public String getGame(Integer subID){
      return userGames.get(subID);
    }

    public boolean checkIfSubscribed(String game){//might be red
        return userGames.containsValue(game) ;
    }
    public boolean isActive() {
        return isActive;
    }
    public void disconnectClient(){
        isActive=false;
    }
    public Collection<String> removeAllGames(){
// delete all elements from the map
        Collection<String> values=new ArrayList<>();// get a collection of all values
        for (String value : userGames.values()) {
            values.add(value);
//          Integer subId=gamesToSub.remove(value);// remove the value from gamesToSub
//          userGames.remove(subId); // remove the value from userGames
        }
        System.out.println(values.contains("germany_spain"));
        gamesToSub.clear();
        userGames.clear();
      return values;
    }

    public Integer gameToSub(String game){
       return gamesToSub.get(game);
    }
}
