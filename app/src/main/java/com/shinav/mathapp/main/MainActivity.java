package com.shinav.mathapp.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.ProgressBar;

import com.shinav.mathapp.MyApplication;
import com.shinav.mathapp.R;
import com.shinav.mathapp.db.dataMapper.StoryProgressMapper;
import com.shinav.mathapp.db.pojo.Conversation;
import com.shinav.mathapp.db.pojo.Question;
import com.shinav.mathapp.db.pojo.Storyboard;
import com.shinav.mathapp.db.pojo.StoryboardFrame;
import com.shinav.mathapp.db.repository.ConversationRepository;
import com.shinav.mathapp.db.repository.QuestionRepository;
import com.shinav.mathapp.db.repository.StoryboardFrameRepository;
import com.shinav.mathapp.db.repository.StoryboardRepository;
import com.shinav.mathapp.firebase.FirebaseChildRegisterer;
import com.shinav.mathapp.injection.component.ComponentFactory;
import com.shinav.mathapp.main.storyboard.StoryboardFrameListItem;
import com.shinav.mathapp.main.storyboard.StoryboardFrameListItemClicked;
import com.shinav.mathapp.main.storyboard.StoryboardView;
import com.shinav.mathapp.storytelling.StorytellingService;
import com.shinav.mathapp.tab.TabsView;
import com.shinav.mathapp.tutorial.TutorialActivity;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Actions;
import rx.functions.Func1;
import rx.functions.Func3;
import rx.schedulers.Schedulers;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.shinav.mathapp.main.storyboard.StoryboardFrameListItem.TYPE_QUESTION;

public class MainActivity extends ActionBarActivity {

    @InjectView(R.id.toolbar) Toolbar toolbar;
    @InjectView(R.id.tabs_view) TabsView tabsView;
    @InjectView(R.id.progress) ProgressBar progressBar;

    @Inject Bus bus;
    @Inject FirebaseChildRegisterer registerer;
    @Inject SharedPreferences sharedPreferences;

    @Inject StoryProgressMapper storyProgressMapper;

    @Inject StoryboardView storyboardView;

    @Inject QuestionRepository questionRepository;
    @Inject ConversationRepository conversationRepository;

    @Inject StoryboardRepository storyboardRepository;
    @Inject StoryboardFrameRepository storyboardFrameRepository;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);
        ComponentFactory.getActivityComponent(this).inject(this);

        registerer.register();

        initToolbar();
        initTabs();
    }

    @Override public void onStart() {
        super.onStart();
        bus.register(this);
        loadStoryboardFrames();
    }

    @Override public void onStop() {
        super.onStop();
        bus.unregister(this);
    }

    private void loadStoryboardFrames() {

        int characterResId = sharedPreferences.getInt(MyApplication.PREF_CHOSEN_CHARACTER, 0);

        if (characterResId == 0) {

            progressBar.setVisibility(VISIBLE);

            toolbar.setTitle(getResources().getString(R.string.choose_character));

            // Wait 5 seconds to load the data the first time.
            Observable.timer(5000, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Long>() {
                        @Override public void call(Long aLong) {
                            progressBar.setVisibility(GONE);
                            showTutorial();
                        }
                    });

        } else {
            loadStoryboard();
        }

    }

    private void loadStoryboard() {
        storyboardRepository.getFirst(new Action1<Storyboard>() {
            @Override public void call(Storyboard storyboard) {
                startStorytellingService(storyboard.getKey());
                loadStoryboardFrames(storyboard);
            }
        });
    }

    private void loadStoryboardFrames(final Storyboard storyboard) {

        final Observable<List<StoryboardFrame>> framesObservable =
                storyboardFrameRepository.getByStoryboardKey(storyboard.getKey());

        framesObservable.subscribe(new Action1<List<StoryboardFrame>>() {
            @Override public void call(List<StoryboardFrame> storyboardFrames) {

                List<String> questionKeys = new ArrayList<>();
                List<String> conversationKeys = new ArrayList<>();

                for (StoryboardFrame storyboardFrame : storyboardFrames) {
                    if (storyboardFrame.isQuestion()) {
                        questionKeys.add(storyboardFrame.getFrameTypeKey());

                    } else if (storyboardFrame.isConversation()) {
                        conversationKeys.add(storyboardFrame.getFrameTypeKey());
                    }
                }

                String questionKeysString = TextUtils.join("','", questionKeys);
                String conversationKeysString = TextUtils.join("','", conversationKeys);

                Observable<List<Question>> questionObservable =
                        questionRepository.getCollection(questionKeysString)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread());

                Observable<List<StoryboardFrameListItem>> questionFramesObservable =
                        questionObservable.map(new Func1<List<Question>, List<StoryboardFrameListItem>>() {
                            @Override public List<StoryboardFrameListItem> call(List<Question> questions) {

                                List<StoryboardFrameListItem> listItems = new ArrayList<>();

                                for (final Question question : questions) {

                                    listItems.add(new StoryboardFrameListItem() {

                                                      @Override public String getKey() {
                                                          return question.getKey();
                                                      }

                                                      @Override public String getType() {
                                                          return StoryboardFrameListItem.TYPE_QUESTION;
                                                      }

                                                      @Override public String getTitle() {
                                                          return question.getTitle();
                                                      }

                                                      @Override public int getState() {
                                                          return question.getProgressState();
                                                      }

                                                      @Override public String getBackgroundImage() {
                                                          return question.getBackgroundImageUrl();
                                                      }
                                                  }
                                    );
                                }

                                return listItems;
                            }
                        });

                Observable<List<Conversation>> conversationObservable =
                        conversationRepository.getCollection(conversationKeysString)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread());

                Observable<List<StoryboardFrameListItem>> conversationFramesObserable =

                        conversationObservable.map(new Func1<List<Conversation>, List<StoryboardFrameListItem>>() {
                            @Override
                            public List<StoryboardFrameListItem> call(List<Conversation> conversations) {

                                List<StoryboardFrameListItem> listItems = new ArrayList<>();

                                for (final Conversation conversation : conversations) {

                                    listItems.add(new StoryboardFrameListItem() {

                                                      @Override public String getKey() {
                                                          return conversation.getKey();
                                                      }

                                                      @Override public String getType() {
                                                          return StoryboardFrameListItem.TYPE_CONVERSATION;
                                                      }

                                                      @Override public String getTitle() {
                                                          return conversation.getTitle();
                                                      }

                                                      @Override public int getState() {
                                                          return 0;
                                                      }

                                                      @Override public String getBackgroundImage() {
                                                          return conversation.getBackgroundImageUrl();
                                                      }
                                                  }
                                    );
                                }

                                return listItems;
                            }
                        });

                Observable.combineLatest(
                        framesObservable,
                        questionFramesObservable,
                        conversationFramesObserable,
                        new FramesReady()
                ).first().subscribe(Actions.empty());

            }
        });
    }

    private class FramesReady implements Func3<
            List<StoryboardFrame>,
            List<StoryboardFrameListItem>,
            List<StoryboardFrameListItem>,
            Void> {
        @Override
        public Void call(
                List<StoryboardFrame> storyboardFrames,
                List<StoryboardFrameListItem> storyboardFrameListItems,
                List<StoryboardFrameListItem> storyboardFrameListItems2
        ) {

            storyboardFrameListItems.addAll(storyboardFrameListItems2);

            ArrayList<String> frameObjectKeys = new ArrayList<>(storyboardFrames.size());

            for (StoryboardFrame frame : storyboardFrames) {
                frameObjectKeys.add(frame.getFrameTypeKey());
            }

            List<StoryboardFrameListItem> listItems = new ArrayList<>(storyboardFrames.size());
            for (StoryboardFrameListItem frameListItem : storyboardFrameListItems) {
                int index = frameObjectKeys.indexOf(frameListItem.getKey());
                listItems.add(index, frameListItem);
            }

            for (StoryboardFrameListItem listItem : listItems) {

                if (listItem.getState() == StoryboardFrameListItem.STATE_CLOSED) {

                    final StoryboardFrameListItem finalListItem = listItem;
                    listItem = new StoryboardFrameListItem() {
                        @Override public String getKey() {
                            return finalListItem.getKey();
                        }

                        @Override public String getType() {
                            return finalListItem.getType();
                        }

                        @Override public String getTitle() {
                            return finalListItem.getTitle();
                        }

                        @Override public int getState() {
                            return StoryboardFrameListItem.STATE_OPENED;
                        }

                        @Override public String getBackgroundImage() {
                            return finalListItem.getBackgroundImage();
                        }

                    };

                    // Make every frame open until next question
                    if (listItem.getType().equals(TYPE_QUESTION)) {
                        break;
                    }

                }

            }

            storyboardView.setListItems(listItems);
            return null;
        }

    }

    private void initToolbar() {
        toolbar.setTitle(R.string.main_toolbar_title);
        setSupportActionBar(toolbar);
    }

    private void initTabs() {
        tabsView.addTab(getResources().getString(R.string.main_tab_1), storyboardView);
    }

    private void showTutorial() {
        Intent intent = new Intent(this, TutorialActivity.class);
        startActivity(intent);
        finish();
    }

    private void startStorytellingService(String storyboardKey) {
        Intent intent = new Intent(this, StorytellingService.class);

        intent.setAction(StorytellingService.ACTION_START);
        intent.putExtra(StorytellingService.EXTRA_STORYBOARD_KEY, storyboardKey);

        startService(intent);
    }

    @Subscribe public void onStoryboardFrameListItemClicked(StoryboardFrameListItemClicked event) {
        Intent intent = new Intent(this, StorytellingService.class);

        intent.setAction(StorytellingService.ACTION_START_FROM);
        intent.putExtra(StorytellingService.EXTRA_FRAME_TYPE_KEY, event.getFrameTypeKey());

        startService(intent);
    }

}
