package com.example.crossposter2.utils;

import android.animation.Animator;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.widget.ImageView;

public class ImageUI {
    private boolean touched;
    private final double dif_const = 0.00304615384;
    private float yCoOrdinate;
    private ViewPropertyAnimator viewPropertyAnimator, secViewPropertyAnimator;

    public ImageUI() {
    }

    public View.OnTouchListener getImageListener() {
        View.OnTouchListener removeListener = new View.OnTouchListener() {
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
                                double vision = (dif > 0.5) ? 0.5 : 1 - dif;
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
                        removeImg(v, v.getY());
                        return false;
                }
                return true;
            }
        };
        return removeListener;
    }

    private void removeImg(View view, float yCord) {
        ViewPropertyAnimator delAnim;
        Animation imageAnim = new Animation() {
        };
        if (view instanceof ImageView) {
            try {
                if (Math.abs(yCord) >= 350) {
                    view.animate().alpha(0).setDuration(200).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            view.setVisibility(View.GONE);
                            ((ImageView) view).setImageResource(0);
                            view.setY(0);
                            view.setAlpha(1);
                        }
                    });
                } else {
                    view.animate().y(0).alpha(1).setDuration(100).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}


