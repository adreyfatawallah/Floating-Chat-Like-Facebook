package com.example.adrey.floatingicon;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * Created by adrey on 8/7/17.
 */

public class ChatHeadService extends Service {

    private WindowManager mWindowManager;
    private View mChatHeadView, mClose;

    public ChatHeadService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Inflate the chat head layout we created
        mChatHeadView = LayoutInflater.from(this).inflate(R.layout.layout_chat_head, null);

        //Add the view the window
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        //Specify the chat head position
        //Initially view will be added to top-left corner
        params.gravity = Gravity.TOP | Gravity.RIGHT;
        params.x = 0;
        params.y = 0;

        /**
         * view menu close
         */
        mClose = LayoutInflater.from(this).inflate(R.layout.layout_chat_close, null);
        final WindowManager.LayoutParams paramsClose = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        paramsClose.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;

        final ImageView ivClose = mClose.findViewById(R.id.iv_menu_close);
        /**
         *
         */

        //Add the view to the window
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mChatHeadView, params);
        mWindowManager.addView(mClose, paramsClose);

        //Set the close button
        ImageView closeButton = mChatHeadView.findViewById(R.id.close_btn);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //close the service and remove the chat head from the window
                stopSelf();
            }
        });

        //Drag and move chat head using user's touch action
        final ImageView chatHeadImage = mChatHeadView.findViewById(R.id.chat_head_profile_iv);
        chatHeadImage.setOnTouchListener(new View.OnTouchListener() {

            private Intent intent;
            private int lastAction;
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //remember the initial position
                        initialX = params.x;
                        initialY = params.y;

                        //get the touch location
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();

                        lastAction = event.getAction();

                        return true;
                    case MotionEvent.ACTION_UP:
                        //As we implemented on touch listener with ACTION_MOVE,
                        //we have to check if the previous action was ACTION_DOWN
                        //to identity if the user clicked the view or not
                        if (lastAction == MotionEvent.ACTION_DOWN) {
                            //Open the chat conversation click
                            intent = new Intent(ChatHeadService.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);

                            //close the service and remove the chat heads
                            stopSelf();
                        } else if (lastAction == MotionEvent.ACTION_MOVE) {
                            //Get location on screen vewi chathead and view menu close
                            int intParams[] = new int[2];
                            mChatHeadView.getLocationOnScreen(intParams);
                            int intParamsClose[] = new int[2];
                            mClose.getLocationOnScreen(intParamsClose);
//                            Log.e("DATA", "params : " + intParams[0] + ", " + intParams[1]);
//                            Log.e("DATA", "paramsClose : " + intParamsClose[0] + ", " + intParamsClose[1]);

                            //Cek position icon floating between menu close
                            if ((intParams[0] > intParamsClose[0]-75 && intParams[0] < intParamsClose[0]+75)
                                    &&
                                    (intParams[1] > intParamsClose[1]-75 && intParams[1] < intParamsClose[1]+75)) {
                                stopSelf();
                            } else {
                                //Icon floating stay on the edge
                                int x = (int) (initialTouchX - event.getRawX());
                                if (x > 500)
                                    params.x = 1000;
                                else if (x < -500)
                                    params.x = 0;
                                else
                                    params.x = initialX;

                                mWindowManager.updateViewLayout(mChatHeadView, params);
                            }
                            ivClose.setVisibility(View.GONE);
                        }
                        lastAction = event.getAction();

                        return true;
                    case MotionEvent.ACTION_MOVE:
                        ivClose.setVisibility(View.VISIBLE);

                        //Calculate the X and Y coordinate of the view
                        params.x = initialX + (int) (initialTouchX - event.getRawX());
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);

//                        Log.e("Event", event.getRawX() + ", " + event.getRawY());
//                        Log.e("Initial", initialX + ", " + initialY);
//                        Log.e("Touch", initialTouchX + ", " + initialTouchY);
//
//                        int x = initialX + (int) (event.getRawX() - initialTouchX);
//                        int y = initialY + (int) (event.getRawY() - initialTouchY);
//                        Log.e("Move", "x : " + x + " - y : " + y);

                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(mChatHeadView, params);
                        lastAction = event.getAction();

                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatHeadView != null) mWindowManager.removeView(mChatHeadView);
        if (mClose != null) mWindowManager.removeView(mClose);
    }
}
