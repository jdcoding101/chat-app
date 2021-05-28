package ph.umak.chatapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import static ph.umak.chatapp.helper.constants.USERS;

public class UpdateEmailActivity extends AppCompatActivity {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private TextView currentEmailTextView;
    private EditText newEmailEditText, confirmPasswordEditText;
    private Button updateButton;

    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = auth.getCurrentUser();
        updateUI(user);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            setUpView(user);
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @SuppressLint("SetTextI18n")
    private void setUpView(FirebaseUser user) {
        currentEmailTextView = findViewById(R.id.currentEmailTextViewUpdateEmail);
        newEmailEditText = findViewById(R.id.newEmailEditTextUpdateEmail);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditTextUpdateEmail);
        updateButton = findViewById(R.id.updateButtonUpdateEmail);

        dialog = new ProgressDialog(this);

        currentEmailTextView.setText("Current email: " + user.getEmail());

        updateButton.setOnClickListener(v -> {
            dialog.show();
            dialog.setMessage("Please wait...");
            dialog.setCancelable(false);

            try {
                String newEmail = newEmailEditText.getText().toString();
                String password = confirmPasswordEditText.getText().toString();

                if (newEmail.isEmpty() || password.isEmpty())
                    throw new Exception("All fields are required.");

                if (password.length() < 6)
                    throw new Exception("Password must contain at least six (6) characters.");

                if (newEmail.equalsIgnoreCase(user.getEmail()))
                    throw new Exception("New email must not be equal to current email.");

                updateEmail(user, password, newEmail);

            } catch (Exception e) {
                interrupt(e);
            }
        });

    }

    private void updateEmail(FirebaseUser user, String password, String newEmail) {
        AuthCredential credential = EmailAuthProvider
                .getCredential(Objects.requireNonNull(user.getEmail()), password);

        user.reauthenticate(credential)
                .addOnSuccessListener(task -> user.updateEmail(newEmail)
                        // re authenticated
                        .addOnSuccessListener(task1 -> {
                            // user email updated
                            db.collection(USERS).document(user.getUid()).update("email", newEmail).addOnSuccessListener(task2 -> {
                                // user data updated
                                user.sendEmailVerification()
                                        .addOnSuccessListener(task4 -> {
                                            // link sent
                                            Toast.makeText(this, "Email verification link is sent to " + newEmail, Toast.LENGTH_LONG).show();
                                            finish();
                                        }).addOnFailureListener(this::interrupt);
                            }).addOnFailureListener(this::interrupt);
                        })
                        .addOnFailureListener(this::interrupt)).addOnFailureListener(this::interrupt);
    }

    private void interrupt(Exception e) {
        dialog.dismiss();
        Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    }
}