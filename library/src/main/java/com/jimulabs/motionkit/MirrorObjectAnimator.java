package com.jimulabs.motionkit;

import android.animation.Animator;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 *
 * A simple wrapper for {@link android.animation.ObjectAnimator}.
 *
 * Created by lintonye on 2014-12-19.
 */
public class MirrorObjectAnimator extends MirrorAnimator {
    private static final long DEFAULT_DURATION = 500;
    private static final TimeInterpolator DEFAULT_INTERPOLATOR = new AccelerateDecelerateInterpolator();
    private final ObjectAnimator mAnimator;
    private final Keyframe mFirstFrame;
    private final Keyframe mLastFrame;

    public MirrorObjectAnimator(ObjectAnimator animator, Keyframe firstFrame, Keyframe lastFrame) {
        super();
        mAnimator = animator;
        mFirstFrame = firstFrame;
        mLastFrame = lastFrame;
        setDefaults(animator);
    }

    @Override
    public Animator getAnimator() {
        return mAnimator;
    }

    @Override
    public MirrorAnimator duration(long duration) {
        mAnimator.setDuration(MotionKit.computeDuration(duration));
        return this;
    }

    @Override
    public MirrorAnimator startDelay(long delay) {
        mAnimator.setStartDelay(MotionKit.computeDuration(delay));
        return this;
    }

    @Override
    public long getDuration() {
        return mAnimator.getDuration();
    }

    @Override
    public long getStartDelay() {
        return mAnimator.getStartDelay();
    }

    private static void setDefaults(ObjectAnimator animator) {
        animator.setDuration(MotionKit.computeDuration(DEFAULT_DURATION));
        animator.setInterpolator(DEFAULT_INTERPOLATOR);
    }

    public Object getTarget() {
        return mAnimator.getTarget();
    }

    public String getPropertyName() {
        return mAnimator.getPropertyName();
    }

    public Keyframe getFirstFrame() {
        return mFirstFrame;
    }
}
