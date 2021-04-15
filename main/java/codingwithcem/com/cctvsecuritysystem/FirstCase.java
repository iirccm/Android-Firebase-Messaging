package codingwithcem.com.cctvsecuritysystem;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import codingwithcem.com.cctvsecuritysystem.models.Note;

public class FirstCase extends AppCompatActivity {
    // determine id information
    Button circle;
    public TextView Message;
    private FirebaseFirestore mFirestore;
 private static final String TAG = "FirstCase";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // the screen that will show up when opening the program(saved screen onCreate)
        setContentView(R.layout.first_case);
        Message = findViewById(R.id.textView);
        String Label = LoginActivity.getInstance().getSomeVariable();
        Message.setText(Label);
        Log.d(TAG, "text: "+Message.getText());

        mFirestore = FirebaseFirestore.getInstance();


        circle = findViewById(R.id.button); // find the "button"'s id and read it
        circle.setOnClickListener(new View.OnClickListener() {
            // go to secondActivity when clicking the button
            @Override
            public void onClick(View v) {

                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DocumentReference uidTime = mFirestore
                        .collection("red_alert")
                        .document();

                Note note = new Note();
                note.setUser_id(userId);

                uidTime.set(note).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "Kimlik ve zaman eklendi");
                            Toast.makeText(FirstCase.this,"Security informed", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Log.d(TAG, "Uyarı!! Kimlik ve zaman eklenemedi!");
                            Toast.makeText(FirstCase.this, "Error! Couldn't access. ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                goToFirstCase2ndPage();
            }
        });
    }

    private void goToFirstCase2ndPage() {
        String value="The security has been warned!";
        Intent i3 = new Intent(this, FirstCase2ndPage.class);
        i3.putExtra("key",value);
        startActivity(i3);
    }

}