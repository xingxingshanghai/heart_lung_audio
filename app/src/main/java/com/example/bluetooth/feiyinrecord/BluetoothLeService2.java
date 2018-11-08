//package com.example.bluetooth.feiyinrecord;
//import android.app.Service;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothGatt;
//import android.bluetooth.BluetoothGattCallback;
//import android.bluetooth.BluetoothGattCharacteristic;
//import android.bluetooth.BluetoothGattDescriptor;
//import android.bluetooth.BluetoothGattService;
//import android.bluetooth.BluetoothManager;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Binder;
//import android.os.IBinder;
//import android.util.Log;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//
//public class BluetoothLeService2 extends Service {
//
//    private HashMap<String, BluetoothGatt> gattArrayMap = new HashMap<String, BluetoothGatt>();
//    private BluetoothAdapter mBluetoothAdapter;
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        initBluetooth();
//    }
//
//    /**
//     * 蓝牙是否开启
//     */
//    public boolean isBluetoothEnabled() {
//        return mBluetoothAdapter.isEnabled();
//    }
//
//
//    private void initBluetooth() {
//        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//        mBluetoothAdapter = bluetoothManager.getAdapter();
//    }
//
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        return super.onStartCommand(intent, Service.START_FLAG_RETRY, startId);
//    }
//
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return mBinder;
//    }
//
//
//    public class LocalBinder extends Binder {
//        public BluetoothLeService2 getService() {
//            return BluetoothLeService2.this;
//        }
//    }
//
//    private final IBinder mBinder = new LocalBinder();
//
//    @Override
//    public boolean onUnbind(Intent intent) {
//        return super.onUnbind(intent);
//    }
//
//
//    /**
//     * 连接设备
//     *
//     * @param address 设备地址
//     */
//    public synchronized void connect(final String address) {
//        BluetoothGatt gatt = gattArrayMap.get(address);
//        if (gatt != null) {
//            gatt.disconnect();
//            gatt.close();
//            gattArrayMap.remove(address);
//        }
//        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
//        if (device == null) return;
//        device.connectGatt(this, false, mGattCallback);
//    }
//
//    //蓝牙连接，数据通信的回掉
//    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
//        @Override
//        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
//            String address = gatt.getDevice().getAddress();
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                if (newState == BluetoothGatt.STATE_CONNECTED) {// 连接成功
//                    gatt.discoverServices();// 寻找服务
//                    gattArrayMap.put(address, gatt);
//                    Log.i("yushu", "connect succeed: ");
//                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {// 断开连接
//                    onDisConnected(gatt, address);
//                    Log.i("yushu", "connect fail ");
//                }
//            } else {
//                onDisConnected(gatt, address);
//                Log.i("yushu", "connect fail ");
//            }
//        }
//
//
//        @Override
//        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                enableNotification(gatt);//notification
//            }
//        }
//
//
//        @Override
//        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//            byte[] value = characteristic.getValue();
///**
// * 这里可以拿到设备notification回来的数据
// */
//
//        }
//
//
//        @Override
//        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
//            super.onReadRemoteRssi(gatt, rssi, status);
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//
//            }
//        }
//    };
//
//
//    private void onDisConnected(BluetoothGatt gatt, String address) {
//        gatt.disconnect();
//        gatt.close();
//    }
//
//
//    /**
//     * 使能通知
//     */
//    private void enableNotification(BluetoothGatt mBluetoothGatt) {
//        if (mBluetoothGatt == null) return;
//        BluetoothGattService ableService = mBluetoothGatt.getService(UUIDUtils.UUID_LOST_SERVICE);
//        if (ableService == null) return;
//        BluetoothGattCharacteristic TxPowerLevel = ableService.getCharacteristic(UUIDUtils.UUID_LOST_ENABLE);
//        if (TxPowerLevel == null) return;
//        setCharacteristicNotification(mBluetoothGatt, TxPowerLevel, true);
//    }
//
//    /**
//     * 使能通知
//     */
//    private void setCharacteristicNotification(BluetoothGatt mBluetoothGatt, BluetoothGattCharacteristic characteristic, boolean enabled) {
//        if (mBluetoothGatt == null) return;
//        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
//        if (UUIDUtils.UUID_LOST_ENABLE.equals(characteristic.getUuid())) {
//            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUIDUtils.CLIENT_CHARACTERISTIC_CONFIG);
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            mBluetoothGatt.writeDescriptor(descriptor);
//        }
//    }
//
//
//    /**
//     * 写数据到硬件，这里的服务UUID和特这的UUID参考硬件那边，两边要对应
//     */
//    public synchronized void writeDataToDevice(byte[] bs, String address) {
//        BluetoothGatt mBluetoothGatt = gattArrayMap.get(address);
//        if (mBluetoothGatt == null) return;
//        BluetoothGattService alertService = mBluetoothGatt.getService(UUIDUtils.UUID_LOST_SERVICE);
//        if (alertService == null) return;
//        BluetoothGattCharacteristic alertLevel = alertService.getCharacteristic(UUIDUtils.UUID_LOST_WRITE);
//        if (alertLevel == null) return;
//        alertLevel.setValue(bs);
//        alertLevel.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
//        mBluetoothGatt.writeCharacteristic(alertLevel);
//    }
//
//
//    public boolean readRssi(String address) {
//        BluetoothGatt bluetoothGatt = gattArrayMap.get(address);
//        return bluetoothGatt != null && bluetoothGatt.readRemoteRssi();
//    }
//
//
//    public void remove(String address) {
//        BluetoothGatt bluetoothGatt = gattArrayMap.get(address);
//        if (bluetoothGatt != null) {
//            bluetoothGatt.disconnect();
//            bluetoothGatt.close();
//            gattArrayMap.remove(address);
//        }
//    }
//
//    public void disConnect(String address) {
//        BluetoothGatt bluetoothGatt = gattArrayMap.get(address);
//        if (bluetoothGatt != null) {
//            bluetoothGatt.disconnect();
//            gattArrayMap.remove(address);
//        }
//    }
//
//
//    @Override
//    public void onDestroy() {
//        gattArrayMap.clear();
//        super.onDestroy();
//    }
//}