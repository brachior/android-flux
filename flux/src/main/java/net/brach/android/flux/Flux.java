package net.brach.android.flux;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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

    void remove();

    class Builder {
        private static final Random random = new Random();

        private int topOffset;
        private final ViewGroup root;
        private final DisplayMetrics dm;

        private View from, to;

        private int count;
        private int x1, y1;
        private int x4, y4;

        private FluxType type;

        private int durationMin;
        private int durationMax;

        private int maxDuration;

        private final List<Animator> animators;
        private final ArrayList<TimeInterpolator> interpolators;

        public Builder(final Activity activity) {
            root = activity.findViewById(Window.ID_ANDROID_CONTENT);

            dm = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(dm);

            type = null;
            durationMin = durationMax = topOffset = count = x1 = y1 = x4 = y4 = -1;
            maxDuration = 0;

            animators = new ArrayList<>();
            interpolators = new ArrayList<>();
        }

        public Builder number(int count) {
            this.count = count;

            return this;
        }

        public Builder from(View from) {
            this.from = from;

            return this;
        }

        public Builder to(View to) {
            this.to = to;

            return this;
        }

        public Builder duration(int min, int max) {
            this.durationMin = min;
            this.durationMax = max;

            return this;
        }

        public Builder circle(float radiusMin, float radiusMax) {
            if (type != null) {
                throw new IllegalStateException();
            }

            type = new Radius(radiusMin, radiusMax);

            return this;
        }

        public Builder assets(List<String> assets, int sizeMin, int sizeMax) {
            if (type != null) {
                throw new IllegalStateException();
            }

            type = new Asset(assets, sizeMin, sizeMax);

            return this;
        }

        public Builder addInterpolators(TimeInterpolator... interpolators) {
            Collections.addAll(this.interpolators, interpolators);

            return this;
        }

        public Builder removeInterpolators(TimeInterpolator... interpolators) {
            for (TimeInterpolator interpolator : interpolators) {
                this.interpolators.remove(interpolator);
            }

            return this;
        }

        public Builder clearInterpolators() {
            interpolators.clear();

            return this;
        }

        public void run() {
            build().start();
        }

        public Flux build() {
            if (type == null || from == null || to == null
                    || durationMin == -1 || durationMax == -1 || count == -1) {
                throw new IllegalStateException();
            }

            final FluxImpl flux = new FluxImpl();

            root.post(new Runnable() {
                @Override
                public void run() {
                    topOffset = dm.heightPixels - root.getMeasuredHeight();

                    from.post(new Runnable() {
                        @Override
                        public void run() {
                            int[] location = new int[2];
                            from.getLocationInWindow(location);

                            x1 = location[0] + from.getWidth() / 2;
                            y1 = location[1] + from.getHeight() / 2 - topOffset;

                            to.post(new Runnable() {
                                @Override
                                public void run() {
                                    int[] location = new int[2];
                                    to.getLocationInWindow(location);

                                    x4 = location[0] + to.getWidth() / 2;
                                    y4 = location[1] + to.getHeight() / 2 - topOffset;

                                    if (interpolators.isEmpty()) {
                                        defaultInterpolators();
                                    }

                                    ArrayList<Particle> particles = type.build();

                                    final AnimatorSet set = new AnimatorSet();
                                    set.playTogether(animators);
                                    set.setStartDelay(250);

                                    ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
                                    animator.setDuration(250);
                                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                        @Override
                                        public void onAnimationUpdate(ValueAnimator animation) {
                                            from.setTranslationX((random.nextFloat() - 0.5f) * from.getWidth() * 0.05f);
                                            from.setTranslationY((random.nextFloat() - 0.5f) * from.getHeight() * 0.05f);
                                        }
                                    });
                                    animator.addListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationStart(Animator animation) {
                                            set.start();
                                        }

                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            from.setAlpha(0);
                                        }
                                    });

                                    flux.init(particles, animator);
                                }
                            });
                        }
                    });
                }
            });

            return flux;
        }

        /*************/
        /** private **/
        /*************/

        private void defaultInterpolators() {
            interpolators.add(new AccelerateInterpolator());
            interpolators.add(new DecelerateInterpolator());
            interpolators.add(new AccelerateDecelerateInterpolator());
            interpolators.add(new AnticipateInterpolator());
            interpolators.add(new AnticipateOvershootInterpolator());
            interpolators.add(new OvershootInterpolator());
            interpolators.add(new FastOutSlowInInterpolator());
        }

        private Context getContext() {
            return root.getContext();
        }

        private static float middle(Random random, float start, float end) {
            float direction = end - start;

            if (direction > 0) {
                return start + random.nextFloat() * direction;
            } else if (direction < 0) {
                return end + random.nextFloat() * direction;
            } else {
                return start;
            }
        }

        @Nullable
        private static Bitmap createBitmap(Context ctx, String asset, int size) {
            try {
                InputStream is = ctx.getAssets().open(asset);
                Bitmap imageBitmap = BitmapFactory.decodeStream(is);

                float width = imageBitmap.getWidth();
                float height = imageBitmap.getHeight();
                float ratio = width / height;
                imageBitmap = Bitmap.createScaledBitmap(imageBitmap, (int) (size * ratio), size, false);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                return imageBitmap;
            } catch (Exception ex) {
                return null;
            }
        }

        private abstract class FluxType {
            ArrayList<Particle> build() {
                Context ctx = getContext();
                int durationInterval = durationMax - durationMin;
                ArrayList<Particle> particles = new ArrayList<>(count);

                for (int i = 0; i < count; i++) {
                    float x1 = Builder.this.x1 + (int) (random.nextBoolean() ? -(random.nextFloat() * (from.getWidth() / 4)) : random.nextFloat() * (from.getWidth() / 4));
                    float y1 = Builder.this.y1 - (int) (random.nextBoolean() ? -(random.nextFloat() * from.getHeight() / 3) : random.nextFloat() * from.getHeight() / 3);

                    float x2 = x1 + (random.nextBoolean() ? -random.nextFloat() * 200 : random.nextFloat() * 200);
                    float y2 = y1 - (100 + random.nextFloat() * 200);

                    float x3 = middle(random, x2, x4);
                    float y3 = middle(random, y2, y4);

                    Path path = new Path();
                    path.moveTo(x1, y1);
                    path.cubicTo(x2, y2, x3, y3, x4, y4);

                    int duration = random.nextInt(durationInterval) + durationMin;
                    TimeInterpolator interpolator = interpolators.get(random.nextInt(interpolators.size()));

                    int delay = random.nextInt(duration / 2);
                    duration -= delay;

                    final Particle particle = create(ctx, path, delay, duration, interpolator);
                    animators.add(particle.anim());
                    particles.add(particle);
                    root.addView(particle);

                    if (duration > maxDuration) {
                        maxDuration = duration;
                    }
                }

                return particles;
            }

            abstract Particle create(Context ctx, Path path, int delay, int duration, TimeInterpolator interpolator);
        }

        private final class Radius extends FluxType {
            private final float min;
            private final float max;

            private Radius(float min, float max) {
                this.min = min;
                this.max = max - min;
            }

            @Override
            Particle create(Context ctx, Path path, int delay, int duration, TimeInterpolator interpolator) {
                float radius = min + (max * random.nextFloat());

                Particle particle = new Particle(ctx);
                particle.init(root, path, x1, y1, radius, delay, duration, interpolator);
                return particle;
            }
        }

        private final class Asset extends FluxType {
            private final List<String> assets;
            private final int min;
            private final int max;

            private Asset(List<String> assets, int min, int max) {
                this.assets = assets;
                this.min = min;
                this.max = max - min;
            }

            @Override
            Particle create(Context ctx, Path path, int delay, int duration, TimeInterpolator interpolator) {
                int size = random.nextInt(max) + min;

                Particle particle = new Particle(ctx);
                particle.init(root, path, x1, y1, size, assets.get(random.nextInt(assets.size())), delay, duration, interpolator);
                return particle;
            }
        }

        public static class Particle extends View {
            private Paint paint;

            private float cx;
            private float cy;
            private ValueAnimator animator;

            private Particle.Behavior behavior;

            public Particle(Context context) {
                super(context);
            }

            public Particle(Context context, @Nullable AttributeSet attrs) {
                super(context, attrs);
            }

            public Particle(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
                super(context, attrs, defStyleAttr);
            }

            /******************/
            /** {@link View} **/
            /******************/

            @Override
            protected void onDraw(Canvas canvas) {
                behavior.draw(canvas);
                super.onDraw(canvas);
            }

            /*************/
            /** private **/
            /*************/

            void init(ViewGroup root, Path path, float cx, float cy, float radius, int delay, int duration, TimeInterpolator interpolator) {
                behavior = new Particle.Radius(radius);
                init(root, path, cx, cy, delay, duration, interpolator);
            }

            void init(ViewGroup root, Path path, float cx, float cy, int size, String asset, int delay, int duration, TimeInterpolator interpolator) {
                behavior = new Particle.Asset(asset, size);
                init(root, path, cx, cy, delay, duration, interpolator);
            }

            Animator anim() {
                return animator;
            }

            private void init(final ViewGroup root, final Path path, float cx, float cy, int delay, int duration, TimeInterpolator interpolator) {
                paint = new Paint();
                paint.setColor(Color.BLUE);

                this.cx = cx;
                this.cy = cy;

                ValueAnimator animator = ValueAnimator.ofFloat(0.f, 1.f);
                animator.setDuration(duration);
                animator.setStartDelay(delay);
                animator.setInterpolator(interpolator);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    private final float[] point = new float[2];

                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float val = (float) animation.getAnimatedValue();

                        PathMeasure pathMeasure = new PathMeasure(path, false);
                        pathMeasure.getPosTan(pathMeasure.getLength() * val, point, null);

                        move(point[0], point[1]);
                    }
                });
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        setVisibility(VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        root.removeView(Particle.this);
                    }
                });

                this.animator = animator;

                setVisibility(GONE);
            }

            void move(float x, float y) {
                this.cx = x;
                this.cy = y;

                invalidate();
            }

            private class Radius implements Particle.Behavior {
                private float radius;

                Radius(float radius) {
                    this.radius = radius;
                }

                @Override
                public void draw(Canvas canvas) {
                    canvas.drawCircle(cx, cy, radius, paint);
                }
            }

            private class Asset implements Particle.Behavior {
                private Bitmap bitmap;

                Asset(String asset, int size) {
                    bitmap = createBitmap(getContext(), asset, size);
                }

                @Override
                public void draw(Canvas canvas) {
                    canvas.drawBitmap(bitmap, cx - bitmap.getWidth() / 2, cy - bitmap.getHeight() / 2, paint);
                }
            }

            private interface Behavior {
                void draw(Canvas canvas);
            }
        }
    }
}
