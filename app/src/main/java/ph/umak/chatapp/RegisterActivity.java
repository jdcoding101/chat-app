package ph.umak.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;

import static ph.umak.chatapp.helper.constants.USERS;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Button signUp;
    private TextView login;
    private ProgressDialog dialog;
    private EditText firstNameEditText, lastNameEditText, emailEditText, confirmPasswordEditText, passwordEditText;

    private String firstName, lastName, email, confirmPassword, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setUpView();

        login.setOnClickListener(v -> {
            login();
        });

        signUp.setOnClickListener(v -> signUp());

    }

    private void signUp() {
        Log.d(TAG, "Sign up is clicked");

        dialog.show();
        dialog.setMessage("Loading...");
        dialog.setCancelable(false);

        try {

            firstName = firstNameEditText.getText().toString();
            lastName = lastNameEditText.getText().toString();
            email = emailEditText.getText().toString();
            password = passwordEditText.getText().toString();
            confirmPassword = confirmPasswordEditText.getText().toString();

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                throw new Exception("All fields are required.");
            }

            if (password.length() < 6) {
                throw new Exception("Password must contain at least six (6) characters.");
            }

            if (!confirmPassword.equals(password)) {
                throw new Exception("Password and confirm password must match.");
            }

            auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(result -> {
                if (result.getUser() != null) {

                    FirebaseUser user = result.getUser();

                    HashMap<String, Object> data = new HashMap<>();
                    data.put("uid", user.getUid());
                    data.put("email", email);
                    data.put("firstName", firstName);
                    data.put("lastName", lastName);
                    data.put("imageUrl", "default");
                    data.put("online", false);
                    data.put("verified", false);

                    db.collection(USERS).document(user.getUid()).set(data).addOnSuccessListener(result1 -> {
                        Objects.requireNonNull(result.getUser()).sendEmailVerification().addOnSuccessListener(task -> {
                            Intent intent = new Intent(this, EmailVerificationActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }).addOnFailureListener(this::interrupt);
                    }).addOnFailureListener(this::interrupt);
                }
            }).addOnFailureListener(this::interrupt);
        } catch (Exception e) {
            interrupt(e);
        }

    }

    private void interrupt(Exception e) {
        Toast.makeText(this, "Failed to sign up. \n" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    private void login() {
        Log.d(TAG, "Login is clicked");
        finish();
    }

    private void setUpView() {
        firstNameEditText = findViewById(R.id.fistNameEditTextRegister);
        lastNameEditText = findViewById(R.id.lastNameEditTextRegister);
        emailEditText = findViewById(R.id.emailEditTextRegister);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditTextRegister);
        passwordEditText = findViewById(R.id.passwordEditTextRegister);
        signUp = findViewById(R.id.signUpButtonRegister);
        login = findViewById(R.id.loginTextViewRegister);

        dialog = new ProgressDialog(this);
    }
}