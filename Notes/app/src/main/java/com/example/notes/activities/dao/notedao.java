package com.example.notes.activities.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;


import com.example.notes.activities.entities.note;

import java.util.List;

@Dao
public interface notedao {
    @Query("SELECT * FROM notes ORDER BY id DESC")
    List<note> getAllNotes();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(note note);

    @Delete
    void deleteNote(note note);
}
