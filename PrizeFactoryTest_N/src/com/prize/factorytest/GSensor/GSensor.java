package com.prize.factorytest.GSensor;

import com.prize.factorytest.R;
import com.prize.factorytest.PrizeFactoryTestListActivity;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.KeyEvent;

import android.content.Intent;
import android.util.Log;
import java.io.File;
import java.io.IOException; 
import java.io.FileInputStream;
import java.io.InputStreamReader;  
import java.io.FileOutputStream;

public class GSensor extends Activity {

	private SensorManager mSensorManager = null;
	private Sensor mGSensor = null;
	private GSensorListener mGSensorListener;
	TextView mTextView;
	Button cancelButton;
	private final static String INIT_VALUE = "";
	private static String value = INIT_VALUE;
	private static String pre_value = INIT_VALUE;
	private final int MIN_COUNT = 15;
	private final static int SENSOR_TYPE = Sensor.TYPE_ACCELEROMETER;
	private final static int SENSOR_DELAY = SensorManager.SENSOR_DELAY_FASTEST;

	private boolean bLeft = false;
	private boolean bRight = false;

	private static Button buttonPass;
	private boolean isHorCali = false;
	
	private static final String filePath = "/data/prize_backup/";
	private static final String fileName = "prize_factory_gsensor";
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return true;
		}
		return false;
	}
	
	@Override
	public void finish() {
		try {
			mSensorManager.unregisterListener(mGSensorListener, mGSensor);
		} catch (Exception e) {
		}
		super.finish();
	}

	void getService() {

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		if (mSensorManager == null) {
			fail(getString(R.string.service_get_fail));
		}

		mGSensor = mSensorManager.getDefaultSensor(SENSOR_TYPE);
		if (mGSensor == null) {
			fail(getString(R.string.sensor_get_fail));
		}

		mGSensorListener = new GSensorListener(this);
		if (!mSensorManager.registerListener(mGSensorListener, mGSensor,
				SENSOR_DELAY)) {
			mSensorManager.registerListener(mGSensorListener, mGSensor,
					SENSOR_DELAY);
			fail(getString(R.string.sensor_register_fail));
		}
	}

	void updateView(Object s) {
		if(!isHorCali){
			buttonPass.setEnabled(false);
			mTextView.setText(getString(R.string.gsensor_name) + " : " + "sensorCalibration fail!");
			return;
		}
		if (bLeft && bRight) {
			buttonPass.setEnabled(true);
		}
		mTextView.setText(getString(R.string.gsensor_name) + " : " + s);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.gsensor);
		mTextView = (TextView) findViewById(R.id.gsensor_result);
		confirmButton();
		
		Intent intent = new Intent();
		intent.setClassName("com.android.HorCali", 
			"com.android.HorCali.sensor.SensorCalibration");
		intent.putExtra("gsensor_factorytest", true);  
		startActivityForResult(intent, 0);
	}

	@Override  
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
        super.onActivityResult(requestCode, resultCode, data);  
		if(resultCode == RESULT_OK){  
			isHorCali = true;
			String sHorCali = data.getStringExtra("sHorCali");
			creatFile();
			writeFile(sHorCali);
			if(null == sHorCali){
				return;
			}
			try {	
				String[] cmdMode = new String[]{"/system/bin/sh","-c","echo" + " " + sHorCali + " > /proc/gsensor_cali"};
				Runtime.getRuntime().exec(cmdMode);				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			Log.e("liup","sHorCali = " + sHorCali);
			getService();
		}     
		updateView(value);		
    }  

	private void creatFile() {
		File file = null;
		try {
			file = new File(filePath);
			if (!file.exists()) {
				file.mkdirs();
			}
		} catch (Exception e) {
		}
	
		file = new File(filePath + fileName);
        if(!file.exists()){  
            try {  
                file.createNewFile();
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }else {  
        }  
		
		int status = -1;  
		try {  
			Process p = Runtime.getRuntime().exec("chmod 777 " + filePath + fileName);  
			status = p.waitFor();  
		} catch (IOException e) {  
			e.printStackTrace();  
		} catch (InterruptedException e) {  
			e.printStackTrace();  
		}  
		if (status == 0) {       
			Log.e("liup","chmod succeed");
		} else {      
			Log.e("liup","chmod failed");
		}    
		 
	}
	
	private void writeFile(String data) {
		try {
			FileOutputStream fout = new FileOutputStream(filePath + fileName);
			byte[] bytes = data.getBytes();
			fout.write(bytes);
			fout.flush();				
			fout.close();
			Log.e("liup","writeFile succcess");
		} catch (Exception e) {
		}
	}
	
	public void confirmButton() {
		buttonPass = (Button) findViewById(R.id.passButton);
		buttonPass.setEnabled(false);
		final Button buttonFail = (Button) findViewById(R.id.failButton);
		buttonPass.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (PrizeFactoryTestListActivity.toStartAutoTest == true) {
					PrizeFactoryTestListActivity.itempos++;
				}
				setResult(RESULT_OK);
				finish();
			}
		});
		buttonFail.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (PrizeFactoryTestListActivity.toStartAutoTest == true) {
					PrizeFactoryTestListActivity.itempos++;
				}
				setResult(RESULT_CANCELED);
				finish();

			}

		});
	}

	void fail(Object msg) {
		toast(msg);
		setResult(RESULT_CANCELED);
		finish();
	}
	@Override
	protected void onDestroy() {

		super.onDestroy();
		
		if (mSensorManager == null || mGSensorListener == null
				|| mGSensor == null)
			return;
		mSensorManager.unregisterListener(mGSensorListener, mGSensor);
	}

	public class GSensorListener implements SensorEventListener {

		private int count = 0;

		public GSensorListener(Context context) {

			super();
		}

		public void onSensorChanged(SensorEvent event) {

			synchronized (this) {
				ImageView gsensor = (ImageView) findViewById(R.id.gsensor_image);
				if (event.sensor.getType() == SENSOR_TYPE) {
					String value = "(x:" + event.values[0] + ", y:"
							+ event.values[1] + ", z:" + event.values[2] + ")";

					if (event.values[0] > event.values[1]
							&& event.values[1] > 0) {
						bLeft = true;
						gsensor.setBackgroundResource(R.drawable.gsensor_left);
					}
					if (event.values[1] > event.values[0]
							&& event.values[0] > 0) {
						gsensor.setBackgroundResource(R.drawable.gsensor_down);
					}
					if (event.values[1] < 0
							&& event.values[0] < event.values[1]) {
						bRight = true;
						gsensor.setBackgroundResource(R.drawable.gsensor_right);
					}
					if (event.values[0] < 0
							&& event.values[1] < event.values[0]) {
						gsensor.setBackgroundResource(R.drawable.gsensor_up);
					}
					if (event.values[0] > 0 && event.values[1] < 0) {
						if (event.values[0] > Math.abs(event.values[1])) {
							bLeft = true;
							gsensor.setBackgroundResource(R.drawable.gsensor_left);
						} else {
							gsensor.setBackgroundResource(R.drawable.gsensor_up);
						}
					}
					if (event.values[0] < 0 && event.values[1] > 0) {
						if (Math.abs(event.values[0]) > event.values[1]) {
							bRight = true;
							gsensor.setBackgroundResource(R.drawable.gsensor_right);
						} else {
							gsensor.setBackgroundResource(R.drawable.gsensor_down);
						}
					}

					updateView(value);
					if (value != pre_value)
						count++;
					if (count >= MIN_COUNT)
						pre_value = value;
				}
			}
		}

		public void onAccuracyChanged(Sensor arg0, int arg1) {

		}
	}

	public void toast(Object s) {

		if (s == null)
			return;
		Toast.makeText(this, s + "", Toast.LENGTH_SHORT).show();
	}
}
