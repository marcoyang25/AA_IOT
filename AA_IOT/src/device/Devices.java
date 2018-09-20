package device;

import java.util.HashMap;

public class Devices extends HashMap<Integer, Device> {

	@Override
	public String toString() {
		return String.format("Devices: %s", super.toString());
	}

}
