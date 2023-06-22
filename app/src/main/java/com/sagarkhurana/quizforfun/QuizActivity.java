package com.sagarkhurana.quizforfun;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sagarkhurana.quizforfun.data.Answer;
import com.sagarkhurana.quizforfun.data.Quiz;
import com.sagarkhurana.quizforfun.data.QuizDatabase;
import com.sagarkhurana.quizforfun.data.QuizDatabaseClient;
import com.sagarkhurana.quizforfun.widgets.CircleProgressBar;

import java.util.List;
import java.util.Random;

public class QuizActivity extends AppCompatActivity {

    private RelativeLayout container;
    private TextView getReadyText;
    private TextView countdownText;
    private RelativeLayout quizContainer;
    private CardView resultCard;
    private TextView quizTitleText;
    private TextView quizQuestionText;
    private CircleProgressBar circleProgressBar;
    private RecyclerView answerRecyclerView;

    private List<Quiz> quizzes;
    private Random random;
    private CountDownTimer countdownTimer;
    private AnimatorSet flipAnimatorSet;
    private Handler mCircleProgressBarHandler = new Handler();

    private int currentQuizIndex;

    private QuizDatabase quizDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        container = findViewById(R.id.container);
        getReadyText = findViewById(R.id.getReadyText);
        countdownText = findViewById(R.id.countdownText);
        quizContainer = findViewById(R.id.quizContainer);
        resultCard = findViewById(R.id.resultCardView);
        quizTitleText = findViewById(R.id.quizTitleText);
        quizQuestionText = findViewById(R.id.quizQuestionText);
        circleProgressBar = findViewById(R.id.circleProgressBar);
        answerRecyclerView = findViewById(R.id.answerRecyclerView);

        quizDatabase = QuizDatabaseClient.getInstance(this);

        new LoadQuizzesTask().execute();
    }

    private void startCountdown(int seconds) {
        getReadyText.setAlpha(1.0f);
        getReadyText.setVisibility(View.VISIBLE);
        countdownText.setVisibility(View.VISIBLE);
        quizContainer.setVisibility(View.GONE);

        countdownText.setText(String.valueOf(seconds));

        countdownTimer = new CountDownTimer(seconds * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                countdownText.setText(String.valueOf(secondsRemaining + 1));
            }

            @Override
            public void onFinish() {
                countdownText.setVisibility(View.GONE);
                getReadyText.animate()
                        .alpha(0f)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                getReadyText.setVisibility(View.GONE);
                                showNextQuiz();
                            }
                        })
                        .start();
            }
        };

        countdownTimer.start();
    }

    private void showNextQuiz() {
        if (quizzes == null || quizzes.size() == 0) {
            return;
        }
        resultCard.setVisibility(View.GONE);
        quizContainer.setVisibility(View.VISIBLE);
        quizContainer.setAlpha(0f);
        quizContainer.animate()
                .setDuration(300)
                .alpha(1f)
                .start();

        // Pick a random quiz
        currentQuizIndex = random.nextInt(quizzes.size());
        Quiz quiz = quizzes.get(currentQuizIndex);

        quizTitleText.setText(quiz.getTitle());
        quizQuestionText.setText(quiz.getQuestion());

        // Set up answer cards RecyclerView
        new LoadAnswersTask().execute(quiz.getId());
    }

    private class LoadAnswersTask extends AsyncTask<Long, Void, List<Answer>> {
        @Override
        protected List<Answer> doInBackground(Long... quizIds) {
            long quizId = quizIds[0];
            return quizDatabase.answerDao().getAnswersForQuiz(quizId);
        }

        @Override
        protected void onPostExecute(List<Answer> answerList) {
            AnswerAdapter answerAdapter = new AnswerAdapter(answerList);
            answerRecyclerView.setAdapter(answerAdapter);

            // Use GridLayoutManager to display answer cards in two columns
            GridLayoutManager layoutManager = new GridLayoutManager(QuizActivity.this, 2);
            answerRecyclerView.setLayoutManager(layoutManager);

            // Start the progress bar countdown
            startProgressBarCountdown();
        }
    }

    private void startProgressBarCountdown() {
        circleProgressBar.setProgress(0);

        // Animate the progress bar countdown
        ObjectAnimator animation = ObjectAnimator.ofInt(circleProgressBar, "progress", 100);
        animation.setDuration(10000); // 5 seconds
        animation.setInterpolator(new LinearInterpolator());
        animation.start();

        // Call showNextQuiz() after the animation completes
        mCircleProgressBarHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showNextQuiz();
            }
        }, 10000); // Delay of 10 seconds
    }

    // ViewHolder for answer cards
    private static class AnswerViewHolder extends RecyclerView.ViewHolder {

        private TextView answerTextView;
        private float translationX;
        private float translationY;

        public AnswerViewHolder(View itemView) {
            super(itemView);
            answerTextView = itemView.findViewById(R.id.answerText);
        }

        public void setTranslationX(float translationX) {
            this.translationX = translationX;
            itemView.setTranslationX(translationX);
        }

        public void setTranslationY(float translationY) {
            this.translationY = translationY;
            itemView.setTranslationY(translationY);
        }

        public float getTranslationX() {
            return translationX;
        }

        public float getTranslationY() {
            return translationY;
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
            Answer answer = answerOptions.get(position);
            holder.answerTextView.setText(answer.getText());

            int columnPosition = position % 2; // Calculate column position

            if (columnPosition == 0) {
                holder.setTranslationX(0f);
                holder.setTranslationY(0f);
            } else {
                float translationX = -holder.itemView.getWidth() / 2f;
                holder.setTranslationX(translationX);
                holder.setTranslationY(0f);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle answer card click
                    flipAnswerCard(holder, answer.isCorrect());
                }
            });
        }

        @Override
        public int getItemCount() {
            return answerOptions.size();
        }
    }

    private void flipAnswerCard(AnswerViewHolder viewHolder, boolean isCorrect) {
        mCircleProgressBarHandler.removeCallbacksAndMessages(null);
        quizContainer.setVisibility(View.GONE);

        View resultCard = container.findViewById(R.id.resultCardView);
        ImageView resultImage = resultCard.findViewById(R.id.resultImage);
        if (isCorrect) {
            resultImage.setImageResource(R.drawable.ic_check); // Set green check icon for correct answer
        } else {
            resultImage.setImageResource(R.drawable.ic_cross); // Set red cross icon for incorrect answer
        }
        resultCard.setVisibility(View.VISIBLE);
        resultCard.setAlpha(0f);
        resultCard.animate()
                .setDuration(500)
                .alpha(1f)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (isCorrect) {
                                    restartQuiz();
                                } else {
                                    showNextQuiz();
                                }
                            }
                        }, 1000);
                    }
                })
                .start();
    }

    private void restartQuiz() {
        // Hide the quiz container
        quizContainer.setVisibility(View.GONE);
        resultCard.setVisibility(View.GONE);

        // Restart the countdown
        startCountdown(3);
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
            startCountdown(3);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countdownTimer != null) {
            countdownTimer.cancel();
        }
    }
}