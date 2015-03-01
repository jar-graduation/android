package com.shinav.mathapp.sync;

import com.firebase.client.Firebase;
import com.shinav.mathapp.firebase.FirebaseInterface;
import com.shinav.mathapp.firebase.FirebaseProvider;
import com.shinav.mathapp.firebase.listeners.FirebaseQuestionListener;

public final class FirebaseRealmAdapter {

    private static final FirebaseRealmAdapter ADAPTER = new FirebaseRealmAdapter();
    private final Firebase REF = FirebaseProvider.getBaseRef();

    public static FirebaseRealmAdapter getInstance() {
        return ADAPTER;
    }

    public void register() {
        REF.child(FirebaseInterface.Nodes.QUESTIONS).addChildEventListener(new FirebaseQuestionListener());
    }

}