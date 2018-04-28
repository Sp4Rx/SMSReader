package io.github.sp4rx.smsreader.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import io.github.sp4rx.smsreader.R;
import io.github.sp4rx.smsreader.model.Message;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Message> messages;
    private static final int HOUR_MILLIS = 3600000;

    public MessagesAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case Message.HEADER:
                View headerItemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_header, parent, false);
                return new HeaderViewHolder(headerItemView);
            default:
                View messageItemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message, parent, false);
                return new MessageViewHolder(messageItemView);
        }

    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        long diff = System.currentTimeMillis() - message.getDate();
        switch (getItemViewType(position)) {
            case Message.HEADER:
                HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                headerViewHolder.header.setText(String.format(Locale.getDefault(), "%d hours ago", diff / HOUR_MILLIS));
                break;
            case Message.MESSAGE:
                MessageViewHolder messageViewHolder = (MessageViewHolder) holder;
                messageViewHolder.body.setText(message.getBody());
                messageViewHolder.time.setText(DateUtils.getRelativeTimeSpanString(message.getDate()));
                messageViewHolder.address.setText(message.getAddress());
                break;
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView body, time, address;

        public MessageViewHolder(View itemView) {
            super(itemView);
            body = itemView.findViewById(R.id.itemMessageBody);
            time = itemView.findViewById(R.id.itemMessageTime);
            address = itemView.findViewById(R.id.itemMessageAddress);
        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView header;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.itemHeader);
        }
    }

    public void addAll(List<Message> messages) {
        this.messages.addAll(messages);
        notifyDataSetChanged();
    }
}
