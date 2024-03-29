package codingwithcem.com.cctvsecuritysystem;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.ArrayList;
import codingwithcem.com.cctvsecuritysystem.models.Note;

/**
 * Created by User on 5/14/2018.
 */

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        IMainActivity,
        SwipeRefreshLayout.OnRefreshListener
{

    private static final String TAG = "MainActivity";
    private static final int CAMERA_REQUEST_CODE = 1;

    private StorageReference mStorage;
    private ProgressDialog mProgress;
    public FloatingActionButton contacts;

    //Firebase
    private FirebaseAuth.AuthStateListener mAuthListener;


    //widgets
    private FloatingActionButton mFab, mFab2;
    private ImageView mImageView;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    //vars
    private View mParentLayout;
    private ArrayList<Note> mNotes = new ArrayList<>();
    private NoteRecyclerViewAdapter mNoteRecyclerViewAdapter;
    private DocumentSnapshot mLastQueriedDocument;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStorage = FirebaseStorage.getInstance().getReference();

        mFab = findViewById(R.id.fab);
        mFab2 = findViewById(R.id.fab2);
        mImageView = findViewById(R.id.imageView);
        mProgress = new ProgressDialog(this);

        mParentLayout = findViewById(android.R.id.content);
        mRecyclerView = findViewById(R.id.recycler_view);
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        mFab.setOnClickListener(this);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mFab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            }
        });

        contacts = findViewById(R.id.fab3);
        contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickUsersIcon(v);
            }
        });

        setupFirebaseAuth();
        initRecyclerView();
        getNotes();
    }

    public void clickUsersIcon(View v) {
        Intent intent = new Intent(this, UserListActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){

            mProgress.setMessage("Uploading Image ...");
            mProgress.show();

            Uri uri = data.getData();

            StorageReference filepath = mStorage.child("Photos").child(uri.getLastPathSegment());

            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    mProgress.dismiss();
                    Toast.makeText(MainActivity.this, "Uploading Finished...", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void deleteNote(final Note note){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference noteRef = db
                .collection("notes")
                .document(note.getNote_id());

        noteRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    makeSnackBarMessage("Deleted note");
                    mNoteRecyclerViewAdapter.removeNote(note);
                }
                else{
                    makeSnackBarMessage("Failed. Check log.");
                }
            }
        });
    }

    @Override
    public void onRefresh() {
        getNotes();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    CollectionReference notesCollectionRef = db
            .collection("notes");

    private void getNotes(){
        Query notesQuery = null;
        if(mLastQueriedDocument != null){
           notesQuery = notesCollectionRef
                    .whereEqualTo("user_id", FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .startAfter(mLastQueriedDocument);
        }
        else{
            notesQuery = notesCollectionRef
                    .whereEqualTo("user_id", FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .orderBy("timestamp", Query.Direction.ASCENDING);
        }

        notesQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){

                    for(QueryDocumentSnapshot document: task.getResult()){
                        Note note = document.toObject(Note.class);
                        mNotes.add(note);
//                        Log.d(TAG, "onComplete: got a new note. Position: " + (mNotes.size() - 1));
                    }

                    if(task.getResult().size() != 0){
                        mLastQueriedDocument = task.getResult().getDocuments()
                                .get(task.getResult().size() -1);
                    }

                    mNoteRecyclerViewAdapter.notifyDataSetChanged();
                }
                else{
                    makeSnackBarMessage("Query Failed. Check Logs.");
                }
            }
        });
    }

    private void initRecyclerView(){
        if(mNoteRecyclerViewAdapter == null){
            mNoteRecyclerViewAdapter = new NoteRecyclerViewAdapter(this, mNotes);
        }
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mNoteRecyclerViewAdapter);
    }

    @Override
    public void updateNote(final Note note){

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference noteRef = db
                .collection("notes")
                .document(note.getNote_id());

        noteRef.update(
                "title", note.getTitle(),
                "content", note.getContent()
        ).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    makeSnackBarMessage("Updated note");
                    mNoteRecyclerViewAdapter.updateNote(note);
                }
                else{
                    makeSnackBarMessage("Failed. Check log.");
                }
            }
        });
    }

    @Override
    public void onNoteSelected(Note note) {
        ViewNoteDialog dialog = ViewNoteDialog.newInstance(note);
        dialog.show(getSupportFragmentManager(), getString(R.string.dialog_view_note));
    }

    @Override
    public void createNewNote(String title, String content) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference newNoteRef = db
                .collection("notes")
                .document();

        Note note = new Note();
        note.setTitle(title);
        note.setContent(content);
        note.setNote_id(newNoteRef.getId());
        note.setUser_id(userId);

        newNoteRef.set(note).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    makeSnackBarMessage("Created new note");
                    getNotes();
                }
                else{
                    makeSnackBarMessage("Failed. Check log.");
                }
            }
        });
    }

    private void makeSnackBarMessage(String message){
        Snackbar.make(mParentLayout, message, Snackbar.LENGTH_SHORT).show();
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.fab:{
                //create a new note
                NewNoteDialog dialog = new NewNoteDialog();
                dialog.show(getSupportFragmentManager(), getString(R.string.dialog_new_note));
                break;
            }
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.optionSignOut:
                signOut();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void signOut(){
        Log.d(TAG, "signOut: signing out");
        FirebaseAuth.getInstance().signOut();
    }

    /*
            ----------------------------- Firebase setup ---------------------------------
         */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: started.");

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
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










