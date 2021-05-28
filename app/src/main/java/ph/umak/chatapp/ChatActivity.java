package ph.umak.chatapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import de.hdodenhof.circleimageview.CircleImageView;
import ph.umak.chatapp.adapters.MessageAdapter;
import ph.umak.chatapp.vm.MessageVM;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static ph.umak.chatapp.helper.constants.*;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    RecyclerView chatsRecyclerView;
    CircleImageView profileImageView;
    TextView nameTextView, emailTextView;
    EditText messageEditText;
    ImageView sendImageView;

    ProgressDialog dialog;

    MessageAdapter messageAdapter;
    MessageVM messageVM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

    }

    @Override
    public void onStop() {
        super.onStop();
        messageVM.resetAll();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = auth.getCurrentUser();
        updateUI(user);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            if (getIntent().getExtras() != null) {
                setUpView(user);
            } else {
                Toast.makeText(this, "No friend has been found.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void setUpView(FirebaseUser user) {
        chatsRecyclerView = findViewById(R.id.chatsRecyclerViewChat);
        profileImageView = findViewById(R.id.profileImageViewChat);
        nameTextView = findViewById(R.id.nameTextViewChat);
        emailTextView = findViewById(R.id.emailTextViewChat);
        messageEditText = findViewById(R.id.messageEditTextChat);
        sendImageView = findViewById(R.id.sendImageViewChat);

        dialog = new ProgressDialog(this);

        messageAdapter = new MessageAdapter();

        messageVM = new ViewModelProvider(this).get(MessageVM.class);

        getIntentData(user);
    }

    private void getIntentData(FirebaseUser user) {
        Intent intent = getIntent();

        String friendUid = intent.getStringExtra(UID);
        String friendEmail = intent.getStringExtra(EMAIL);
        String friendName = intent.getStringExtra(NAME);
        String friendImageUrl = intent.getStringExtra(IMAGE_URL);

        displayData(friendEmail, friendName, friendImageUrl);

        messageVM.getMessageFromDB(user.getUid(), friendUid);
        messageVM.messages().observe(this, messages -> {
            messageAdapter.setMessages(messages, user.getUid(), friendUid, user.getEmail(), friendEmail);
            chatsRecyclerView.setAdapter(messageAdapter);
        });

        sendImageView.setOnClickListener(v -> {
            sendMessage(user, friendUid);
        });

    }

    private void sendMessage(FirebaseUser user, String friendUid) {
        dialog.show();
        dialog.setMessage("Sending message...");
        dialog.setCancelable(false);

        try {
            String message = messageEditText.getText().toString();

            Log.d(TAG, "Message: " + message);

            if (message.isEmpty())
                throw new Exception("Message must not be empty");

            HashMap<String, Object> data = new HashMap<>();

            data.put("sender", user.getUid());
            data.put("receiver", friendUid);
            data.put("message", message);
            data.put("time", getCurrentTime());

            db.collection("Messages").document(getCurrentTime()).set(data).addOnSuccessListener(task -> {
                dialog.dismiss();
                messageEditText.setText("");
                messageEditText.requestFocus();
                Toast.makeText(this, "Sent!", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(this::interrupt);

        } catch (Exception e) {
            interrupt(e);
        }
    }

    private String getCurrentTime() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter= new SimpleDateFormat( "HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }

    private void interrupt(Exception e) {
        dialog.dismiss();
        Toast.makeText(this, "Failed to send message.\n" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("SetTextI18n")
    private void displayData(String email, String name, String imageUrl) {

        nameTextView.setText(name);
        emailTextView.setText(email);

        if (!imageUrl.equals("default"))
            Glide.with(this).load(imageUrl).into(profileImageView);
    }



}