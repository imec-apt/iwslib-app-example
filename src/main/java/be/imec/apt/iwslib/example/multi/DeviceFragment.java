package be.imec.apt.iwslib.example.multi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import be.imec.apt.iwslib.example.ChillbandClient;
import be.imec.apt.iwslib.example.DeviceClient;
import be.imec.apt.iwslib.example.R;
import be.imec.apt.iwslib.example.StatusObserver;
import be.imec.apt.iwslib.example.StingrayClient;
import be.imec.apt.iwslib.shared.xmp.XmpConnectionServer;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Multi-device example to demonstrate the use of the IWS (imec Wearable Streaming) library.
 */
public class DeviceFragment extends Fragment implements StatusObserver {

	private static final String PARAM_DEVICE_IDX = "PARAM_DEVICE_IDX";
	static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("HH:mm:ss");

	static private final @IdRes int DEFAULT_DEVICE_TYPE_RADIO_ID = R.id.radioChillband;

	public static DeviceFragment NewInstance(int idx) {
		DeviceFragment fragment = new DeviceFragment();
		Bundle args = new Bundle();
		args.putInt(PARAM_DEVICE_IDX, idx);
		fragment.setArguments(args);
		return fragment;
	}

	private DeviceClient client;

	@BindView(R.id.lblMac)
	TextView lblMac;

	@BindView(R.id.txtMac)
	EditText txtMac;

	@BindView(DEFAULT_DEVICE_TYPE_RADIO_ID)
	RadioButton radioButtonDefaultType;

	@BindView(R.id.radioGrpDeviceType)
	RadioGroup radioGrpDeviceType;

	@BindView(R.id.lblConnectionState)
	TextView lblConnectionState;

	@BindView(R.id.lblReadingsCount)
	TextView lblReadingsCount;

	public int getIdx() {
		return getArguments().getInt(PARAM_DEVICE_IDX);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@SuppressLint("SetTextI18n")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		final View view = inflater.inflate(R.layout.fragment_device, container, false);
		ButterKnife.bind(this, view);

		// Set label:
		lblMac.setText("#" + getIdx() + ":");

		// Select the default:
		radioButtonDefaultType.setChecked(true);

		return view;
	}

	private DeviceClient getDeviceClient(@IdRes int selectionDeviceTypeRadioButtonId, XmpConnectionServer connectionServer) {
		switch(selectionDeviceTypeRadioButtonId) {
			case R.id.radioStingray : return new StingrayClient("#" + getIdx(), connectionServer).setObserver(this);
			case R.id.radioChillband : return new ChillbandClient("#" + getIdx(), connectionServer).setObserver(this);
		}
		return null;
	}

	public void startStream(XmpConnectionServer connectionServer) {
		if(!isStreaming()) {
			// Create & start client (will also start connectionServer):
			final String mac = txtMac.getText().toString().trim();
			client = !TextUtils.isEmpty(mac) ? getDeviceClient(radioGrpDeviceType.getCheckedRadioButtonId(), connectionServer).startStream(mac) : null;

			txtMac.setEnabled(false);
			radioGrpDeviceType.getChildAt(0).setEnabled(false);
			radioGrpDeviceType.getChildAt(1).setEnabled(false);
			updateStatus();
		}
	}

	public void stopStream() {
		if(isStreaming()) {
			client.stopStream(false);
			client = null;

			txtMac.setEnabled(true);
			radioGrpDeviceType.getChildAt(0).setEnabled(true);
			radioGrpDeviceType.getChildAt(1).setEnabled(true);
			updateStatus();
		}
	}

	public boolean isStreaming() {
		return client != null && client.isStreaming();
	}

	public void updateStatus() {
		String msg = getString(client != null && client.isConnected() ? R.string.connected : R.string.disconnected) + " (" + TIME_FORMATTER.print(System.currentTimeMillis()) + ")";

		lblConnectionState.setText(msg);
		lblConnectionState.setTextColor(client != null && client.isConnected() ? Color.GREEN : Color.RED);
	}

	@Override
	public void connectionChange(boolean connected) {
		final Activity activity = getActivity();
		if(activity != null)
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					updateStatus();
				}
			});
	}

	@Override
	public void readingsReceived(final int number) {
		final Activity activity = getActivity();
		if(activity != null)
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					lblReadingsCount.setText(number + " readings");
				}
			});
	}
}
