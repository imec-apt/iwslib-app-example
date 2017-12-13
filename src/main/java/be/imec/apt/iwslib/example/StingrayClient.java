package be.imec.apt.iwslib.example;

import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;

import be.imec.apt.iwslib.shared.xmp.XmpConnectionServer;
import be.imec.apt.iwslib.stingray.StingrayDevice;
import be.imec.apt.iwslib.stingray.api.StingrayStreamApi;

public class StingrayClient extends DeviceClient<StingrayDevice, StingrayClient> implements StingrayStreamApi {
	public StingrayClient(String label) {
		super(label);
	}

	public StingrayClient(String label, XmpConnectionServer connectionServer) {
		super(label, connectionServer);
	}

	@Override
	protected StingrayDevice createDevice(@NonNull BluetoothSocket btSocket) {
		return new StingrayDevice(btSocket);
	}

	@Override
	protected StingrayDevice createDevice(@NonNull String macAddress) {
		return new StingrayDevice(macAddress);
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
		countReading();
	}
}