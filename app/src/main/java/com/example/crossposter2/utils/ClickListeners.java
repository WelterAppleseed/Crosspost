package com.example.crossposter2.utils;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.example.crossposter2.MainActivity;
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

    public void scrollImageViewsListener(HorizontalScrollView scrollView) {
        scrollView.scrollTo(30, 0);
    }
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

    public View.OnClickListener getPostClickListener(Context context) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog switchDialog = new Dialog(context);
                switchDialog.setContentView(R.layout.switches);

                fS = (Switch) switchDialog.findViewById(R.id.facebook_switch);
                vS = (Switch) switchDialog.findViewById(R.id.vk_switch);
                tS = (Switch) switchDialog.findViewById(R.id.telegram_switch);
                oS = (Switch) switchDialog.findViewById(R.id.unknown_switch);

                //Getting switch states
                switch_prf = context.getSharedPreferences("test", Context.MODE_PRIVATE);
                facebook_switch_state = switch_prf.getBoolean("facebook_switch_state", false);
                vk_switch_state = switch_prf.getBoolean("vk_switch_state", false);
                telegram_switch_state = switch_prf.getBoolean("telegram_switch_state", false);
                unknown_switch_state = switch_prf.getBoolean("unknown_switch_state", false);
                //Setting checked
                fS.setChecked(facebook_switch_state);
                tS.setChecked(telegram_switch_state);
                oS.setChecked(unknown_switch_state);
                vS.setChecked(vk_switch_state);
                switchDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        editor = context.getSharedPreferences("test", Context.MODE_PRIVATE).edit();
                        editor.putBoolean("facebook_switch_state", fS.isChecked());
                        editor.putBoolean("vk_switch_state", vS.isChecked());
                        editor.putBoolean("telegram_switch_state", tS.isChecked());
                        editor.putBoolean("unknown_switch_state", oS.isChecked());
                        editor.apply();
                        switchDialog.cancel();
                    }
                });
                post = (Button) switchDialog.findViewById(R.id.post);
                post.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    if (vS.isChecked()) {
                                        System.out.println("VS");
                                        //vkAction();
                                    }
                                    if (tS.isChecked()) {
                                        System.out.println("TS");
                                        //tgAction();
                                    }
                                    if (fS.isChecked()) {
                                        System.out.println("FS");
                                        //fbAction();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }
                });
                switchDialog.show();
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