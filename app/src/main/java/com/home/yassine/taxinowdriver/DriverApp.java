package com.home.yassine.taxinowdriver;

import android.app.Application;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.home.yassine.taxinowdriver.protocol.AuthenticationMessage;
import com.home.yassine.taxinowdriver.protocol.RiderInfoMessage;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Created by Yassine on 22/09/2016.
 */
public class DriverApp extends Application {

    private WebSocketClient client;
    private IConnectionHandler handler;
    private ArrayList<RiderInfoMessage> ridersInfo = new ArrayList<>();
    private ArrayList<Passenger> savedPassengers;
    private DestinationFragment.DestInfo savedDestInfo;

    public void saveCurrentDestInfo(DestinationFragment.DestInfo destInfo) {
        savedDestInfo = destInfo;
    }

    public DestinationFragment.DestInfo loadSavedDestInfo() {
        return savedDestInfo;
    }

    public enum ConnectionState {
        CONNECTED,
        CLOSED
    }

    public interface IConnectionHandler {
        void onOpen(ServerHandshake serverHandshake);
        void onMessage(final String message);
        void onClose(int i, String s, boolean b);
        void onError(Exception e);
    }

    public ConnectionState mConnectionState = ConnectionState.CLOSED;

    public WebSocketClient getClient() {
        return client;
    }

    public boolean isClientConnected() {
        return mConnectionState != ConnectionState.CLOSED;
    }

    public void setClientConnected() {
        mConnectionState = ConnectionState.CONNECTED;
    }

    public void ReplaceHandler(IConnectionHandler connHandler) {
        this.handler = connHandler;
    }

    public void connectWebSocket(final IConnectionHandler conn) {


        URI uri;
        try {
            uri = new URI(HttpClient.BASE_URL_WS);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        this.handler = conn;

        client = new WebSocketClient(uri) {

            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");

                if (handler != null)
                    handler.onOpen(serverHandshake);
            }

            @Override
            public void onMessage(String message) {
                Log.e("RECEIVED", message);

                if (handler != null)
                    handler.onMessage(message);
            }
            @Override
            public void onClose(int i, String s, boolean b) {
                mConnectionState = ConnectionState.CLOSED;
                Log.e("Websocket", "Closed " + s+ " "+ String.valueOf(b));

                if (handler != null)
                    handler.onClose(i, s, b);
            }

            @Override
            public void onError(Exception e) {
                mConnectionState = ConnectionState.CLOSED;
                Log.e("Websocket", "Error " + e.getMessage());

               if (handler != null)
                   handler.onError(e);
            }
        };

        client.connect();
    }

    public void disconnectClient() {
        if (client != null)
            client.close();
    }

    public void sendConnectionString(String message) {

        client.send(message);
    }

    public void sendString(String message) {

        if (mConnectionState == ConnectionState.CLOSED)
            return;

        client.send(message);
    }

    public void addRiderInfo(RiderInfoMessage riderInfoMessage) {

        if (getRiderInfo(riderInfoMessage.id) == null)
            this.ridersInfo.add(riderInfoMessage);
    }

    public RiderInfoMessage getRiderInfo(String riderId) {

        for (RiderInfoMessage aRiderInfo : ridersInfo) {

            if (aRiderInfo.id.equals(riderId))
                return aRiderInfo;
        }

        return null;
    }

    public void removeRiderInfo(String riderId) {

        for (int i = 0; i < ridersInfo.size(); i++) {

            if (ridersInfo.get(i).id.equals(riderId)) {
                ridersInfo.remove(i);
                break;
            }
        }
    }

    @Override
    public void onCreate() {
        Log.e("CREATE", "CREATE app");
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        Log.e("TERMINATE", "TERMINATE app");
        disconnectClient();
        super.onTerminate();
    }

    public void savePassengers(ArrayList<Passenger> passengers) {
        savedPassengers = passengers;
    }

    public ArrayList<Passenger> LoadSavedPassengers() {
        return savedPassengers;
    }
}
