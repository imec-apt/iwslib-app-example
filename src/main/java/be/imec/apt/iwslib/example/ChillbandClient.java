package be.imec.apt.iwslib.example;

import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;
import android.util.Log;

import be.imec.apt.iwslib.chillband.ChillbandDevice;
import be.imec.apt.iwslib.chillband.api.ChillbandStreamApi;
import be.imec.apt.iwslib.shared.xmp.XmpConnectionServer;

public class ChillbandClient extends DeviceClient<ChillbandDevice, ChillbandClient> implements ChillbandStreamApi {

	public ChillbandClient(String label) {
		super(label);
	}

	public ChillbandClient(String label, XmpConnectionServer connectionServer) {
		super(label, connectionServer);
	}

	@Override
	protected ChillbandDevice createDevice(@NonNull BluetoothSocket btSocket) {
		return new ChillbandDevice(btSocket);
	}

	@Override
	protected ChillbandDevice createDevice(@NonNull String macAddress) {
		return new ChillbandDevice(macAddress);
	}

	@Override
	public int getDeviceTypeStringRes() {
		return R.string.chillband;
	}

	@Override
	public void didStartCalibration(@NonNull ChillbandDevice device) {

	}

	@Override
	public void didFinishCalibration(@NonNull ChillbandDevice device) {

	}

	@Override
	public void didUpdateGsrSampleRate(@NonNull ChillbandDevice device, int newGsrSampleRate) {

	}

	@Override
	public void didReceiveGsr(final @NonNull ChillbandDevice device, final double gsr, final double gsr1uS, final double gsr20uS, final long timestamp) {
		countReading();
	}

	@Override
	public void didUpdateTemperatureSampleRate(@NonNull ChillbandDevice device, int newTemperatureSampleRate) {

	}

	@Override
	public void didReceiveTemperature(@NonNull ChillbandDevice device, double temperature, long timestamp) {
		countReading();
	}
}