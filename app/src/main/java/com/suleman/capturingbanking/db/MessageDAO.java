package com.suleman.capturingbanking.db;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.suleman.capturingbanking.model.MessageModel;

import java.util.List;

@Dao
public interface  MessageDAO {
    @Insert(onConflict = REPLACE)
    void insert(MessageModel messageModel);

    @Query("SELECT * FROM message")
    LiveData<List<MessageModel>> getAllMessages();

    @Query("DELETE FROM message")
    void deleteAll();

    @Query("DELETE FROM message WHERE id = :messageId")
    void deleteById(int messageId);
}
