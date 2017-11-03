package be.imec.apt.iwslib.example;

import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;
import android.util.Log;

import be.imec.apt.iwslib.chillband.ChillbandDevice;
import be.imec.apt.iwslib.chillband.api.ChillbandStreamApi;

public class ChillbandClient extends DeviceClient<ChillbandDevice> implements ChillbandStreamApi {
	@Override
	protected ChillbandDevice createDevice(@NonNull BluetoothSocket btSocket) {
		return new ChillbandDevice(btSocket);
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
	public void didReceiveGsr(@NonNull ChillbandDevice device, double gsr, long timestamp) {
		numberOfReadings++;
	}

	@Override
	public void didUpdateTemperatureSampleRate(@NonNull ChillbandDevice device, int newTemperatureSampleRate) {

	}

	@Override
	public void didReceiveTemperature(@NonNull ChillbandDevice device, double temperature, long timestamp) {
		numberOfReadings++;
	}
}