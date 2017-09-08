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

    class FluxBuilder {
        private static final Random random = new Random();

        private int topOffset;
        private final ViewGroup root;
        private final DisplayMetrics dm;

        private View from, to;

        private int count;
        private int x1, y1;
        private int x3, y3;

        private FluxType type;

        private int durationMin;
        private int durationMax;

        private int maxDuration;

        private final List<Animator> animators;
        private final ArrayList<TimeInterpolator> interpolators;

        public FluxBuilder(final Activity activity) {
            root = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);

            dm = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(dm);

            type = null;
            durationMin = durationMax = topOffset = count = x1 = y1 = x3 = y3 = -1;
            maxDuration = 0;

            animators = new ArrayList<>();
            interpolators = new ArrayList<>();
        }

        public FluxBuilder number(int count) {
            this.count = count;

            return this;
        }

        public FluxBuilder from(final View from) {
            this.from = from;

            return this;
        }

        public FluxBuilder to(final View to) {
            this.to = to;

            return this;
        }

        public FluxBuilder duration(int min, int max) {
            this.durationMin = min;
            this.durationMax = max;

            return this;
        }

        public FluxBuilder circle(float radiusMin, float radiusMax) {
            if (type != null) {
                throw new IllegalStateException();
            }

            type = new Radius(radiusMin, radiusMax);

            return this;
        }

        public FluxBuilder assets(List<String> assets, int sizeMin, int sizeMax) {
            if (type != null) {
                throw new IllegalStateException();
            }

            type = new Asset(assets, sizeMin, sizeMax);

            return this;
        }

        public FluxBuilder addInterpolators(TimeInterpolator... interpolators) {
            Collections.addAll(this.interpolators, interpolators);

            return this;
        }

        public FluxBuilder removeInterpolators(TimeInterpolator... interpolators) {
            for (TimeInterpolator interpolator : interpolators) {
                this.interpolators.remove(interpolator);
            }

            return this;
        }

        public FluxBuilder clearInterpolators() {
            interpolators.clear();

            return this;
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

                                    x3 = location[0] + to.getWidth() / 2;
                                    y3 = location[1] + to.getHeight() / 2 - topOffset;

                                    if (interpolators.isEmpty()) {
                                        defaultInterpolators();
                                    }

                                    ValueAnimator fadeOut = ValueAnimator.ofFloat(1.f, 0.f);
                                    fadeOut.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                        @Override
                                        public void onAnimationUpdate(ValueAnimator animation) {
                                            from.setAlpha((float) animation.getAnimatedValue());
                                            from.requestLayout();
                                        }
                                    });
                                    animators.add(fadeOut);

                                    type.build();

                                    fadeOut.setDuration(maxDuration + 200);

                                    AnimatorSet set = new AnimatorSet();
                                    set.playTogether(animators);

                                    flux.init(set);
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

        private interface FluxType {
            void build();
        }

        private final class Radius implements FluxType {
            private final float min;
            private final float max;

            private Radius(float min, float max) {
                this.min = min;
                this.max = max;
            }

            @Override
            public void build() {
                Context ctx = getContext();
                int durationInterval = durationMax - durationMin;
                float radiusInterval = max - min;

                for (int i = 0; i < count; i++) {
                    Path path = new Path();
                    path.moveTo(x1, y1);
                    path.quadTo(middle(random, x1, x3), middle(random, y1, y3), x3, y3);

                    Particule particule = new Particule(ctx);
                    int duration = random.nextInt(durationInterval) + durationMin;
                    float radius = min + (radiusInterval * random.nextFloat());
                    TimeInterpolator interpolator = interpolators.get(random.nextInt(interpolators.size()));
                    particule.init(root, path, x1, y1, radius, duration, interpolator);
                    animators.add(particule.anim());
                    root.addView(particule);

                    if (duration > maxDuration) {
                        maxDuration = duration;
                    }
                }
            }
        }

        private final class Asset implements FluxType {
            private final List<String> assets;
            private final int min;
            private final int max;

            private Asset(List<String> assets, int min, int max) {
                this.assets = assets;
                this.min = min;
                this.max = max;
            }

            @Override
            public void build() {
                Context ctx = getContext();
                int durationInterval = durationMax - durationMin;
                int sizeInterval = max - min;

                for (int i = 0; i < count; i++) {
                    Path path = new Path();
                    path.moveTo(x1, y1);
                    path.quadTo(middle(random, x1, x3), middle(random, y1, y3), x3, y3);

                    Particule particule = new Particule(ctx);
                    int duration = random.nextInt(durationInterval) + durationMin;
                    int size = random.nextInt(sizeInterval) + min;
                    TimeInterpolator interpolator = interpolators.get(random.nextInt(interpolators.size()));
                    particule.init(root, path, x1, y1, size, assets.get(random.nextInt(assets.size())), duration, interpolator);
                    animators.add(particule.anim());
                    root.addView(particule);

                    if (duration > maxDuration) {
                        maxDuration = duration;
                    }
                }
            }
        }

        public static class Particule extends View {
            private Paint paint;

            private float cx;
            private float cy;
            private ValueAnimator animator;

            private Particule.Behavior behavior;

            public Particule(Context context) {
                super(context);
            }

            public Particule(Context context, @Nullable AttributeSet attrs) {
                super(context, attrs);
            }

            public Particule(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
                super(context, attrs, defStyleAttr);
            }

            /*******************************/
            /** {@link android.view.View} **/
            /*******************************/

            @Override
            protected void onDraw(Canvas canvas) {
                behavior.draw(canvas);
                super.onDraw(canvas);
            }

            /*************/
            /** private **/
            /*************/

            void init(ViewGroup root, Path path, float cx, float cy, float radius, int duration, TimeInterpolator interpolator) {
                behavior = new Particule.Radius(radius);
                init(root, path, cx, cy, duration, interpolator);
            }

            void init(ViewGroup root, Path path, float cx, float cy, int size, String asset, int duration, TimeInterpolator interpolator) {
                behavior = new Particule.Asset(asset, size);
                init(root, path, cx, cy, duration, interpolator);
            }

            Animator anim() {
                return animator;
            }

            private void init(final ViewGroup root, final Path path, float cx, float cy, int duration, TimeInterpolator interpolator) {
                paint = new Paint();
                paint.setColor(Color.BLUE);

                this.cx = cx;
                this.cy = cy;

                ValueAnimator animator = ValueAnimator.ofFloat(0.f, 1.f);
                animator.setDuration(duration);
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
                        root.removeView(Particule.this);
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

            private class Radius implements Particule.Behavior {
                private float radius;

                Radius(float radius) {
                    this.radius = radius;
                }

                @Override
                public void draw(Canvas canvas) {
                    canvas.drawCircle(cx, cy, radius, paint);
                }
            }

            private class Asset implements Particule.Behavior {
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
