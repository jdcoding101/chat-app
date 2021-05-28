package ph.umak.chatapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import de.hdodenhof.circleimageview.CircleImageView;
import ph.umak.chatapp.objects.UserModel;
import ph.umak.chatapp.vm.ProfileVM;

import static ph.umak.chatapp.helper.constants.USERS;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private TextView emailTextView, nameTextView;
    private EditText firstNameEditText, lastNameEditText;
    private CircleImageView profileImageView;
    private Button updateButton;

    private ProgressDialog dialog;

    private ProfileVM profileVM;

    public static final int GALLERYPICK = 200; // GALLERY PIC
    private Uri imageUri = null;
    private String imageUrl, userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = auth.getCurrentUser();
        updateUI(user);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            setUpView();

        }
    }

    private void setUpView() {
        profileImageView = findViewById(R.id.profileImageViewProfile);
        nameTextView = findViewById(R.id.nameTextViewProfile);
        emailTextView = findViewById(R.id.emailTextViewProfile);
        firstNameEditText = findViewById(R.id.firstNameEditTextProfile);
        lastNameEditText = findViewById(R.id.lastNameEditTextProfile);
        updateButton = findViewById(R.id.updateButtonProfile);

        dialog = new ProgressDialog(this);

        profileVM = new ViewModelProvider(this).get(ProfileVM.class);

        getUser();
    }

    @SuppressLint("SetTextI18n")
    private void getUser() {
        profileVM.getUser().observe(this, user -> {
            nameTextView.setText(user.getFirstName() + " " + user.getLastName());
            emailTextView.setText(user.getEmail());
            firstNameEditText.setText(user.getFirstName());
            lastNameEditText.setText(user.getLastName());

            if (!user.getImageUrl().equals("default"))
                Glide.with(this).load(user.getImageUrl()).into(profileImageView);

            updateButton.setOnClickListener(v -> {
                updateData(user);
            });

            profileImageView.setOnClickListener(v -> showImagePickDialog());
        });
    }

    private void showImagePickDialog() {
        String[] items = {"Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setItems(items, (dialog, i) -> {
            if (i == 0) {
                showGalleryDialogue();
            }
        });
        builder.create().show();
    }

    private void showGalleryDialogue() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERYPICK);
    }


    private void updateData(UserModel user) {
        dialog.show();
        dialog.setMessage("Updating data...");
        dialog.setCancelable(false);

        try {
            String firstName = firstNameEditText.getText().toString();
            String lastName = lastNameEditText.getText().toString();

            if (firstName.isEmpty() || lastName.isEmpty())
                throw new Exception("Fields must not be empty.");

            if ((firstName + "" + lastName).equalsIgnoreCase((user.getFirstName() + "" + user.getLastName())))
                throw new Exception("No changes has been made.");

            db.collection(USERS).document(user.getUid()).update("firstName", firstName, "lastName", lastName)
                    .addOnSuccessListener(result -> {
                        dialog.dismiss();
                        Toast.makeText(this, "Updated!", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(this::interrupt);

        } catch (Exception e) {
            interrupt(e);
        }
    }

    private void interrupt(Exception e) {
        dialog.dismiss();
        Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERYPICK && resultCode == RESULT_OK) {
            Uri uri = data.getData();

            String timestamp = String.valueOf(System.currentTimeMillis());
            String path = "Photos/" + auth.getCurrentUser().getUid()+ "/photos_" + timestamp;

            StorageReference storageRef = FirebaseStorage.getInstance().getReference(path);

            storageRef.putFile(uri).addOnSuccessListener(taskSnapshot -> {
                Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();

                task.addOnSuccessListener(uri1 -> {
                    String photoId = uri1.toString();
                    imageUri = uri1;

                    Glide.with(getApplicationContext()).load(imageUri).centerCrop().into(profileImageView);

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    userId = user.getUid();
                    db.collection("Users").document(userId).update("imageUrl", photoId).addOnSuccessListener(aVoid -> { });
                });
            });
        }

    }
}