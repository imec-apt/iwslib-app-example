package be.imec.apt.iwslib.example.simple;

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

import be.imec.apt.iwslib.example.ChillbandClient;
import be.imec.apt.iwslib.example.DeviceClient;
import be.imec.apt.iwslib.example.R;
import be.imec.apt.iwslib.example.StingrayClient;
import be.imec.apt.iwslib.shared.xmp.XmpDiscoverer;
import be.imec.apt.iwslib.shared.xmp.api.XmpDiscoveryApi;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Minimalistic example to demonstrate the use of the IWS (imec Wearable Streaming) library.
 */
public class DiscoverAndStreamActivity extends AppCompatActivity implements View.OnClickListener, XmpDiscoveryApi {

	static private final @IdRes int DEFAULT_DEVICE_TYPE_RADIO_ID = R.id.radioChillband;

	@BindView(R.id.btn_discover_and_stream)
	Button button;

	@BindView(DEFAULT_DEVICE_TYPE_RADIO_ID)
	RadioButton radioButtonDefaultType;

	@BindView(R.id.radioGrpDeviceType)
	RadioGroup radioGrpDeviceType;

	@BindView(R.id.txtStatus)
	TextView txtStatus;

	private ProgressDialog scanDialog;

	private DeviceClient deviceClient;

	private Timer statusTimer = new Timer();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle(R.string.simple_activity_name);
		setContentView(R.layout.activity_discover_and_stream);
		ButterKnife.bind(this);

		// Switching between device types:
		radioGrpDeviceType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				deviceClient = getDeviceClient(checkedId);
			}
		});

		// Select the default:
		radioButtonDefaultType.setChecked(true);

		// Button logic:
		button.setOnClickListener(this);
	}

	private DeviceClient getDeviceClient(@IdRes int selectionDeviceTypeRadioButtonId) {
		switch(selectionDeviceTypeRadioButtonId) {
			case R.id.radioStingray : return new StingrayClient(getString(R.string.stingray));
			case R.id.radioChillband : return new ChillbandClient(getString(R.string.chillband));
		}
		return null;
	}

	@Override
	public void onClick(View v) {
		if(!deviceClient.isStreaming()) {
			// Detect device:
			final XmpDiscoverer discoverer = new XmpDiscoverer();
			scanDialog = ProgressDialog.show(DiscoverAndStreamActivity.this, getString(R.string.app_name), getString(R.string.scanning_for, getString(deviceClient.getDeviceTypeStringRes())));
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
			// Stop startStream:
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					statusTimer.cancel();
					txtStatus.setText("");
					deviceClient.stopStream(which == DialogInterface.BUTTON_POSITIVE);
					radioGrpDeviceType.setEnabled(true);
					button.setText(R.string.find_and_stream);
				}
			};
			new AlertDialog.Builder(DiscoverAndStreamActivity.this)
					.setMessage(R.string.also_end_data_collection)
					.setPositiveButton(R.string.yes, dialogClickListener)
					.setNegativeButton(R.string.no, dialogClickListener)
					.show();
		}
	}

	@Override
	public void didFail(@Nullable Exception e) {
		Log.e(DiscoverAndStreamActivity.class.getSimpleName(),"Device detection failed", e);
		// TODO cancel dialog & discovery
	}

	@Override
	public void didDiscoverDeviceWithAddress(final @NonNull String address, final @Nullable String name, final BluetoothSocket btSocket) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				new AlertDialog.Builder(DiscoverAndStreamActivity.this)
						.setMessage(getString(R.string.stream_confirmation, name, address, getString(deviceClient.getDeviceTypeStringRes())))
						.setPositiveButton(R.string.yes,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										scanDialog.cancel();
										button.setText(R.string.stop_stream);
										deviceClient.startStream(btSocket);
										statusTimer.scheduleAtFixedRate(new TimerTask() {
											@Override
											public void run() {
												runOnUiThread(new Runnable() {
													@Override
													public void run() {
														txtStatus.setText(getString(R.string.status_msg, getString(deviceClient.isConnected() ? R.string.connected : R.string.disconnected), deviceClient.getNumberOfReadings(), deviceClient.getBatteryLevel()));
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
