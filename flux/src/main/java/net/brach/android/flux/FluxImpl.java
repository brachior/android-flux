package net.brach.android.flux;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.annotation.TargetApi;
import android.os.Build;

import java.util.ArrayList;

class FluxImpl implements Flux {
    private Flux deleguate;

    FluxImpl() {
        this.deleguate = new EmptyFlux();
    }

    void init(AnimatorSet animator) {
        this.deleguate = new AnimFlux(animator);
    }

    public void addListener(Animator.AnimatorListener listener) {
        this.deleguate.addListener(listener);
    }

    public void start() {
        this.deleguate.start();
    }

    public void cancel() {
        this.deleguate.cancel();
    }

    public void end() {
        this.deleguate.end();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void pause() {
        this.deleguate.pause();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void resume() {
        this.deleguate.resume();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public boolean isPaused() {
        return this.deleguate.isPaused();
    }

    @TargetApi(Build.VERSION_CODES.N)
    public long getTotalDuration() {
        return this.deleguate.getTotalDuration();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public boolean isStarted() {
        return this.deleguate.isStarted();
    }

    public void removeListener(Animator.AnimatorListener listener) {
        this.deleguate.removeListener(listener);
    }

    public ArrayList<Animator.AnimatorListener> getListeners() {
        return this.deleguate.getListeners();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void addPauseListener(Animator.AnimatorPauseListener listener) {
        this.deleguate.addPauseListener(listener);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void removePauseListener(Animator.AnimatorPauseListener listener) {
        this.deleguate.removePauseListener(listener);
    }

    public void removeAllListeners() {
        this.deleguate.removeAllListeners();
    }

    public long getDuration() {
        return this.deleguate.getDuration();
    }

    public boolean isRunning() {
        return this.deleguate.isRunning();
    }

    /*************/
    /** private **/
    /*************/

    private final class AnimFlux implements Flux {
        private final AnimatorSet animator;

        AnimFlux(AnimatorSet animator) {
            this.animator = animator;
        }

        @Override
        public void addListener(Animator.AnimatorListener listener) {
            this.animator.addListener(listener);
        }

        @Override
        public void start() {
            animator.start();
        }

        @Override
        public void cancel() {
            animator.cancel();
        }

        @Override
        public void end() {
            animator.end();
        }

        @Override
        @TargetApi(Build.VERSION_CODES.KITKAT)
        public void pause() {
            animator.pause();
        }

        @Override
        @TargetApi(Build.VERSION_CODES.KITKAT)
        public void resume() {
            animator.resume();
        }

        @Override
        @TargetApi(Build.VERSION_CODES.KITKAT)
        public boolean isPaused() {
            return animator.isPaused();
        }

        @Override
        @TargetApi(Build.VERSION_CODES.N)
        public long getTotalDuration() {
            return animator.getTotalDuration();
        }

        @Override
        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        public boolean isStarted() {
            return animator.isStarted();
        }

        @Override
        public void removeListener(Animator.AnimatorListener listener) {
            animator.removeListener(listener);
        }

        @Override
        public ArrayList<Animator.AnimatorListener> getListeners() {
            return animator.getListeners();
        }

        @Override
        @TargetApi(Build.VERSION_CODES.KITKAT)
        public void addPauseListener(Animator.AnimatorPauseListener listener) {
            animator.addPauseListener(listener);
        }

        @Override
        @TargetApi(Build.VERSION_CODES.KITKAT)
        public void removePauseListener(Animator.AnimatorPauseListener listener) {
            animator.removePauseListener(listener);
        }

        @Override
        public void removeAllListeners() {
            animator.removeAllListeners();
        }

        @Override
        public long getDuration() {
            return animator.getDuration();
        }

        @Override
        public boolean isRunning() {
            return animator.isRunning();
        }
    }

    private final class EmptyFlux implements Flux {
        @Override
        public void addListener(Animator.AnimatorListener listener) {}

        @Override
        public void start() {}

        @Override
        public void cancel() {}

        @Override
        public void end() {}

        @Override
        @TargetApi(Build.VERSION_CODES.KITKAT)
        public void pause() {}

        @Override
        @TargetApi(Build.VERSION_CODES.KITKAT)
        public void resume() {}

        @Override
        @TargetApi(Build.VERSION_CODES.KITKAT)
        public boolean isPaused() {
            return false;
        }

        @Override
        @TargetApi(Build.VERSION_CODES.N)
        public long getTotalDuration() {
            return 0;
        }

        @Override
        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        public boolean isStarted() {
            return false;
        }

        @Override
        public void removeListener(Animator.AnimatorListener listener) {}

        @Override
        public ArrayList<Animator.AnimatorListener> getListeners() {
            return null;
        }

        @Override
        @TargetApi(Build.VERSION_CODES.KITKAT)
        public void addPauseListener(Animator.AnimatorPauseListener listener) {}

        @Override
        @TargetApi(Build.VERSION_CODES.KITKAT)
        public void removePauseListener(Animator.AnimatorPauseListener listener) {}

        @Override
        public void removeAllListeners() {}

        @Override
        public long getDuration() {
            return 0;
        }

        @Override
        public boolean isRunning() {
            return false;
        }
    }
}
