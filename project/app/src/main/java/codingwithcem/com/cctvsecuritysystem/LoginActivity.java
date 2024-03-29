package codingwithcem.com.cctvsecuritysystem;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity{

    private static final String TAG = "LoginActivity";
    private static LoginActivity instance;
    private String someVariable = "";
    private String imageUrl = "";
    //constants
    private static final int ERROR_DIALOG_REQUEST = 9001;

    //Firebase
    private FirebaseAuth.AuthStateListener mAuthListener;

    // widgets
    private EditText mEmail, mPassword;
    private ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mProgressBar = findViewById(R.id.progressBar);
        instance = this;

        setupFirebaseAuth();
        if(servicesOK()){
            init();
        }
        hideSoftKeyboard();

    }
    public static LoginActivity getInstance() {
        return instance;
    }
    private void init(){
        Button signIn = findViewById(R.id.email_sign_in_button);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //check if the fields are filled out
                if(!isEmpty(mEmail.getText().toString())
                        && !isEmpty(mPassword.getText().toString())){
                    Log.d(TAG, "onClick: attempting to authenticate.");

                    showDialog();

                    FirebaseAuth.getInstance().signInWithEmailAndPassword(mEmail.getText().toString(),
                            mPassword.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    hideDialog();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                            hideDialog();
                        }
                    });
                }else{
                    Toast.makeText(LoginActivity.this, "You didn't fill in all the fields.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        TextView register = findViewById(R.id.link_register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        TextView forgotPassword = findViewById(R.id.link_forgot_password);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ForgotPassword.class);
                startActivity(intent);
            }
        });
    }


    public boolean servicesOK(){
        Log.d(TAG, "servicesOK: Checking Google Services.");

        int isAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(LoginActivity.this);

        if(isAvailable == ConnectionResult.SUCCESS){
            //everything is ok and the user can make mapping requests
            Log.d(TAG, "servicesOK: Play Services is OK");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(isAvailable)){
            //an error occured, but it's resolvable
            Log.d(TAG, "servicesOK: an error occured, but it's resolvable.");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(LoginActivity.this, isAvailable, ERROR_DIALOG_REQUEST);
            dialog.show();
        }
        else{
            Toast.makeText(this, "Can't connect to mapping services", Toast.LENGTH_SHORT).show();
        }

        return false;
    }


    /**
     * Return true if the @param is null
     * @param string
     * @return
     */
    private boolean isEmpty(String string){
        return string.equals("");
    }


    private void showDialog(){
        mProgressBar.setVisibility(View.VISIBLE);

    }

    private void hideDialog(){
        if(mProgressBar.getVisibility() == View.VISIBLE){
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    /*
        ----------------------------- Firebase setup ---------------------------------
     */
    public String getSomeVariable() {
        return LoginActivity.this.someVariable;
    }
    public String getSomeURL() {
        return LoginActivity.this.imageUrl;
    }

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: started.");

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                // if something is written then get the information if the key is equal to "message"
                if(getIntent().getExtras()!=null) {
                    int cases = 0;
                    for (String key : getIntent().getExtras().keySet()) {
                        Log.d(TAG, "Key => " + key);
                        if (key.equals("message")) {
                            LoginActivity.this.someVariable = getIntent().getExtras().getString(key);
                            cases = 1;
                        } else if (key.equals("img_url")) {
                            LoginActivity.this.imageUrl = getIntent().getExtras().getString(key);
                            cases = 2;
                        } else if (key.equals("profile")) {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            Log.d(TAG, "key.equals = null");
                        }
                    }
// bu alttaki parantezi yorum yap(bildirim olayını yaparken)
//                }
                    // if something is written in cloudNotif. & user logged in, then call case-1 page
                    if ((user != null) && (cases == 1)) {
                        setContentView(R.layout.first_case);
                        Log.d(TAG, "PushNotification: Case-1");
                        Toast.makeText(LoginActivity.this, "Signed in", Toast.LENGTH_SHORT).show();
                        Intent i1 = new Intent(LoginActivity.this, FirstCase.class);
                        startActivity(i1);
                        finish();
                    }
                    else if ((user != null) && (cases == 2)) {
                        setContentView(R.layout.second_case);
                        Log.d(TAG, "PushNotification: Case-2");
                        Toast.makeText(LoginActivity.this, "Signed in", Toast.LENGTH_SHORT).show();
                        Intent i2 = new Intent(LoginActivity.this, SecondCase.class);
                        startActivity(i2);
                        finish();
                    }
// bu alttaki parantezi yorum yap normal çalıştırırken(bildirimsiz)
                }

                // case çalıştırırken bunu else if yap
                 else if (user != null) {

                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    Toast.makeText(LoginActivity.this, "Authenticated with: " + user.getEmail(), Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    //check for extras from FCM
                    if (getIntent().getExtras() != null) {
                        Log.d(TAG, "initFCM: found intent extras: " + getIntent().getExtras().toString());
                        for (String key : getIntent().getExtras().keySet()) {
                            Object value = getIntent().getExtras().get(key);
                            Log.d(TAG, "initFCM: Key: " + key + " Value: " + value);
                        }
                        String data = getIntent().getStringExtra("data");
                        Log.d(TAG, "initFCM: data: " + data);
                    }
                    startActivity(intent);
                    finish();


                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
        }
    }
}














