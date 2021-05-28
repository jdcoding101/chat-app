package ph.umak.chatapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import ph.umak.chatapp.objects.UserModel;

import java.util.Objects;

import static ph.umak.chatapp.helper.constants.USERS;

public class EmailVerificationActivity extends AppCompatActivity {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Toolbar toolbar;
    private TextView statusTextView, reloadTextView, resendTextView, captionTextView;

    private ProgressDialog dialog;

    private Boolean isReload = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);
    }

    @Override
    protected void onStart() {
        super.onStart();
        dialog = new ProgressDialog(this);

        FirebaseUser user = auth.getCurrentUser();
        updateUI(user);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            auth.getCurrentUser().reload();

            new CountDownTimer(5000, 1000) {
                public void onTick(long millisUntilFinished) {
                    dialog.show();
                    dialog.setMessage("Please wait...");
                    dialog.setCancelable(false);
                }

                public void onFinish() {
                   getUser(user);
                }
            }.start();

        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void getUser(FirebaseUser user) {
        db.collection(USERS).document(user.getUid()).get().addOnCompleteListener(result-> {
            if (result.isSuccessful()) {
                UserModel data = result.getResult().toObject(UserModel.class);
                if (data.getVerified() && user.isEmailVerified()) goToUserActivity();
                else {
                    if (!user.isEmailVerified()) {
                        if (isReload) {
                            Toast.makeText(this, "No changes detected.", Toast.LENGTH_SHORT).show();
                            isReload = false;
                        }
                        dialog.dismiss();
                        setUpView(user);
                        return;
                    }
                    updateData(user);
                }
                return;
            }
            interrupt(new Exception("Failed to fetch data."));
        });
    }

    private void updateData(FirebaseUser user) {
        db.collection(USERS).document(user.getUid()).update("verified", true, "online", true, "email", user.getEmail()).addOnSuccessListener(task-> {
            Toast.makeText(getApplicationContext(), "Verified!", Toast.LENGTH_SHORT).show();
            goToUserActivity();
        }).addOnFailureListener(e -> interrupt(e));
    }

    private void goToUserActivity() {
        Intent intent = new Intent(this, UserActivity.class);
        startActivity(intent);
        finish();
    }

    @SuppressLint("SetTextI18n")
    private void setUpView(FirebaseUser user) {
        captionTextView = findViewById(R.id.captionTextViewEmailVerification);
        statusTextView = findViewById(R.id.statusTextViewEmailVerification);
        resendTextView = findViewById(R.id.resendTextViewEmailVerification);
        reloadTextView = findViewById(R.id.reloadTextViewEmailVerification);

        toolbar = findViewById(R.id.toolbarEmailVerification);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");

        captionTextView.setText(
                "Hi New User! Thank you for signing up to Chat App. Verify your email address to complete your registration. We sent an email verification link to " +
                        user.getEmail()
        );

        if (user.isEmailVerified())
            statusTextView.setText("status: email verified");

        resendTextView.setOnClickListener(v -> resendEmailVerificationLink());

        reloadTextView.setOnClickListener(v -> {
            isReload = true;
            onStart();
        });

    }

    private void resendEmailVerificationLink() {
        auth.getCurrentUser().reload();
        new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                dialog.show();
                dialog.setMessage("Please wait...");
                dialog.setCancelable(false);
            }

            @Override
            public void onFinish() {
                if (!auth.getCurrentUser().isEmailVerified()) {
                    auth.getCurrentUser().sendEmailVerification()
                            .addOnCompleteListener(task -> {
                                String msg = "Sent!";
                                if (!task.isSuccessful()) {
                                  msg = "Failed to resend link. Please try again.";
                                }
                                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            });
                    return;
                }
                interrupt(new Exception("Failed to resend link. Email is already verified."));
                onStart();
            }
        }.start();

    }

    private void changeEmail() {
        auth.getCurrentUser().reload();

        new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                dialog.show();
                dialog.setMessage("Checking email...");
                dialog.setCancelable(false);
            }

            @Override
            public void onFinish() {
                if (!auth.getCurrentUser().isEmailVerified()) {
                    dialog.dismiss();
                    Intent intent = new Intent(getApplicationContext(), UpdateEmailActivity.class);
                    startActivity(intent);
                    return;
                }
                interrupt(new Exception("Email cannot be changed because email is already verified."));
                onStart();
            }
        }.start();
    }

    private void deleteAccount(FirebaseUser user) {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    db.collection(USERS).document(user.getUid()).delete().addOnSuccessListener(task -> {
                        user.delete().addOnSuccessListener(task1 -> {
                            Toast.makeText(this, "Deleted!", Toast.LENGTH_SHORT).show();
                            onStart();
                        }).addOnFailureListener(e -> interrupt(e, dialog));
                    }).addOnFailureListener(e -> interrupt(e, dialog));
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    dialog.dismiss();
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you user you want to delete your account?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show();
    }

    private void interrupt(Exception e) {
        dialog.dismiss();
        Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    }

    private void interrupt(Exception e, DialogInterface dialog) {
        Toast.makeText(this, "Failed to delete account.\n " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.email_verification_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        FirebaseUser user = auth.getCurrentUser();
        switch (item.getItemId()) {
            case R.id.delete_account:
                deleteAccount(user);
                return true;
            case R.id.change_email:
                changeEmail();
                return true;
            case R.id.logout:
                dialog.show();
                dialog.setMessage("Logging out...");
                dialog.setCancelable(false);

                auth.signOut();
                onStart();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}