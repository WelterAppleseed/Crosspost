package com.example.crossposter2.utils;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import com.example.crossposter2.R;

public class ToolbarsAnimations {
    public ToolbarsAnimations() {}

    static void startShowAnimation(LinearLayout layout, RelativeLayout toolbar) {
        toolbar.setVisibility(View.VISIBLE);
        layout.animate().setInterpolator(new LinearOutSlowInInterpolator()).translationY(0).setDuration(200);
    }
    static void startHideAnimation(LinearLayout layout, RelativeLayout toolbar) {
        layout.animate().setInterpolator(new FastOutLinearInInterpolator()).translationY(Utils.dpToPx(110)).setDuration(200).withEndAction(() -> toolbar.setVisibility(View.GONE));
    }
}
