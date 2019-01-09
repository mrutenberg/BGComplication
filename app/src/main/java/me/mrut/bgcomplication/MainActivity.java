package me.mrut.bgcomplication;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;

public class MainActivity extends WearableActivity {

    private TextView mTextView;
    private TextView mcurrentBG;

    public void updateBG() {
        mcurrentBG = findViewById(R.id.currentBG);
        mcurrentBG.setText("a");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.text);

        // Enables Always-on
        //setAmbientEnabled();
    }


}
