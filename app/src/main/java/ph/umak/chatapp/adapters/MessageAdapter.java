package ph.umak.chatapp.adapters;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ph.umak.chatapp.R;
import ph.umak.chatapp.objects.MessageModel;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyMessageHolder> {

    private List<MessageModel> messages;
    private String currentUid, friendUid;
    private String currentEmail, friendEmail;

    public void setMessages(List<MessageModel> messages, String currentUid, String friendUid, String currentEmail, String friendEmail) {
        this.messages = messages;
        this.currentUid = currentUid;
        this.friendUid = friendUid;
        this.currentEmail = currentEmail;
        this.friendEmail = friendEmail;
    }

    @NonNull
    @Override
    public MyMessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, parent, false);
            return new MyMessageHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyMessageHolder holder, int position) {
        if (messages.get(position).getSender().equals(currentUid) && messages.get(position).getReceiver().equals(friendUid)) {
            holder.emailTextView.setText(currentEmail);
            holder.emailTextView.setTextColor(Color.CYAN);
        } else {
            holder.emailTextView.setText(friendEmail);
        }
        holder.messageTextView.setText(messages.get(position).getMessage());
        holder.timeTextView.setText(messages.get(position).getTime().substring(0,5));
    }

    @Override
    public int getItemCount() {
        if (messages == null) {
            return 0;
        } else {
            return messages.size();
        }
    }

    class MyMessageHolder extends RecyclerView.ViewHolder {
        TextView emailTextView, messageTextView, timeTextView;

        public MyMessageHolder(@NonNull View itemView) {
            super(itemView);
            emailTextView = itemView.findViewById(R.id.emailTextViewChatItem);
            messageTextView = itemView.findViewById(R.id.messageTextViewChatItem);
            timeTextView = itemView.findViewById(R.id.timeTextViewChatItem);
        }
    }
}
