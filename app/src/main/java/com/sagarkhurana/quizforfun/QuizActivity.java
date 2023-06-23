package com.sagarkhurana.quizforfun;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sagarkhurana.quizforfun.data.Answer;
import com.sagarkhurana.quizforfun.data.Quiz;
import com.sagarkhurana.quizforfun.data.QuizDatabase;
import com.sagarkhurana.quizforfun.data.QuizDatabaseClient;
import com.sagarkhurana.quizforfun.widgets.CircleProgressBar;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class QuizActivity extends AppCompatActivity {

    private CircleProgressBar circleProgressBar;
    private TextView tvLoading;
    private RelativeLayout quizContainer;
    private TextView tvQuizTitle;
    private ImageView ivAmplifier;
    private RecyclerView answerRecyclerView;
    private ImageView ivResult;
    private TextView tvError;
//    private Button btnConfirm;
    private Answer selectedAnswer = null;
    TextToSpeech txt2Speech;
    private Visualizer visualizer;

    private List<Quiz> quizzes;
    private Random random;
    private Handler mCircleProgressBarHandler = new Handler();

    private int currentQuizIndex;

    private QuizDatabase quizDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        circleProgressBar = findViewById(R.id.circleProgressBar);
        tvLoading = findViewById(R.id.loadingTxt);
        quizContainer = findViewById(R.id.quizContainer);
        tvQuizTitle = findViewById(R.id.quizTitleText);
        ivAmplifier = findViewById(R.id.ivAmplifier);
        ivResult = findViewById(R.id.ivResult);
        tvError = findViewById(R.id.tvError);
        answerRecyclerView = findViewById(R.id.answerRecyclerView);
        // Use GridLayoutManager to display answer cards in two columns
        GridLayoutManager layoutManager = new GridLayoutManager(QuizActivity.this, 2);
        answerRecyclerView.setLayoutManager(layoutManager);

        txt2Speech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    txt2Speech.setLanguage(Locale.UK);
                }
            }
        });

//        btnConfirm = findViewById(R.id.btnConfirm);
//        btnConfirm.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                flipAnswerCard();
//            }
//        });

        quizDatabase = QuizDatabaseClient.getInstance(this);

        new LoadQuizzesTask().execute();
    }

    // AsyncTask to load quizzes from the database
    private class LoadQuizzesTask extends AsyncTask<Void, Void, List<Quiz>> {
        @Override
        protected List<Quiz> doInBackground(Void... voids) {
            return quizDatabase.quizDao().getAllQuizzes();
        }

        @Override
        protected void onPostExecute(List<Quiz> quizList) {
            quizzes = quizList;
            random = new Random();
            startLoading();
        }
    }

    private void startLoading() {
        circleProgressBar.setVisibility(View.VISIBLE);
        tvLoading.setVisibility(View.VISIBLE);
        quizContainer.setVisibility(View.GONE);
        answerRecyclerView.setVisibility(View.GONE);
        ivResult.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);
//        btnConfirm.setVisibility(View.GONE);

        if (quizzes == null || quizzes.size() == 0) {
            Toast.makeText(this, getString(R.string.no_quiz), Toast.LENGTH_SHORT).show();
            tvError.setText(R.string.no_quiz);
            tvError.setVisibility(View.VISIBLE);
            circleProgressBar.setVisibility(View.GONE);
            tvLoading.setVisibility(View.GONE);
            return;
        }

        circleProgressBar.setProgress(0);

        ObjectAnimator animation = ObjectAnimator.ofInt(circleProgressBar, "progress", 100);
        animation.setDuration(3000);
        animation.setInterpolator(new LinearInterpolator());
        animation.start();

        mCircleProgressBarHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showNextQuiz();
            }
        }, 3000);
    }

    private void showNextQuiz() {
        selectedAnswer = null;

        // Pick a random quiz
        currentQuizIndex = random.nextInt(quizzes.size());
        Quiz quiz = quizzes.get(currentQuizIndex);
        tvQuizTitle.setText(quiz.getTitle());

        quizContainer.setVisibility(View.VISIBLE);
        circleProgressBar.setVisibility(View.GONE);
        tvLoading.setVisibility(View.GONE);
        answerRecyclerView.setVisibility(View.GONE);
        ivResult.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);
//        btnConfirm.setVisibility(View.GONE);

        quizContainer.setAlpha(0f);
        quizContainer.animate()
                .setDuration(500)
                .alpha(1f)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                speakQuestion(quiz);
                            }
                        }, 500);
                    }
                })
                .start();
    }

    private void speakQuestion(Quiz quiz) {

        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "utteranceId");
        txt2Speech.speak(quiz.getQuestion(), TextToSpeech.QUEUE_FLUSH, params);

        // Set up a listener for when the speech has completed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            txt2Speech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    // Do nothing
                }

                @Override
                public void onDone(String utteranceId) {
                    new LoadAnswersTask().execute(quiz.getId());
                }

                @Override
                public void onError(String utteranceId) {
                    // Do nothing
                }
            });
        } else {
            // For backwards compatibility with older versions of Android
            txt2Speech.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
                @Override
                public void onUtteranceCompleted(String utteranceId) {
                    new LoadAnswersTask().execute(quiz.getId());
                }
            });
        }
    }

    private class LoadAnswersTask extends AsyncTask<Long, Void, List<Answer>> {
        @Override
        protected List<Answer> doInBackground(Long... quizIds) {
            long quizId = quizIds[0];
            return quizDatabase.answerDao().getAnswersForQuiz(quizId);
        }

        @Override
        protected void onPostExecute(List<Answer> answerList) {
            if (answerList == null || answerList.size() == 0) {
                Toast.makeText(QuizActivity.this, getString(R.string.no_answer), Toast.LENGTH_SHORT).show();
                tvLoading.setText(R.string.no_answer);
                tvError.setVisibility(View.VISIBLE);
                return;
            }
            AnswerAdapter answerAdapter = new AnswerAdapter(answerList);
            answerRecyclerView.setAdapter(answerAdapter);

            circleProgressBar.setVisibility(View.GONE);
            tvLoading.setVisibility(View.GONE);
            quizContainer.setVisibility(View.GONE);
            ivResult.setVisibility(View.GONE);
            tvError.setVisibility(View.GONE);
            answerRecyclerView.setVisibility(View.VISIBLE);
//            btnConfirm.setVisibility(View.VISIBLE);
        }
    }

    // ViewHolder for answer cards
    private static class AnswerViewHolder extends RecyclerView.ViewHolder {

        private TextView answerTextView;
        private ImageView checkMark;

        public AnswerViewHolder(View itemView) {
            super(itemView);
            answerTextView = itemView.findViewById(R.id.answerText);
            checkMark = itemView.findViewById(R.id.checkMark);
        }
    }

    // Adapter for answer cards
    private class AnswerAdapter extends RecyclerView.Adapter<AnswerViewHolder> {

        private List<Answer> answerOptions;

        public AnswerAdapter(List<Answer> answerOptions) {
            this.answerOptions = answerOptions;
        }

        @NonNull
        @Override
        public AnswerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_answer_card, parent, false);
            return new AnswerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AnswerViewHolder holder, int position) {
            final int pos = position;
            Answer answer = answerOptions.get(pos);
            holder.answerTextView.setText(answer.getText());
            holder.checkMark.setVisibility(View.GONE);
            if (selectedAnswer != null && answer.getId() == selectedAnswer.getId()) {
                holder.checkMark.setVisibility(View.VISIBLE);
            } else {
                holder.checkMark.setVisibility(View.INVISIBLE);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedAnswer = answer;
                    notifyDataSetChanged();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            flipAnswerCard();
                        }
                    }, 500);
                }
            });
        }

        @Override
        public int getItemCount() {
            return answerOptions.size();
        }
    }

    private void flipAnswerCard() {
        if (selectedAnswer == null) {
            return;
        }

        quizContainer.setVisibility(View.GONE);

        if (selectedAnswer.isCorrect()) {
            ivResult.setImageResource(R.drawable.ic_check); // Set green check icon for correct answer
        } else {
            ivResult.setImageResource(R.drawable.ic_cross); // Set red cross icon for incorrect answer
        }
        ivResult.setVisibility(View.VISIBLE);
        ivResult.setAlpha(0f);
        ivResult.animate()
                .setDuration(500)
                .alpha(1f)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (selectedAnswer.isCorrect()) {
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                        stopLockTask();
                                    }
                                    launchYouTubeApp();
                                    delayAndReturnToApp();
                                } else {
//                                    showNextQuiz();
                                    startLoading();
                                }
                            }
                        }, 1000);
                    }
                })
                .start();
    }

    @Override
    protected void onPause() {
        if(txt2Speech !=null){
            txt2Speech.stop();
            txt2Speech.shutdown();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCircleProgressBarHandler != null) {
            mCircleProgressBarHandler.removeCallbacksAndMessages(null);
        }
        if (visualizer != null) {
            visualizer.setEnabled(false);
            visualizer.release();
        }
    }

    private void launchYouTubeApp() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com"));
        startActivity(intent);
    }

    private void delayAndReturnToApp() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Return to your app
                Intent intent = new Intent(QuizActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }, 10000);
    }
}