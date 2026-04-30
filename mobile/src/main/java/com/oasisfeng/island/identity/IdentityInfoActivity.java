package com.oasisfeng.island.identity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Process;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.oasisfeng.island.util.FakeIdentityManager;

public class IdentityInfoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scrollView = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        TextView title = new TextView(this);
        title.setText("Island Fake Identity Info");
        title.setTextSize(24);
        title.setPadding(0, 0, 0, 32);
        layout.addView(title);

        String[] keys = new String[]{
                FakeIdentityManager.KEY_ANDROID_ID,
                FakeIdentityManager.KEY_SERIAL,
                FakeIdentityManager.KEY_MODEL,
                FakeIdentityManager.KEY_MANUFACTURER,
                FakeIdentityManager.KEY_FINGERPRINT,
                FakeIdentityManager.KEY_IMEI,
                FakeIdentityManager.KEY_SIM_SERIAL_NUMBER,
                FakeIdentityManager.KEY_MAC_ADDRESS
        };

        for (String key : keys) {
            TextView textView = new TextView(this);
            String value = FakeIdentityManager.getFakeIdentityValue(this, Process.myUserHandle(), key);
            textView.setText(key + ": " + (value != null ? value : "Not Set"));
            textView.setTextSize(16);
            textView.setPadding(0, 8, 0, 8);
            layout.addView(textView);
        }

        scrollView.addView(layout);
        setContentView(scrollView);
    }
}
