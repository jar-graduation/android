package com.shinav.mathapp.question;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.shinav.mathapp.MyApplication;
import com.shinav.mathapp.R;
import com.shinav.mathapp.animation.AnimationFactory;
import com.shinav.mathapp.approach.Approach;
import com.shinav.mathapp.approach.ApproachAdapter;
import com.shinav.mathapp.view.FlipCard;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class QuestionApproachView extends FlipCard {

    public static final float PERCENTAGE_HEIGHT = 0.38f;
    @InjectView(R.id.approach_list) RecyclerView approachList;

    public QuestionApproachView(Context context, List<Approach> approaches) {
        super(context);
        init(approaches);
    }

    public void init(List<Approach> approaches) {
        View view = LayoutInflater.from(this.getContext())
                .inflate(R.layout.question_approach_card, null, false);

        ButterKnife.inject(this, view);

        initApproachList(approaches);
        setParams(view);

        addView(view);
    }

    @OnClick(R.id.close)
    public void onCloseClicked() {
        flip(AnimationFactory.FlipDirection.LEFT_RIGHT, 300);
    }

    private void initApproachList(List<Approach> approaches) {
        ApproachAdapter approachAdapter = new ApproachAdapter();

        approachList.setAdapter(approachAdapter);
        approachList.setLayoutManager(new LinearLayoutManager(this.getContext()));

        approachAdapter.setApproaches(approaches);

        // Set layout
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                MyApplication.screenWidth,
                (int) (MyApplication.screenHeight * PERCENTAGE_HEIGHT)
        );
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        approachList.setLayoutParams(layoutParams);
    }

}
