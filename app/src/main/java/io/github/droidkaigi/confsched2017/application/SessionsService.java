package io.github.droidkaigi.confsched2017.application;

import android.util.Log;

import java.util.Locale;

import javax.inject.Inject;

import io.github.droidkaigi.confsched2017.model.Session;
import io.github.droidkaigi.confsched2017.repository.sessions.MySessionsRepository;
import io.github.droidkaigi.confsched2017.repository.sessions.SessionsRepository;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

public class SessionsService {

    private final String TAG = SessionsService.class.getSimpleName();

    private final SessionsRepository sessionsRepository;

    private final MySessionsRepository mySessionsRepository;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    SessionsService(SessionsRepository sessionsRepository, MySessionsRepository mySessionsRepository) {
        this.sessionsRepository = sessionsRepository;
        this.mySessionsRepository = mySessionsRepository;
    }

    public void findSession(int sessionId) {
        final String languageId = Locale.getDefault().getLanguage().toLowerCase();
        compositeDisposable.add(sessionsRepository.find(sessionId, languageId).subscribe(sessionSubject::onNext));

        existMySessionSubject.onNext(mySessionsRepository.isExist(sessionId));
    }

    public void toggleFab(Session session) {
        if (mySessionsRepository.isExist(session.id)) {
            mySessionsRepository.delete(session)
                    .subscribe((result) -> Log.d(TAG, "Deleted my session"),
                            throwable -> Log.e(TAG, "Failed to delete my session", throwable));
            deleteMySessionSubject.onNext(session);
        } else {
            mySessionsRepository.save(session)
                    .subscribe(() -> Log.d(TAG, "Saved my session"),
                            throwable -> Log.e(TAG, "Failed to save my session", throwable));
            saveMySessionSubject.onNext(session);
        }
    }

    public void dispose() {
        compositeDisposable.clear();
    }

    private final BehaviorSubject<Session> sessionSubject = BehaviorSubject.create();

    public final Observable<Session> session = sessionSubject.hide();

    private final BehaviorSubject<Boolean> existMySessionSubject = BehaviorSubject.create();

    public final Observable<Boolean> existMySession = existMySessionSubject.hide();

    private final PublishSubject<Session> deleteMySessionSubject = PublishSubject.create();
    public final Observable<Session> deleteMySession = deleteMySessionSubject.hide();

    private final PublishSubject<Session> saveMySessionSubject = PublishSubject.create();
    public final Observable<Session> saveMySession = saveMySessionSubject.hide();

}

