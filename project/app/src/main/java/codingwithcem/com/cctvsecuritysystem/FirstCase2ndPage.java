package codingwithcem.com.cctvsecuritysystem;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class FirstCase2ndPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_case2ndpage);
        TextView Message = findViewById(R.id.text2);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString("key");
            //The key argument here must match that used in the other activity

            Message.setText(value);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    final Intent mainIntent = new Intent(FirstCase2ndPage.this, MainActivity.class);
                    FirstCase2ndPage.this.startActivity(mainIntent);
                    FirstCase2ndPage.this.finish();
                }
            }, 1500);

        }

    }
}
