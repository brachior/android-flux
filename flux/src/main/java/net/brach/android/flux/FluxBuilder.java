package net.brach.android.flux;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
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
import java.util.List;
import java.util.Random;

public class FluxBuilder {
    public static void attach2Activity(final Activity activity) {
        root = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);

        root.post(new Runnable() {
            @Override
            public void run() {
                DisplayMetrics dm = new DisplayMetrics();
                activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
                topOffset = dm.heightPixels - root.getMeasuredHeight();
            }
        });
    }

    public static Flux make(Context ctx, final View from, View to, int count,
                            float radiusMin, float radiusMax, int durationMin, int durationMax) {
        return make(ctx, from, to, count, KIND_RADIUS, radiusMin, radiusMax, durationMin, durationMax, null);
    }

    public static Flux make(Context ctx, View from, View to, int count, List<String> assets,
                            int sizeMin, int sizeMax, int durationMin, int durationMax) {
        return make(ctx, from, to, count, KIND_BITMAP, sizeMin, sizeMax, durationMin, durationMax, assets);
    }

    /*************/
    /** private **/
    /*************/

    private static ViewGroup root;
    private static int topOffset;

    private static final int KIND_RADIUS = 1;
    private static final int KIND_BITMAP = 2;

    private static <E> Flux make(final Context ctx, final View from, final View to, final int count,
                                 final int kind, final E min, final E max, final int durationMin, final int durationMax, final List<String> assets) {
        if (root == null) {
            throw new IllegalStateException();
        }

        final FluxImpl flux = new FluxImpl();

        from.post(new Runnable() {
            @Override
            public void run() {
                to.post(new Runnable() {
                    @Override
                    public void run() {
                        int[] startLocation = new int[2];
                        from.getLocationInWindow(startLocation);

                        float x1 = startLocation[0] + from.getWidth() / 2;
                        float y1 = startLocation[1] + from.getHeight() / 2 - topOffset;

                        int[] endLocation = new int[2];
                        to.getLocationInWindow(endLocation);

                        float x3 = endLocation[0] + to.getWidth() / 2;
                        float y3 = endLocation[1] + to.getHeight() / 2 - topOffset;

                        int maxDuration = 0;
                        ArrayList<Animator> animators = new ArrayList<>(count + 1);

                        ValueAnimator fadeOut = ValueAnimator.ofFloat(1.f, 0.f);
                        fadeOut.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                from.setAlpha((float) animation.getAnimatedValue());
                                from.requestLayout();
                            }
                        });
                        animators.add(fadeOut);

                        switch (kind) {
                            case KIND_RADIUS:
                                maxDuration = radius(ctx, root, animators, count, x1, y1, x3, y3, (Float) min, (Float) max, durationMin, durationMax);
                                break;
                            case KIND_BITMAP:
                                maxDuration = bitmap(ctx, root, animators, count, x1, y1, x3, y3, assets, (Integer) min, (Integer) max, durationMin, durationMax);
                                break;
                        }

                        fadeOut.setDuration(maxDuration + 200);

                        AnimatorSet set = new AnimatorSet();
                        set.playTogether(animators);

                        flux.init(set);
                    }
                });
            }
        });

        return flux;
    }

    private static int radius(Context ctx, ViewGroup root, ArrayList<Animator> animators, int count,
                              float x1, float y1, float x3, float y3,
                              float radiusMin, float radiusMax, int durationMin, int durationMax) {
        int maxDuration = 0;
        int durationInterval = durationMax - durationMin;
        float radiusInterval = radiusMax - radiusMin;

        for (int i = 0; i < count; i++) {
            Path path = new Path();
            path.moveTo(x1, y1);
            path.quadTo(middle(random, x1, x3), middle(random, y1, y3), x3, y3);

            Particule particule = new Particule(ctx);
            int duration = random.nextInt(durationInterval) + durationMin;
            float radius = radiusMin + (radiusInterval * random.nextFloat());
            particule.init(root, path, x1, y1, radius, duration);
            animators.add(particule.anim());
            root.addView(particule);

            if (duration > maxDuration) {
                maxDuration = duration;
            }
        }

        return maxDuration;
    }

    private static int bitmap(Context ctx, ViewGroup root, ArrayList<Animator> animators, int count,
                              float x1, float y1, float x3, float y3, List<String> assets,
                              int sizeMin, int sizeMax, int durationMin, int durationMax) {
        int maxDuration = 0;
        int durationInterval = durationMax - durationMin;
        int sizeInterval = sizeMax - sizeMin;

        for (int i = 0; i < count; i++) {
            Path path = new Path();
            path.moveTo(x1, y1);
            path.quadTo(middle(random, x1, x3), middle(random, y1, y3), x3, y3);

            Particule particule = new Particule(ctx);
            int duration = random.nextInt(durationInterval) + durationMin;
            int size = random.nextInt(sizeInterval) + sizeMin;
            particule.init(root, path, x1, y1, size, assets.get(random.nextInt(assets.size())), duration);
            animators.add(particule.anim());
            root.addView(particule);

            if (duration > maxDuration) {
                maxDuration = duration;
            }
        }

        return maxDuration;
    }

    private static final Random random = new Random();

    private static final TimeInterpolator[] interpolators = {
            new AccelerateInterpolator(),
            new DecelerateInterpolator(),
            new AccelerateDecelerateInterpolator(),
            new AnticipateInterpolator(),
            new AnticipateOvershootInterpolator(),
            new OvershootInterpolator(),
            new FastOutSlowInInterpolator()
    };

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
        } catch(Exception ex) {
            return null;
        }
    }

    public static class Particule extends View {
        private Paint paint;

        private float cx;
        private float cy;
        private ValueAnimator animator;

        private Behavior behavior;

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

        void init(final ViewGroup root, final Path path, float cx, float cy, float radius, int duration) {
            behavior = new Radius(radius);
            init(root, path, cx, cy, duration);
        }

        void init(final ViewGroup root, final Path path, float cx, float cy, int size, String asset, int duration) {
            behavior = new Asset(asset, size);
            init(root, path, cx, cy, duration);
        }

        Animator anim() {
            return animator;
        }

        private void init(final ViewGroup root, final Path path, float cx, float cy, int duration) {
            paint = new Paint();
            paint.setColor(Color.BLUE);

            this.cx = cx;
            this.cy = cy;

            ValueAnimator animator = ValueAnimator.ofFloat(0.f, 1.f);
            animator.setDuration(duration);
            animator.setInterpolator(interpolators[random.nextInt(interpolators.length)]);
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

        private class Radius implements Behavior {
            private float radius;

            Radius(float radius) {
                this.radius = radius;
            }

            @Override
            public void draw(Canvas canvas) {
                canvas.drawCircle(cx, cy, radius, paint);
            }
        }

        private class Asset implements Behavior {
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
