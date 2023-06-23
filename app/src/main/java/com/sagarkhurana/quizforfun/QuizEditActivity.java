package com.sagarkhurana.quizforfun;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class QuizEditActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 2;
    private static final String IMAGE_DIRECTORY = "Quiz";

    private EditText editTextTitle;
    private EditText editTextQuestion;
    private LinearLayout answersLayout;
    private ImageView buttonAdd;
    private ImageView buttonRemove;
    private ImageView ivAnswerImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_edit);

        // Find views
        ivAnswerImage = findViewById(R.id.ivAnswerImage);
        editTextTitle = findViewById(R.id.edtTitle);
        editTextQuestion = findViewById(R.id.edtQuestion);
        answersLayout = findViewById(R.id.answersLayout);
        buttonAdd = findViewById(R.id.buttonAdd);

        ivAnswerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onImageViewClick(v);
            }
        });

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddButtonClick();
            }
        });

        // Set click listener for the Test button
        findViewById(R.id.btnTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform test logic here
            }
        });
    }

    public void onAddButtonClick() {
        View answerItem = getLayoutInflater().inflate(R.layout.answer_item, answersLayout, false);
        View buttonAdd = answerItem.findViewById(R.id.buttonAdd);
        View buttonRemove = answerItem.findViewById(R.id.buttonRemove);

        buttonAdd.setVisibility(View.GONE);
        buttonRemove.setVisibility(View.VISIBLE);

        buttonRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answersLayout.removeView(answerItem);
            }
        });

        buttonAdd.setOnClickListener(null);

        answersLayout.addView(answerItem, 0);
    }

    public void onImageViewClick(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        } else {
            openImageChooser();
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            ImageView imageViewAnswer = findViewById(R.id.ivAnswerImage);
            imageViewAnswer.setImageURI(selectedImageUri);
            saveImageToDirectory(selectedImageUri);
        }
    }

    private void saveImageToDirectory(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            File directory = new File(getExternalFilesDir(null) + "/" + IMAGE_DIRECTORY);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            File file = new File(directory, "quiz_image.jpg");
            OutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImageChooser();
            }
        }
    }
}