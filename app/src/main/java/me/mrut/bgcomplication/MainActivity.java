package me.mrut.bgcomplication;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends WearableActivity {

    private TextView mTextView;
    private TextView mCurrentBG;
    public TextView mNightscoutPath;

    public void updateBG() throws IOException {
        mCurrentBG = findViewById(R.id.CurrentBG);
        BGComplicationService bgComplicationService = new BGComplicationService();
        mCurrentBG.setText(bgComplicationService.BGWebRequest());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.text);

        try {
            updateBG();
        } catch (IOException e) {
            e.printStackTrace();
        }


        // Enables Always-on
        //setAmbientEnabled();
    }


}
