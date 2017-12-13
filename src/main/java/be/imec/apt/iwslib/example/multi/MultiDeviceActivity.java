package be.imec.apt.iwslib.example.multi;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;

import be.imec.apt.iwslib.example.R;
import be.imec.apt.iwslib.shared.xmp.XmpConnectionServer;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Multi-device example to demonstrate the use of the IWS (imec Wearable Streaming) library.
 */
public class MultiDeviceActivity extends AppCompatActivity implements View.OnClickListener {

	static private final int deviceCount = 4;

	@BindView(R.id.tabDevices)
	TableLayout tabDevices;

	@BindView(R.id.btn_start_streams)
	Button button;

	private XmpConnectionServer connectionServer;
	private final DeviceFragment[] fragments = new DeviceFragment[deviceCount];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle(R.string.multi_device_activity_name);
		setContentView(R.layout.activity_multi_device);
		ButterKnife.bind(this);

		// Add 4 device fragments:
		for(int c = 0; c < deviceCount; c++) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			try {
				fragments[c] = DeviceFragment.NewInstance((c + 1));
				ft.add(tabDevices.getId(), fragments[c], DeviceFragment.class.getSimpleName() + "#" + (c + 1));
			}
			catch (Exception e) {
				Log.e(getClass().getSimpleName(), "Error on adding fragment #" + (c + 1), e);
			}
			finally {
				ft.commit();
			}
		}

		// Button logic:
		button.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(!isStreaming()) {
			// Create connection server:
			this.connectionServer = new XmpConnectionServer();

			// Create & start clients (will also start connectionServer):
			for(int c = 0; c < deviceCount; c++) {
				fragments[c].startStream(connectionServer);
			}

			// UI:
			button.setText(R.string.stop_streams);
		}
		else {
			// Stop connection server:
			if(connectionServer != null) {
				connectionServer.stop();
				connectionServer = null;
			}
			// Stop streams:
			for(int c = 0; c < deviceCount; c++) {
				fragments[c].stopStream();
			}

			// UI:
			button.setText(R.string.start_streams);
		}
	}

	private boolean isStreaming() {
		for(DeviceFragment fragment : fragments)
			if(fragment != null && fragment.isStreaming())
				return true;
		return false;
	}

}
