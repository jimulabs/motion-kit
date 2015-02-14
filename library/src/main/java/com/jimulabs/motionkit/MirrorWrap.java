package com.jimulabs.motionkit;

import android.content.Context;

/**
 * Wrap an arbitrary object to use {@link com.jimulabs.motionkit.MirrorAnimator}
 *
 * Created by lintonye on 2014-12-21.
 */
public class MirrorWrap {
    private final Object mWrapped;
    private final Context mContext;

    public MirrorWrap(Context context, Object obj) {
        mContext = context;
        mWrapped = obj;
    }
    public MirrorAnimator animator(String property, int... values) {
        return MotionKit.animator(mWrapped, property, values);
    }

    public MirrorAnimator animator(String property, float... values) {
        return MotionKit.animator(mWrapped, property, values);
    }


}
