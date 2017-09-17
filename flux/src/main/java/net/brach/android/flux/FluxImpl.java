package net.brach.android.flux;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;

import java.util.ArrayList;

class FluxImpl implements Flux {
    private final static int STOPPED = 0;
    private final static int STARTED = 1;
    private final static int PAUSED = 2;
    private final static int RESUMED = 3;
    private final static int CANCELED = 4;
    private final static int ENDED = 5;
    private final static int REMOVED = 6;

    private Flux delegate;

    FluxImpl() {
        this.delegate = new EmptyFlux();
    }

    void init(ArrayList<Flux.Builder.Particle> particles, Animator animator) {
        Flux flux = this.delegate;
        this.delegate = new AnimFlux(particles, animator);
        if (flux instanceof EmptyFlux) {
            EmptyFlux previous = (EmptyFlux) flux;

            for (Animator.AnimatorListener listener: previous.listeners) {
                this.delegate.addListener(listener);
            }

            for (Animator.AnimatorPauseListener listener: previous.pauseListeners) {
                this.delegate.addPauseListener(listener);
            }

            switch (previous.status) {
                case STARTED:
                    this.delegate.start();
                    break;
                case PAUSED:
                    this.delegate.start();
                    this.delegate.pause();
                    break;
                case RESUMED:
                    this.delegate.start();
                    this.delegate.pause();
                    this.delegate.resume();
                    break;
                case CANCELED:
                    this.delegate.start();
                    this.delegate.cancel();
                    break;
                case ENDED:
                    this.delegate.end();
                    break;
                case REMOVED:
                    this.delegate.remove();
                    break;
                case STOPPED:
                    break;
            }
        }
    }

    public void addListener(Animator.AnimatorListener listener) {
        this.delegate.addListener(listener);
    }

    public void start() {
        this.delegate.start();
    }

    public void cancel() {
        this.delegate.cancel();
    }

    public void end() {
        this.delegate.end();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void pause() {
        this.delegate.pause();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void resume() {
        this.delegate.resume();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public boolean isPaused() {
        return this.delegate.isPaused();
    }

    @TargetApi(Build.VERSION_CODES.N)
    public long getTotalDuration() {
        return this.delegate.getTotalDuration();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public boolean isStarted() {
        return this.delegate.isStarted();
    }

    public void removeListener(Animator.AnimatorListener listener) {
        this.delegate.removeListener(listener);
    }

    public ArrayList<Animator.AnimatorListener> getListeners() {
        return this.delegate.getListeners();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void addPauseListener(Animator.AnimatorPauseListener listener) {
        this.delegate.addPauseListener(listener);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void removePauseListener(Animator.AnimatorPauseListener listener) {
        this.delegate.removePauseListener(listener);
    }

    public void removeAllListeners() {
        this.delegate.removeAllListeners();
    }

    public long getDuration() {
        return this.delegate.getDuration();
    }

    public boolean isRunning() {
        return this.delegate.isRunning();
    }

    @Override
    public void remove() {
        this.delegate.remove();
    }

    @Override
    public String toString() {
        return this.delegate.getClass().getSimpleName();
    }

    /*************/
    /** private **/
    /*************/

    private final class AnimFlux implements Flux {
        private final ArrayList<Flux.Builder.Particle> particles;
        private final Animator animator;

        AnimFlux(ArrayList<Flux.Builder.Particle> particles, Animator animator) {
            this.particles = particles;
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
            remove();
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

        @Override
        public void remove() {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    for (Flux.Builder.Particle particle: particles) {
                        ((ViewGroup) particle.getParent()).removeView(particle);
                    }
                }
            });
        }
    }

    private static final class EmptyFlux implements Flux {
        int status;
        ArrayList<Animator.AnimatorListener> listeners;
        ArrayList<Animator.AnimatorPauseListener> pauseListeners;

        EmptyFlux() {
            status = STOPPED;
            listeners = new ArrayList<>();
            pauseListeners = new ArrayList<>();
        }

        @Override
        public void addListener(Animator.AnimatorListener listener) {
            listeners.add(listener);
        }

        @Override
        public void start() {
            status = STARTED;
        }

        @Override
        public void cancel() {
            status = CANCELED;
        }

        @Override
        public void end() {
            status = ENDED;
        }

        @Override
        @TargetApi(Build.VERSION_CODES.KITKAT)
        public void pause() {
            status = PAUSED;
        }

        @Override
        @TargetApi(Build.VERSION_CODES.KITKAT)
        public void resume() {
            status = RESUMED;
        }

        @Override
        @TargetApi(Build.VERSION_CODES.KITKAT)
        public boolean isPaused() {
            return status == PAUSED;
        }

        @Override
        @TargetApi(Build.VERSION_CODES.N)
        public long getTotalDuration() {
            return 0;
        }

        @Override
        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        public boolean isStarted() {
            return status == STARTED;
        }

        @Override
        public void removeListener(Animator.AnimatorListener listener) {
            listeners.remove(listener);
        }

        @Override
        public ArrayList<Animator.AnimatorListener> getListeners() {
            return listeners;
        }

        @Override
        @TargetApi(Build.VERSION_CODES.KITKAT)
        public void addPauseListener(Animator.AnimatorPauseListener listener) {
            pauseListeners.add(listener);
        }

        @Override
        @TargetApi(Build.VERSION_CODES.KITKAT)
        public void removePauseListener(Animator.AnimatorPauseListener listener) {
            pauseListeners.remove(listener);
        }

        @Override
        public void removeAllListeners() {
            listeners.clear();
            pauseListeners.clear();
        }

        @Override
        public long getDuration() {
            return 0;
        }

        @Override
        public boolean isRunning() {
            return status == STARTED || status == RESUMED;
        }

        @Override
        public void remove() {
            status = REMOVED;
        }
    }
}
