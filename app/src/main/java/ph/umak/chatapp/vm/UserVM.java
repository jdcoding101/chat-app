package ph.umak.chatapp.vm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import ph.umak.chatapp.objects.UserModel;
import ph.umak.chatapp.repo.UserRepository;

public class UserVM extends ViewModel implements UserRepository.OnAvailableUsers {

    MutableLiveData<List<UserModel>> userMutableLiveData = new MutableLiveData<>();

    UserRepository repo = new UserRepository(this);

    public UserVM() {
        repo.getUsers();
    }

    public LiveData<List<UserModel>> getUsers() {
        return userMutableLiveData;
    }

    @Override
    public void usersFromDB(List<UserModel> users) {
        userMutableLiveData.setValue(users);
    }
}

