package me.mrut.bgcomplication;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.widget.CheckBox;
import android.widget.TextView;
import android.view.View;

import java.io.IOException;

public class MainActivity extends WearableActivity {

    private TextView mCurrentBG;
    private TextView mNightscoutPath;
    private CheckBox mmmolmgCheck;

    public void updateBG() throws IOException {
        mNightscoutPath = findViewById(R.id.NightscoutPath);
        SharedPref.write(SharedPref.nightscout_url, mNightscoutPath.getText().toString());

        mmmolmgCheck = findViewById(R.id.mmolmgCheck);
        SharedPref.write(SharedPref.mmolmg_pref, mmmolmgCheck.isChecked());

        mCurrentBG = findViewById(R.id.CurrentBG);
        BGComplicationService bgComplicationService = new BGComplicationService();
        mCurrentBG.setText(bgComplicationService.BGWebRequest());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPref.init(getApplicationContext());

        try {
            updateBG();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void onClickFetchBG(View v) throws IOException {
        updateBG();
    }


}
