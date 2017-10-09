package mechasolution.mpu_servo_1;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.things.contrib.driver.pwmservo.Servo;

import java.io.IOException;

import mechasolution.mpu6050.mpu6050;

import static mechasolution.mpu6050.mpu6050.DLPF_CFG_6;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    float angle = 0;
    long timepre = 0;
    private Servo mServo;
    private mpu6050 mMpu = new mpu6050();
    Thread mThread = new Thread() {
        public void run() {
            timepre = System.nanoTime();
            while (true) {
                try {
                    long timecur = System.nanoTime();
                    float GyroZ = mMpu.getGyroZ();
                    float dt = (float)((timecur - timepre) / 1000000000.);
                    timepre = timecur;
                    angle -= GyroZ * dt;
                    angle = Math.max(-90, (Math.min(90, angle)));

                    mServo.setAngle(angle);

                    Log.i(TAG, String.format("Time = %f Angle = %f", dt, angle));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    mThread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            mServo = new Servo("PWM1");
            mServo.setPulseDurationRange(0.75, 2.6);
            mServo.setAngleRange(-90, 90);
            mServo.setEnabled(true);
            mServo.setAngle(0);
        } catch (IOException e) { e.printStackTrace(); }

        try {
            mMpu.open();
            mMpu.setDLPF(DLPF_CFG_6);
        } catch (IOException e) { e.printStackTrace(); }

        try {
            mThread.sleep(1000);
        } catch (InterruptedException e) { e.printStackTrace(); }

        mThread.start();
    }
}