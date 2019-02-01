package me.mrut.bgcomplication;

import android.content.SharedPreferences;
import android.os.StrictMode;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.ComplicationManager;
import android.support.wearable.complications.ComplicationProviderService;
import android.support.wearable.complications.ComplicationText;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;


public class BGComplicationService extends ComplicationProviderService {

    private static final String TAG = "ComplicationProvider";

    /*
     * Called when a complication has been activated. The method is for any one-time
     * (per complication) set-up.
     *
     * You can continue sending data for the active complicationId until onComplicationDeactivated()
     * is called.
     */
    @Override
    public void onComplicationActivated(
            int complicationId, int dataType, ComplicationManager complicationManager) {
        Log.d(TAG, "onComplicationActivated(): " + complicationId);

        String numberText = null;
        try {
            numberText = BGWebRequest();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ComplicationData complicationData = null;

        switch (dataType) {
            case ComplicationData.TYPE_SHORT_TEXT:
                complicationData =
                        new ComplicationData.Builder(ComplicationData.TYPE_SHORT_TEXT)
                                .setShortText(ComplicationText.plainText(numberText))
                                .build();
                break;
            default:
                if (Log.isLoggable(TAG, Log.WARN)) {
                    Log.w(TAG, "Unexpected complication type " + dataType);
                }
        }

        if (complicationData != null) {
            complicationManager.updateComplicationData(complicationId, complicationData);

        } else {
            // If no data is sent, we still need to inform the ComplicationManager, so the update
            // job can finish and the wake lock isn't held any longer than necessary.
            complicationManager.noUpdateRequired(complicationId);
        }

    }

    /*
     * Called when the complication needs updated data from your provider. There are four scenarios
     * when this will happen:
     *
     *   1. An active watch face complication is changed to use this provider
     *   2. A complication using this provider becomes active
     *   3. The period of time you specified in the manifest has elapsed (UPDATE_PERIOD_SECONDS)
     *   4. You triggered an update from your own class via the
     *       ProviderUpdateRequester.requestUpdate() method.
     */
    @Override
    public void onComplicationUpdate(
            int complicationId, int dataType, ComplicationManager complicationManager) {
        Log.d(TAG, "onComplicationUpdate() id: " + complicationId);

        // Create Tap Action so that the user can trigger an update by tapping the complication.
        ComponentName thisProvider = new ComponentName(this, getClass());
        // We pass the complication id, so we can only update the specific complication tapped.
        PendingIntent complicationPendingIntent =
                ComplicationTapBroadcastReceiver.getToggleIntent(
                        this, thisProvider, complicationId);

        String numberText = null;
        try {
            numberText = BGWebRequest();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ComplicationData complicationData = null;

        switch (dataType) {
            case ComplicationData.TYPE_SHORT_TEXT:
                complicationData =
                        new ComplicationData.Builder(ComplicationData.TYPE_SHORT_TEXT)
                                .setShortText(ComplicationText.plainText(numberText))
                                .setTapAction(complicationPendingIntent)
                                .build();
                break;
            default:
                if (Log.isLoggable(TAG, Log.WARN)) {
                    Log.w(TAG, "Unexpected complication type " + dataType);
                }
        }

        if (complicationData != null) {
            complicationManager.updateComplicationData(complicationId, complicationData);

        } else {
            // If no data is sent, we still need to inform the ComplicationManager, so the update
            // job can finish and the wake lock isn't held any longer than necessary.
            complicationManager.noUpdateRequired(complicationId);
        }
    }

    /*
     * Called when the complication has been deactivated.
     */
    @Override
    public void onComplicationDeactivated(int complicationId) {
        Log.d(TAG, "onComplicationDeactivated(): " + complicationId);
    }

    String BGWebRequest() throws IOException {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String sURL = "http://" + SharedPref.read(SharedPref.nightscout_url, "DEFAULTSITE.herokuapp.com") + "/api/v1/entries.json?count=1";

        URL url = new URL(sURL);
        URLConnection request = url.openConnection();
        request.connect();

        // Convert to a JSON object to print data
        JsonParser jp = new JsonParser(); //from gson
        JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
        JsonArray jarray = root.getAsJsonArray();
        JsonObject jobject = jarray.get(0).getAsJsonObject();

        //Get BG
        Double BGdouble = jobject.get("sgv").getAsDouble();

        //Get trend and convert to arrow
        int BGtrend = jobject.get("trend").getAsInt();
        String BGarrow = null;

        switch (BGtrend) {
            case 0 :
                BGarrow = "⇼";
                break;
            case 1 :
                BGarrow = "↑↑";
                break;
            case 2 :
                BGarrow = "↑";
                break;
            case 3 :
                BGarrow = "↗";
                break;
            case 4 :
                BGarrow = "→";
                break;
            case 5 :
                BGarrow = "↘";
                break;
            case 6 :
                BGarrow = "↓";
                break;
            case 7 :
                BGarrow = "↓↓";
                break;
        }

        //Check pref and convert to mmol/mg, round value
        String BGresult = "000";
        if(SharedPref.read(SharedPref.mmolmg_pref, true)) {
            BGdouble = Math.round((BGdouble / 18) * 10.0) / 10.0;
            BGresult = BGdouble + " " + BGarrow;
        } else {
            BGresult = BGdouble.intValue() + " " + BGarrow;
        }

        return BGresult;
    }
}
