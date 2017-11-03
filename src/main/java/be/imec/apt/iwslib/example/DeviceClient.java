package be.imec.apt.iwslib.example;

import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.Log;

import be.imec.apt.iwslib.shared.xmp.XmpConnectionServer;
import be.imec.apt.iwslib.shared.xmp.XmpDevice;
import be.imec.apt.iwslib.shared.xmp.api.XmpStreamApi;

public abstract class DeviceClient<XD extends XmpDevice> implements XmpStreamApi<XD> {
	private XD device;
	private XmpConnectionServer connectionServer;
	private volatile boolean connected;
	private volatile int batteryLevel;
	protected volatile int numberOfReadings;

	@Override
	public void didStartSession(@NonNull XD device) {

	}

	@Override
	public void didConnect(@NonNull XD device) {
		connected = true;
	}

	@Override
	public void didExceptionOccur(@NonNull XD device, @Nullable Exception e) {
		Log.e(getClass().getSimpleName(), "Device/stream error", e);
	}

	@Override
	public void didDisconnect(@NonNull XD device) {
		connected = false;
	}

	@Override
	public void didEndSession(@NonNull XD device) {

	}

	@Override
	public void didReceiveBatteryLevel(@NonNull XD device, int batteryLevelPercentage, long timestamp) {
		this.batteryLevel = batteryLevelPercentage;
	}

	@Override
	public void didUpdateAccelerationSampleRate(@NonNull XD device, int newAccelerationSampleRate) {

	}

	@Override
	public void didReceiveAcceleration(@NonNull XD device, double x, double y, double z, long timestamp) {
		numberOfReadings++;
	}

	public void startStream(final @NonNull BluetoothSocket btSocket) {
		numberOfReadings = 0;
		device = createDevice(btSocket);
		connectionServer = XmpConnectionServer.Start(device);
		device.startStream(this);
	}

	public void stopStream(final boolean alsoStopOnDeviceDataCollection)
	{
		if(device != null) {
			device.stopStream(alsoStopOnDeviceDataCollection);
			device = null;
			connectionServer.stop();
			connectionServer = null;
		}
	}

	public boolean isStreaming() {
		return device != null && device.isStreaming();
	}

	protected abstract XD createDevice(final @NonNull BluetoothSocket btSocket);

	public abstract @StringRes int getDeviceTypeStringRes();

	public synchronized int getBatteryLevel() {
		return batteryLevel;
	}

	public synchronized int getNumberOfReadings() {
		return numberOfReadings;
	}

	public synchronized boolean isConnected() {
		return connected;
	}
}