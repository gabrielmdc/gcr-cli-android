package gcr.cli.android.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import gcr.cli.android.sockets.Receiver;

public class ReceiverService extends IntentService {

    private static final String CANONICAL_NAME = ReceiverService.class.getCanonicalName();
    public static final String ACTION_START = CANONICAL_NAME + ".ACTION_START";
    public static final String KEY_RECEIVED = CANONICAL_NAME + ".KEY_RECEIVED";

    public static final String EXTRA_PORT = CANONICAL_NAME + ".EXTRA_PORT";
    public static final String EXTRA_RECEIVER = CANONICAL_NAME + ".EXTRA_RECEIVER";

    public ReceiverService() {
        super("ReceiverService");
    }

    public static void start(int port, Context clientContext, ResultReceiver resultReceiver) {
        Intent requestIntent = new Intent(clientContext, ReceiverService.class);
        requestIntent.setAction(ACTION_START);
        requestIntent.putExtra(EXTRA_PORT, port);
        requestIntent.putExtra(EXTRA_RECEIVER, resultReceiver);
        clientContext.startService(requestIntent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(intent == null || intent.getExtras() == null) {
            return;
        }
        Bundle bundle = intent.getExtras();
        final int port = bundle.getInt(EXTRA_PORT, -1);
        ResultReceiver resultReceiver = bundle.getParcelable(EXTRA_RECEIVER);

        Receiver receiver = new Receiver(port, resultReceiver);
        Thread receiverThread = new Thread(receiver);
        receiverThread.start();
    }
}
