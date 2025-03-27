package fr.emiko.net;

import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Vector;

import fr.emiko.graphicsElement.Line;
import org.json.JSONException;
import org.json.JSONObject;

public class DrawServer {

    private ServerSocket passiveSocket;
    private Vector<DrawClientHandler> clientList = new Vector<DrawClientHandler>();
    private Vector<Line> lines;
    private double canvasWidth;
    private double canvasHeight;
    public DrawServer(int port) throws IOException {
        passiveSocket = new ServerSocket(port);
    }


    public static void main(String[] args) throws IOException {
        DrawServer server = new DrawServer(8090);
        server.acceptClients();
    }

    public void acceptClients() {
        while (true) {
            try {
                Socket sock = passiveSocket.accept();
                handleNewClient(sock);
            } catch (IOException e) {
                System.out.println(e);
            }
        }

    }


    public void removeClient(DrawClientHandler client) {
        clientList.remove(client);
    }


    private void handleNewClient(Socket sock) throws IOException {
        DrawClientHandler client = new DrawClientHandler(sock);
        clientList.add(client);

        Thread clientLoop = new Thread(client::eventReceiveLoop);
        clientLoop.start();
    }


    /**
     * Ferme la connexion du serveur, en fermant la connexion auprès de tous ses clients, puis en fermant son socket en écoute passive.
     * @throws IOException si la connexion
     */
    public void close() throws IOException {
        for (DrawClientHandler client : clientList) {
            client.close();
        }
        passiveSocket.close();
    }



    private class DrawClientHandler {

        /** Message de fin d'une connexion */
        public static final String END_MESSAGE = "fin";
        /**
         * Socket connecté au client
         */
        private Socket sock;
        /**
         * Flux de caractères en sortie
         */
        private PrintStream out;
        /**
         * Flux de caractères en entrée
         */
        private BufferedReader in;
        /**
         * Chaine de caractères "ip:port" du client
         */
        private String ipPort;
        private User user;

        /**
         * Initialise les attributs {@link #sock} (socket connecté au client),
         * {@link #out} (flux de caractères UTF-8 en sortie) et
         * {@link #in} (flux de caractères UTF-8 en entrée).
         *
         * @param sock socket connecté au client
         * @throws IOException si la connexion ne peut être établie ou si les flux ne peuvent être récupérés
         */
        public DrawClientHandler(Socket sock) throws IOException {
            this.sock = sock;
            this.ipPort = "%s:%d".formatted(sock.getInetAddress().getHostAddress(), sock.getPort());
            OutputStream os = sock.getOutputStream();
            InputStream is = sock.getInputStream();
            this.out = new PrintStream(os, true, StandardCharsets.UTF_8);
            this.in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        }


        /**
         * Boucle de réception d'évènement : réceptionne les messages reçus et les délèguent à `handleEvent(java.lang.String)` pour les interpréter
         */
        public void eventReceiveLoop() {
            try {
                String message = null;
                while (!END_MESSAGE.equals(message)) {
                    message = in.readLine();
                    if (message == null) {
                        break;
                    }
                    System.out.println("Réception de message : " + message);
                    try {
                        if (!handleEvent(message)) {
                            break;
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        break;
                    }
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            close();
        }



        /**
         * Traitement d'un évènement. Ventile vers les méthodes traitant chaque type d'évènement.
         * @param message objet évènement sous la forme d'une chaine JSON brute de réception
         * @return `false` si l'évènement est de type Event.QUIT , `true` pour tous les autres types.
         * @throws JSONException si l'objet JSON n'est pas conforme
         * @throws IllegalStateException si l'authentification n'est pas effectuée
         */
        private boolean handleEvent(String message) throws JSONException, IllegalStateException {
            Event event = Event.fromJSON(message);
            switch (event.getType()) {
                case Event.AUTH -> {
                    doLogin(event.getContent());
                    return true;
                }
                case Event.ADDLINE -> {
                    doAddLine(event.getContent());
                    return true;
                }
                case Event.DELLINE -> {
                    doDelLine(event.getContent());
                    return true;
                }
                case Event.LINELST -> {
                    doSendLines();
                    return true;
                }
                case Event.ADDCANVAS -> {
                    doAddCanvas(event.getContent());
                    return true;
                }
                default -> {
                    return false;
                }
            }
        }

        private void doAddCanvas(JSONObject content) throws JSONException {
            canvasWidth = content.getDouble("width");
            canvasHeight = content.getDouble("height");
            sendAllOtherUsers(new Event(Event.CNVS, content));
        }

        private void doDelLine(JSONObject content) {
            Line line = Line.fromJSONArray(content.getJSONArray("line"));
            this.user.getLines().remove(line);

            sendAllOtherUsers(new Event("DELLINE", line.toJSONObject()));
        }

        private void doAddLine(JSONObject content) {
            try {
                System.out.println(Line.fromJSONArray(content.getJSONArray("line")));
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }
            Line line = Line.fromJSONArray(content.getJSONArray("line"));
            this.user.getLines().add(line);
            sendAllOtherUsers(new Event("LINE", line.toJSONObject()));

        }

        private void sendAllOtherUsers(Event event) {
            System.out.println("current user: " + this.user.getUsername());
            for (DrawClientHandler client : clientList) {
                System.out.println("calculating user: " + client.user.getUsername());
                if (client.user != this.user) {
                    System.out.println("found user: " + client.user.getUsername());
                    sendEvent(client, event);
                }
            }
        }

        private void sendEvent(DrawClientHandler client, Event event) {
            String jsonEvent = event.toJSON();
            client.out.println(jsonEvent);
        }

        private void doSendLines() {
            out.println(
                    new Event("CNVS", new JSONObject()
                                    .put("width", canvasWidth)
                                    .put("height", canvasHeight))
            );

            Vector<Line> lines = new Vector<>();
            for (DrawClientHandler client: clientList) {
                for (Line line: client.user.getLines()) {
                    lines.add(line);
                }
            }
            for (Line line: lines) {
                out.println(new Event("LINE", line.toJSONObject()));
            }
        }

        private void doLogin(JSONObject content) {
            this.user = new User(content.getString("username"));
        }


        public void close() {
            try {
                sock.close();
                removeClient(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }


    }
}
