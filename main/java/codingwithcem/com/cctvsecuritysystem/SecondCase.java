package codingwithcem.com.cctvsecuritysystem;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;

public class SecondCase extends AppCompatActivity {
    Button circle2;
    Button circle3;
    private static final String TAG = "SecondCase";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // the screen that will show up when opening the program(saved screen onCreate)
        setContentView(R.layout.second_case);
        String myUrl = LoginActivity.getInstance().getSomeURL();

        ImageView Image = findViewById(R.id.imageView);

        Picasso.get().load(myUrl).resize(1920, 1088).into(Image);

        circle2 = findViewById(R.id.button2); // find the "button"'s id and read it
        circle2.setOnClickListener(new View.OnClickListener() {
            // go to secondActivity when clicking the button
            @Override
            public void onClick(View v) {

                goTo2ndCaseRejectPage();
            }
        });

        circle3 = findViewById(R.id.button3); // find the "button"'s id and read it
        circle3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                goTo2ndCaseAcceptPage();
            }
        });
    }

    private void goTo2ndCaseAcceptPage() {
        String value="Entrance Accepted";
        Intent i4 = new Intent(this, FirstCase2ndPage.class);
        i4.putExtra("key",value);
        Log.d(TAG, "text: "+value);
        startActivity(i4);
    }
    private void goTo2ndCaseRejectPage() {
        String value="Entrance Rejected";
        Intent i5 = new Intent(this, FirstCase2ndPage.class);
        i5.putExtra("key",value);
        Log.d(TAG, "text: "+value);
        startActivity(i5);
    }
}