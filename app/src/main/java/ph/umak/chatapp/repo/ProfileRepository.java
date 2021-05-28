package ph.umak.chatapp.repo;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import ph.umak.chatapp.objects.UserModel;

public class ProfileRepository {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    UserModel user = new UserModel();

    OnUser userInterface;

    public ProfileRepository(OnUser userInterface) {
        this.userInterface = userInterface;
    }

    public void getUser() {
        db.collection("Users").document(auth.getCurrentUser().getUid()).addSnapshotListener((value, error) -> {
            UserModel data = value.toObject(UserModel.class);

            assert data != null;

            user.setUid(data.getUid());
            user.setEmail(data.getEmail());
            user.setFirstName(data.getFirstName());
            user.setLastName(data.getLastName());
            user.setImageUrl(data.getImageUrl());
            user.setOnline(data.getOnline());

            userInterface.userFromDB(user);

        });
    }

    public interface OnUser {
        void userFromDB(UserModel user);
    }
}
