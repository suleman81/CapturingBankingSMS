package com.suleman.capturingbanking.utilies;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.suleman.capturingbanking.db.AppDatabase;
import com.suleman.capturingbanking.db.MessageDAO;
import com.suleman.capturingbanking.model.MessageModel;

import java.util.List;

public class MessageRepository {
    private MessageDAO messageDAO;
    private LiveData<List<MessageModel>> allMessages;

    public MessageRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        messageDAO = database.messageDAO();
        allMessages = messageDAO.getAllMessages();
    }

    public LiveData<List<MessageModel>> getAllMessages() {
        return allMessages;
    }

    public void insert(MessageModel messageModel) {
        AppDatabase.databaseWriteExecutor.execute(() -> messageDAO.insert(messageModel));
    }

    public void deleteById(int messageId) {
        AppDatabase.databaseWriteExecutor.execute(() -> messageDAO.deleteById(messageId));
    }

    public void deleteAll() {
        AppDatabase.databaseWriteExecutor.execute(messageDAO::deleteAll);
    }
}

