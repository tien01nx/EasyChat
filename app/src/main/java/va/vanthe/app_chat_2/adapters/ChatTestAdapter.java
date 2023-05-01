package va.vanthe.app_chat_2.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import va.vanthe.app_chat_2.entity.ChatMessage;

public class ChatTestAdapter extends RecyclerView.Adapter<ChatTestAdapter.ChatViewHolder> {



    private final List<ChatMessage> chatMessages;

    private final String senderId;
    private final int styleChat;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public static OnItemClickListener onItemClickListener;

    public ChatTestAdapter (List<ChatMessage> chatMessages,String senderId, int styleChat, OnItemClickListener onItemClickListener) {
        this.chatMessages = chatMessages;
        this.senderId = senderId;
        this.styleChat = styleChat;
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(Uri uri);
    }


    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder{


        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

}
