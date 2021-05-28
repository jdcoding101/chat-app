package ph.umak.chatapp.repo;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import ph.umak.chatapp.objects.MessageModel;

import java.util.ArrayList;
import java.util.List;

import static ph.umak.chatapp.helper.constants.MESSAGES;

public class MessageRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private List<MessageModel> messages = new ArrayList<>();

    OnMessageAdded messagesInterface;

    public MessageRepository(OnMessageAdded messagesInterface) {
        this.messagesInterface = messagesInterface;
    }

    public void getMessages(String currentEmail, String friendEmail) {
            db.collection(MESSAGES).addSnapshotListener((value, error) -> {
                messages.clear();
                for (DocumentSnapshot ds: value.getDocuments()) {
                    MessageModel data = ds.toObject(MessageModel.class);

                    if (data.getSender().equals(currentEmail) && data.getReceiver().equals(friendEmail) ||
                            data.getReceiver().equals(currentEmail) && data.getSender().equals(friendEmail)) {
                        messages.add(data);
                        messagesInterface.messagesFromDB(messages);
                    }
                }
            });
    }

    public interface OnMessageAdded{
        void messagesFromDB(List<MessageModel> messages);
    }
}