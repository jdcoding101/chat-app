package ph.umak.chatapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private EditText emailEditText;
    private Button sendButton;

    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailEditText = findViewById(R.id.emailEditTextForgotPassword);
        sendButton = findViewById(R.id.sendButtonForgotPassword);

        dialog = new ProgressDialog(this);

        sendButton.setOnClickListener(v->{
            dialog.show();
            dialog.setMessage("Please wait...");
            dialog.setCancelable(false);

            try {
                String email = emailEditText.getText().toString();

                if (email.isEmpty())
                    throw new Exception("Email is required.");

                auth.sendPasswordResetEmail(email)
                        .addOnSuccessListener(task->{
                            interrupt(new Exception("Sent!"));
                            emailEditText.setText("");
                            finish();
                        }).addOnFailureListener(this::interrupt);

            } catch (Exception e) {
                interrupt(e);
            }
        });
    }

    private void interrupt(Exception e) {
        dialog.dismiss();
        Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    }
}