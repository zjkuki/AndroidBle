package org.eson.liteble.util;

import java.util.UUID;

/**
 * @author xiaoyunfei
 * @date: 2017/3/22
 * @Description：
 */

public interface UUIDFormat {

	UUID DESC =
			UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");

	//UUID BATTERY_SERVICE = UUID.fromString("00000180f-0000-1000-8000-00805f9b34fb");
	//UUID BATTERY = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");


	UUID DEVICE_INFO_SERVICE = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
	// Manufacturer name
	UUID MANUFACTURER = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb");

	// Hardware   302e3031
	UUID HARDWARE = UUID.fromString("00002a01-0000-1000-8000-00805f9b34fb");

	// firmware   302e3031
	UUID FIRMWARE = UUID.fromString("00002a04-0000-1000-8000-00805f9b34fb");








}
