package be.imec.apt.iwslib.example;

import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.Log;

import be.imec.apt.iwslib.shared.xmp.XmpConnectionServer;
import be.imec.apt.iwslib.shared.xmp.XmpDevice;
import be.imec.apt.iwslib.shared.xmp.api.XmpStreamApi;

public abstract class DeviceClient<XD extends XmpDevice, DC extends DeviceClient<XD, DC>> implements XmpStreamApi<XD> {
	static protected final int READINGS_UPDATE_THRESHOLD = 500;
	protected final String label;

	private StatusObserver observer;

	private XD device;
	private XmpConnectionServer connectionServer;
	private volatile boolean connected;
	private volatile int batteryLevel;
	private volatile int numberOfReadings;
	private int readingsDiv;

	public DeviceClient(String label) {
		this(label, null);
	}

	public DeviceClient(String label, XmpConnectionServer connectionServer) {
		this.label = label;
		this.connectionServer = connectionServer;
	}

	public DC setObserver(StatusObserver observer) {
		this.observer = observer;
		return (DC) this;
	}

	@Override
	public void didStartSession(@NonNull XD device) {
		numberOfReadings = 0;
		readingsDiv = -1;
	}

	@Override
	public void didConnect(@NonNull XD device, final int number) {
		connected = true;
		Log.i(getClass().getSimpleName(), label + ": connected");
		if(observer != null)
			observer.connectionChange(true);
	}

	@Override
	public void didExceptionOccur(@NonNull XD device, @Nullable Exception e, final boolean fatal) {
		Log.e(getClass().getSimpleName(), label + ": " + (fatal ? "" : "non-") + "fatal device/startStream error", e);
	}

	@Override
	public void didDisconnect(@NonNull XD device, final int number) {
		connected = false;
		Log.i(getClass().getSimpleName(), label + ": disconnected");
		if(observer != null)
			observer.connectionChange(false);
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
		countReading();
	}

	protected void countReading() {
		numberOfReadings++;
		final int curDiv = readingsDiv;
		readingsDiv = numberOfReadings / READINGS_UPDATE_THRESHOLD;
		if(curDiv != readingsDiv && observer != null)
			observer.readingsReceived(numberOfReadings);
	}

	@Override
	public int getMaxGapToFillSeconds() {
		return 120;
	}

	@Override
	public void didDataGapOccur(long seconds) {
		Log.i(getClass().getSimpleName(), label + ": experienced connection gap of ~" + seconds + " seconds");
	}

	public DC startStream(final @NonNull BluetoothSocket btSocket) {
		return startStream(createDevice(btSocket));
	}

	public DC startStream(final @NonNull String macAddress) {
		return startStream(createDevice(macAddress));
	}

	private DC startStream(final @NonNull XD device) {
		this.device = device;
		if(connectionServer == null)
			connectionServer = XmpConnectionServer.Start(device);
		else {
			connectionServer.expect(device);
			connectionServer.start();
		}
		device.startStream(this);
		return (DC)this;
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

	protected abstract XD createDevice(final @NonNull String macAddress);

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