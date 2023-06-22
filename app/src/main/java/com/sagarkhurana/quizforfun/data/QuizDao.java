package com.sagarkhurana.quizforfun.data;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface QuizDao {

    @Query("SELECT * FROM quiz")
    List<Quiz> getAllQuizzes();

    // Add more query methods as per your requirements
}
