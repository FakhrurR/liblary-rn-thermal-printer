package com.testmodule.sdk;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactMethod;

public interface RNPrinterModule {
    public void init(Callback successCallback, Callback errorCallback);

    public void closeConn();

    public void getDeviceList(Callback successCallback, Callback errorCallback);

    @ReactMethod
    public void printRawData(String base64Data, Callback errorCallback) ;

    @ReactMethod
    public void printCustom(String msg, int size, int align, Callback errorCallback) ;

    @ReactMethod
    public void printImageData(String imageUrl, Callback errorCallback) ;

    @ReactMethod
    public void printQrCode(String qrCode, Callback errorCallback) ;

    @ReactMethod
    public void LineSpacePrinter(Callback errorCallback);
}