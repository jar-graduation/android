package com.shinav.mathapp.tutorial;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.shinav.mathapp.db.dataMapper.TutorialMapper;
import com.shinav.mathapp.db.dataMapper.TutorialPartMapper;
import com.shinav.mathapp.db.pojo.Tutorial;
import com.shinav.mathapp.db.pojo.TutorialPart;
import com.shinav.mathapp.injection.component.ComponentFactory;
import com.shinav.mathapp.progress.Storyteller;

import java.util.List;

import javax.inject.Inject;

public class TutorialManagingService extends Service {

    public static final String PERSPECTIVE_MALE = "male";
    public static final String PERSPECTIVE_FEMALE = "female";

    public static final String EXTRA_PERSPECTIVE = "perspective";

    public static final String ACTION_START = "start";
    public static final String ACTION_NEXT = "next";
    public static final String ACTION_RESET = "reset";

    @Inject TutorialPartMapper tutorialPartMapper;
    @Inject TutorialMapper tutorialMapper;

    private List<TutorialPart> tutorialParts;
    private int currentPosition = -1;

    @Override public IBinder onBind(Intent intent) { return null; }

    @Override public void onCreate() {
        super.onCreate();

        ComponentFactory.getApplicationComponent(this).inject(this);

    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_START:
                    fetchTutorialForPerspective(
                            intent.getStringExtra(EXTRA_PERSPECTIVE));

                    startNext();
                    break;
                case ACTION_NEXT:
                    startNext();
                    break;
                case ACTION_RESET:
                    reset();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void reset() {
        currentPosition = -1;
    }

    private void fetchTutorialForPerspective(String perspective) {
        Tutorial tutorial = tutorialMapper.getByPerspective(perspective);
        tutorialParts = tutorialPartMapper.getByTutorialKey(tutorial.getKey());
    }

    private void startNext() {
        if (currentPosition+1 < tutorialParts.size()) {
            currentPosition++;
            TutorialPart tutorialPart = tutorialParts.get(currentPosition);
            startBasedOnType(tutorialPart);
        } else {
            startMainActivity();
        }
    }

    private void startBasedOnType(TutorialPart tutorialPart) {
        if (tutorialPart.isQuestion()) {
            startActivity(TutorialQuestionActivity.class, tutorialPart.getTypeKey());

        } else if (tutorialPart.isConversation()) {
            startActivity(TutorialConversationActivity.class, tutorialPart.getTypeKey());
        }
    }

    private void startActivity(Class<? extends Activity> cls, String typeKey) {
        Intent intent = new Intent(this, cls);
        intent.putExtra(Storyteller.TYPE_KEY, typeKey);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);

//        ((Activity) this.getApplicationContext()).overridePendingTransition(R.anim.slide_left_from_outside, R.anim.slide_left_to_outside);
    }

    private void startMainActivity() {

    }


}
