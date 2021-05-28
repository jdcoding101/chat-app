package ph.umak.chatapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import ph.umak.chatapp.adapters.UserAdapter;
import ph.umak.chatapp.objects.UserModel;
import ph.umak.chatapp.vm.UserVM;

import java.util.List;
import java.util.Objects;

import static ph.umak.chatapp.helper.constants.*;

public class UserActivity extends AppCompatActivity implements UserAdapter.OnUserClicked {

    private static final String TAG = "UserActivity";

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private RecyclerView friendsRecyclerView;
    private TextView userNoTextView;
    private Toolbar toolbar;

    private ProgressDialog dialog;

    private UserAdapter userAdapter;

    private UserVM userVM;

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            dialog = new ProgressDialog(this);

            dialog.show();
            dialog.setMessage("Please wait...");
            dialog.setCancelable(false);

            try {
                db.collection(USERS).document(user.getUid()).get().addOnSuccessListener(t-> {
                    if (!t.exists()) {
                        interrupt(new Exception("No data has been found."));
                        return;
                    }

                    UserModel data = t.toObject(UserModel.class);
                    if (data != null) {
                        if (!data.getVerified() && !user.isEmailVerified()) {
                            Intent intent = new Intent(this, EmailVerificationActivity.class);
                            startActivity(intent);
                            finish();
                            return;
                        }

                        dialog.dismiss();
                        setUpView(user, data);
                    }
                }).addOnFailureListener(this::interrupt);
            } catch (Exception e) {
                interrupt(e);
            }

        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void interrupt(Exception e) {
        dialog.dismiss();
        Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    }

    private void setUpView(FirebaseUser user, UserModel data) {
        friendsRecyclerView = findViewById(R.id.friendsRecyclerViewHome);
        userNoTextView = findViewById(R.id.userNoTextViewUser);
        toolbar = findViewById(R.id.toolbarHome);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        userAdapter = new UserAdapter(this);

        userVM = new ViewModelProvider(this).get(UserVM.class);

        setUpData(user, data);
    }

    @SuppressLint("SetTextI18n")
    private void setUpData(FirebaseUser user, UserModel data) {
        getUsers();
    }

    @SuppressLint("SetTextI18n")
    private void getUsers() {
        userVM.getUsers().observe(this, users -> {
            if (users.size() != 0) {
                if (users.size() > 1)
                    userNoTextView.setText(users.size() + " Users");
                else
                    userNoTextView.setText(users.size() + " User");
            }

            userAdapter.setUsers(users);
            friendsRecyclerView.setAdapter(userAdapter);
        });
    }

    @Override
    public void clickUser(int position, List<UserModel> users) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(UID, users.get(position).getUid());
        intent.putExtra(EMAIL, users.get(position).getEmail());
        intent.putExtra(NAME, users.get(position).getFirstName() + " " + users.get(position).getLastName());
        intent.putExtra(IMAGE_URL, users.get(position).getImageUrl());
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                dialog.show();
                dialog.setMessage("Logging out...");
                dialog.setCancelable(false);

                db.collection("Users").document(Objects.requireNonNull(auth.getCurrentUser()).getUid()).update("online", false).addOnCompleteListener(task -> {
                    auth.signOut();
                    onStart();
                });
                return true;
            case R.id.profile:
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = auth.getCurrentUser();
        updateUI(user);
    }
}