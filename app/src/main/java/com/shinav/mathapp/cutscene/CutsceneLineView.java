package com.shinav.mathapp.cutscene;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.shinav.mathapp.MyApplication;
import com.shinav.mathapp.R;
import com.shinav.mathapp.db.pojo.CutsceneLine;
import com.shinav.mathapp.event.CutsceneLineTextShownEvent;
import com.shinav.mathapp.injection.component.Injector;
import com.shinav.mathapp.view.ButterKnifeLayout;
import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

public class CutsceneLineView extends ButterKnifeLayout {

    public static final String IS_TYPING_TEXT = "aan het typen";
    public static final int DELAY_MILLIS = 150;

    private Bus bus;
    private ViewHolder holder;
    private CutsceneLine cutsceneLine;

    @Inject SharedPreferences sharedPreferences;

    public CutsceneLineView(Context context, CutsceneLine cutsceneLine, Bus bus) {
        super(context);
        this.bus = bus;
        this.cutsceneLine = cutsceneLine;

        init();
    }

    private void init() {
        Injector.getViewComponent(this.getContext()).inject(this);

        int layout = getLayout(cutsceneLine);
        View view = inflate(layout, this, false);

        holder = new ViewHolder(view);

        loadCharacterImage(cutsceneLine);
        holder.line_value.setVisibility(INVISIBLE);

        addView(view);
    }

    private int getLayout(CutsceneLine cutsceneLine) {
        int layout;

        switch (cutsceneLine.getAlignment()) {
            case CutsceneLine.ALIGNMENT_RIGHT:
                layout = R.layout.cutscene_line_right;
                break;
            default:
                layout = R.layout.cutscene_line_left;
        }
        return layout;
    }

    private void loadCharacterImage(CutsceneLine cutsceneLine) {
        int characterResId = sharedPreferences.getInt(MyApplication.PREF_CHOSEN_CHARACTER, 0);

        if (cutsceneLine.isMainCharacter() && characterResId != 0) {
            holder.image.setImageResource(characterResId);
        } else {
            Picasso.with(this.getContext())
                    .load(cutsceneLine.getImageUrl())
                    .centerCrop()
                    .fit()
                    .into(holder.image);
        }
    }

    public void startTyping() {

        holder.line_value.setText(IS_TYPING_TEXT);
        holder.line_value.setVisibility(VISIBLE);

        final Subscription typingSubscription = Observable.interval(DELAY_MILLIS, TimeUnit.MILLISECONDS).map(new Func1<Long, String>() {

            @Override public String call(Long aLong) {

                String text = IS_TYPING_TEXT + "   ";

                switch ((int) (aLong % 3)) {
                    case 0:
                        text = IS_TYPING_TEXT + ".  ";
                        break;
                    case 1:
                        text = IS_TYPING_TEXT + ".. ";
                        break;
                    case 2:
                        text = IS_TYPING_TEXT + "...";
                }

                return text;
            }

        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override public void call(String text) {
                        holder.line_value.setText(text);
                    }
                });

        Observable<Long> timer = Observable.timer(cutsceneLine.getTypingDuration(), TimeUnit.MILLISECONDS);
        timer
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override public void call(Long aLong) {
                        typingSubscription.unsubscribe();
                        showMessage();
                    }
                });

    }

    public void showMessage() {
        holder.line_value.setText(cutsceneLine.getValue());
        bus.post(new CutsceneLineTextShownEvent(cutsceneLine.getPosition()));
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.cutscene_line_value) TextView line_value;
        @InjectView(R.id.cutscene_line_image) ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }

}
