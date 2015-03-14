package com.shinav.mathapp.approach;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.RelativeLayout;

import com.shinav.mathapp.MyApplication;
import com.shinav.mathapp.R;
import com.shinav.mathapp.approach.feedback.ApproachFeedbackActivity;
import com.shinav.mathapp.drag.DragSortRecycler;
import com.shinav.mathapp.progress.ProgressProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class ApproachActivity extends Activity {

    @InjectView(R.id.approach_list) RecyclerView approachList;
    private ApproachAdapter approachAdapter;
    private List<Approach> approaches;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_approach);

        ButterKnife.inject(this);

        initApproachList();

        populate();
    }

    private void initApproachList() {
        approachAdapter = new ApproachAdapter();

        approachList.setAdapter(approachAdapter);
        approachList.setLayoutManager(new LinearLayoutManager(this));
        approachList.setItemAnimator(null);

        populate();

        setupDrag();
        setupDragArea();
    }

    private void populate() {
        approaches = new LinkedList<>(Arrays.asList(
                new Approach("Uit de tekst halen dat het om de getallen 200 en 163 gaat.", 0),
                new Approach("Uitrekenen hoeveel procent nu geschreven is.", 1),
                new Approach("Uitrekenen hoeveel procent nog geschreven moet worden.", 2),
                new Approach("Afronden op juiste aantal decimalen.", 3)
        ));

        Collections.shuffle(approaches);

        approachAdapter.setApproaches(approaches);
    }

    private void setupDragArea() {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                MyApplication.screenWidth,
                (int) (MyApplication.screenHeight * 0.41f)
        );
        layoutParams.addRule(RelativeLayout.ABOVE, R.id.submit_button);
        approachList.setLayoutParams(layoutParams);
    }

    private void setupDrag() {
        DragSortRecycler dragSortRecycler = new DragSortRecycler();
        dragSortRecycler.setViewHandleId(R.id.approach_list_item);
        dragSortRecycler.setFloatingBgColor(Color.parseColor("#ffffff"));

        dragSortRecycler.setOnItemMovedListener(new DragSortRecycler.OnItemMovedListener() {
            @Override public void onItemMoved(int from, int to) {
                approaches.add(to, approaches.remove(from));
                approachAdapter.setApproaches(approaches);
            }
        });

        approachList.addItemDecoration(dragSortRecycler);
        approachList.addOnItemTouchListener(dragSortRecycler);
        approachList.setOnScrollListener(dragSortRecycler.getScrollListener());
    }

    @OnClick(R.id.submit_button)
    public void onSubmitClicked() {
        ProgressProvider.setCurrentApproach(approaches);
        startActivity(new Intent(this, ApproachFeedbackActivity.class));
        overridePendingTransition(R.anim.slide_left_from_outside, R.anim.slide_left_to_outside);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_right_from_outside, R.anim.slide_right_to_outside);
    }

}
