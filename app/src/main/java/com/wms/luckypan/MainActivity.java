package com.wms.luckypan;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private ImageView mStartBtn;
    private LuckyPan mLuckypan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStartBtn = (ImageView) findViewById(R.id.start);
        mLuckypan = (LuckyPan) findViewById(R.id.luckypan);
        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mLuckypan.isStart()) {
                    mLuckypan.start();
                    mStartBtn.setImageResource(R.drawable.stop);
                } else {
                    if (!mLuckypan.isShouldEnd()) {
                        mLuckypan.stop();
                        mStartBtn.setImageResource(R.drawable.start);
                    }
                }
            }
        });
    }
}
