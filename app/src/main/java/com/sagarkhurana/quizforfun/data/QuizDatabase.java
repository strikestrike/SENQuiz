package com.sagarkhurana.quizforfun.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(
entities = {User.class,Attempt.class, Quiz.class, Answer.class},
        exportSchema = false,
        version = 3
)
public abstract class QuizDatabase extends RoomDatabase {

    public abstract UserDao userDao();

    public abstract QuizDao quizDao();

    public abstract AnswerDao answerDao();

}