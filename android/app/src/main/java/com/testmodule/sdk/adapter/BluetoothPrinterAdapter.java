package com.testmodule.sdk.adapter;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.printservice.PrintService;
import android.util.Log;
import android.widget.Toast;

import com.facebook.common.internal.ImmutableMap;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import zj.com.customize.sdk.Other;

public class BluetoothPrinterAdapter implements PrinterAdapter {
        private static BluetoothPrinterAdapter mInstance;


        private final String LOG_TAG = "RNBLEPrinter";

        private BluetoothDevice mBluetoothDevice;
        private BluetoothSocket mBluetoothSocket;

        private ReactApplicationContext mContext;

        private int[] PRINTER_ON_PORTS = { 9100 };
        private static final String EVENT_SCANNER_RESOLVED = "scannerResolved";
        private static final String EVENT_SCANNER_RUNNING = "scannerRunning";

        private final static char ESC_CHAR = 0x1B;
        private static byte[] SELECT_BIT_IMAGE_MODE = { 0x1B, 0x2A, 33 };
        private final static byte[] SET_LINE_SPACE_24 = new byte[] { ESC_CHAR, 0x33, 24 };
        private final static byte[] SET_LINE_SPACE_32 = new byte[] { ESC_CHAR, 0x33, 32 };
        private final static byte[] LINE_FEED = new byte[] { 0x0A };
        private static byte[] CENTER_ALIGN = { 0x1B, 0X61, 0X31 };
        private static byte[] ALIGN_LEFT = new byte[] { 0x1b, 'a', 0x00 };
        private static byte[] ALIGN_RIGHT = new byte[] { 0x1b, 'a', 0x02 };
        private static byte[] START_PRINT = new byte[] {0x10,0x02};
        private static byte[] END_PRINT = new byte[] {0x10,0x03};

    private BluetoothPrinterAdapter(){}

        public static BluetoothPrinterAdapter getInstance() {
            if(mInstance == null) {
                mInstance = new BluetoothPrinterAdapter();
            }
            return mInstance;
        }

        @Override
        public void init(ReactApplicationContext reactContext, Callback successCallback, Callback errorCallback) {
            this.mContext = reactContext;
            BluetoothAdapter bluetoothAdapter = getBTAdapter();
            if(bluetoothAdapter == null) {
                errorCallback.invoke("No bluetooth adapter available");
                return;
            }
            if(!bluetoothAdapter.isEnabled()) {
                errorCallback.invoke("bluetooth device is not enabled");
                return;
            }else{
                successCallback.invoke("bluetooth is enabled");
            }

        }

        private static BluetoothAdapter getBTAdapter() {
            return BluetoothAdapter.getDefaultAdapter();
        }

        @Override
        public List<PrinterDevice> getDeviceList(Callback errorCallback) {
            BluetoothAdapter bluetoothAdapter = getBTAdapter();
            List<PrinterDevice> printerDevices = new ArrayList<>();
            if(bluetoothAdapter == null) {
                errorCallback.invoke("No bluetooth adapter available");
                return printerDevices;
            }
            if (!bluetoothAdapter.isEnabled()) {
                errorCallback.invoke("bluetooth is not enabled");
                return printerDevices;
            }
            @SuppressLint("MissingPermission")
            Set<BluetoothDevice> pairedDevices = getBTAdapter().getBondedDevices();
            for (BluetoothDevice device : pairedDevices) {
                printerDevices.add(new BluetoothPrinterDevice(device));
            }
            return printerDevices;
        }

        @Override
        public void selectDevice(PrinterDeviceId printerDeviceId, Callback successCallback, Callback errorCallback) {
            BluetoothAdapter bluetoothAdapter = getBTAdapter();
            if(bluetoothAdapter == null) {
                errorCallback.invoke("No bluetooth adapter available");
                return;
            }
            if (!bluetoothAdapter.isEnabled()) {
                errorCallback.invoke("bluetooth is not enabled");
                return;
            }
            BluetoothPrinterDeviceId blePrinterDeviceId = (BluetoothPrinterDeviceId)printerDeviceId;
            if(this.mBluetoothDevice != null){
                if(this.mBluetoothDevice.getAddress().equals(blePrinterDeviceId.getInnerMacAddress()) && this.mBluetoothSocket != null){
                    Log.v(LOG_TAG, "do not need to reconnect");
                    successCallback.invoke(new BluetoothPrinterDevice(this.mBluetoothDevice).toRNWritableMap());
                    return;
                }else{
                    closeConnectionIfExists();
                }
            }
            @SuppressLint("MissingPermission")
            Set<BluetoothDevice>
                    pairedDevices = getBTAdapter().getBondedDevices();

            for (BluetoothDevice device : pairedDevices) {
                if(device.getAddress().equals(blePrinterDeviceId.getInnerMacAddress())){

                    try{
                        connectBluetoothDevice(device);
                        successCallback.invoke(new BluetoothPrinterDevice(this.mBluetoothDevice).toRNWritableMap());
                        return;
                    }catch (IOException e){
                        e.printStackTrace();
                        errorCallback.invoke(e.getMessage());
                        return;
                    }
                }
            }
            String errorText = "Can not find the specified printing device, please perform Bluetooth pairing in the system settings first.";
            Toast.makeText(this.mContext, errorText, Toast.LENGTH_LONG).show();
            errorCallback.invoke(errorText);
            return;
        }

        @SuppressLint("MissingPermission")
        private void connectBluetoothDevice(BluetoothDevice device) throws IOException{
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            this.mBluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
            this.mBluetoothSocket.connect();
            this.mBluetoothDevice = device;//最后一步执行
        }

        @Override
        public void closeConnectionIfExists() {
            try{
                if(this.mBluetoothSocket != null){
                    this.mBluetoothSocket.close();
                    this.mBluetoothSocket = null;
                }
            }catch(IOException e){
                e.printStackTrace();
            }

            if(this.mBluetoothDevice != null) {
                this.mBluetoothDevice = null;
            }
        }

        @Override
        public void printRawData(String rawBase64Data, Callback errorCallback) {

            if(this.mBluetoothSocket == null){
                errorCallback.invoke("bluetooth connection is not built, may be you forgot to connectPrinter");
                return;
            }
            Log.v(LOG_TAG, "start to print raw data " + "rawData");
            if(rawBase64Data.length() > 0) {
                try {
                    writePrint(rawBase64Data.getBytes("GBK"), errorCallback);
                } catch (UnsupportedEncodingException e) {
                    Log.d(LOG_TAG, "failed to print data" + e.getMessage());
                    e.printStackTrace();
                    errorCallback.invoke(e.getMessage());
                }
            } else {
                errorCallback.invoke("There is no text to print");
            }
        }

    @Override
    public void printCustom(String msg, int size, int align, Callback errorCallback) {
        final BluetoothSocket socket = this.mBluetoothSocket;
        //Print config "mode"
        byte[] cc = new byte[]{0x1B,0x21,0x03};  // 0- normal size text
        byte[] bb = new byte[]{0x1B,0x21,0x08};  // 1- only bold text
        byte[] bb2 = new byte[]{0x1B,0x21,0x20}; // 2- bold with medium text
        byte[] bb3 = new byte[]{0x1B,0x21,0x10}; // 3- bold with large text

        try {
            OutputStream printerOutputStream = socket.getOutputStream();
//            printerOutputStream.write(CENTER_ALIGN);
            switch (size){
                case 0:
                    printerOutputStream.write(cc);
                    break;
                case 1:
                    printerOutputStream.write(bb);
                    break;
                case 2:
                    printerOutputStream.write(bb2);
                    break;
                case 3:
                    printerOutputStream.write(bb3);
                    break;
            }

            switch (align){
                case 0:
                    //left align
                    printerOutputStream.write(ALIGN_LEFT);
                    break;
                case 1:
                    //center align
                    printerOutputStream.write(CENTER_ALIGN);
                    break;
                case 2:
                    //right align
                    printerOutputStream.write(ALIGN_RIGHT);
                    break;
            }
            writePrint(msg.getBytes("GBK"), errorCallback);
        } catch (IOException e) {
            e.printStackTrace();
            errorCallback.invoke("failed print : ", e);
        }
    }

    private void writePrint(byte[] buffer, Callback errorCallback) {
            final BluetoothSocket socket = this.mBluetoothSocket;
            try {
                OutputStream printerOutputStream = socket.getOutputStream();
                printerOutputStream.write(buffer);
                byte[] data = Other.byteArraysToBytes(new byte[][]{Command.DLE_eot});
                byte[] data2 = Other.byteArraysToBytes(new byte[][]{Command.DLE_DC4});
                String s = new String(data, StandardCharsets.UTF_8);
                String d = new String(data2, StandardCharsets.UTF_8);
                Log.e(LOG_TAG, "print status :" + s);
                Log.e(LOG_TAG, "print status :" + d);
                printerOutputStream.write(LINE_FEED);
                printerOutputStream.flush();
            } catch (IOException e){
                Log.e(LOG_TAG, "failed " + e.getMessage());
                e.printStackTrace();
                errorCallback.invoke(e.getMessage());
            }
        }


        public static Bitmap getBitmapFromURL(String src) {
            try {
                URL url = new URL(src);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                myBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

                return myBitmap;
            } catch (IOException e) {
                // Log exception
                return null;
            }
        }

        @Override
        public void printImageData(String imageUrl, Callback errorCallback) {

//            final Bitmap bitmapImage = getImageFromAssetsFile(imageUrl);
            final Bitmap bitmapImage = getBitmapFromURL(imageUrl);

//            Log.d("TAG", "printImageData: " + bitmapImage.toString());
            Log.d("TAG", "printImageData: " + imageUrl);

            if (bitmapImage == null) {
                errorCallback.invoke("image not found");
                return;
            }
            if(this.mBluetoothSocket == null){
                errorCallback.invoke("bluetooth connection is not built, may be you forgot to connectPrinter");
                return;
            }

            final BluetoothSocket socket = this.mBluetoothSocket;

            try {
                OutputStream printerOutputStream = socket.getOutputStream();

                printerOutputStream.write(SET_LINE_SPACE_24);
                printerOutputStream.write(CENTER_ALIGN);

//                byte[] data = POS_PrintBMP(bitmapImage, 100, 0);
//                printerOutputStream.write(data);
                int[][] pixels = getPixelsSlow(bitmapImage);
                for (int y = 0; y < pixels.length; y += 24) {
                    // Like I said before, when done sending data,
                    // the printer will resume to normal text printing
                    printerOutputStream.write(SELECT_BIT_IMAGE_MODE);
                    // Set nL and nH based on the width of the image
                    printerOutputStream.write(
                            new byte[] { (byte) (0x00ff & pixels[y].length), (byte) ((0xff00 & pixels[y].length) >> 8) });
                    for (int x = 0; x < pixels[y].length; x++) {
                        // for each stripe, recollect 3 bytes (3 bytes = 24 bits)
                        printerOutputStream.write(recollectSlice(y, x, pixels));
                    }

                    // Do a line feed, if not the printing will resume on the same line
                    printerOutputStream.write(LINE_FEED);
                }

                printerOutputStream.write(SET_LINE_SPACE_32);
                printerOutputStream.write(LINE_FEED);

                printerOutputStream.flush();
            } catch (IOException e) {
                Log.e(LOG_TAG, "failed to print data");
                e.printStackTrace();
            }

        }

        @Override
        public void printQrCode(String qrCode, Callback errorCallback) {
            final Bitmap bitmapImage = TextToQrImageEncode(qrCode);

            if (bitmapImage == null) {
                errorCallback.invoke("image not found");
                return;
            }
            if(this.mBluetoothSocket == null){
                errorCallback.invoke("bluetooth connection is not built, may be you forgot to connectPrinter");
                return;
            }

            final BluetoothSocket socket = this.mBluetoothSocket;

            try {
                int[][] pixels = getPixelsSlow(bitmapImage);

                OutputStream printerOutputStream = socket.getOutputStream();

                printerOutputStream.write(SET_LINE_SPACE_24);
                printerOutputStream.write(CENTER_ALIGN);

                for (int y = 0; y < pixels.length; y += 24) {
                    // Like I said before, when done sending data,
                    // the printer will resume to normal text printing
                    printerOutputStream.write(SELECT_BIT_IMAGE_MODE);
                    // Set nL and nH based on the width of the image
                    printerOutputStream.write(
                            new byte[] { (byte) (0x00ff & pixels[y].length), (byte) ((0xff00 & pixels[y].length) >> 8) });
                    for (int x = 0; x < pixels[y].length; x++) {
                        // for each stripe, recollect 3 bytes (3 bytes = 24 bits)
                        printerOutputStream.write(recollectSlice(y, x, pixels));
                    }

                    // Do a line feed, if not the printing will resume on the same line
                    printerOutputStream.write(LINE_FEED);
                }
                printerOutputStream.write(SET_LINE_SPACE_32);
                printerOutputStream.write(LINE_FEED);

                printerOutputStream.flush();
            } catch (IOException e) {
                Log.e(LOG_TAG, "failed to print data");
                e.printStackTrace();
            }
        }

    @Override
    public void LineSpacePrinter(Callback errorCallback) {
        final BluetoothSocket socket = this.mBluetoothSocket;
        OutputStream printerOutputStream = null;
        try {
            printerOutputStream = socket.getOutputStream();
            printerOutputStream.write(PrinterCommand.POS_Set_LineSpace(5));
            printerOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            errorCallback.invoke("Error : " + e);
        }
    }


    private Bitmap TextToQrImageEncode(String Value) {
            com.google.zxing.Writer writer = new QRCodeWriter();

            BitMatrix bitMatrix = null;
            try {
                bitMatrix = writer.encode(Value, com.google.zxing.BarcodeFormat.QR_CODE, 250, 250,
                        ImmutableMap.of(EncodeHintType.MARGIN, 1));
                int width = 250;
                int height = 250;
                Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        bmp.setPixel(i, j, bitMatrix.get(i, j) ? Color.BLACK : Color.WHITE);
                    }
                }
                return bmp;
            } catch (WriterException e) {
                 Log.e("QR ERROR", "QR : " +e);
            }
            return null;
        }

        public static int[][] getPixelsSlow(Bitmap image2) {

            Bitmap image = resizeTheImageForPrinting(image2);

            int width = image.getWidth();
            int height = image.getHeight();
            int[][] result = new int[height][width];
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    result[row][col] = getRGB(image, col, row);
                }
            }
            return result;
        }

        private byte[] recollectSlice(int y, int x, int[][] img) {
            byte[] slices = new byte[] { 0, 0, 0 };
            for (int yy = y, i = 0; yy < y + 24 && i < 3; yy += 8, i++) {
                byte slice = 0;
                for (int b = 0; b < 8; b++) {
                    int yyy = yy + b;
                    if (yyy >= img.length) {
                        continue;
                    }
                    int col = img[yyy][x];
                    boolean v = shouldPrintColor(col);
                    slice |= (byte) ((v ? 1 : 0) << (7 - b));
                }
                slices[i] = slice;
            }
            return slices;
        }

        private boolean shouldPrintColor(int col) {
            final int threshold = 127;
            int a, r, g, b, luminance;
            a = (col >> 24) & 0xff;
            if (a != 0xff) {// Ignore transparencies
                return false;
            }
            r = (col >> 16) & 0xff;
            g = (col >> 8) & 0xff;
            b = col & 0xff;

            luminance = (int) (0.299 * r + 0.587 * g + 0.114 * b);

            return luminance < threshold;
        }

        public static Bitmap resizeTheImageForPrinting(Bitmap image) {
            // making logo size 150 or less pixels
            int width = image.getWidth();
            int height = image.getHeight();
            if (width > 200 || height > 200) {
                if (width > height) {
                    float decreaseSizeBy = (200.0f / width);
                    return getBitmapResized(image, decreaseSizeBy);
                } else {
                    float decreaseSizeBy = (200.0f / height);
                    return getBitmapResized(image, decreaseSizeBy);
                }
            }
            return image;
        }

        public static int getRGB(Bitmap bmpOriginal, int col, int row) {
            // get one pixel color
            int pixel = bmpOriginal.getPixel(col, row);
            // retrieve color of all channels
            int R = Color.red(pixel);
            int G = Color.green(pixel);
            int B = Color.blue(pixel);
            return Color.rgb(R, G, B);
        }

        public static Bitmap getBitmapResized(Bitmap image, float decreaseSizeBy) {
            Bitmap resized = Bitmap.createScaledBitmap(image, (int) (image.getWidth() * decreaseSizeBy),
                    (int) (image.getHeight() * decreaseSizeBy), true);
            return resized;
        }

//        private Bitmap getImageFromAssetsFile(String fileName) {
//            Bitmap image = null;
//            AssetManager am = mContext.getResources().getAssets();
//            try {
//                InputStream is = am.open(fileName);
//                image = BitmapFactory.decodeStream(is);
//                is.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            return image;
//
//        }
}
