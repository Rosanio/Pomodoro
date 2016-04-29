/*todo:
*/

package com.example.guest.pomodoro.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.guest.pomodoro.models.QA;
import com.example.guest.pomodoro.adapters.QAListAdapter;
import com.example.guest.pomodoro.R;
import com.example.guest.pomodoro.services.YandexService;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CreateDeckActivity extends AppCompatActivity implements View.OnClickListener {

    @Bind(R.id.questionEditText) EditText mQuestionEditText;
    @Bind(R.id.answerEditText) EditText mAnswerEditText;
    @Bind(R.id.addCardButton) Button mAddCardButton;
    @Bind(R.id.recyclerView) RecyclerView mQaRecyclerView;
    @Bind(R.id.studyButton) Button mStudyButton;
    @Bind(R.id.languageSpinner) Spinner mLanguageSpinner;
    @Bind(R.id.translateQuestionButton) Button mTranslateQuestionButton;
    @Bind(R.id.loadingTextView) TextView mLoadingTextView;
    ArrayList<String> questions = new ArrayList<String>();
    ArrayList<String> answers = new ArrayList<String>();
    ArrayList<QA> qas = new ArrayList<>();
    QAListAdapter adapter;
    String[] languages = {"Spanish", "French", "German", "Italian"};

    public void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public void setupUI(View view) {
        if(!(view instanceof EditText || view instanceof Button)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideKeyboard(CreateDeckActivity.this);
                    findViewById(R.id.parentContainer).requestFocus();
                    return false;
                }
            });
        }

        if(view instanceof ViewGroup) {
            for(int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_deck);
        ButterKnife.bind(this);
        setupUI(findViewById(R.id.parentContainer));

        ArrayAdapter spinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, languages);
        mLanguageSpinner.setAdapter(spinnerAdapter);

        mAddCardButton.setOnClickListener(this);
        mStudyButton.setOnClickListener(this);
        mTranslateQuestionButton.setOnClickListener(this);

        adapter = new QAListAdapter(this, qas);
        mQaRecyclerView.setAdapter(adapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(CreateDeckActivity.this);
        mQaRecyclerView.setLayoutManager(layoutManager);
        mQaRecyclerView.setHasFixedSize(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addCardButton:
                String question = mQuestionEditText.getText().toString();
                String answer = mAnswerEditText.getText().toString();
                if(question.length()>0 && answer.length() > 0) {

                    if(qas.size()==0) {
                        QA qa = new QA(question, answer);
                        qas.add(qa);
                        adapter.notifyDataSetChanged();
                    } else {
                        Boolean contains = false;
                        for(int i = 0; i < qas.size(); i++) {
                            Log.d("it", "works");
                            if(!qas.get(i).getQuestion().equals(question)) {
                                contains = true;
                            }
                        }
                        if(!contains) {
                            QA qa = new QA(question, answer);
                            qas.add(qa);
                            adapter.notifyDataSetChanged();
                            break;
                        } else {
                            Toast.makeText(CreateDeckActivity.this, "This question has already been added", Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Toast.makeText(CreateDeckActivity.this, "Please fill out both question and answer forms", Toast.LENGTH_LONG).show();
                }

                mQuestionEditText.setText("");
                mAnswerEditText.setText("");
                mQuestionEditText.requestFocus();
                break;
            case R.id.studyButton:
                Intent intent = new Intent(CreateDeckActivity.this, StudyActivity.class);
                intent.putExtra("qas", qas);
                startActivity(intent);
                break;
            case R.id.translateQuestionButton:
                String language = mLanguageSpinner.getSelectedItem().toString();
                String text = mQuestionEditText.getText().toString();
                if(text.length() > 0) {
                    if(qas.size() == 0) {
                        mLoadingTextView.setText("loading...");
                        translateText(text, language);
                    } else {
                        Boolean contains = false;
                        for(int i = 0; i < qas.size(); i++) {
                            if(qas.get(i).getQuestion().equals(text)) {
                                contains = true;
                            }
                        }
                        if(!contains) {
                            mLoadingTextView.setText("loading...");
                            translateText(text, language);
                        } else {
                            Toast.makeText(CreateDeckActivity.this, "This question has already been added", Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Toast.makeText(CreateDeckActivity.this, "Please fill out question form", Toast.LENGTH_LONG).show();
                }
                mQuestionEditText.setText("");
                mAnswerEditText.setText("");
                mQuestionEditText.requestFocus();
                break;

        }
    }

    private void translateText(String text, String language) {
        final YandexService yandexService = new YandexService();
        final String question = text;

        yandexService.translateText(text, language, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) {
                qas = yandexService.processResults(qas, question, response);

                CreateDeckActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        mLoadingTextView.setText("");
                    }
                });

            }
        });
    }
}