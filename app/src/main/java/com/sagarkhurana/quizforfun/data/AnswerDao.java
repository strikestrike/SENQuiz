package com.sagarkhurana.quizforfun.data;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AnswerDao {

    @Query("SELECT * FROM answer WHERE quiz_id = :quizId")
    List<Answer> getAnswersForQuiz(long quizId);

    // Add more query methods as per your requirements
}