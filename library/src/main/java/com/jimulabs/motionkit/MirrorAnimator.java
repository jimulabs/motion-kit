package com.jimulabs.motionkit;

import android.animation.Animator;
import android.animation.Keyframe;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.jimulabs.util.Optional;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 *
 * This is the superclass of a few simple wrappers for Android's property animator system.
 *
 * The most interesting bit so far is the concept of "stage setting": when MirrorAnimator#start()
 * is called, the properties of all animation targets will be automatically set to the start
 * value according to the animator. This removes the need for manual setup code before the animation.
 *
 * It's better to illustrate this with an example: say we have two views, view1 and view2. We want
 * to fade in view1 and afterwards zoom in view2. With just Android's animator, before starting the
 * animation, we'd need to set the alpha value of view1 to 0, and set the scale of view2 to 0.
 * With MirrorAnimator, we can just do this:
 *
 *   view1.alpha(0, 1).followedBy(view2.scale(0, 1)).start();
 *
 *
 * Created by lintonye on 14-12-16.
 */
public abstract class MirrorAnimator {

    public MirrorAnimator() {
    }

    public MirrorAnimator together(MirrorAnimator... mirrorAnimators) {
        List<MirrorAnimator> mas = mePlus(mirrorAnimators);
        return MotionKit.together(mas);
    }

    public MirrorAnimator followedBy(MirrorAnimator mirrorAnimator) {
        List<MirrorAnimator> mas = mePlus(mirrorAnimator);
        return MotionKit.sequence(mas);
    }

    public abstract Animator getAnimator();

    private List<MirrorAnimator> mePlus(MirrorAnimator... mirrorAnimators) {
        List<MirrorAnimator> result = new ArrayList<>(mirrorAnimators.length + 1);
        result.add(this);
        result.addAll(Arrays.asList(mirrorAnimators));
        return result;
    }

    public MirrorAnimator interpolator(Context context, int resId) {
        MotionKit.setInterpolator(context, getAnimator(), resId);
        return this;
    }

    public MirrorAnimator interpolator(TimeInterpolator interpolator) {
        getAnimator().setInterpolator(interpolator);
        return this;
    }

    public abstract MirrorAnimator duration(long duration);

    public abstract MirrorAnimator startDelay(long delay);

    public abstract long getDuration();

    public abstract long getStartDelay();

    public void startNoStageSetting() {
        getAnimator().start();
    }

    public void start() {
        start(new UseFirstFrameOnlyStageSetter());
    }

    public void start(StageSetter stageSetter) {
        setupStage(this, stageSetter);
        startNoStageSetting();
    }

    private void setupStage(MirrorAnimator animator, StageSetter stageSetter) {
        List<Pair<MirrorObjectAnimator, Long>> animatorStartTimes = new ArrayList<>();
        collectStartTime(animator, animatorStartTimes, 0);
        Collections.sort(animatorStartTimes, new Comparator<Pair<MirrorObjectAnimator, Long>>() {
            @Override
            public int compare(Pair<MirrorObjectAnimator, Long> lhs, Pair<MirrorObjectAnimator, Long> rhs) {
                return (int) (lhs.second - rhs.second);
            }
        });

        stageSetter.setup(animatorStartTimes);
    }

    private void collectStartTime(MirrorAnimator animator, List<Pair<MirrorObjectAnimator, Long>> output, long startTime) {
        if (animator instanceof MirrorAnimatorSet) {
            MirrorAnimatorSet set = (MirrorAnimatorSet) animator;
            long accuTimeBeforeMe = set.getStartDelay();
            for (MirrorAnimator c : set.getChildAnimations()) {
                collectStartTime(c, output, startTime + accuTimeBeforeMe);
                if (set.getOrdering() == MirrorAnimatorSet.Ordering.Sequentially) {
                    accuTimeBeforeMe += c.getStartDelay() + c.getDuration();
                }
            }
        } else if (animator instanceof MirrorObjectAnimator) {
            MirrorObjectAnimator o = (MirrorObjectAnimator) animator;
            output.add(new Pair<>(o, startTime + o.getStartDelay()));
        } else {
            throw new IllegalStateException("Unsupported animator type: " + animator);
        }
    }

    private static class UseFirstFrameOnlyStageSetter implements StageSetter {
        private static final String LOG_TAG = "FirstFrameStageSetter";
        private static final List<String> PROPS_AFFECTED_BY_LAYOUT = Arrays.asList(new String[]{"left", "right", "top", "bottom"});
        private Map<Object, Set<String>> mRegistry = new HashMap<>();

        @Override
        public void setup(List<Pair<MirrorObjectAnimator, Long>> sortedAnimatorStartTimes) {
            for (Pair<MirrorObjectAnimator, Long> pair : sortedAnimatorStartTimes) {
                setupOne(pair.first);
            }
        }

        private void setupOne(MirrorObjectAnimator objectAnimator) {
            Object target = objectAnimator.getTarget();
            Set<String> props = mRegistry.get(target);
            if (props == null) {
                props = new HashSet<>();
                mRegistry.put(target, props);
            }
            String propertyName = objectAnimator.getPropertyName();
            if (!props.contains(propertyName)) {
                if ((target instanceof View) && willBeUpdatedDuringLayout(propertyName)) {
                    callSetterOnLayoutChange((View)target, propertyName, objectAnimator.getFirstFrame());
                } else {
                    callSetter(target, propertyName, objectAnimator.getFirstFrame());
                }
                props.add(propertyName);
            }
        }

        private void callSetterOnLayoutChange(final View target, final String propertyName, final Keyframe firstFrame) {
            target.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    callSetter(target, propertyName, firstFrame);
                    target.removeOnLayoutChangeListener(this);
                }
            });
        }

        private boolean willBeUpdatedDuringLayout(String propertyName) {
            return PROPS_AFFECTED_BY_LAYOUT.contains(propertyName);
        }

        private void callSetter(Object target, String propertyName, Keyframe firstFrame) {
            try {
                Class type = firstFrame.getType();
                Optional<Method> setter = MotionKit.getSetter(target, propertyName, type);
                if (setter.isPresent()) {
                    setter.get().invoke(target, firstFrame.getValue());
                    Log.d(LOG_TAG, String.format("%s=%s", propertyName, firstFrame.getValue()));
                } else {
                    Log.e(LOG_TAG, String.format("Setter for %s(%s) does not exist", propertyName, type));
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                Log.e(LOG_TAG, String.format("Failed to set value of \"%s\"", propertyName), e);
            }
        }

    }

    public interface StageSetter {
        void setup(List<Pair<MirrorObjectAnimator, Long>> sortedAnimatorStartTimes);
    }

}
