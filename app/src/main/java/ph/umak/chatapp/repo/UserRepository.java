package ph.umak.chatapp.repo;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import ph.umak.chatapp.objects.UserModel;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    OnAvailableUsers usersInterface;

    List<UserModel> users = new ArrayList<>();

    public UserRepository(OnAvailableUsers usersInterface) {
        this.usersInterface = usersInterface;
    }

    public void getUsers() {
        String uid = auth.getCurrentUser().getUid();

        db.collection("Users").addSnapshotListener((value, error) -> {
            users.clear();

            assert value != null;
            for (DocumentSnapshot ds: value.getDocuments()) {

                UserModel data = ds.toObject(UserModel.class);

                assert data != null;
                if (!uid.equals(data.getUid())) {
                    users.add(data);
                    usersInterface.usersFromDB(users);
                }
            }
        });
    }

    public interface OnAvailableUsers {
        void usersFromDB(List<UserModel> users);
    }
}
