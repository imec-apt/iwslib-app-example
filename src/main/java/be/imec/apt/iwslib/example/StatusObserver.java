package be.imec.apt.iwslib.example;

/**
 * Created by mstevens on 2017-12-12.
 */

public interface StatusObserver {

	public void connectionChange(boolean connected);

	public void readingsReceived(int number);

}
