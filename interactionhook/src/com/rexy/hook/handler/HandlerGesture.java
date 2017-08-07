package com.rexy.hook.handler;

import com.rexy.hook.InteractionHook;
import com.rexy.hook.record.TouchRecord;

import java.util.Map;

/**
 * this handler is used observing touch gesture and calculate a result {@link ResultGesture} for its subscriber
 *
 * @author: rexy
 * @date: 2017-08-01 10:48
 */
public class HandlerGesture extends HookHandler {

    public HandlerGesture(String tag) {
        super(tag);
    }

    @Override
    public boolean handle(InteractionHook caller) {
        TouchRecord record = caller.getTouchRecord();
        if (record.isDraggedPossible()) {
            reportResult(new ResultGesture(record,getTag()));
            return true;
        }
        return false;
    }

    /**
     * a class to record gesture information such as position ,timestamp,distance,angle,velocity and so on.
     */
    public static class ResultGesture extends HandleResult {
        /**
         * touch down time
         */
        private long mDownTime;

        /**
         * touch down x
         */
        private float mDownX;

        /**
         * touch down y
         */
        private float mDownY;

        /**
         * distance between down point and up point
         */
        private float mLength;

        /**
         * [0,360] a angle of the vector quantity from down point to up point .
         * from 3 clock direction with a clockwise rotation for mAngle .
         */
        private float mAngle;

        /**
         * touch fling velocity x {@link android.view.VelocityTracker}
         */
        private float mFlingX;

        /**
         * touch fling velocity y {@link android.view.VelocityTracker}
         */
        private float mFlingY;


        private ResultGesture(TouchRecord record,String tag) {
            super(record.getTargetView(),tag, record.getUpTime());
            mDownX = record.getDownX();
            mDownY = record.getDownY();
            mDownTime = record.getDownTime();
            mFlingX = record.getVelocityX();
            mFlingY = record.getVelocityY();
            float upX = record.getUpX(), upY = record.getUpY();
            float dx = upX - mDownX, dy = upY - mDownY;
            mLength = (float) Math.sqrt(dx * dx + dy * dy);
            mAngle = pointToAngle(upX - mDownX, upY-mDownY);
        }

        private float pointToAngle(float dx, float dy) {
            if (dx == 0) {
                return (dy == 0 ? -1 : (dy > 0 ? 90 : 270));
            } else {
                if (dx > 0) {
                    return (dy < 0 ? 360 : 0) + (float) (180 * Math.atan(dy / dx) / Math.PI);
                } else {
                    return 180 + (float) (180 * Math.atan(dy / dx) / Math.PI);
                }
            }
        }

        /**
         * get point down x position.
         */
        public float getDownX() {
            return mDownX;
        }

        /**
         * get point down y position.
         */
        public float getDownY() {
            return mDownY;
        }

        /**
         * get touch fling velocity x {@link android.view.VelocityTracker}
         */
        public float getFlingX() {
            return mFlingX;
        }

        /**
         * get touch down timestamp
         */
        public long getDownTime() {
            return mDownTime;
        }

        /**
         * touch fling velocity y {@link android.view.VelocityTracker}
         */
        public float getFlingY() {
            return mFlingY;
        }

        /**
         * get point up x position.
         */
        public float getUpX() {
            return (float) (mDownX + mLength * Math.cos(mAngle * Math.PI / 180));
        }

        /**
         * get point up y position.
         */
        public float getUpY() {
            return (float) (mDownY + mLength * Math.sin(mAngle * Math.PI / 180));
        }

        /**
         * get touch up timestamp
         */
        public long getUpTime() {
            return getTimestamp();
        }

        /**
         * get distance move direct from down point to up point
         */
        public float getLength() {
            return mLength;
        }

        /**
         * get angle from up position refer to down position ,from 3 clock direction with a clockwise rotation for angle in range [0,360] .
         * @return
         */
        public float getAngle() {
            return mAngle;
        }

        /**
         * get gesture time delta between start and end .
         */
        public long getDeltaTime() {
            return getTimestamp() - mDownTime;
        }


        @Override
        protected void toShortStringImpl(StringBuilder receiver) {
            receiver.append(formatView(getTargetView())).append("{");
            receiver.append("downX=").append((int) mDownX).append(',');
            receiver.append("downY=").append((int) mDownY).append(',');
            receiver.append("length=").append((int) getLength()).append(',');
            receiver.append("angle=").append((int) getAngle()).append(',');
            receiver.append("flingX=").append((int)getFlingX()).append(',');
            receiver.append("flingY=").append((int)getFlingY()).append(',');
            receiver.append("downTime=").append(formatTime(getDownTime(), null)).append(',');
            receiver.append("time=").append(formatTime(getTimestamp(), null)).append(',');
            receiver.setCharAt(receiver.length() - 1, '}');
        }

        @Override
        protected void dumpResultImpl(Map<String, Object> receiver) {
            receiver.put("view", getTargetView());
            receiver.put("time", getTimestamp());
            receiver.put("downTime", getDownTime());
            receiver.put("downX", getDownX());
            receiver.put("downY", getDownY());
            receiver.put("upX", getUpX());
            receiver.put("upY", getUpY());
            receiver.put("upTime", getUpTime());
            receiver.put("deltaTime", getDeltaTime());
            receiver.put("flingX", getFlingX());
            receiver.put("flingY", getFlingY());
            receiver.put("length", getLength());
            receiver.put("angle", getAngle());
        }
    }
}
