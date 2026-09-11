package com.muntasir.launcher;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    LauncherView launcherView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.setStatusBarColor(Color.rgb(7, 9, 15));
        window.setNavigationBarColor(Color.rgb(7, 9, 15));

        launcherView = new LauncherView();
        setContentView(launcherView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (launcherView != null) {
            launcherView.loadApps();
        }
    }

    private class LauncherView extends View {

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        List<AppInfo> apps = new ArrayList<>();

        Handler handler = new Handler();

        float centerX;
        float centerY;

        float rotation = 0f;
        float targetRotation = 0f;

        float lastX;
        boolean dragging = false;

        Runnable clockRunnable;

        LauncherView() {
            super(MainActivity.this);

            setLayerType(View.LAYER_TYPE_SOFTWARE, null);

            paint.setTypeface(Typeface.create("sans", Typeface.NORMAL));

            clockRunnable = new Runnable() {
                @Override
                public void run() {
                    invalidate();
                    handler.postDelayed(this, 1000);
                }
            };

            handler.post(clockRunnable);

            loadApps();
        }

        void loadApps() {

            apps.clear();

            PackageManager pm = getPackageManager();

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            List<ResolveInfo> activities =
                    pm.queryIntentActivities(intent, 0);

            Collections.sort(
                    activities,
                    new Comparator<ResolveInfo>() {
                        @Override
                        public int compare(
                                ResolveInfo a,
                                ResolveInfo b) {

                            String aName =
                                    a.loadLabel(pm).toString();

                            String bName =
                                    b.loadLabel(pm).toString();

                            return aName.compareToIgnoreCase(bName);
                        }
                    }
            );

            for (ResolveInfo info : activities) {

                String label =
                        info.loadLabel(pm).toString();

                Drawable icon =
                        info.loadIcon(pm);

                Intent launchIntent =
                        new Intent(Intent.ACTION_MAIN);

                launchIntent.addCategory(
                        Intent.CATEGORY_LAUNCHER
                );

                launchIntent.setClassName(
                        info.activityInfo.packageName,
                        info.activityInfo.name
                );

                apps.add(
                        new AppInfo(
                                label,
                                icon,
                                launchIntent
                        )
                );
            }

            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {

            super.onDraw(canvas);

            centerX = getWidth() / 2f;
            centerY = getHeight() / 2f;

            drawBackground(canvas);
            drawHeader(canvas);
            drawOrbitalApps(canvas);
            drawBottomInfo(canvas);

            rotation +=
                    (targetRotation - rotation) * 0.08f;

            if (Math.abs(targetRotation - rotation) > 0.2f) {
                invalidate();
            }
        }

        void drawBackground(Canvas canvas) {

            canvas.drawColor(
                    Color.rgb(7, 9, 15)
            );

            paint.setStyle(Paint.Style.FILL);

            paint.setColor(
                    Color.argb(25, 90, 120, 255)
            );

            canvas.drawCircle(
                    centerX,
                    centerY,
                    Math.min(getWidth(), getHeight()) * 0.42f,
                    paint
            );

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2f);

            paint.setColor(
                    Color.argb(45, 110, 140, 255)
            );

            float radius =
                    Math.min(getWidth(), getHeight()) * 0.36f;

            canvas.drawCircle(
                    centerX,
                    centerY,
                    radius,
                    paint
            );

            paint.setColor(
                    Color.argb(22, 120, 150, 255)
            );

            canvas.drawCircle(
                    centerX,
                    centerY,
                    radius * 0.68f,
                    paint
            );

            paint.setStyle(Paint.Style.FILL);
        }

        void drawHeader(Canvas canvas) {

            paint.setTypeface(
                    Typeface.create(
                            "sans",
                            Typeface.BOLD
                    )
            );

            paint.setTextAlign(
                    Paint.Align.CENTER
            );

            paint.setTextSize(30);

            paint.setColor(Color.WHITE);

            canvas.drawText(
                    "MUNTASIR",
                    centerX,
                    70,
                    paint
            );

            paint.setTypeface(
                    Typeface.create(
                            "sans",
                            Typeface.NORMAL
                    )
            );

            paint.setTextSize(14);

            paint.setColor(
                    Color.rgb(145, 155, 180)
            );

            canvas.drawText(
                    "LAUNCHER",
                    centerX,
                    94,
                    paint
            );

            paint.setTextSize(28);

            paint.setColor(Color.WHITE);

            String time =
                    new SimpleDateFormat(
                            "HH:mm",
                            Locale.getDefault()
                    ).format(new Date());

            canvas.drawText(
                    time,
                    centerX,
                    145,
                    paint
            );

            paint.setTextSize(13);

            paint.setColor(
                    Color.rgb(130, 140, 165)
            );

            String date =
                    new SimpleDateFormat(
                            "EEEE • dd MMMM",
                            Locale.getDefault()
                    ).format(new Date());

            canvas.drawText(
                    date,
                    centerX,
                    169,
                    paint
            );
        }

        void drawOrbitalApps(Canvas canvas) {

            if (apps.isEmpty()) {

                paint.setTextAlign(
                        Paint.Align.CENTER
                );

                paint.setTextSize(17);

                paint.setColor(Color.WHITE);

                canvas.drawText(
                        "لا توجد تطبيقات",
                        centerX,
                        centerY,
                        paint
                );

                return;
            }

            int visible =
                    Math.min(apps.size(), 18);

            float radius =
                    Math.min(getWidth(), getHeight())
                            * 0.31f;

            for (int i = 0; i < visible; i++) {

                double angle =
                        ((double) i / visible)
                        * Math.PI * 2
                        + rotation;

                float x =
                        centerX
                        + (float)Math.cos(angle)
                        * radius;

                float y =
                        centerY
                        + (float)Math.sin(angle)
                        * radius;

                drawApp(canvas, apps.get(i), x, y);
            }

            paint.setStyle(Paint.Style.FILL);

            paint.setColor(
                    Color.argb(220, 18, 22, 34)
            );

            canvas.drawCircle(
                    centerX,
                    centerY,
                    58,
                    paint
            );

            paint.setStyle(Paint.Style.STROKE);

            paint.setStrokeWidth(2);

            paint.setColor(
                    Color.argb(100, 110, 140, 255)
            );

            canvas.drawCircle(
                    centerX,
                    centerY,
                    58,
                    paint
            );

            paint.setStyle(Paint.Style.FILL);

            paint.setTextAlign(
                    Paint.Align.CENTER
            );

            paint.setTypeface(
                    Typeface.create(
                            "sans",
                            Typeface.BOLD
                    )
            );

            paint.setTextSize(14);

            paint.setColor(Color.WHITE);

            canvas.drawText(
                    "M",
                    centerX,
                    centerY + 5,
                    paint
            );
        }

        void drawApp(
                Canvas canvas,
                AppInfo app,
                float x,
                float y
        ) {

            float size = 54;

            paint.setStyle(Paint.Style.FILL);

            paint.setColor(
                    Color.argb(210, 22, 26, 39)
            );

            paint.setShadowLayer(
                    14,
                    0,
                    5,
                    Color.argb(80, 0, 0, 0)
            );

            canvas.drawCircle(
                    x,
                    y,
                    size / 2,
                    paint
            );

            paint.clearShadowLayer();

            RectF rect =
                    new RectF(
                            x - 21,
                            y - 21,
                            x + 21,
                            y + 21
                    );

            app.icon.setBounds(
                    (int)rect.left,
                    (int)rect.top,
                    (int)rect.right,
                    (int)rect.bottom
            );

            app.icon.draw(canvas);

            paint.setTextAlign(
                    Paint.Align.CENTER
            );

            paint.setTypeface(
                    Typeface.create(
                            "sans",
                            Typeface.NORMAL
                    )
            );

            paint.setTextSize(11);

            paint.setColor(Color.WHITE);

            String name = app.name;

            if (name.length() > 13) {
                name = name.substring(0, 12) + "…";
            }

            canvas.drawText(
                    name,
                    x,
                    y + 47,
                    paint
            );
        }

        void drawBottomInfo(Canvas canvas) {

            paint.setTextAlign(
                    Paint.Align.CENTER
            );

            paint.setTextSize(12);

            paint.setColor(
                    Color.rgb(100, 110, 135)
            );

            canvas.drawText(
                    "اسحب لتدوير التطبيقات",
                    centerX,
                    getHeight() - 55,
                    paint
            );

            canvas.drawText(
                    apps.size() + " تطبيق",
                    centerX,
                    getHeight() - 32,
                    paint
            );
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:

                    lastX = event.getX();
                    dragging = false;

                    return true;

                case MotionEvent.ACTION_MOVE:

                    float dx =
                            event.getX() - lastX;

                    if (Math.abs(dx) > 4) {
                        dragging = true;

                        targetRotation +=
                                dx * 0.008f;

                        lastX = event.getX();

                        invalidate();
                    }

                    return true;

                case MotionEvent.ACTION_UP:

                    if (!dragging) {
                        launchAt(
                                event.getX(),
                                event.getY()
                        );
                    }

                    return true;
            }

            return true;
        }

        void launchAt(float touchX, float touchY) {

            if (apps.isEmpty()) {
                return;
            }

            int visible =
                    Math.min(apps.size(), 18);

            float radius =
                    Math.min(getWidth(), getHeight())
                            * 0.31f;

            float bestDistance = 999999;
            AppInfo selected = null;

            for (int i = 0; i < visible; i++) {

                double angle =
                        ((double) i / visible)
                        * Math.PI * 2
                        + rotation;

                float x =
                        centerX
                        + (float)Math.cos(angle)
                        * radius;

                float y =
                        centerY
                        + (float)Math.sin(angle)
                        * radius;

                float dx =
                        touchX - x;

                float dy =
                        touchY - y;

                float distance =
                        (float)Math.sqrt(
                                dx * dx + dy * dy
                        );

                if (distance < bestDistance) {

                    bestDistance = distance;
                    selected = apps.get(i);
                }
            }

            if (selected != null &&
                    bestDistance < 65) {

                try {

                    selected.intent.addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                    );

                    startActivity(
                            selected.intent
                    );

                } catch (Exception e) {

                    Toast.makeText(
                            MainActivity.this,
                            "تعذر فتح التطبيق",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        }
    }

    private static class AppInfo {

        String name;
        Drawable icon;
        Intent intent;

        AppInfo(
                String name,
                Drawable icon,
                Intent intent
        ) {
            this.name = name;
            this.icon = icon;
            this.intent = intent;
        }
    }
                              }
