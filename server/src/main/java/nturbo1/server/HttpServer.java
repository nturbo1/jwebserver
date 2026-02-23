package nturbo1.server;

import nturbo1.cmd.Argument;
import nturbo1.log.CustomLogger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class HttpServer
{
    private final ServerSocket serverSocket;
    private final int port;

    private static final int DEFAULT_PORT = 8080;
    private static final CustomLogger log = CustomLogger.getLogger(HttpServer.class.getName());

    private HttpServer(int port) throws IOException
    {
        this.serverSocket = new ServerSocket(port);
        this.port = port;
    }

    public static HttpServer init(Map<String, String> args)
    {
        log.info("Initializing an http server...");
        HttpServer httpServer = null;
        try
        {
            httpServer = new HttpServer(getPort(args));
        }
        catch(IOException ex)
        {
            log.error(ex.getMessage());
        }

        return httpServer;
    }

    public void start()
    {
        log.info("Listening on port " + port + "...");
        log.warn("Handle requests asynchronously!");

        while (true)
        {
            Socket socket;

            try
            {
                socket = this.serverSocket.accept();
                log.debug("New connection: " + socket.toString());
            }
            catch (IOException ex)
            {
                log.fatal("Failed to accept socket connection because: " + ex.getMessage());
                break;
            }

            Connection conn = new Connection(socket);
            conn.handle();
        }
    }

    private static int getPort(Map<String, String> args)
    {
        String portVal = args.get(Argument.PORT);

        return portVal != null ? Integer.parseInt(portVal) : DEFAULT_PORT;
    }

    public int getPort() { return port; }
}
