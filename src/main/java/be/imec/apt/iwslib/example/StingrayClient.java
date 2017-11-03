package be.imec.apt.iwslib.example;

import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;

import be.imec.apt.iwslib.stingray.StingrayDevice;
import be.imec.apt.iwslib.stingray.api.StingrayStreamApi;

public class StingrayClient extends DeviceClient<StingrayDevice> implements StingrayStreamApi {
	@Override
	protected StingrayDevice createDevice(@NonNull BluetoothSocket btSocket) {
		return new StingrayDevice(btSocket);
	}

	@Override
	public int getDeviceTypeStringRes() {
		return R.string.stingray;
	}

	@Override
	public void didUpdateEcgSampleRate(@NonNull StingrayDevice device, int newEcgSampleRate) {

	}

	@Override
	public void didReceiveEcg(@NonNull StingrayDevice device, double ecg, long timestamp) {
		numberOfReadings++;
	}
}