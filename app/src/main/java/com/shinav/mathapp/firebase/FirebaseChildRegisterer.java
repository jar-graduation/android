package com.shinav.mathapp.firebase;

import com.firebase.client.ChildEventListener;
import com.firebase.client.Firebase;
import com.shinav.mathapp.firebase.listener.FirebaseApproachListener;
import com.shinav.mathapp.firebase.listener.FirebaseApproachPartListener;
import com.shinav.mathapp.firebase.listener.FirebaseConversationListener;
import com.shinav.mathapp.firebase.listener.FirebaseConversationPartListener;
import com.shinav.mathapp.firebase.listener.FirebaseQuestionListener;
import com.shinav.mathapp.firebase.listener.FirebaseStoryListener;
import com.shinav.mathapp.firebase.listener.FirebaseStoryPartListener;

import javax.inject.Inject;

import static com.shinav.mathapp.firebase.FirebaseNodes.Nodes;

public class FirebaseChildRegisterer {

    private final Firebase firebase;
    private final FirebaseQuestionListener firebaseQuestionListener;
    private final FirebaseStoryListener firebaseStoryListener;
    private final FirebaseConversationListener firebaseConversationListener;
    private final FirebaseApproachListener firebaseApproachListener;
    private final FirebaseApproachPartListener firebaseApproachPartListener;
    private final FirebaseStoryPartListener firebaseStoryPartListener;
    private final FirebaseConversationPartListener firebaseConversationPartListener;

    @Inject
    public FirebaseChildRegisterer(
            Firebase firebase,
            FirebaseQuestionListener firebaseQuestionListener,
            FirebaseStoryListener firebaseStoryListener,
            FirebaseConversationListener firebaseConversationListener,
            FirebaseConversationPartListener firebaseConversationPartListener,
            FirebaseApproachListener firebaseApproachListener,
            FirebaseApproachPartListener firebaseApproachPartListener,
            FirebaseStoryPartListener firebaseStoryPartListener
    ) {
        this.firebase = firebase;
        this.firebaseQuestionListener = firebaseQuestionListener;
        this.firebaseStoryListener = firebaseStoryListener;
        this.firebaseConversationListener = firebaseConversationListener;
        this.firebaseConversationPartListener = firebaseConversationPartListener;
        this.firebaseApproachListener = firebaseApproachListener;
        this.firebaseApproachPartListener = firebaseApproachPartListener;
        this.firebaseStoryPartListener = firebaseStoryPartListener;
    }

    public void register() {
        addChildEventListener(Nodes.QUESTIONS, firebaseQuestionListener);
        addChildEventListener(Nodes.STORIES, firebaseStoryListener);
        addChildEventListener(Nodes.CONVERSATIONS, firebaseConversationListener);
        addChildEventListener(Nodes.CONVERSATION_PARTS, firebaseConversationPartListener);
        addChildEventListener(Nodes.APPROACHES, firebaseApproachListener);
        addChildEventListener(Nodes.APPROACH_PARTS, firebaseApproachPartListener);
        addChildEventListener(Nodes.STORIES, firebaseStoryListener);
        addChildEventListener(Nodes.STORY_PARTS, firebaseStoryPartListener);
    }

    private void addChildEventListener(String node, ChildEventListener listener) {
        firebase.child(node).addChildEventListener(listener);
    }

}