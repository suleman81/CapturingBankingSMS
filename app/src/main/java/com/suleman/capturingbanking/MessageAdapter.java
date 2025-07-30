package com.suleman.capturingbanking;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.suleman.capturingbanking.model.MessageModel;

import java.util.List;

public class MessageAdapter extends ListAdapter<MessageModel, MessageAdapter.MessageVH> {
    private final Context context;

    public static interface RetryCallback {
        void retry(MessageModel model);
    }

    RetryCallback retryCallback;

    public void setRetryCallback(RetryCallback retryCallback) {
        this.retryCallback = retryCallback;
    }

    public MessageAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
    }

    private static final DiffUtil.ItemCallback<MessageModel> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<MessageModel>() {
                @Override
                public boolean areItemsTheSame(@NonNull MessageModel oldItem, @NonNull MessageModel newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull MessageModel oldItem, @NonNull MessageModel newItem) {
                    return oldItem.equals(newItem);
                }
            };

    @NonNull
    @Override
    public MessageVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MessageVH(LayoutInflater.from(context).inflate(R.layout.messages, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MessageVH holder, int position) {
        MessageModel model = getItem(position);
        holder.channel.setText(model.getChannel());
        holder.message.setText(model.getSms());

        holder.copy.setOnClickListener(v -> {
            String text = "Channel " + model.getChannel() + "\nMessage: " + model.getSms();
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
        });

        holder.retry.setOnClickListener(v -> {
            retryCallback.retry(model);
        });
    }

    public static class MessageVH extends RecyclerView.ViewHolder {
        TextView channel, message;
        Button copy, retry;

        public MessageVH(@NonNull View itemView) {
            super(itemView);
            channel = itemView.findViewById(R.id.channel);
            message = itemView.findViewById(R.id.message);
            copy = itemView.findViewById(R.id.copy);
            retry = itemView.findViewById(R.id.retry);
        }
    }
}

