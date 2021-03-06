package com.shinav.mathapp.db.repository;

import com.shinav.mathapp.db.cursorParser.TutorialFrameListCursorParser;
import com.shinav.mathapp.db.pojo.TutorialFrame;
import com.squareup.sqlbrite.SqlBrite;

import java.util.List;

import javax.inject.Inject;

import rx.functions.Action1;

import static com.shinav.mathapp.db.helper.Tables.TutorialFrame.TABLE_NAME;
import static com.shinav.mathapp.db.helper.Tables.TutorialFrame.TUTORIAL_KEY;

public class TutorialFrameRepository {

    @Inject SqlBrite db;
    @Inject TutorialFrameListCursorParser parser;

    @Inject
    public TutorialFrameRepository() { }

    public void getByTutorialKey(String tutorialKey, Action1<List<TutorialFrame>> action) {
        db.createQuery(
                TABLE_NAME,
                "SELECT * FROM " + TABLE_NAME +
                        " WHERE " + TUTORIAL_KEY + " = ?"
                , tutorialKey
        ).map(parser).first().subscribe(action);
    }

}
