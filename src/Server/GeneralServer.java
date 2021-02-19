package Server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GeneralServer extends HttpHandler implements Runnable {

    public static final int defaultPort = 8000;

    /*The server name*/
    private static String serverName = "HomeServer";

    /*The server version*/
    private static String serverVersion = "0.0.1";

    public int port;
    private ServerSocket socket = null;
    private HttpRouter router;

    private boolean running = true;
    private Logger logger = Logger.getLogger("java-httpserver");

    public GeneralServer() {
        this(defaultPort);
    }
    public GeneralServer(String name, String version) {
        this(defaultPort, name, version);
    }

    public HttpServer(int port, String name, String version) {
        this(port);
        setServerInfo(name, version);
    }

    public HttpServer(int port) {
        setPort(port);

        setRouter(new HttpRouter());
        getRouter().setDefaultHandler(this);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

    }

    @Override
    public void run() {
        try {
            running = true;
            socket = new ServerSocket();
            logger.info("Starting HttpServer at http://127.0.0.1:" + getPort());

            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(getPort()));

            while (running){
                Socket connection = null;
                try {
                    connection = socket.accept();
                    HttpRequest request = new HttpRequest(getRouter(), connection);
                    Thread t = new Thread(request);
                    t.start();

                    logger.info(String.format(
                            "Http request from %s:%d", connection.getInetAddress(), connection.getPort()));
                    }
                catch (SocketException e){
                    logger.log(Level.WARNING, "Client broke connection early!", e);
                }catch (IOException e) {
                    /*  This typically means there's a problem in the HttpRequest
                     */
                    logger.log(Level.WARNING, "IOException. Probably an HttpRequest issue.", e);

                } catch (HttpException e) {

                    logger.log(Level.WARNING, "HttpException.", e);

                } catch (Exception e) {
                    /*  Some kind of unexpected exception occurred, something bad might
                        have happened.
                        */
                    logger.log(Level.SEVERE, "Generic Exception!", e);

                    /*  If you're currently developing using this, you might want to
                        leave this break here, because this means something unexpected
                        occured. If the break is left in, the server stops running, and
                        you should probably look into the exception.
                        If you're in production, you shouldn't have this break here,
                        because you probably don't want to kill the server...
                        */
                    break;
                }

                }


            }

        } catch (Exception e) {
            logger.log(Level.WARNING, "Something bad happened...", e);
        }   finally {
        try {
            socket.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Well that's not good...", e);
        }
    }
    logger.info("Server shutting down.");

}

    public HttpRouter getRouter() {
        return this.router;
    }

    public void setRouter(HttpRouter router) {
        this.router = router;
    }

    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
}
