package ph.umak.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import static ph.umak.chatapp.helper.constants.USERS;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private EditText email, password;
    private Button login;
    private TextView forgotPassword, signUp;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setUpView();

        login.setOnClickListener(v -> {
            login();
        });

        forgotPassword.setOnClickListener(v -> {
            forgotPassword();
        });

        signUp.setOnClickListener(v -> {
            signUp();
        });
    }

    private void login() {
        Log.d(TAG, "Login is clicked");

        try {
            if (email.getText().toString().isEmpty() || password.getText().toString().isEmpty())
                throw new Exception("All fields are required");

            if (password.getText().toString().length() < 6)
                throw new Exception("Password must contain at least six (6) characters.");

            dialog.show();
            dialog.setMessage("Signing in...");
            dialog.setCancelable(false);

            auth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnSuccessListener(result -> {
                if (result.getUser() != null) {

                    FirebaseUser user = result.getUser();

                    db.collection(USERS).document(user.getUid()).update("online", true, "email", user.getEmail()).addOnSuccessListener(result1 -> {
                        if (user.isEmailVerified()) {
                            db.collection(USERS).document(user.getUid()).update("verified", true);
                            updateUI(user);
                        } else {
                            updateUI(user);
                        }

                    });
                }

            }).addOnFailureListener(this::interrupt);


        } catch (Exception e) {
            interrupt(e);
        }
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, UserActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void interrupt(Exception e) {
        dialog.dismiss();
        Toast.makeText(this, "Failed to sign in.\n " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    }

    private void forgotPassword() {
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    private void signUp() {
        Log.d(TAG, "Sign up is clicked");

        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void setUpView() {
        email = findViewById(R.id.emailEditTextLogin);
        password = findViewById(R.id.passwordEditTextLogin);

        login = findViewById(R.id.loginButtonLogin);
        forgotPassword = findViewById(R.id.forgotPasswordTextViewLogin);
        signUp = findViewById(R.id.signUpTextViewLogin);

        dialog = new ProgressDialog(this);
    }
}