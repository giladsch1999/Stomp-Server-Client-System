package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class ConnectionsImp<T> implements Connections<T> {
    private AtomicInteger idCounter;
    private ConcurrentHashMap<Integer, ConnectionHandler<T>> activeclients;
    protected AtomicInteger msgIdCounter;
    private ReadWriteLock readWriteLock;

    public ConnectionsImp(){
        activeclients = new ConcurrentHashMap<>();
        msgIdCounter = new AtomicInteger(0);
        idCounter = new AtomicInteger(0);
        readWriteLock = new ReentrantReadWriteLock();
    }

    private static class SingletonHolder {
        private static ConnectionsImp connections = new ConnectionsImp<>();

    }


    public boolean send(int connectionId, T msg) {
        try {
            readWriteLock.readLock().lock();
            if (activeclients.containsKey(connectionId)) {
                activeclients.get(connectionId).send(msg);
                return true;
            } else
                return false;
        }
        finally {
            readWriteLock.readLock().unlock();
        }
    }

    public static ConnectionsImp getInstance() {
        return SingletonHolder.connections;
    }

    public void send(String channel, T msg) {
        readWriteLock.readLock().lock();
        if(Controller.getInstance().games.get(channel)!=null) {
            Controller.getInstance().games.get(channel).forEach(value -> {
                if (activeclients.containsKey(value)){
                    msgIdCounter.addAndGet(1);
                    T realMsg = (T) ((String) msg).replace("$", msgIdCounter.toString());
                    activeclients.get(value).send(realMsg);
                }
                        });
                    }
        readWriteLock.readLock().unlock();
    }

    public void disconnect(int connectionId) {
        readWriteLock.writeLock().lock();
        activeclients.remove(connectionId);
        readWriteLock.writeLock().unlock();

    }

    public void AddConnection(ConnectionHandler<T> newConnectionHandler, int connectionId){
        readWriteLock.writeLock().lock();
        activeclients.putIfAbsent(connectionId,newConnectionHandler);
        readWriteLock.writeLock().unlock();

    }

    public int getConnectionId() {
        return idCounter.incrementAndGet();
    }
}