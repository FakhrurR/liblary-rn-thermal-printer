package com.testmodule.sdk.adapter;

public class BluetoothPrinterDeviceId extends PrinterDeviceId {
    private String innerMacAddress;

    public static BluetoothPrinterDeviceId valueOf(String innerMacAddress) {
        return new BluetoothPrinterDeviceId(innerMacAddress);
    }

    private BluetoothPrinterDeviceId(String innerMacAddress) {
        this.innerMacAddress = innerMacAddress;
    }

    public String getInnerMacAddress() {
        return innerMacAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BluetoothPrinterDeviceId that = (BluetoothPrinterDeviceId) o;

        return innerMacAddress.equals(that.innerMacAddress);

    }

    @Override
    public int hashCode() {
        return innerMacAddress.hashCode();
    }
}
