package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.Server;

import java.util.Scanner;

public class StompServer  {

    public static void main(String[] args) {
        int port =7777;
        String serverType ="REACTOR";

        switch (serverType){
            case("TCP"):{
                Server.threadPerClient(
                        port,
                        () -> new StompMessagingProtocolImp(),
                        () -> new StompMessageEncoderDecoder()
                ).serve();
                break;
            }
            case("REACTOR"):{
                Server.reactor(
                        Runtime.getRuntime().availableProcessors(),
                        port,
                        () ->  new StompMessagingProtocolImp(),
                        () -> new StompMessageEncoderDecoder()
                ).serve();
                break;
            }
        }
    }
}

