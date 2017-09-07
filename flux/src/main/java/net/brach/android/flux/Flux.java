package net.brach.android.flux;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.annotation.TargetApi;
import android.os.Build;

import java.util.ArrayList;

public interface Flux {
    void addListener(Animator.AnimatorListener listener);

    void start();

    void cancel();

    void end();

    @TargetApi(Build.VERSION_CODES.KITKAT)
    void pause();

    @TargetApi(Build.VERSION_CODES.KITKAT)
    void resume();

    @TargetApi(Build.VERSION_CODES.KITKAT)
    boolean isPaused();

    @TargetApi(Build.VERSION_CODES.N)
    long getTotalDuration();

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    boolean isStarted();

    void removeListener(Animator.AnimatorListener listener);

    ArrayList<Animator.AnimatorListener> getListeners();

    @TargetApi(Build.VERSION_CODES.KITKAT)
    void addPauseListener(Animator.AnimatorPauseListener listener);

    @TargetApi(Build.VERSION_CODES.KITKAT)
    void removePauseListener(Animator.AnimatorPauseListener listener);

    void removeAllListeners();

    long getDuration();

    boolean isRunning();
}
