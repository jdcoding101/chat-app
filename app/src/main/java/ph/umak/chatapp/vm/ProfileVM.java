package ph.umak.chatapp.vm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import ph.umak.chatapp.objects.UserModel;
import ph.umak.chatapp.repo.ProfileRepository;

public class ProfileVM extends ViewModel implements ProfileRepository.OnUser {

    MutableLiveData<UserModel> userMutableLiveData = new MutableLiveData<>();

    ProfileRepository repo = new ProfileRepository(this);

    public ProfileVM() { repo.getUser(); }

    public LiveData<UserModel> getUser() {
        return userMutableLiveData;
    }

    @Override
    public void userFromDB(UserModel user) {
        userMutableLiveData.setValue(user);
    }

}

