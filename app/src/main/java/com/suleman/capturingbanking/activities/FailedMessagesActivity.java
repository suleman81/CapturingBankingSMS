package com.suleman.capturingbanking.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.suleman.capturingbanking.MessageAdapter;
import com.suleman.capturingbanking.R;
import com.suleman.capturingbanking.api.ResponseCallback;
import com.suleman.capturingbanking.api.ServerConnector;
import com.suleman.capturingbanking.databinding.ActivityFailedMessagesBinding;
import com.suleman.capturingbanking.db.MessageViewModel;
import com.suleman.capturingbanking.model.MessageModel;
import com.suleman.capturingbanking.utilies.NetworkUtil;
import com.suleman.capturingbanking.utilies.NotificationHelper;

import java.util.ArrayList;
import java.util.List;

public class FailedMessagesActivity extends AppCompatActivity {
    ActivityFailedMessagesBinding binding;
    private MessageViewModel messageViewModel;
    private MessageAdapter adapter;
    private List<MessageModel> finalList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityFailedMessagesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.back.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        binding.messageRC.setLayoutManager(new LinearLayoutManager(this));
        binding.messageRC.setHasFixedSize(false);

        adapter = new MessageAdapter(this);
        adapter.setRetryCallback(new MessageAdapter.RetryCallback() {
            @Override
            public void retry(MessageModel model) {
                retryMessage(model);
            }
        });
        binding.messageRC.setAdapter(adapter);

        messageViewModel = new ViewModelProvider(this).get(MessageViewModel.class);

        // Observe LiveData and submit list to adapter
        messageViewModel.getAllMessages().observe(this, messages -> {
            adapter.submitList(messages);
            if (messages.isEmpty()) {
                Toast.makeText(this, "No Record Found", Toast.LENGTH_SHORT).show();
            }
        });

        binding.retry.setOnClickListener(v -> {
            if (!NetworkUtil.isNetworkAvailable(this)){
                Toast.makeText(this, "Check your internet connection", Toast.LENGTH_SHORT).show();
                return;
            }

            finalList = new ArrayList<>(adapter.getCurrentList());
            if (finalList.isEmpty()) {
                Toast.makeText(this, "No Data Found", Toast.LENGTH_SHORT).show();
                return;
            }
            sendData(0);
        });

    }

    private void retryMessage(MessageModel model) {
        ServerConnector.getInstance(false).sendMessage(model.toJson(), new ResponseCallback() {
            @Override
            public void onSuccess(Object response) {
                messageViewModel.deleteById(model.getId());
                Toast.makeText(FailedMessagesActivity.this, "Message Send successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String response) {
                Toast.makeText(FailedMessagesActivity.this, "Error : " + response, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendData(int i) {
        if (i >= finalList.size()) {
            return;
        }
        MessageModel model = finalList.get(i);
        final int[] finalI = {i};
        ServerConnector.getInstance(false).sendMessage(model.toJson(), new ResponseCallback() {
            @Override
            public void onSuccess(Object response) {
                messageViewModel.deleteById(model.getId());
                sendData(finalI[0]++);
            }

            @Override
            public void onError(String response) {
                Toast.makeText(FailedMessagesActivity.this, "Error : " + response, Toast.LENGTH_SHORT).show();
            }
        });
    }
}