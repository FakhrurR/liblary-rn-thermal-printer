package com.testmodule;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.testmodule.sdk.RNPrinterModule;
import com.testmodule.sdk.adapter.BluetoothPrinterAdapter;
import com.testmodule.sdk.adapter.BluetoothPrinterDeviceId;
import com.testmodule.sdk.adapter.PrinterAdapter;
import com.testmodule.sdk.adapter.PrinterDevice;

import java.util.List;

public class PrinterModule extends ReactContextBaseJavaModule implements RNPrinterModule {
    protected ReactApplicationContext reactContext;

    protected PrinterAdapter adapter;

    public PrinterModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @ReactMethod
    @Override
    public void init(Callback successCallback, Callback errorCallback) {
        this.adapter = BluetoothPrinterAdapter.getInstance();
        this.adapter.init(reactContext,  successCallback, errorCallback);
    }

    @ReactMethod
    @Override
    public void closeConn()  {
        adapter.closeConnectionIfExists();
    }

    @ReactMethod
    @Override
    public void getDeviceList(Callback successCallback, Callback errorCallback)  {
        List<PrinterDevice> printerDevices = adapter.getDeviceList(errorCallback);
        WritableArray pairedDeviceList = Arguments.createArray();
        if(printerDevices.size() > 0) {
            for (PrinterDevice printerDevice : printerDevices) {
                pairedDeviceList.pushMap(printerDevice.toRNWritableMap());
            }
            successCallback.invoke(pairedDeviceList);
        } else {
            errorCallback.invoke("No Device Found");
        }
    }


    @ReactMethod
    @Override
    public void printRawData(String base64Data, Callback errorCallback){
        adapter.printRawData(base64Data, errorCallback);
    }

    @ReactMethod
    @Override
    public void printCustom(String msg, int size, int align, Callback errorCallback) {
        adapter.printCustom(msg, size, align, errorCallback);
    }

    @ReactMethod
    @Override
    public void printImageData(String imageUrl, Callback errorCallback) {
        adapter.printImageData(imageUrl, errorCallback);
    }
    @ReactMethod
    @Override
    public void printQrCode(String qrCode, Callback errorCallback) {
        adapter.printQrCode(qrCode, errorCallback);
    }

    @Override
    public void LineSpacePrinter(Callback errorCallback) {
        adapter.LineSpacePrinter(errorCallback);
    }


    @ReactMethod
    public void connectPrinter(String innerAddress, Callback successCallback, Callback errorCallback) {
        adapter.selectDevice(BluetoothPrinterDeviceId.valueOf(innerAddress), successCallback, errorCallback);
    }

    @NonNull
    @Override
    public String getName() {
        return "PrinterModule";
    }
}
