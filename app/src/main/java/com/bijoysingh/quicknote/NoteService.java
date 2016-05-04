package com.bijoysingh.quicknote;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.github.bijoysingh.starter.Functions;


/**
 * Note Service
 * Created by bijoy on 5/4/16.
 */
public class NoteService extends Service {

    private static final Integer LONG_PRESS_DELTA = 300;
    private static final Integer ANIMATION_TIME = 500;
    private static final Integer ANIMATION_STEP = 5;

    private WindowManager windowManager;

    private RelativeLayout chatHeadView;
    private RelativeLayout removeView;
    private ImageView removeImage;

    private int initialXCoordinate;
    private int initialYCoordinate;
    private int chatHeadInitialX;
    private int chatHeadInitialY;

    private Point windowSize = new Point();
    NoteItem item = null;
    NoteDatabase db;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(NoteService.class.getSimpleName(), "ChatHeadService.onCreate()");
        db = new NoteDatabase(this);
    }

    /**
     * Get the default window layout params
     *
     * @return the layout param
     */
    WindowManager.LayoutParams getDefaultWindowParams() {
        return new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT);
    }

    /**
     * Creates the views
     */
    private void handleStart() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        /**
         * Setting up the Remove Layout
         */
        removeView = (RelativeLayout) inflater.inflate(R.layout.remove, null);
        WindowManager.LayoutParams paramRemove = getDefaultWindowParams();
        paramRemove.gravity = Gravity.TOP | Gravity.START;
        removeView.setVisibility(View.GONE);
        removeImage = (ImageView) removeView.findViewById(R.id.remove_img);
        windowManager.addView(removeView, paramRemove);


        /**
         * Setting up the Chat Head View
         */
        chatHeadView = (RelativeLayout) inflater.inflate(R.layout.chathead, null);
        windowManager.getDefaultDisplay().getSize(windowSize);

        WindowManager.LayoutParams params = getDefaultWindowParams();
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = windowSize.y / 2 - chatHeadView.getHeight() / 2;
        windowManager.addView(chatHeadView, params);

        /**
         * Setting up the Chat Head touch listener
         */
        chatHeadView.setOnTouchListener(new View.OnTouchListener() {
            long timeStart = 0;
            long timeEnd = 0;
            boolean isLongClick = false;
            boolean inBounded = false;
            int removeImageWidth = 0;
            int removeImageHeight = 0;

            Handler handlerLongClick = new Handler();
            Runnable runnableLongClick = new Runnable() {

                @Override
                public void run() {
                    isLongClick = true;
                    removeView.setVisibility(View.VISIBLE);
                    showRemoveView();
                }
            };

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                WindowManager.LayoutParams chatHeadParams =
                    (WindowManager.LayoutParams) chatHeadView.getLayoutParams();

                int currentXCoordinate = (int) event.getRawX();
                int currentYCoordinate = (int) event.getRawY();
                int destinationXCoordinate;
                int destinationYCoordinate;

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        timeStart = System.currentTimeMillis();
                        handlerLongClick.postDelayed(runnableLongClick, LONG_PRESS_DELTA);

                        removeImageWidth = removeImage.getLayoutParams().width;
                        removeImageHeight = removeImage.getLayoutParams().height;

                        initialXCoordinate = currentXCoordinate;
                        initialYCoordinate = currentYCoordinate;

                        chatHeadInitialX = chatHeadParams.x;
                        chatHeadInitialY = chatHeadParams.y;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        int motionX = currentXCoordinate - initialXCoordinate;
                        int motionY = currentYCoordinate - initialYCoordinate;

                        destinationXCoordinate = chatHeadInitialX + motionX;
                        destinationYCoordinate = chatHeadInitialY + motionY;

                        if (isLongClick) {
                            int xBoundLeft = windowSize.x / 2 - (int) (removeImageWidth * 1.5);
                            int xBoundRight = windowSize.x / 2 + (int) (removeImageWidth * 1.5);
                            int yBoundTop = windowSize.y - (int) (removeImageHeight * 1.5);

                            if ((currentXCoordinate >= xBoundLeft
                                && currentXCoordinate <= xBoundRight)
                                && currentYCoordinate >= yBoundTop) {
                                inBounded = true;

                                int removeViewXCoordinate =
                                    (int) ((windowSize.x - (removeImageHeight * 1.5)) / 2);
                                int removeViewYCoordinate =
                                    (int) (windowSize.y - ((removeImageWidth * 1.5)
                                        + getBottomMargin()));

                                if (removeImage.getLayoutParams().height == removeImageHeight) {
                                    removeImage.getLayoutParams().height = (int) (removeImageHeight * 1.5);
                                    removeImage.getLayoutParams().width = (int) (removeImageWidth * 1.5);

                                    WindowManager.LayoutParams param_remove =
                                        (WindowManager.LayoutParams) removeView.getLayoutParams();
                                    param_remove.x = removeViewXCoordinate;
                                    param_remove.y = removeViewYCoordinate;

                                    windowManager.updateViewLayout(removeView, param_remove);
                                }

                                // Set the chat head to the center of remove view
                                chatHeadParams.x = removeViewXCoordinate
                                    + (Math.abs(removeView.getWidth() - chatHeadView.getWidth())) / 2;
                                chatHeadParams.y = removeViewYCoordinate
                                    + (Math.abs(removeView.getHeight() - chatHeadView.getHeight())) / 2;
                                windowManager.updateViewLayout(chatHeadView, chatHeadParams);

                                break;
                            } else {
                                // The remove view is not under the chat head
                                inBounded = false;
                                removeImage.getLayoutParams().height = removeImageHeight;
                                removeImage.getLayoutParams().width = removeImageWidth;

                                int removeViewXCoordinate = (windowSize.x - removeView.getWidth()) / 2;
                                int removeViewYCoordinate = windowSize.y - (removeView.getHeight() + getBottomMargin());

                                WindowManager.LayoutParams removeViewParams =
                                    (WindowManager.LayoutParams) removeView.getLayoutParams();
                                removeViewParams.x = removeViewXCoordinate;
                                removeViewParams.y = removeViewYCoordinate;

                                windowManager.updateViewLayout(removeView, removeViewParams);
                            }
                        }

                        // Update the position of the chat head
                        chatHeadParams.x = destinationXCoordinate;
                        chatHeadParams.y = destinationYCoordinate;
                        windowManager.updateViewLayout(chatHeadView, chatHeadParams);

                        break;
                    case MotionEvent.ACTION_UP:
                        isLongClick = false;
                        removeView.setVisibility(View.GONE);
                        removeImage.getLayoutParams().height = removeImageHeight;
                        removeImage.getLayoutParams().width = removeImageWidth;
                        handlerLongClick.removeCallbacks(runnableLongClick);

                        if (inBounded) {
                            if (NoteActivity.active) {
                                NoteActivity.instance.finish();
                            }

                            stopService(new Intent(NoteService.this, NoteService.class));
                            inBounded = false;
                            break;
                        }


                        int xDiff = currentXCoordinate - initialXCoordinate;
                        int yDiff = currentYCoordinate - initialYCoordinate;

                        if (Math.abs(xDiff) < 5 && Math.abs(yDiff) < 5) {
                            timeEnd = System.currentTimeMillis();
                            if ((timeEnd - timeStart) < LONG_PRESS_DELTA) {
                                chatHeadClick();
                            }
                        }

                        destinationYCoordinate = chatHeadInitialY + yDiff;

                        int BarHeight = getBottomMargin();
                        if (destinationYCoordinate < 0) {
                            destinationYCoordinate = 0;
                        } else if (destinationYCoordinate + (chatHeadView.getHeight() + BarHeight) > windowSize.y) {
                            destinationYCoordinate = windowSize.y - (chatHeadView.getHeight() + BarHeight);
                        }
                        chatHeadParams.y = destinationYCoordinate;

                        inBounded = false;
                        resetPosition(currentXCoordinate);

                        break;
                    default:
                        Log.d(NoteService.class.getSimpleName(), "chatHeadView.setOnTouchListener  -> event.getAction() : default");
                        break;
                }
                return true;
            }
        });
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        windowManager.getDefaultDisplay().getSize(windowSize);
        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) chatHeadView.getLayoutParams();

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (layoutParams.y + (chatHeadView.getHeight() + getBottomMargin()) > windowSize.y) {
                layoutParams.y = windowSize.y - (chatHeadView.getHeight() + getBottomMargin());
                windowManager.updateViewLayout(chatHeadView, layoutParams);
            }

            if (layoutParams.x != 0 && layoutParams.x < windowSize.x) {
                resetPosition(windowSize.x);
            }

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (layoutParams.x > windowSize.x) {
                resetPosition(windowSize.x);
            }
        }
    }

    /**
     * Resets the position of the chat head to the corner
     *
     * @param currentXCoordinate the current x coordinate of the head
     */
    private void resetPosition(int currentXCoordinate) {
        if (currentXCoordinate <= windowSize.x / 2) {
            moveToLeft(currentXCoordinate);
        } else {
            moveToRight(currentXCoordinate);
        }
    }

    /**
     * Move chat head to the top corner
     *
     * @param currentYCoordinate the coordinate value
     */
    private void moveToTop(final int currentYCoordinate, final int finalCoordinate) {
        final int y = windowSize.y - currentYCoordinate;

        new CountDownTimer(ANIMATION_TIME, ANIMATION_STEP) {
            WindowManager.LayoutParams chatHeadParams =
                (WindowManager.LayoutParams) chatHeadView.getLayoutParams();

            public void onTick(long t) {
                long step = (ANIMATION_TIME - t) / ANIMATION_STEP;
                chatHeadParams.y = finalCoordinate - (int) (double) motionValue(step, y);
                windowManager.updateViewLayout(chatHeadView, chatHeadParams);
            }

            public void onFinish() {
                chatHeadParams.y = finalCoordinate;
                windowManager.updateViewLayout(chatHeadView, chatHeadParams);
            }
        }.start();
    }

    /**
     * Move chat head to the left corner
     *
     * @param currentXCoordinate the coordinate value
     */
    private void moveToLeft(final int currentXCoordinate) {
        final int x = windowSize.x - currentXCoordinate;

        new CountDownTimer(ANIMATION_TIME, ANIMATION_STEP) {
            WindowManager.LayoutParams chatHeadParams =
                (WindowManager.LayoutParams) chatHeadView.getLayoutParams();

            public void onTick(long t) {
                long step = (ANIMATION_TIME - t) / ANIMATION_STEP;
                chatHeadParams.x = 0 - (int) (double) motionValue(step, x);
                windowManager.updateViewLayout(chatHeadView, chatHeadParams);
            }

            public void onFinish() {
                chatHeadParams.x = 0;
                windowManager.updateViewLayout(chatHeadView, chatHeadParams);
            }
        }.start();
    }

    /**
     * Move chat head to the right corner
     *
     * @param currentXCoordinate the coordinate value
     */
    private void moveToRight(final int currentXCoordinate) {
        new CountDownTimer(ANIMATION_TIME, ANIMATION_STEP) {
            WindowManager.LayoutParams chatHeadParams =
                (WindowManager.LayoutParams) chatHeadView.getLayoutParams();

            public void onTick(long t) {
                long step = (ANIMATION_TIME - t) / ANIMATION_STEP;
                chatHeadParams.x = windowSize.x + (int) (double) motionValue(step, currentXCoordinate)
                    - chatHeadView.getWidth();
                windowManager.updateViewLayout(chatHeadView, chatHeadParams);
            }

            public void onFinish() {
                chatHeadParams.x = windowSize.x - chatHeadView.getWidth();
                windowManager.updateViewLayout(chatHeadView, chatHeadParams);
            }
        }.start();
    }

    /**
     * The motion amount
     *
     * @param step  the step count from 1 -> 0
     * @param scale the distance to step
     * @return the value
     */
    private double motionValue(long step, long scale) {
        return scale * java.lang.Math.exp(-0.1 * step);
    }

    /**
     * Returns the bottom margin of the remove view
     *
     * @return the margin value
     */
    private int getBottomMargin() {
        return Functions.dpToPixels(this, 36);
    }

    /**
     * Handles the chat head click - opens the activity
     */
    private void chatHeadClick() {
        if (NoteActivity.active) {
            item = NoteActivity.instance.getSavableNote();
            NoteActivity.instance.finish();
        } else {
            // moveToRight((int)chatHeadView.getX());
            moveToTop((int) chatHeadView.getY(), Functions.dpToPixels(this, 24));

            Intent noteIntent = new Intent(this, NoteActivity.class).setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            if (item != null && item.id != null) {
                noteIntent.putExtra(NoteActivity.EXISTING_NOTE, db.get(item.id, item));
            }
            noteIntent.putExtra(NoteActivity.OPENED_FROM_SERVICE, true);

            startActivity(noteIntent);
        }

    }

    /**
     * Displays the Remove View on the bottom
     */
    private void showRemoveView() {
        int removeViewX = (windowSize.x - removeView.getWidth()) / 2;
        int removeViewY = windowSize.y - (removeView.getHeight() + getBottomMargin());

        WindowManager.LayoutParams removeViewParams =
            (WindowManager.LayoutParams) removeView.getLayoutParams();
        removeViewParams.x = removeViewX;
        removeViewParams.y = removeViewY;

        windowManager.updateViewLayout(removeView, removeViewParams);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && intent.hasExtra(NoteActivity.EXISTING_NOTE)) {
            item = (NoteItem) intent.getSerializableExtra(NoteActivity.EXISTING_NOTE);
        }

        if (startId == Service.START_STICKY) {
            handleStart();
            return super.onStartCommand(intent, flags, startId);
        } else {
            return Service.START_NOT_STICKY;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (chatHeadView != null) {
            windowManager.removeView(chatHeadView);
        }

        if (removeView != null) {
            windowManager.removeView(removeView);
        }

    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}