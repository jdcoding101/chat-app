package ph.umak.chatapp.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import ph.umak.chatapp.objects.UserModel;
import ph.umak.chatapp.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyUserHolder> {

    List<UserModel> users;

    OnUserClicked userInterface;

    public UserAdapter(OnUserClicked userInterface) {
        this.userInterface = userInterface;
    }

    public void setUsers(List<UserModel> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public MyUserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new MyUserHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyUserHolder holder, int position) {

        holder.name.setText(users.get(position).getFirstName() + " " + users.get(position).getLastName());
        holder.email.setText(users.get(position).getEmail());

        if (!users.get(position).getImageUrl().equals("default"))
            Glide.with(holder.itemView.getContext()).load(users.get(position).getImageUrl()).into(holder.displayPicture);

        if (users.get(position).getOnline().equals(true))
            holder.status.setImageResource(R.drawable.online);
        else
            holder.status.setImageResource(R.drawable.offline);

    }

    @Override
    public int getItemCount() {
        if (users == null) {
            return 0;
        } else {
            return users.size();
        }
    }

    class MyUserHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name, email;
        CircleImageView displayPicture;
        ImageView status;

        public MyUserHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.nameTextViewUserItem);
            email = itemView.findViewById(R.id.emailTextViewUserItem);
            displayPicture = itemView.findViewById(R.id.profileImageViewUserItem);
            status = itemView.findViewById(R.id.statusImageViewUserItem);

            name.setOnClickListener(this);
            displayPicture.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            userInterface.clickUser(getAdapterPosition(), users);
        }
    }

    public interface OnUserClicked {
        void clickUser(int position, List<UserModel> userModels);
    }
}
