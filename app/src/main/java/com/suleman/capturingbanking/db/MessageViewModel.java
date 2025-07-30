package com.suleman.capturingbanking.db;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.suleman.capturingbanking.model.MessageModel;
import com.suleman.capturingbanking.utilies.MessageRepository;

import java.util.List;

public class MessageViewModel extends AndroidViewModel {
    private MessageRepository repository;
    private LiveData<List<MessageModel>> allMessages;

    public MessageViewModel(@NonNull Application application) {
        super(application);
        repository = new MessageRepository(application);
        allMessages = repository.getAllMessages();
    }

    public LiveData<List<MessageModel>> getAllMessages() {
        return allMessages;
    }

    public void insert(MessageModel message) {
        repository.insert(message);
    }

    public void deleteById(int messageId) {
        repository.deleteById(messageId);
    }

    public void deleteAll() {
        repository.deleteAll();
    }
}

