package bgu.spl.net.impl.stomp;

import bgu.spl.net.api.MessageEncoderDecoder;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class StompMessageEncoderDecoder<T> implements MessageEncoderDecoder {
    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;
    @Override
    public Object decodeNextByte(byte nextByte) {
        if (nextByte == '\u0000') {
            return StringToFrame();
        }
        pushByte(nextByte);
        return null;
    }

    @Override
    public byte[] encode(Object message) {
        return (message + "\u0000").getBytes();
    }

    public Frame StringToFrame(){
        String msg = new String(bytes, 0, len, StandardCharsets.UTF_8);
        len = 0;
        String[] lines = msg.split("\n");
        switch (lines[0]){
            case ("CONNECT"):{
                return new ConnectFrame(msg,lines);
            }
            case ("SUBSCRIBE"):{
                System.out.println("ze subsc");
                return new SubscribeFrame(msg,lines);
            }
            case ("UNSUBSCRIBE"):{
                return new UnsubscribeFrame(msg,lines);
            }
            case ("SEND"):{
                return new SendFrame(msg,lines);
            }
            case ("DISCONNECT"):{
                return new DisconnectFrame(msg,lines);
            }
            default:
                System.out.println(lines[0]);
                System.out.println("default decode next");
                return null;

        }
    }
    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }
        bytes[len++] = nextByte;
    }
}
