package com.example.crossposter2.utils;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.net.Uri;
import android.preference.SwitchPreference;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import com.example.crossposter2.MainActivity;
import com.example.crossposter2.R;

import java.util.ArrayList;
import java.util.Map;

public class ClickListeners {
    private static final int y = Utils.dpToPx(6);
    private SharedPreferences tgPrf;
    private final double dif_const = 0.00588235294;
    private float yCoOrdinate;
    private SharedPreferences.Editor editor;
    private ViewPropertyAnimator viewPropertyAnimator;
    private SharedPreferences switch_prf;

    public View.OnTouchListener getImageListener(LinearLayout layout) {
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
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

    public View.OnClickListener onBottomLayoutCallListener(LinearLayout[] layouts, LinearLayout selectedLayout, RelativeLayout[] toolbars, RelativeLayout selectedToolbar) {
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
                if (closedToolbarId != selectedToolbar.getId()) {
                    ToolbarsAnimations.startShowAnimation(selectedLayout, selectedToolbar);
                }
                closedToolbarId = 0;
            }
        };
    }

    private void removeImg(View view, float yCord, LinearLayout imgLayout) {
        Map<Uri, String> images = new MainActivity().getUris();
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
                            images.values().remove(((ImageView) view).getDrawable().toString());
                            System.out.println(images.size());
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

    public View.OnClickListener onInfoClickListener(Context context) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.info);
                dialog.setTitle("Title...");
                Button dialogButton = (Button) dialog.findViewById(R.id.ok_dialog_bt);
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        };
    }

    public View.OnClickListener onReportClickListener(Context context) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.report);
                Button dialogButton = (Button) dialog.findViewById(R.id.ok_warn_dialog_bt);
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                TextView copyText = (TextView) dialog.findViewById(R.id.copy_text);
                copyText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ClipboardManager clipboard = (ClipboardManager) context.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("", copyText.getText().toString());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(context, "E-mail is copied.", Toast.LENGTH_LONG).show();
                    }
                });
                dialog.show();
            }
        };
    }

    public View.OnClickListener onTelegramConnectListener(Context context, Button logoutBt, Button connectBt, Switch swtch) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.insert_telegram);
                EditText channelNameInsert = (EditText) dialog.findViewById(R.id.type_here);
                tgPrf = context.getSharedPreferences("channel", Context.MODE_PRIVATE);
                channelNameInsert.setPaintFlags(channelNameInsert.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                String defValue = context.getResources().getString(R.string.type_here);
                channelNameInsert.setText(tgPrf.getString("channel_name", defValue));
                Button save = (Button) dialog.findViewById(R.id.save_channel);
                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (channelNameInsert.getText().toString().equals("") || channelNameInsert.getText().toString().equals(defValue)) {
                            Toast.makeText(context, "Channel name is not provided", Toast.LENGTH_SHORT);
                        } else {
                            editor = context.getSharedPreferences("channel", Context.MODE_PRIVATE).edit();
                            editor.putString("channel_name", channelNameInsert.getText().toString());
                            editor.apply();
                            Utils.onLogin(context, swtch, logoutBt, connectBt);
                            dialog.dismiss();
                        }
                    }
                });
                Button cancel = (Button) dialog.findViewById(R.id.back);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        };
    }


    public CompoundButton.OnCheckedChangeListener onFacebookSwitchClickListener(Context context) {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.aware);
                CheckBox show = (CheckBox) dialog.findViewById(R.id.show_or_not);
                show.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        editor = context.getSharedPreferences("show", Context.MODE_PRIVATE).edit();
                        editor.putBoolean("isSwitched", isChecked);
                        editor.apply();
                    }
                });
                switch_prf = context.getSharedPreferences("show", Context.MODE_PRIVATE);
                show.setChecked(switch_prf.getBoolean("isSwitched", false));
                if (isChecked && !show.isChecked()) {
                    Button dialogButton = (Button) dialog.findViewById(R.id.understand);
                    dialogButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    TextView textview = dialog.findViewById(R.id.another_method_of_posting);
                    textview.setPaintFlags(textview.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                    textview.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final Dialog methodDialog = new Dialog(context);
                            methodDialog.setContentView(R.layout.method);
                            methodDialog.setCanceledOnTouchOutside(true);
                            ImageView img = methodDialog.findViewById(R.id.method_img);
                            img.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    methodDialog.dismiss();
                                }
                            });
                            methodDialog.show();
                        }
                    });
                    dialog.show();
                }
            }
        };
    }
}

