import {NativeModules, Platform} from 'react-native';

const {PrinterModule} = NativeModules;

export enum BLEPrinterSize {
  NORMAL_SIZE = 0,
  ONLY_BOLD = 1,
  BOLD_MEDIUM = 2,
  BOLD_LARGE = 3
}

export enum BLEPrinterAlign {
  LEFT = 0,
  CENTER = 1,
  RIGHT = 2,
}

export interface IBLEPrinter {
    device_name: string;
    inner_mac_address: string;
}

export const RNBluetoothPrinter = {
    init: (): Promise<void> =>
      new Promise((resolve, reject) =>
        PrinterModule.init(
          () => resolve(),
          (error: Error) => reject(error)
        )
      ),
  
    getDeviceList: (): Promise<IBLEPrinter[]> =>
      new Promise((resolve, reject) =>
        PrinterModule.getDeviceList(
          (printers: IBLEPrinter[]) => resolve(printers),
          (error: Error) => reject(error)
        )
      ),
  
    connectPrinter: (inner_mac_address: string): Promise<IBLEPrinter> =>
      new Promise((resolve, reject) =>
        PrinterModule.connectPrinter(
          inner_mac_address,
          (printer: IBLEPrinter) => resolve(printer),
          (error: Error) => reject(error)
        )
      ),
  
    closeConn: (): Promise<void> =>
      new Promise((resolve) => {
        PrinterModule.closeConn();
        resolve();
      }),
      
      printText: (text: string, opts = {}): void => {
        if (Platform.OS === "android") {
            PrinterModule.printRawData(text, (error: Error) =>
                console.warn(error)
            );
        } else {
            console.log('Not Supported');
            // ToastAndroid.show('Device Not Supported', 1000)
        }
      },

      printQRCode: (text: string, opts = {}): void => {
        if (Platform.OS === "android") {
            PrinterModule.printQrCode(text, (error: Error) =>
                console.warn(error)
            );
        } else {
            console.log('Not Supported');
        }
      },

      printImage: (
          fileName: string | object | undefined | any, 
          opts = {}
        ): void => {
        const resolveAssetSource = require('react-native/Libraries/Image/resolveAssetSource');
        const resolvedImage = resolveAssetSource(fileName);
        let file = fileName?.uri ? fileName?.uri : resolvedImage?.uri;
        if (Platform.OS === "android") {
            PrinterModule.printImageData(file, (error: Error) =>
                console.warn(error)
            );
        } else {
            console.log('Not Supported');
        }
      },

      printCustom: (
        message: string,
        opts = {
          size: BLEPrinterSize.NORMAL_SIZE,
          align: BLEPrinterAlign.LEFT,
        }
      ): void => {
        if (Platform.OS === "android") {
          PrinterModule.printCustom(message, opts.size, opts.align, (error: Error) =>
              console.warn(error)
          );
        } else {
            console.log('Not Supported');
        }
      }
  };