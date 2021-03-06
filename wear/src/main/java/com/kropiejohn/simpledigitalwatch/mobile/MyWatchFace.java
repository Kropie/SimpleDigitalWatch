/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kropiejohn.simpledigitalwatch.mobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.kropiejohn.simpledigitalwatch.R;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 */
public class MyWatchFace extends CanvasWatchFaceService {
    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    /**
     * Update rate in milliseconds for interactive mode. We update once a second since seconds are
     * displayed in interactive mode.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    private static final String TAG = "MyWatchFace";
    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine implements SharedPreferences.OnSharedPreferenceChangeListener {
        final Handler mUpdateTimeHandler = new EngineHandler(this);
        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };
        boolean mRegisteredTimeZoneReceiver = false;

        Paint mBackgroundPaint;
        Paint mMinutesTextPaint;
        Integer mTimeTextColor;

        boolean mAmbient;
        Calendar mCalendar;

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;
        boolean mShowSeconds;
        boolean mShowDate;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            setWatchFaceStyle(new WatchFaceStyle.Builder(MyWatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .setAcceptsTapEvents(true)
                    .build());
            Resources resources = MyWatchFace.this.getResources();

            mBackgroundPaint = new Paint();

            mMinutesTextPaint = new Paint();
            mMinutesTextPaint = createTextPaint(resources.getColor(R.color.digital_text));

            mCalendar = Calendar.getInstance();

            setupSharedPreferences();
        }

        private void setupSharedPreferences() {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            String timeTypefaceFile = sharedPreferences.getString(getString(R.string.time_typeface_key), null);
            if (timeTypefaceFile != null) {
                Typeface timeTypeface = Typeface.DEFAULT;

                try {
                    timeTypeface = Typeface.createFromAsset(getAssets(), String.format("%s/%s", getString(R.string.fonts_path), timeTypefaceFile));
                } catch (Exception e) {
                    Log.e(TAG, String.format(Locale.US, "setupSharedPreferences: Unable to find font asset %s", timeTypefaceFile));
                }

                mMinutesTextPaint.setTypeface(timeTypeface);
            }

            mBackgroundPaint.setColor(sharedPreferences.getInt(getString(R.string.background_color_key), Color.BLACK));

            mTimeTextColor = sharedPreferences.getInt(getString(R.string.foreground_color_key), Color.WHITE);
            mMinutesTextPaint.setColor(mTimeTextColor);

            mShowSeconds = sharedPreferences.getBoolean(getString(R.string.show_seconds_key), false);
            mShowDate = sharedPreferences.getBoolean(getString(R.string.show_date_key), false);

            sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        }


        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(getString(R.string.background_color_key))) {
                int backgroundColor = sharedPreferences.getInt(getString(R.string.background_color_key), Color.BLACK);
                mBackgroundPaint.setColor(backgroundColor);
                invalidate();
            } else if (key.equals(getString(R.string.foreground_color_key))) {
                mTimeTextColor = sharedPreferences.getInt(getString(R.string.foreground_color_key), Color.WHITE);
                mMinutesTextPaint.setColor(mTimeTextColor);
                invalidate();
            } else if (key.equals(getString(R.string.time_typeface_key))) {
                String typefaceFile = sharedPreferences.getString(getString(R.string.time_typeface_key), null);
                if (typefaceFile != null) {
                    try {
                        Typeface typeFace = Typeface.createFromAsset(getAssets(), String.format("%s/%s", getString(R.string.fonts_path), typefaceFile));
                        mMinutesTextPaint.setTypeface(typeFace);
                        invalidate();
                    } catch (Exception e) {
                        Log.e(TAG, String.format("onSharedPreferenceChanged: ERROR %s", e.toString()));
                    }
                }
            } else if (key.equals(getString(R.string.show_date_key))) {
                mShowDate = sharedPreferences.getBoolean(key, true);
            } else if (key.equals(getString(R.string.show_seconds_key))) {
                mShowSeconds = sharedPreferences.getBoolean(key, true);
            }
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        private Paint createTextPaint(int textColor) {
            Paint paint = new Paint();
            paint.setColor(textColor);
            paint.setTypeface(NORMAL_TYPEFACE);
            paint.setAntiAlias(true);
            return paint;
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            } else {
                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            MyWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            MyWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            Resources resources = MyWatchFace.this.getResources();
            boolean isRound = insets.isRound();
            float textSize = resources.getDimension(isRound
                    ? R.dimen.digital_text_size_round : R.dimen.digital_text_size);

            mMinutesTextPaint.setTextSize(textSize);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;
                if (mLowBitAmbient) {
                    mMinutesTextPaint.setAntiAlias(!inAmbientMode);
                }

                if (mAmbient) {
                    mMinutesTextPaint.setColor(Color.WHITE);
                } else {
                    mMinutesTextPaint.setColor(mTimeTextColor);
                }

                invalidate();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            drawBackground(canvas, mBackgroundPaint, bounds);
            drawTimeAndDateText(canvas, bounds);
        }

        private void drawBackground(Canvas canvas, Paint paint, Rect bounds) {
            // Draw the background.
            if (isInAmbientMode()) {
                canvas.drawColor(Color.BLACK);
            } else {
                canvas.drawRect(0, 0, bounds.width(), bounds.height(), paint);
            }
        }

        private String getTimeText() {
            // Draw H:MM in ambient mode or H:MM:SS in interactive mode.
            long now = System.currentTimeMillis();
            mCalendar.setTimeInMillis(now);

            // If the time is midnight and the time is "0" change the hour value to "12"
            int hour = mCalendar.get(Calendar.HOUR);
            hour = (hour == 0) ? hour + 12 : hour;
            String time = String.format(Locale.US, "%2d:%02d", hour, mCalendar.get(Calendar.MINUTE));

            // If not ambient return time without seconds.
            if (mAmbient || !mShowSeconds) {
                return time;
            }

            // If ambient return time with seconds.
            return String.format(Locale.US, "%s:%02d", time, mCalendar.get(Calendar.SECOND));
        }

        private String getDateText() {
            SimpleDateFormat monthDateFormat = new SimpleDateFormat("MMM");
            String day = String.valueOf(mCalendar.get(Calendar.DAY_OF_MONTH));
            String month = monthDateFormat.format(mCalendar.getTime());

            return String.format(Locale.US, "%s %s", month, day);
        }

        private void drawTimeAndDateText(Canvas canvas, Rect bounds) {
            String text = getTimeText();
            String defaultText;

            if(mShowSeconds)
                defaultText = text.substring(0, text.length() - 3) + ":00";
            else
                defaultText = text;

            Rect textBounds = new Rect();

            float minutesX;
            float minutesY;

            // Use the same text for all times to ensure that the watch face does not move as the
            // time changes
            mMinutesTextPaint.setTextAlign(Paint.Align.LEFT);
            mMinutesTextPaint.getTextBounds(defaultText, 0, defaultText.length(), textBounds);

            minutesX = bounds.width() / 2f - textBounds.width() / 2f - textBounds.left;
            minutesY = bounds.height() / 2f + textBounds.height() / 2f - textBounds.bottom;
            canvas.drawText(text, minutesX, minutesY, mMinutesTextPaint);

            if (mShowDate) {
                float originalTextSize = mMinutesTextPaint.getTextSize();
                float textSize = originalTextSize / 2f;
                mMinutesTextPaint.setTextSize(textSize);

                String dateText = getDateText();
                mMinutesTextPaint.getTextBounds(dateText, 0, dateText.length(), textBounds);

                float dateX = bounds.width() / 2f - textBounds.width() / 2f - textBounds.left;
                float dateY = minutesY + textBounds.height() * 5 / 4;

                canvas.drawText(getDateText(), dateX, dateY, mMinutesTextPaint);
                mMinutesTextPaint.setTextSize(originalTextSize);
            }

        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<MyWatchFace.Engine> mWeakReference;

        public EngineHandler(MyWatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            MyWatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }
}
