/*todo:
    wrap questions in a scroll view, possibly adjust text size based on length
    try and incorporate timer
    add warnings for locking phone and how to use accents
    maybe prompt user for time to spend studying
    try to incorporate fragments somehow, maybe scroll through flash cards, just for this week hopefully
    on completion, display options to study the same deck or pick a new one
    maybe just display option to study same deck if score is not high enough
 */

package com.example.guest.pomodoro.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.guest.pomodoro.R;
import com.example.guest.pomodoro.adapters.CardPagerAdapter;
import com.example.guest.pomodoro.models.QA;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;

public class StudyActivity extends AppCompatActivity implements View.OnClickListener {

    //    @Bind(R.id.pointsTextView) TextView mPointsTextView;
    ArrayList<QA> mQas = new ArrayList<>();
    @Bind(R.id.pointsTextView)
    TextView mPointsTextView;
    @Bind(R.id.answerEditText)
    EditText mAnswerEditText;
    @Bind(R.id.submitButton)
    Button mSubmitButton;
    @Bind(R.id.resultsTextView)
    TextView mResultsTextView;
    @Bind(R.id.adjustPointsTextView)
    TextView mAdjustPointsTextView;
    @Bind(R.id.viewPager)
    ViewPager mViewPager;
    private CardPagerAdapter adapterViewPager;

    int index;
    int points;

    public void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public void setupUI(View view) {
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideKeyboard(StudyActivity.this);
                    findViewById(R.id.parentContainer).requestFocus();
                    return false;
                }
            });
        }

        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);
        ButterKnife.bind(this);
        setupUI(findViewById(R.id.parentContainer));

        Intent intent = getIntent();
        mQas = Parcels.unwrap(intent.getParcelableExtra("qas"));
        adapterViewPager = new CardPagerAdapter(getSupportFragmentManager(), mQas);
        mViewPager.setAdapter(adapterViewPager);
        mSubmitButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submitButton:
                String answer = mAnswerEditText.getText().toString();
                index = mViewPager.getCurrentItem();
                mAnswerEditText.setText("");
                mResultsTextView.setText("Your Answer: " + answer);
                if (answer.toLowerCase().equals(mQas.get(index).getAnswer().toLowerCase())) {
                    points += 1;
                    mPointsTextView.setText(String.valueOf(points));
                    mAdjustPointsTextView.setText("+1 points");
                } else {
                    points -= 1;
                    mPointsTextView.setText(String.valueOf(points));
                    mAdjustPointsTextView.setText("-1 points");
                }
        }

    }
}
