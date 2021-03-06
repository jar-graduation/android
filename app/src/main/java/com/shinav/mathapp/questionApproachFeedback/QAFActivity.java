package com.shinav.mathapp.questionApproachFeedback;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shinav.mathapp.R;
import com.shinav.mathapp.card.Card;
import com.shinav.mathapp.card.CardViewPager;
import com.shinav.mathapp.db.helper.Tables;
import com.shinav.mathapp.db.pojo.GivenQuestionApproach;
import com.shinav.mathapp.db.pojo.Question;
import com.shinav.mathapp.db.pojo.QuestionApproach;
import com.shinav.mathapp.db.pojo.QuestionApproachPart;
import com.shinav.mathapp.db.repository.GivenQuestionApproachRepository;
import com.shinav.mathapp.db.repository.QuestionApproachPartRepository;
import com.shinav.mathapp.db.repository.QuestionApproachRepository;
import com.shinav.mathapp.db.repository.QuestionRepository;
import com.shinav.mathapp.image.BackgroundLoader;
import com.shinav.mathapp.injection.component.Injector;
import com.shinav.mathapp.question.QuestionActivity;
import com.shinav.mathapp.question.card.QuestionAnnexCardView;
import com.shinav.mathapp.questionApproach.QuestionSimpleCardView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.functions.Action1;

import static android.support.v4.view.ViewPager.SimpleOnPageChangeListener;

public class QAFActivity extends ActionBarActivity {

    @InjectView(R.id.toolbar) Toolbar toolbar;
    @InjectView(R.id.background_view) ImageView backgroundView;
    @InjectView(R.id.card_view_pager) CardViewPager cardViewPager;
    @InjectView(R.id.view_pager_indicator_container) LinearLayout viewPagerIndicator;
    @InjectView(R.id.feedback_view_pager) QAFViewPager viewPager;
    @InjectView(R.id.indicator_container) LinearLayout indicatorContainer;
    @InjectView(R.id.selected_part_text_view) TextView selectedPartTextView;

    @Inject QuestionRepository questionRepository;
    @Inject QuestionApproachRepository questionApproachRepository;
    @Inject QuestionApproachPartRepository questionApproachPartRepository;
    @Inject GivenQuestionApproachRepository givenQuestionApproachRepository;
    @Inject QuestionSimpleCardView questionSimpleCardView;
    @Inject BackgroundLoader backgroundLoader;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_approach_feedback);

        ButterKnife.inject(this);
        inject();

        final String questionKey = getIntent().
                getStringExtra(Tables.StoryboardFrame.FRAME_TYPE_KEY);

        loadQuestion(questionKey);
        fetchQuestionApproach(questionKey);
    }

    public void inject() {
        Injector.getActivityComponent(this).inject(this);
    }

    private void loadQuestion(String questionKey) {
        questionRepository.get(questionKey, new Action1<Question>() {

            @Override public void call(Question question) {
                initToolbar(question.getTitle());

                backgroundLoader.loadBackground(
                        backgroundView,
                        question.getBackgroundImageUrl()
                );

                initQuestionViewPager(question);
            }
        });
    }

    private void initToolbar(String title) {
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void fetchQuestionApproach(String questionKey) {
        questionApproachRepository.get(questionKey, new Action1<QuestionApproach>() {

            @Override public void call(QuestionApproach questionApproach) {
                fetchQuestionApproachParts(questionApproach.getKey());
            }
        });
    }

    private void fetchQuestionApproachParts(final String approachKey) {
        questionApproachPartRepository.getApproachParts(approachKey, new Action1<List<QuestionApproachPart>>() {

            @Override public void call(List<QuestionApproachPart> questionApproachParts) {
                Collections.sort(questionApproachParts);
                fetchGivenQuestionApproach(approachKey, questionApproachParts);
            }
        });
    }

    private void fetchGivenQuestionApproach(
            String approachKey,
            final List<QuestionApproachPart> questionApproachParts)
    {
        givenQuestionApproachRepository.get(approachKey, new Action1<GivenQuestionApproach>() {
            @Override public void call(GivenQuestionApproach givenQuestionApproach) {
                List<QuestionApproachPart> arrangedQuestionApproachParts =
                        sortOnGivenApproachArrangement(questionApproachParts, givenQuestionApproach);

                initQuestionApproachPartsViewPager(questionApproachParts, arrangedQuestionApproachParts);
            }
        });
    }

    private void initQuestionViewPager(Question question) {
        List<Card> cards = new ArrayList<>();

        questionSimpleCardView.setQuestionValue(question.getValue());
        cards.add(questionSimpleCardView);

        if (!TextUtils.isEmpty(question.getAnnexImageUrl())) {
            QuestionAnnexCardView questionAnnexCardView =
                    new QuestionAnnexCardView(this);

            questionAnnexCardView.setAnnexImageUrl(question.getAnnexImageUrl());
            cards.add(questionAnnexCardView);
        }

        cardViewPager.setIndicator(viewPagerIndicator);
        cardViewPager.setCards(cards);
        cardViewPager.setCurrentItem(0);
    }

    private void initQuestionApproachPartsViewPager(
            List<QuestionApproachPart> questionApproachParts,
            List<QuestionApproachPart> arrangedQuestionApproachParts) {

        viewPager.setIndicatorContainer(indicatorContainer);

        viewPager.setupQuestionApproachParts(
                arrangedQuestionApproachParts,
                questionApproachParts
        );

        viewPager.setOnPageChangeListener(new SimpleOnPageChangeListener() {
            @Override public void onPageSelected(int position) {
                selectedPartTextView.setText("Stap " + (position + 1));
            }
        });
    }

    private List<QuestionApproachPart> sortOnGivenApproachArrangement(final List<QuestionApproachPart> questionApproachParts, GivenQuestionApproach givenQuestionApproach) {

        final String[] arrangement = TextUtils.split(givenQuestionApproach.getArrangement(), ",");

        List<QuestionApproachPart> arrangedApproaches = new ArrayList<>();

        for (int i = 0; i < arrangement.length; i++) {
            int index = Integer.parseInt(arrangement[i]);

            // Don't go out of bounds if amount changed since last attempt.
            if (index < questionApproachParts.size()) {
                arrangedApproaches.add(questionApproachParts.get(index));
            }
        }

        return arrangedApproaches;
    }

    @OnClick(R.id.next_question_button)
    public void onNextClicked() {
        next();
    }

    public void next() {
        Intent intent = getIntent().setClass(this, QuestionActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_right_from_outside, R.anim.slide_right_to_outside);
    }

}
