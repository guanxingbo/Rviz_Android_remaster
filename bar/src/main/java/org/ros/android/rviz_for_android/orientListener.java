//package org.ros.android.rviz_for_android;
//
//import android.content.Context;
//import android.content.pm.ActivityInfo;
//import android.util.Log;
//import android.view.OrientationEventListener;
//
//import static android.content.ContentValues.TAG;
//
//public class orientListener extends OrientationEventListener {
//    public orientListener(Context context) {
//        super(context);
//    }
//
//    public orientListener(Context context, int rate) {
//        super(context, rate);
//    }
//
//    @Override
//    public void onOrientationChanged(int orientation) {
//        Log.d(TAG, "orention" + orientation);
//        int screenOrientation = getResources().getConfiguration().orientation;
//        if (((orientation >= 0) && (orientation < 45)) || (orientation > 315)) {//设置竖屏
//            if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT && orientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
//                Log.d(TAG, "0");
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//
//            }
//        } else if (orientation > 225 && orientation < 315) { //设置横屏
//            Log.d(TAG, "90");
//            if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//
//            }
//        } else if (orientation > 45 && orientation < 135) {// 设置反向横屏
//            Log.d(TAG, "270");
//            if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
//
//            }
//        } else if (orientation > 135 && orientation < 225) {
//            Log.d(TAG, "180");
//            if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
//
//            }
//        }
//    }
//}
