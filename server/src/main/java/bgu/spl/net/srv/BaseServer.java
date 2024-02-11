package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.impl.stomp.ConnectionsImp;
import bgu.spl.net.impl.stomp.StompMessageEncoderDecoder;
import bgu.spl.net.impl.stomp.StompMessagingProtocolImp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Supplier;

public abstract class BaseServer<T> implements Server<T> {

    private final int port;
    private final Supplier<StompMessagingProtocolImp> protocolFactory;
    private final Supplier<StompMessageEncoderDecoder> encdecFactory;
    private ServerSocket sock;
    private ConnectionsImp<T> connections;
    private Integer IdCounter=0;


    public BaseServer(
            int port,
            Supplier<StompMessagingProtocolImp> protocolFactory,
            Supplier<StompMessageEncoderDecoder> encdecFactory) {
        this.connections = ConnectionsImp.getInstance();
        this.port = port;
        this.protocolFactory = protocolFactory;
        this.encdecFactory = encdecFactory;
		this.sock = null;

    }

    @Override
    public void serve() {

        try (ServerSocket serverSock = new ServerSocket(port)) {
			System.out.println("Server started");

            this.sock = serverSock; //just to be able to close

            while (!Thread.currentThread().isInterrupted()) {

                Socket clientSock = serverSock.accept();

                IdCounter++;
                StompMessagingProtocolImp stomp=protocolFactory.get();
                BlockingConnectionHandler<T> handler = new BlockingConnectionHandler<>(
                        clientSock,
                        encdecFactory.get(),
                        stomp);
                stomp.start(IdCounter,connections);
                connections.AddConnection(handler,IdCounter);
                System.out.println("after add connection");
                execute(handler);

            }
        } catch (IOException ex) {
        }

        System.out.println("server closed!!!");
    }

    @Override
    public void close() throws IOException {
		if (sock != null)
			sock.close();
    }

    protected abstract void execute(BlockingConnectionHandler<T>  handler);

}
