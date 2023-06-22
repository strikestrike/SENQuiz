package com.sagarkhurana.quizforfun.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "answer",
        foreignKeys = @ForeignKey(entity = Quiz.class,
                parentColumns = "id",
                childColumns = "quiz_id",
                onDelete = ForeignKey.CASCADE),
        indices = @Index("quiz_id"))
public class Answer {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "quiz_id")
    private int quizId;

    @ColumnInfo(name = "text")
    private String text;

    @ColumnInfo(name = "image")
    private String image;

    @ColumnInfo(name = "correct")
    private boolean correct;

    public Answer(int quizId, String text, String image, boolean correct) {
        this.quizId = quizId;
        this.text = text;
        this.image = image;
        this.correct = correct;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }
}