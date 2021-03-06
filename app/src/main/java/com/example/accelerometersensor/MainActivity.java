package com.example.accelerometersensor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.Objects;

import static java.lang.Thread.*;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";
    SensorManager sensorManager;
    Sensor accelerometerSensor;

    public static TextView xValue;
    public static TextView yValue;
    public static TextView zValue;

    public static LineChart lineChart;
    Thread thread;
    boolean plotData = true;
    Context context;
    RelativeLayout rl;

    View view;

    long lastUpdate;

    Button resetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        xValue = findViewById(R.id.xValue);
        rl = findViewById(R.id.rl);
        rl.setBackgroundColor(Color.WHITE);
        xValue.setBackgroundColor(Color.MAGENTA);
        yValue = findViewById(R.id.yValue);
        yValue.setBackgroundColor(Color.MAGENTA);
        zValue = findViewById(R.id.zValue);
        zValue.setBackgroundColor(Color.MAGENTA);
        context = MainActivity.this;
        resetButton = findViewById(R.id.resetButton);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rl.setBackgroundColor(Color.WHITE);
                Toast.makeText(context, "You have reset the background color to white.", Toast.LENGTH_LONG).show();
            }
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        lineChart = findViewById(R.id.lineChart);
        lineChart.getDescription().setEnabled(true);
        lineChart.getDescription().setText("Real Time Accelerometer Sensor Data Plot");
        lineChart.setTouchEnabled(false);
        lineChart.setDragEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setPinchZoom(false);
        lineChart.setBackgroundColor(Color.WHITE);

        LineData lineData = new LineData();
        lineData.setValueTextColor(Color.BLACK);
        lineChart.setData(lineData);

        Legend legend = lineChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextColor(Color.BLACK);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(true);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setEnabled(true);

        YAxis yAxisLeft = lineChart.getAxisLeft();
        yAxisLeft.setTextColor(Color.BLACK);
        yAxisLeft.setDrawGridLines(false);
        yAxisLeft.setAxisMaximum(20f);
        yAxisLeft.setAxisMinimum(-10f);
        yAxisLeft.setDrawGridLines(true);

        YAxis yAxisRight = lineChart.getAxisRight();
        yAxisRight.setEnabled(false);

        lineChart.getAxisLeft().setDrawGridLines(false);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.setDrawBorders(false);

        startPlot();


        rl.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {
            public void onSwipeTop() {
                Toast.makeText(context, "You have swiped upwards.", Toast.LENGTH_LONG).show();
                rl.setBackgroundColor(Color.WHITE);
            }

            public void onSwipeRight() {
                Toast.makeText(context, "You have answered the call.", Toast.LENGTH_LONG).show();
                rl.setBackgroundColor(Color.GREEN);
            }

            public void onSwipeLeft() {
                Toast.makeText(context, "You have rejected the call.", Toast.LENGTH_LONG).show();
                rl.setBackgroundColor(Color.RED);
            }

            public void onSwipeBottom() {
                Toast.makeText(context, "You have swiped downwards.", Toast.LENGTH_LONG).show();
                rl.setBackgroundColor(Color.WHITE);
            }

        });

    }

    private void startPlot() {
        if (thread != null) {
            thread.interrupt();
        }
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    plotData = true;
                    try {
                        sleep(1000);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }


    private void addEntry(SensorEvent event) {
        LineData data = lineChart.getData();

        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            ILineDataSet set1 = data.getDataSetByIndex(1);
            ILineDataSet set2 = data.getDataSetByIndex(2);

            if (set == null) {
                set = createSet();
                set1 = createSet1();
                set2 = createSet2();
                data.addDataSet(set);
                data.addDataSet(set1);
                data.addDataSet(set2);
            }

            data.addEntry(new Entry(set.getEntryCount(), event.values[0] + 5), 0);
            data.addEntry(new Entry(set1.getEntryCount(), event.values[1] + 1), 1);
            data.addEntry(new Entry(set2.getEntryCount(), event.values[2] + 5), 2);
            data.notifyDataChanged();
            lineChart.notifyDataSetChanged();
            lineChart.setMaxVisibleValueCount(150);
            lineChart.moveViewToX(data.getEntryCount());
            lineChart.setVisibleXRange(0f, 20f);
        }
    }

    private LineDataSet createSet() {
        LineDataSet lineDataSet = new LineDataSet(null, "Dynamic Data");
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setLineWidth(3f);
        lineDataSet.setColor(Color.RED);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setCubicIntensity(0.2f);
        return lineDataSet;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        xValue.setText("X Value: " + event.values[0]);
        yValue.setText("Y Value: " + event.values[1]);
        zValue.setText("Z Value: " + event.values[2]);

        if(event.values[0] < -3) {
            rl.setBackgroundColor(Color.GREEN);
            Toast.makeText(context, "You have answered the call.", Toast.LENGTH_LONG).show();
        }

        if(event.values[0] > 3) {
            rl.setBackgroundColor(Color.RED);
            Toast.makeText(context, "You have rejected the call.", Toast.LENGTH_LONG).show();
        }

        addEntry(event);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        thread.interrupt();
        xValue = null;
        yValue = null;
        zValue = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (thread != null) {
            thread.interrupt();
        }
        sensorManager.unregisterListener(this);
    }

    private LineDataSet createSet1() {
        LineDataSet set = new LineDataSet(null, "Y Axis");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(3f);
        set.setColor(Color.BLUE);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }

    private LineDataSet createSet2() {
        LineDataSet set = new LineDataSet(null, "Z Axis");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(3f);
        set.setColor(Color.CYAN);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }
}