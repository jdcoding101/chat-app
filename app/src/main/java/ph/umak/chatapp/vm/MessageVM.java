package ph.umak.chatapp.vm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import ph.umak.chatapp.objects.MessageModel;
import ph.umak.chatapp.repo.MessageRepository;

import java.util.List;

public class MessageVM extends ViewModel implements MessageRepository.OnMessageAdded {
    MutableLiveData<List<MessageModel>> mutableLiveData = new MutableLiveData<>();
    MessageRepository repo = new MessageRepository(this);

    public MessageVM() {}

    public void getMessageFromDB(String currentEmail, String friendId) {
        repo.getMessages(currentEmail, friendId);
    }

    public void resetAll() {
        mutableLiveData.postValue(null);
    }

    public LiveData<List<MessageModel>> messages() {
        return  mutableLiveData;
    }

    @Override
    public void messagesFromDB(List<MessageModel> messages) {
        mutableLiveData.setValue(messages);
    }
}
