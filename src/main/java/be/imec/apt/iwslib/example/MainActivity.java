package be.imec.apt.iwslib.example;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import be.imec.apt.iwslib.shared.xmp.XmpDiscoverer;
import be.imec.apt.iwslib.shared.xmp.api.XmpDiscoveryApi;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Minimalistic example to demonstrate the use of the IWS (imec Wearable Streaming) library.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, XmpDiscoveryApi {

	static private final @IdRes int DEFAULT_DEVICE_TYPE_RADIO_ID = R.id.radioChillband;

	static private DeviceClient GetDeviceClient(@IdRes int selectionDeviceTypeRadioButtonId) {
		switch(selectionDeviceTypeRadioButtonId) {
			case R.id.radioStingray : return new StingrayClient();
			case R.id.radioChillband : return new ChillbandClient();
		}
		return null;
	}

	@BindView(R.id.button)
	Button button;

	@BindView(DEFAULT_DEVICE_TYPE_RADIO_ID)
	RadioButton radioButtonDefaultType;

	@BindView(R.id.radioGrpDeviceType)
	RadioGroup radioGrpDeviceType;

	@BindView(R.id.txtStatus)
	TextView txtStatus;

	private ProgressDialog scanDialog;

	private DeviceClient<?> deviceClient = GetDeviceClient(DEFAULT_DEVICE_TYPE_RADIO_ID);

	private Timer statusTimer = new Timer();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		// Select the default:
		radioButtonDefaultType.setChecked(true);

		// Switching between device types:
		radioGrpDeviceType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				deviceClient = GetDeviceClient(checkedId);
			}
		});

		// Button logic:
		button.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(!deviceClient.isStreaming()) {
			// Detect device:
			final XmpDiscoverer discoverer = new XmpDiscoverer();
			scanDialog = ProgressDialog.show(MainActivity.this, getString(R.string.app_name), getString(R.string.scanning_for, getString(deviceClient.getDeviceTypeStringRes())));
			scanDialog.setCancelable(true);
			scanDialog.setCanceledOnTouchOutside(false);
			scanDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					radioGrpDeviceType.setEnabled(true);
					discoverer.cancelSearchDevice();
				}
			});
			radioGrpDeviceType.setEnabled(false);
			discoverer.searchDevice(this);
		}
		else {
			// Stop stream:
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					statusTimer.cancel();
					txtStatus.setText("");
					deviceClient.stopStream(which == DialogInterface.BUTTON_POSITIVE);
					radioGrpDeviceType.setEnabled(true);
					button.setText(R.string.findAndStream);
				}
			};
			new AlertDialog.Builder(MainActivity.this)
					.setMessage(R.string.alsoEndDataCollection)
					.setPositiveButton(R.string.yes, dialogClickListener)
					.setNegativeButton(R.string.no, dialogClickListener)
					.show();
		}
	}

	@Override
	public void didFail(@Nullable Exception e) {
		Log.e(MainActivity.class.getSimpleName(),"Device detection failed", e);
		// TODO cancel dialog & discovery
	}

	@Override
	public void didDiscoverDeviceWithAddress(final @NonNull String address, final @Nullable String name, final BluetoothSocket btSocket) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				new AlertDialog.Builder(MainActivity.this)
						.setMessage(getString(R.string.streamConfirmation, name, address, getString(deviceClient.getDeviceTypeStringRes())))
						.setPositiveButton(R.string.yes,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										scanDialog.cancel();
										button.setText(R.string.stopSteam);
										deviceClient.startStream(btSocket);
										statusTimer.scheduleAtFixedRate(new TimerTask() {
											@Override
											public void run() {
												runOnUiThread(new Runnable() {
													@Override
													public void run() {
														txtStatus.setText(getString(R.string.status, getString(deviceClient.isConnected() ? R.string.connected : R.string.disconnected), deviceClient.getNumberOfReadings(), deviceClient.getBatteryLevel()));
													}
												});
											}
										}, 0, 1000);
									}
								})
						.setNegativeButton(android.R.string.no,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										// TODO continue search
									}
								})
						.show();
			}
		});
	}

	@Override
	public void didFinishDiscovery() {
		// TODO ?
	}

	@Override
	public boolean keepConnectionOpenUponDetection() {
		return true;
	}

}
