package codingwithcem.com.cctvsecuritysystem;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import java.util.ArrayList;
import codingwithcem.com.cctvsecuritysystem.models.User;
import codingwithcem.com.cctvsecuritysystem.utility.MessageDialog;
import codingwithcem.com.cctvsecuritysystem.utility.UserAdapter;

public class UserListActivity extends AppCompatActivity {

    private static final String TAG = "UserListActivity";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //widgets
    private RecyclerView mRecyclerView;

    //vars
    private ArrayList<User> mUsers;
    private UserAdapter mUserAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userlist);
        mRecyclerView = findViewById(R.id.recyclerView);

        setupUserList();
        getUserList();
        initFCM();


        //declare the database reference object. This is what we use to access the database.
        //NOTE: Unless you are signed in, this will not be useable.
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    toastMessage("Successfully signed out.");
                }
                // ...
            }
        };


    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


    /**
     * customizable toast
     * @param message Mesajla ilgili bir şey(bu türkçe açıklamayı warning verdiği için ben ekledim)
     */
    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }


    private void initFCM(){
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "initFCM: token: " + token);
        sendRegistrationToServer(token);

    }
    public void openMessageDialog(String userId){
        Log.d(TAG, "openMessageDialog: opening a dialog to send a new message");
        MessageDialog dialog = new MessageDialog();

        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.intent_user_id), userId);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), getString(R.string.dialog_message));
    }

    private void getUserList() throws NullPointerException{
        Log.d(TAG, "getUserList: getting a list of all users");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child(getString(R.string.dbnode_users));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    Log.d(TAG, "onDataChange: found a user: " + user.getName());
                    mUsers.add(user);
                }
                mUserAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        Log.d(TAG, "sendRegistrationToServer: sending token to server: " + token);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child(getString(R.string.dbnode_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(getString(R.string.field_messaging_token))
                .setValue(token);
    }

    private void setupUserList(){
        mUsers = new ArrayList<>();
        mUserAdapter = new UserAdapter(UserListActivity.this, mUsers);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mUserAdapter);
    }

    private void signOut(){
        Log.d(TAG, "signOut: signing out user");
        FirebaseAuth.getInstance().signOut();
        Log.d(TAG, "onAuthStateChanged:signed_out");
        Intent intent = new Intent(UserListActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.sign_out:
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}


















