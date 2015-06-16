package com.shinav.mathapp.firebase.listener;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.shinav.mathapp.db.dataMapper.TutorialDataMapper;
import com.shinav.mathapp.db.pojo.Tutorial;
import com.shinav.mathapp.firebase.FirebaseParser;

import javax.inject.Inject;

import timber.log.Timber;

public class FirebaseTutorialListener implements ChildEventListener {

    @Inject FirebaseParser firebaseParser;
    @Inject TutorialDataMapper tutorialDataMapper;

    @Inject
    public FirebaseTutorialListener() { }

    @Override public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        Tutorial tutorial = firebaseParser.parseTutorial(dataSnapshot);
        tutorialDataMapper.insert(tutorial);

        Timber.d("Firebase added a Tutorial");
    }

    @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        Tutorial tutorial = firebaseParser.parseTutorial(dataSnapshot);
        tutorialDataMapper.update(tutorial, tutorial.getKey());

        Timber.d("Firebase changed a Tutorial");
    }

    @Override public void onChildRemoved(DataSnapshot dataSnapshot) {
        tutorialDataMapper.delete(dataSnapshot.getKey());

        Timber.d("Firebase removed a Tutorial");
    }

    @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {  }

    @Override public void onCancelled(FirebaseError firebaseError) {  }

}
