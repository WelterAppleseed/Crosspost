package com.example.crossposter2.utils;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;

import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import com.example.crossposter2.R;

public class ClickListeners {
    Switch fS, tS, vS, oS;
    public static final int y = Utils.dpToPx(6);
    private boolean touched;
    private final double dif_const = 0.00588235294;
    private float yCoOrdinate;
    SharedPreferences.Editor editor;
    private ViewPropertyAnimator viewPropertyAnimator, secViewPropertyAnimator;
    SharedPreferences switch_prf;
    private ResizeMode _resizeMode;
    LinearLayout parent;
    private ScaleMode _scaleMode;
    private int _boxWidth = 250;
    Button post;
    private int _boxHeight = 250;
    boolean facebook_switch_state, vk_switch_state, telegram_switch_state, unknown_switch_state;
    private boolean _isRecycleSrcBitmap;

    public View.OnClickListener getReadyToImageListener(View.OnTouchListener listener) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setOnTouchListener(listener);
            }
        };
    }

    public View.OnTouchListener getImageListener(LinearLayout layout) {
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                touched = true;
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        yCoOrdinate = v.getY() - event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        viewPropertyAnimator = v.animate().y((event.getRawY() + yCoOrdinate)).setDuration(0);
                        viewPropertyAnimator.setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                double dif = Math.abs(v.getY()) * dif_const;
                                double vision = (dif > 0.7) ? 0.3 : 1 - dif;
                                v.setAlpha((float) vision);
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {

                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }

                        });
                        break;
                    default:
                        removeImg(v, v.getY(), layout);
                        return false;
                }
                return true;
            }
        };
    }

    public View.OnClickListener testPostLis(LinearLayout[] layouts, LinearLayout selectedLayout, RelativeLayout[] toolbars, RelativeLayout selectedToolbar) {
        return new View.OnClickListener() {
            int closedToolbarId;

            @Override
            public void onClick(View v) {
                for (RelativeLayout toolbar : toolbars) {
                    if (toolbar.getVisibility() == View.VISIBLE) {
                        String parent = toolbar.getParent().toString();
                        closedToolbarId = toolbar.getId();
                        if (parent.equals(layouts[0].toString())) {

                            ToolbarsAnimations.startHideAnimation(layouts[0], toolbar);
                        }
                        if (parent.equals(layouts[1].toString())) {
                            ToolbarsAnimations.startHideAnimation(layouts[1], toolbar);
                        }
                        if (parent.equals(layouts[2].toString())) {
                            ToolbarsAnimations.startHideAnimation(layouts[2], toolbar);
                        }
                    }
                }
                System.out.println(closedToolbarId + " " + selectedToolbar.getId());
                if (closedToolbarId != selectedToolbar.getId()) {
                    ToolbarsAnimations.startShowAnimation(selectedLayout, selectedToolbar);
                }
                closedToolbarId = 0;
            }
        };
    }

    public void removeImg(View view, float yCord, LinearLayout imgLayout) {
        ViewPropertyAnimator delAnim;
        Animation imageAnim = new Animation() {
        };
        if (view instanceof ImageView) {
            try {
                if (Math.abs(yCord) >= 200) {
                    view.animate().alpha(0).setDuration(200).withEndAction(new Runnable() {
                        @SuppressLint("ResourceType")
                        @Override
                        public void run() {
                            imgLayout.removeView(view);
                        }
                    });
                } else {
                    view.animate().y(y).alpha(1).setDuration(100).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    }