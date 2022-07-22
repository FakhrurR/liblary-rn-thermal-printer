package com.testmodule.sdk.adapter;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

public class BluetoothPrinterDevice implements PrinterDevice {
    private BluetoothDevice mBluetoothDevice;
    private BluetoothPrinterDeviceId mPrinterDeviceId;

    public BluetoothPrinterDevice(BluetoothDevice bluetoothDevice) {
        this.mBluetoothDevice = bluetoothDevice;
        this.mPrinterDeviceId = BluetoothPrinterDeviceId.valueOf(bluetoothDevice.getAddress());
    }

    @Override
    public BluetoothPrinterDeviceId getPrinterDeviceId() {
        return this.mPrinterDeviceId;
    }

    @SuppressLint("MissingPermission")
    @Override
    public WritableMap toRNWritableMap() {
        WritableMap deviceMap = Arguments.createMap();
        deviceMap.putString("inner_mac_address", this.mPrinterDeviceId.getInnerMacAddress());
        deviceMap.putString("device_name", this.mBluetoothDevice.getName());
        return deviceMap;
    }
}
