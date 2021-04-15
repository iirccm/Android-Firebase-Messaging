package codingwithcem.com.cctvsecuritysystem;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private static final String TAG = "ForgotPasswordActivity";
    private ProgressBar mProgressBar;
    private EditText mEmail;

    String check = "Error";

    //firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);
        mProgressBar = findViewById(R.id.progressBarPassword);
        mEmail = findViewById(R.id.email);

        mAuth = FirebaseAuth.getInstance();

        TextView mSend = findViewById(R.id.link_send);
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
                mAuth.sendPasswordResetEmail(mEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mProgressBar.setVisibility(View.GONE);
                        if(task.isSuccessful()){
                            Toast.makeText(ForgotPassword.this,
                                    "Password request is sent", Toast.LENGTH_SHORT).show();
                            redirectLoginScreen();

                        } else {
                            Toast.makeText(ForgotPassword.this,
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
//                    hideDialog();
                });

                Log.d(TAG, "onClick: attempting to reset password.");

                //check for null valued EditText fields
                if(!isEmpty(mEmail.getText().toString())){
                    Log.d(TAG, "ForgotPassword email: "+check);
                }else{
                    Toast.makeText(ForgotPassword.this, "You must enter your email", Toast.LENGTH_SHORT).show();
                }
            }
        });

        hideSoftKeyboard();


    }

    /**
     * Redirects the user to the login screen
     */
    private void redirectLoginScreen(){
        Log.d(TAG, "redirectLoginScreen: redirecting to login screen.");

        Intent intent = new Intent(ForgotPassword.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Return true if the @param is null
     * @param string string türünde bir şey(incele, bak)
     * @return nereye dönüyor? incele, bak
     */
    private boolean isEmpty(String string){
        return string.equals("");
    }


    private void showDialog(){
        mProgressBar.setVisibility(View.VISIBLE);

    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}
