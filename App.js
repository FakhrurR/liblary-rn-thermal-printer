import React, {useEffect, useState} from 'react';
import {
  Button,
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  useColorScheme,
  View,
  TouchableOpacity,
  ToastAndroid,
} from 'react-native';

import {Colors, Header} from 'react-native/Libraries/NewAppScreen';
import CalendarModule from './CalenderModule';
import {
  BLEPrinterAlign,
  BLEPrinterSize,
  RNBluetoothPrinter,
} from './PrinterModule';

const App = () => {
  const isDarkMode = useColorScheme() === 'dark';
  const [printerData, setPrinterData] = useState([]);

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  useEffect(() => {
    RNBluetoothPrinter.init()
      .then(() => {
        RNBluetoothPrinter.getDeviceList()
          .then(res => {
            // console.log('PrinterModule => ', res);
            setPrinterData(res);
          })
          .catch(err => {
            console.log('error => ', err);
            ToastAndroid.show(err, 1000);
          });
      })
      .catch(err => {
        console.log('error => ', err);
        ToastAndroid.show(err, 1000);
      });
  }, []);

  // console.log('PrinterModule => ', PrinterModule);

  const onPress = () => {
    CalendarModule.createCalendarEvent('testName', 'testLocation');
    const {DEFAULT_EVENT_NAME} = CalendarModule.getConstants();
    console.log(DEFAULT_EVENT_NAME);
  };

  return (
    <SafeAreaView style={backgroundStyle}>
      <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />
      <ScrollView
        contentInsetAdjustmentBehavior="automatic"
        style={backgroundStyle}>
        <Header />
        <Button
          title="Click to invoke your native module!"
          color="#841584"
          onPress={onPress}
        />

        {printerData.length > 0 &&
          printerData.map((data, index) => (
            <TouchableOpacity
              key={`${index}++`}
              style={{marginTop: 10}}
              onPress={() => {
                console.log(
                  'data.inner_mac_address => ',
                  data.inner_mac_address,
                );
                RNBluetoothPrinter.connectPrinter(data?.inner_mac_address || '')
                  .then(() => {
                    ToastAndroid.show('Berhasil Connect', 1000);
                  })
                  .catch(err => {
                    console.log('err => ', err);
                    ToastAndroid.show('Cannot Connect Bluetooth', 1000);
                  });
              }}>
              <Text>{data.device_name}</Text>
              <Text>{data.inner_mac_address}</Text>
            </TouchableOpacity>
          ))}

        <Button
          title="Print Test"
          color="#841584"
          onPress={() => {
            // const myImage = require('./dummy_logo.jpg');
            // RNBluetoothPrinter.printImage(myImage);
            // RNBluetoothPrinter.printCustom('+++ PESANAN +++\n\n', {
            //   size: BLEPrinterSize.ONLY_BOLD,
            //   align: BLEPrinterAlign.CENTER,
            // });
            // RNBluetoothPrinter.printCustom('Burger            Rp.5.000', {
            //   size: BLEPrinterSize.ONLY_BOLD,
            //   align: BLEPrinterAlign.CENTER,
            // });
            RNBluetoothPrinter.printText('');
            // RNBluetoothPrinter.printText('Jagung bakar      Rp.3.000');
            // RNBluetoothPrinter.printText('===========================');
            // RNBluetoothPrinter.printCustom('Total Biaya       Rp.10.000', {
            //   size: BLEPrinterSize.ONLY_BOLD,
            //   align: BLEPrinterAlign.CENTER,
            // });
            // const msg = 'BR1231PP';
            // RNBluetoothPrinter.printQRCode(msg);
          }}
        />

        <View style={{marginVertical: 10}} />
        <Button
          title="Print QR Code"
          color="#841584"
          onPress={() => {
            const msg = 'en.wikipedia.org';
            RNBluetoothPrinter.printQRCode(msg);
          }}
        />
        <View style={{marginVertical: 10}} />
        <Button
          title="Print Image"
          color="#841584"
          onPress={() => {
            // const photo = 'pos.png';
            const myImage = require('./dummy_logo.jpg');
            // console.log('resolvedImage => ', resolvedImage.uri);
            RNBluetoothPrinter.printImage(myImage);
          }}
        />
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: '600',
  },
  sectionDescription: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: '400',
  },
  highlight: {
    fontWeight: '700',
  },
});

export default App;
