package fr.emiko.net;

import fr.emiko.graphicalapp.HelloController;
import javafx.application.Platform;
import org.json.JSONObject;

import java.io.IOException;

public class DrawClient extends ClientTCP{
    private final HelloController listener;

    public DrawClient(String host, int port, HelloController listener) throws IOException {
        super(host, port);
        this.listener = listener;
    }



    /**
     * Envoi d'un évènement, sérialisé dans sa représentation JSON, au serveur.
     * @param event L'évènement à envoyer
     */
    public void sendEvent(Event event) {
        connected = true;
        try {
            String message = event.toJSON();
            if (message == null) { // fin du flux stdIn
                message = END_MESSAGE;
            }
            this.send(message);
            if (END_MESSAGE.equals(message)) {
                connected = false;
            }
        } catch (IOException e) {
            connected = false;
        }
    }


    /**
     * Boucle de réception des messages : chaque message est un évènement sérialisé en JSON, qui est transféré à ChatController.handleEvent(rtgre.modeles.Event) pour traitement.
     * Si le message n'est pas conforme (format JSON), la connection est stoppée.
     */
    @Override
    public void receiveLoop() {
        try {
            while (connected) {
                String message = this.receive();
                if (listener != null) {
                    Platform.runLater(() -> listener.handleEvent(Event.fromJSON(message)));
                }
            }
        } catch (IOException e) {
            connected = false;
        } finally {
            close();
        }
    }


    public void sendAuthEvent(String login) {
        try {
            this.send(new Event(Event.AUTH, new JSONObject().put("username", login)).toJSON());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
