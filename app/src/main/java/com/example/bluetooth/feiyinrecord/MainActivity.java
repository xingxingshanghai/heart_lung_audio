package com.example.bluetooth.feiyinrecord;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.achartengine.GraphicalView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 *   由 MusicListActivity 启动 MainActivity
 *   更改从mainActivity条用startActivityForResult来调取相应的服务来实现后台的播放
 */
public class MainActivity extends android.support.v4.app.FragmentActivity implements View.OnClickListener,CompoundButton.OnCheckedChangeListener,RadioGroup.OnCheckedChangeListener{
    private final static String TAG = MainActivity.class.getSimpleName(),sdPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/js";
    private ImageButton bt_play, bt_pre, bt_next;
    private static final int REQUEST_ENABLE_BT = 3;
    public static boolean isRecord = false;
    private SeekBar seekBar;
    private Button openMusic,clearMusic;
    private int rlength = 0;
    private MyHandler mHandler = new MyHandler(this);
    private SimpleDateFormat format = new SimpleDateFormat("mm:ss");
    private TextView currentTimeTxt, totalTimeTxt,musicinfo;
    private MusicServiceDemo.CallBack callBack;
    private boolean mFlag = true;
    private ArrayList<MusicBean> musicBeanList = new ArrayList<MusicBean>();
    private int mProgress;
    public static int value;
    private boolean times = false;
    private boolean binderFlag = false;
    private Spinner spinner ;
    private SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
    private String  mDeviceAddress;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean bindor = false;
    private BluetoothLeService mBluetoothLeService;
    private BluetoothGattCharacteristic mNotifyCharacteristic,gattCharacteristic;
    protected static boolean bluetoothstate = false;
    double [] test;
    private FileOutputStream fos;
    private BufferedOutputStream bufos;
    private ChartService mService2;
    private LinearLayout mRightCurveLayout;//存放右图表的布局容器
    private GraphicalView mView2;//左右图表
    private RadioGroup rg,position_rg;
    private Button saveAudio,disfinish;
    private String xingbie = "",nianling = "",additondisease = "",position = "";
    private TextView bluescan,devicebat;
    File file = new File(Environment.getExternalStorageDirectory(), "test.ogg");
    private EditText otherdis;
    private CheckBox checkButton1,checkButton2,checkButton3,checkButton4,checkButton5,checkButton6,checkButton7,checkButton8,checkButton9,checkButton10,
            checkButton11,checkButton12,checkButton13,checkButton14,checkButton15,checkButton16,checkButton17,checkButton18;
    ArrayList<String> disease=new ArrayList<String>();
    StringBuilder sb=new StringBuilder();
    StringBuilder stringBuffer1;
    private static int[] steptab = {7, 8, 9, 10, 11, 12, 13, 14,
            16, 17, 19, 21, 23, 25, 28, 31,
            34, 37, 41, 45, 50, 55, 60, 66,
            73, 80, 88, 97, 107, 118, 130, 143,
            157, 173, 190, 209, 230, 253, 279, 307,
            337, 371, 408, 449, 494, 544, 598, 658,
            724, 796, 876, 963, 1060, 1166, 1282, 1411,
            1552, 1707, 1878, 2066, 2272, 2499, 2749, 3024,
            3327, 3660, 4026, 4428, 4871, 5358, 5894, 6484,
            7132, 7845, 8630, 9493, 10442, 11487, 12635, 13899,
            15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, 32767};
    private static int[] indextab = {-1, -1, -1, -1, 2, 4, 6, 8, -1, -1, -1, -1, 2, 4, 6, 8};
    private List<Double> datas = new LinkedList<Double>();
    byte[] jieduanArray;
    private double [] JieMaDouble;
    int dataLen = 0,index, iLen,diff = 0;
    short samp0 = 0,sampx = 0;
    char code = 0;
    boolean odd = true, jieduan = false;
    int iControl = 0;
    boolean waveAgain = true;
    int bufsize = AudioTrack.getMinBufferSize(7880, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);//一个采样点16比特-2个字节
    AudioTrack trackplayer = new AudioTrack(AudioManager.STREAM_MUSIC, 7880, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufsize * 10, AudioTrack.MODE_STREAM);//
    //播放时间的更新
    private  class MyHandler extends Handler {
        // 弱引用
        private WeakReference<MainActivity> reference;
        public MyHandler(MainActivity activity) {
            reference = new WeakReference<MainActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            //播放时间的更新
            MainActivity activity = reference.get();
            if (activity != null&&activity.callBack!=null) {
                int currentTime = activity.callBack.callCurrentTime();
                int totalTime = activity.callBack.callTotalDate();
                activity.seekBar.setMax(totalTime);
                activity.seekBar.setProgress(currentTime);
                String current = activity.format.format(new Date(currentTime));
                String total = activity.format.format(new Date(totalTime));
                activity.currentTimeTxt.setText(current);
                activity.totalTimeTxt.setText(total);
                //更新刷新波形

            }
        }
    }

    //播放服务的绑定
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            callBack = (MusicServiceDemo.MyBinder)service;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            callBack = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.test);
        //设置软键盘
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        initData();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    //数据的初始化
    private void initData(){
        seekBar = (SeekBar)findViewById(R.id.seek_bar);
        bt_play = (ImageButton)findViewById(R.id.bt_play);
        bt_pre = (ImageButton)findViewById(R.id.bt_pre);
        bt_next = (ImageButton)findViewById(R.id.bt_next);
        currentTimeTxt = (TextView)findViewById(R.id.current_time_txt);
        totalTimeTxt = (TextView)findViewById(R.id.total_time_txt);
        musicinfo = (TextView)findViewById(R.id.musicinfo);
        openMusic = (Button) findViewById(R.id.openMusic);
        clearMusic = (Button) findViewById(R.id.clearMusic);
        spinner = (Spinner) findViewById(R.id.spinner);
        rg = (RadioGroup) findViewById(R.id.rg_sex);
        position_rg = (RadioGroup) findViewById(R.id.position_rg);
        rg.setOnCheckedChangeListener(this );
        position_rg.setOnCheckedChangeListener(this );
        saveAudio = (Button) findViewById(R.id.saveAudio);
        saveAudio.setOnClickListener(this);
        saveAudio.setEnabled(false);
        bluescan = (TextView) findViewById(R.id.bluescan);
        //devicebat = (TextView) findViewById(R.id.devicebat);
        otherdis = (EditText) findViewById(R.id.otherdis);
        otherdis.setEnabled(false);
        checkButton1 = (CheckBox) findViewById(R.id.checkButton1);
        checkButton2 = (CheckBox) findViewById(R.id.checkButton2);
        checkButton3 = (CheckBox) findViewById(R.id.checkButton3);
        checkButton4 = (CheckBox) findViewById(R.id.checkButton4);
        checkButton5 = (CheckBox) findViewById(R.id.checkButton5);
        checkButton6 = (CheckBox) findViewById(R.id.checkButton6);
        checkButton7 = (CheckBox) findViewById(R.id.checkButton7);
        checkButton8 = (CheckBox) findViewById(R.id.checkButton8);
        checkButton9 = (CheckBox) findViewById(R.id.checkButton9);
        checkButton10 = (CheckBox) findViewById(R.id.checkButton10);
        checkButton11 = (CheckBox) findViewById(R.id.checkButton11);
        checkButton12 = (CheckBox) findViewById(R.id.checkButton12);
        checkButton13 = (CheckBox) findViewById(R.id.checkButton13);
        checkButton14 = (CheckBox) findViewById(R.id.checkButton14);
        checkButton15 = (CheckBox) findViewById(R.id.checkButton15);
        checkButton16 = (CheckBox) findViewById(R.id.checkButton16);
        checkButton17 = (CheckBox) findViewById(R.id.checkButton17);
        checkButton18 = (CheckBox) findViewById(R.id.checkButton18);
        checkButton1.setOnCheckedChangeListener(this);
        checkButton2.setOnCheckedChangeListener(this);
        checkButton3.setOnCheckedChangeListener(this);
        checkButton4.setOnCheckedChangeListener(this);
        checkButton5.setOnCheckedChangeListener(this);
        checkButton6.setOnCheckedChangeListener(this);
        checkButton7.setOnCheckedChangeListener(this);
        checkButton8.setOnCheckedChangeListener(this);
        checkButton9.setOnCheckedChangeListener(this);
        checkButton10.setOnCheckedChangeListener(this);
        checkButton11.setOnCheckedChangeListener(this);
        checkButton12.setOnCheckedChangeListener(this);
        checkButton13.setOnCheckedChangeListener(this);
        checkButton14.setOnCheckedChangeListener(this);
        checkButton15.setOnCheckedChangeListener(this);
        checkButton16.setOnCheckedChangeListener(this);
        checkButton17.setOnCheckedChangeListener(this);
        checkButton18.setOnCheckedChangeListener(this);
        disfinish = (Button) findViewById(R.id.disfinish);
        disfinish.setOnClickListener(this);
        bt_play.setOnClickListener(this);
        bt_pre.setOnClickListener(this);
        bt_next.setOnClickListener(this);
        openMusic.setOnClickListener(this);
        clearMusic.setOnClickListener(this);
        initView();
//        try {
//                        File filepos1 = new File(Environment.getExternalStorageDirectory() + "/test.ogg");
//                        fos = new FileOutputStream(filepos1, true);
//                        bufos = new BufferedOutputStream(fos);
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }
    }

    private void initView() {
        mRightCurveLayout = (LinearLayout) findViewById(R.id.left_temperature_curve);
        mService2 = new ChartService(this);
        mService2.setXYMultipleSeriesDataset("心音曲线图");
        mService2.setXYMultipleSeriesRenderer(40000, 1, "实时曲线图", "采样点", "相对幅度", Color.RED, Color.BLACK, Color.BLACK, Color.WHITE);
        mView2 = mService2.getGraphicalView();
        //将图表添加到布局容器中
        mRightCurveLayout.addView(mView2, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mService2.updateCharts(8000);
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        final List<String> datas = new ArrayList<String>();
        for (int i = 1; i < 80; i++)  datas.add("" + i);
        MyAdapter adapter = new MyAdapter(this);
        spinner.setAdapter(adapter);
        adapter.setDatas(datas);
        //adapter.setDropDownViewResource(R.layout.spinner_style);
        spinner.setSelection(0, false);
        /**选项选择监听*/
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                nianling = datas.get(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked){
            //添加到疾病数组
            if(buttonView.getText().toString().trim().equals("其它"))
                otherdis.setEnabled(true);
            else
            disease.add(buttonView.getText().toString().trim());
        }else {
            //从数组中移除
            if(buttonView.getText().toString().trim().equals("其它"))
                otherdis.setEnabled(false);
            else
            disease.remove(buttonView.getText().toString().trim());
        }
    }
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        // 选中状态改变时被触发
        switch (checkedId) {
            case R.id.rb_FeMale:
                // 当用户选择女性时
                xingbie = "女";
                break;
            case R.id.rb_Male:
                // 当用户选择男性时
                xingbie = "男";
                break;
            case R.id.position_rb1:
                // 当用户选择录制位置1时
                position = "p1";
                break;
            case R.id.position_rb2:
                // 当用户选择录制位置2时
                position = "p2";
                break;
            case R.id.position_rb3:
                // 当用户选择录制位置3时
                position = "p3";
                break;
            case R.id.position_rb4:
                // 当用户选择录制位置4时
                position = "p4";
                break;
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 播放或者暂停
            case R.id.bt_play:
                if(binderFlag)
                {
                    playerMusicByIBinder();
                    musicinfo.setText(musicBeanList.get(value).getTitle());
                }
                else
                    Toast.makeText(this, "请打开本地音乐", Toast.LENGTH_SHORT).show();
                break;
            case R.id.bt_pre:
                if(binderFlag)
                {   callBack.isPlayPre();
                    musicinfo.setText(musicBeanList.get(value).getTitle());
                }
                else
                    Toast.makeText(this, "请打开本地音乐", Toast.LENGTH_SHORT).show();
                break;
            case R.id.bt_next:
                if(binderFlag){
                    callBack.isPlayNext();
                    musicinfo.setText(musicBeanList.get(value).getTitle());
                }
                else
                    Toast.makeText(this, "请打开本地音乐", Toast.LENGTH_SHORT).show();
                break;
            case R.id.openMusic:
                Intent musicIntent = new Intent(MainActivity.this,MusicListActivity.class);
                startActivityForResult(musicIntent,1);
                Intent intent2 = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent2.setData(Uri.fromFile(new File(sdPath)));
                MainActivity.this.sendBroadcast(intent2);
                break;
            case R.id.clearMusic:
                deleteFile(sdPath+"/FinalAudio.wav");
                mService2.updateCharts(8000);
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(new File(sdPath)));
                MainActivity.this.sendBroadcast(intent);
                break;
            case R.id.otherdis:
                additondisease = otherdis.getText().toString();
                break;
            case R.id.disfinish:
                //将上次保留的选择进行清空初始化
                sb.delete(0,sb.length());
                for (int i =0;i<disease.size();i++) {
                    //把选择的爱好添加到string尾部
                    if(i==(disease.size()-1))
                        sb.append(disease.get(i));
                    else
                        sb.append(disease.get(i)+",");
                }
                saveAudio.setEnabled(true);
                Toast.makeText(this, "finish", Toast.LENGTH_SHORT).show();
                break;
            case R.id.saveAudio:
                Toast.makeText(this, "save", Toast.LENGTH_SHORT).show();
                if(position == null || position.length() == 0){
                    Toast.makeText(MainActivity.this, "请选择录制位置选项", Toast.LENGTH_SHORT).show();
                }
                else if(xingbie == null || xingbie.length() == 0){
                    Toast.makeText(MainActivity.this, "请选择性别选项", Toast.LENGTH_SHORT).show();
                }
                else if(nianling == null || nianling.length() == 0){
                    Toast.makeText(MainActivity.this, "请选择年龄选项", Toast.LENGTH_SHORT).show();
                }
                else{
                    mService2.updateCharts(8000);
                    //oldPath like "mnt/sda/sda1/我.png"
                    File file = new File(sdPath+"/FinalAudio.wav");
                    file.renameTo(new File(sdPath+"/"+sDateFormat.format(new Date())+"_"+position+"_"+sb+additondisease+"_"+xingbie+"_"+nianling+".wav"));
                    Intent intent1 = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intent1.setData(Uri.fromFile(new File(sdPath)));
                    MainActivity.this.sendBroadcast(intent1);
                }
                break;

        }
    }
    /**
     * 对文件重命名
     * @param filePath  文件的路径
     */
    public void changeFileName(String filePath,String reName){
        File file = new File(filePath);
        //前面路径必须一样才能修改成功
        String path = filePath.substring(0, filePath.lastIndexOf("/")+1)+reName+filePath.substring(filePath.lastIndexOf("."), filePath.length());
        File newFile = new File(path);
        file.renameTo(newFile);
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else{
            if(!bindor&&!times){
                mBluetoothAdapter.startLeScan(mLeScanCallback);
                bluescan.setText("蓝牙开始扫描......");
                //Toast.makeText(this, "蓝牙开始扫描......", Toast.LENGTH_SHORT).show();
                times = true;
            }
        }
        Log.d(TAG, "---onResume()---");
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    //注册蓝牙过滤服务
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
    //蓝牙服务
    //BLE设备的搜索结果将通过这个callback返回
    BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //我们肺音设备"0C:61:CF:38:60:8E"
                    //changhong 00:A0:50:11:13:2D
                    //wei"0C:61:CF:38:60:8E",0C:61:CF:39:A9:00，cheng 0C:61:CF:3A:0C:05 cheng1 0C:61:CF:39:7E:0E
                    //血糖 5C:F8:21:A2:62:F9
                    //0C:61:CF:39:7E:0E
                    //0C:61:CF:39:7E:0E   //最新0C:61:CF:37:D7:0E   0C:61:CF:37:B7:09    new 0C:61:CF:37:B7:09    old  0C:61:CF:39:7E:0E
                    if (device.getAddress().equals("0C:61:CF:2B:BF:04"))
                    {
                        mDeviceAddress = device.getAddress().trim();
                        Intent gattServiceIntent = new Intent(MainActivity.this, BluetoothLeService.class);
                        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        bindor = true;
                    }
                }
            });
        }
    };
   //蓝牙数据
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            mBluetoothLeService.connect(mDeviceAddress);
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
            Log.e(TAG, "mBluetoothLeService is null");
        }
    };
    //蓝牙服务接收连接，断开，数据传输服务
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                updateConnectionState(true);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                updateConnectionState(false);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
               Bundle extras = intent.getExtras();
               //String datas = extras.getString(BluetoothLeService.EXTRA_DATA);
                //method2();
               // method2(file.getPath(),datas);
                byte[] dataw = extras.getByteArray("BYTEARRAY");
                //得到ogg文件修改部分
//                try {
//                            bufos.write(dataw, 0, dataw.length);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
                rlength = rlength + dataw.length;
                byte[] datas = jieduanIs(extras.getByteArray("BYTEARRAY"));
                //开始 AA AA
                if (datas.length == 2 && datas[0] == -86 && datas[1] == -86) {
                    //录音开始，清空历史波形显示
                    mService2.updateCharts(8000);
                    //开启播放流
                    trackplayer.play();
                    //建立文件夹和文件
                    File file1 = new File(sdPath);
                    if (!file1.exists()) {
                        file1.mkdirs();
                    }
                    try {
                        File filepos1 = new File(Environment.getExternalStorageDirectory() + "/RawAudio.raw");
                        fos = new FileOutputStream(filepos1, true);
                        bufos = new BufferedOutputStream(fos);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                //结束录制控制符 FF FF FF FF
                else if (datas.length == 20 && datas[0] == -1 && datas[1] == -1 && datas[2] == -1 && datas[3] == -1) {
                    //trackplayer.flush();
                    //借助集合判断元素的重复性
//                    Set set = new HashSet(Arrays.asList(datas));
//                    if (set.size() == 1) {
                        trackplayer.stop();
                        byte[] databuffer = new byte[1024];
                        try {
                            FileInputStream in = new FileInputStream(Environment.getExternalStorageDirectory() + "/RawAudio.raw");
                            FileOutputStream out = new FileOutputStream(sdPath + "/FinalAudio.wav");
                            long totalAudioLen = in.getChannel().size();
                            //long totalDataLen = totalAudioLen + 36;
                            long totalDataLen = totalAudioLen + 52;
                            int srate = 7880;
                            //WriteWaveFileHeader(out, totalAudioLen, totalDataLen, 8000, 1, (8000 * 256 * 4) / 505);
                            //s = totalAudioLen/(256*channels)*505
                            ADWriteWaveFileHeader(out, totalAudioLen, totalDataLen, srate, 1, srate * 256 / 505, totalAudioLen / 256 * 505);
                            while (in.read(databuffer) != -1) out.write(databuffer);
                            in.close();
                            out.close();
                            //删除蓝牙缓存数据文件
                            deleteFile(Environment.getExternalStorageDirectory() + "/RawAudio.raw");
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            fos.close();
                            bufos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    //}
                }
                    else {
                        //写入数据
                        try {
                            bufos.write(dataw, 0, dataw.length);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    if(rlength>655360)
                    {
                        rlength = 0;
                        mService2.updateCharts(32000);
                    }
                        //解码数据显示
                        decodeByte(datas);
                    }
            }
        }
    };
    /**
     * //解码实时数据流文件
     * @param data
     */
    private void decodeByte(byte[] data) {
        //解析完成后实时显示
        if (dataLen == 0 && (dataLen + data.length) > 2) {
            dataLen = dataLen + data.length;
            mService2.updateCharts(Decode_IMA_ADPCM_4BIT_MONO(data, data.length, true), 32000);
        } else if ((dataLen + data.length) < 256 && (dataLen + data.length) > 2) {
            dataLen = dataLen + data.length;
            mService2.updateCharts(Decode_IMA_ADPCM_4BIT_MONO(data, data.length, false), 32000);
        } else if ((dataLen + data.length) >= 256) {
            if ((dataLen + data.length) == 256) {
                dataLen = dataLen + data.length;
                mService2.updateCharts(Decode_IMA_ADPCM_4BIT_MONO(data, data.length, false), 32000);
                dataLen = 0;
            } else {
                dataLen = dataLen + data.length;
                //说明两块有重叠
                //step 1
                //将前一块的数据截取出来
                int tempLength = data.length + 256 - dataLen;
                byte[] temp = new byte[tempLength];
                for (int i = 0; i < tempLength; i++)
                    temp[i] = data[i];
                mService2.updateCharts(Decode_IMA_ADPCM_4BIT_MONO(temp, tempLength, false), 32000);
                //step 2
                jieduanArray = new byte[dataLen - 256];
                for (int i = tempLength, j = 0; i < dataLen - 256; j++, i++) {
                    jieduanArray[j] = data[i];
                }
                jieduan = true;
                dataLen = 0;
            }
        }
    }
    /**
     * @param imaData 压缩的数据字节流
     * @param iDataLen 压缩的数据字节流的数据长度
     * @param start_Flag 数据块的传输是否完毕
     * @return
     */
    //adpcm解码测试程序
    public double[] Decode_IMA_ADPCM_4BIT_MONO(byte[] imaData, int iDataLen, boolean start_Flag) {
        int tl = start_Flag ? ((iDataLen - 4) * 2 + 1) : iDataLen * 2;
        double[] rdata = new double[tl];
        byte[] pcmData = new byte[tl * 2];
        iLen = 0;
        int iLen1 = 0;
        int i = 0;
        odd = true;
        //数据的长度不够4字节说明数据块存在一定的问题
        if (start_Flag) {
            int startValue = Integer.parseInt(String.format("%02X", imaData[1]) + String.format("%02X", imaData[0]), 16);
            if (startValue < 32768)
                samp0 = (short) startValue;
            else
                samp0 = (short) (-65536 - (~startValue + 1));
            //提取索引位置
            index = imaData[2] & 0xFF;
            if (index < 0) index = 0;
            if (index > 88) index = 88;
            sampx = samp0;
            odd = true;
            rdata[iLen++] = sampx / 32768.0;
            datas.add(sampx / 32768.0);
            pcmData[iLen1++] = imaData[0];
            pcmData[iLen1++] = imaData[1];
            i = i + 4;
        }
        while (i < iDataLen) {
            //周期性取一个字节高低位,先去一个字节的低4位，下次循环再取一个字节的高四位
            char a = ((char) (imaData[i] & 0xFF));
            if (odd)
                code = (char) (a & 0x0F);
            else
                code = (char) (a >> 4);
            diff = 0;
            if ((code & 4) != 0) diff = diff + steptab[index];
            if ((code & 2) != 0) diff = diff + (steptab[index] >> 1);
            if ((code & 1) != 0) diff = diff + (steptab[index] >> 2);
            diff = diff + (steptab[index] >> 3);

            if ((code & 8) != 0) diff = -diff;
            if ((sampx + diff) < -32768)
                sampx = -32768;
            else if ((sampx + diff) > 32767)
                sampx = 32767;
            else sampx = (short) (sampx + diff);
            if (sampx >= 0) {
                pcmData[iLen1++] = (byte) (sampx % 256);
                pcmData[iLen1++] = (byte) (sampx / 256);
            } else {
                pcmData[iLen1++] = (byte) ((sampx + 32768) % 256);
                pcmData[iLen1++] = (byte) ((sampx + 32768) / 256 + 128);
            }
            rdata[iLen++] = sampx / 32768.0;
            datas.add(sampx / 32768.0);
            index = index + indextab[code - 0];
            //防止数据索引越界
            if (index < 0) index = 0;
            if (index > 88) index = 88;
            odd = !odd;
            //偶数变奇数时，说明取到下一个字节数据
            if (odd)
                i++;
        }
        trackplayer.write(pcmData, 0, pcmData.length);
//        //将byte数据写入到Audio.raw文件中
//        try {
//            //trackplayer.write(pcmData, 0, pcmData.length);
//
//            //trackplayer.flush();
//            fos.write(pcmData, 0, pcmData.length);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        //trackplayer.write(pcmData, 0, pcmData.length);
        return rdata;
    }
    private byte[] jieduanIs(byte[] data) {
        if (jieduan) {
            //将本次的byte数据和上次截留的byte数据拼接
            byte[] data1 = data;
            data = new byte[data1.length + jieduanArray.length];
            // 合并两个数组
            System.arraycopy(jieduanArray, 0, data, 0, jieduanArray.length);
            System.arraycopy(data1, 0, data, jieduanArray.length, data1.length);
            jieduan = false;
        }
        return data;
    }
    /**
     * 电池电量识别
     *
     */
    public String batterydis(int rank){
       String batterytype ="无法识别状态";
       switch (rank){
           case 1:
               batterytype = "33%";
               break;
           case 2:
               batterytype = "66%";
               break;
           case 3:
               batterytype = "100%";
               break;
           case 4:
               batterytype = "充电中.......";
               break;
           case 5:
               batterytype = "无电........";
               break;
       }

       return batterytype;
    }
    /**
     * 删除单个文件
     *
     * @param fileName
     *            要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public  boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    //补码字节处理
    public double [] array(String res){
        double []c = new double[res.length()/4];
        int j = 0;
        for(int i = 0;i<res.length();){
            if((i+4)>res.length())
                break;
            String a = res.substring(i+2,i+4)+res.substring(i,i+2);
            int bm = Integer.parseInt(a,16);
            if(bm<32768)
                c[j] = bm/32768.0;
            else
            {
                c[j] = (-65536-(~bm+1))/32768.0;
            }
            i = i+4;
            j = j+1;
        }
        return c;
    }
    //totalDataLen = totalAudioLen+52;
    //byteRate = (longSampleRate*channels*256)/505
    //s = totalAudioLen/(256*channels)*505
    public void ADWriteWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate, int channels, long byteRate,long s) throws IOException {
        byte[] header = new byte[60];
        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 20; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 17; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = 0;
        header[33] = 1;
        header[34] = 4;
        header[35] = 0;
        header[36] = 2;
        header[37] = 0;
        header[38] = (byte) 249;
        header[39] = 1;
        //添加fact数据
        header[40] = 'f';
        header[41]='a';
        header[42] = 'c';
        header[43] = 't';
        header[44] = 4;
        header[45] = 0;
        header[46] = 0;
        header[47] = 0;
        header[48] = (byte) (s & 0xff);
        header[49] = (byte) ((s >> 8) & 0xff);
        header[50] = (byte) ((s >> 16) & 0xff);
        header[51] = (byte) ((s >> 24) & 0xff) ;
        header[52] = 'd';
        header[53] = 'a';
        header[54] = 't';
        header[55] = 'a';
        header[56] = (byte) (totalAudioLen & 0xff);
        header[57] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[58] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[59] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 60);
    }
    public void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate, int channels, long byteRate) throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        //
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        //
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        //
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = 16; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }
    public String byteArrayToStr(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        String str = new String(byteArray);
        return str;
    }
    //更新蓝牙连接的状态标识
    public void updateConnectionState(boolean biaoshi) {
        bluetoothstate = biaoshi;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(bluetoothstate)
                    bluescan.setText("蓝牙已连接");
                    //Toast.makeText(MainActivity.this, "已连接", Toast.LENGTH_SHORT).show();
                else {
                    //mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    bluescan.setText("蓝牙已断开");
                    //Toast.makeText(MainActivity.this, "断开连接", Toast.LENGTH_SHORT).show();
                    //bluetoothdis.setImageResource(R.drawable.bluetooth_close);
                    if (bindor) {
                        bindor = false;
                        unbindService(mServiceConnection);
                        mBluetoothLeService = null;
                        mNotifyCharacteristic = null;
                    }
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
                }
            }
        });
    }
    //蓝牙服务
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        gattCharacteristic = gattServices.get(2).getCharacteristics().get(1);
        //wei 00001002-0000-1000-8000-00805f9b34fb
        //gattCharacteristic = gattServices.get(3).getCharacteristics().get(3);
        //gattCharacteristic = gattServices.get(2).getCharacteristics().get(0);
        //gattCharacteristic = gattServices.get(2).getCharacteristics().get(0);//"0003a151-0000-1000-8000-00805f9b0131")
        if(gattCharacteristic.getUuid().toString().equals("00001002-0000-1000-8000-00805f9b34fb"))
        {
            mNotifyCharacteristic = gattCharacteristic;
            mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, true);
        }
    }
//    //数据判断和显示
//    private void displayData(String data) {
//        if (data != null) {
//            //String a = data.substring(9,11)+data.substring(12,14);
//            //String b = data.substring(15,17);
//            //mDataField.setText(String.valueOf(Integer.parseInt(a,16)/100.0));
//            mDataField.setText(data);
//        }
//    }
    //追加文件：使用FileWriter
    public static void method2(String fileName, String content) {
        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            FileWriter writer = new FileWriter(fileName, true);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //通知时间的播放的更新过程，主线程的更新UI
    private void seekTime(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mFlag) {
                    if (callBack != null) {
                        if((iControl+7880)<JieMaDouble.length)
                        {
                            Log.e(TAG,""+iControl+" 1轮"+" "+JieMaDouble.length);
                            if(waveAgain){
                                //Log.e(TAG,"Start");
                                mService2.updateCharts(32000);
                                iControl = 0;
                                waveAgain = false;
                            }
                            else {
                                if ((iControl + 7880 * 2) > JieMaDouble.length) {
                                    mService2.updateCharts(Arrays.copyOfRange(JieMaDouble, iControl, JieMaDouble.length), 32000);
                                    iControl = 0;
                                    waveAgain = true;
                                } else {
                                    mService2.updateCharts(Arrays.copyOfRange(JieMaDouble, iControl, 7880 + iControl), 32000);
                                    iControl = iControl + 7880;
                                }
                            }

                        }
//                        else {
//                            Log.e(TAG,""+iControl+"  2轮");
//                            mService2.updateCharts(Arrays.copyOfRange(JieMaDouble, iControl, JieMaDouble.length), 32000);
//                            iControl = 0;
//                            waveAgain = true;
//                        }
                        mHandler.sendMessage(Message.obtain());
                        try {
                            Thread.sleep(1000);

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        }).start();
    }

    //seekBar的时间的更新，调取主线程的更新
    private void forSeekBar(){
        mProgress = 0;
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (callBack != null) {
                    mProgress = progress;
                    //Toast.makeText(MainActivity.this, ""+mProgress, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (callBack != null) {
                    //音乐服务
                    callBack.iSeekTo(mProgress);
                }
            }
        });
    }
    // 回调方法，从第二个页面回来的时候会执行这个方法
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            Toast.makeText(this, "本应用需要蓝牙服务，请允许打开蓝牙！", Toast.LENGTH_SHORT).show();
            return;
        }
        // 根据上面发送过去的请求吗来区别
        switch (requestCode) {
            case Activity.RESULT_CANCELED:
                break;
            case 1:
                /** 接收音乐列表资源 */
                if(data.getIntExtra("isTouch",0)==11){
                    if(binderFlag){
                        unbindService(conn);
                        callBack = null;
                    }
                musicBeanList = data.getParcelableArrayListExtra("MUSIC_LIST");
                int currentPosition = data.getIntExtra("CURRENT_POSITION", 0);
                Intent intent = new Intent(this, MusicServiceDemo.class);
                intent.putParcelableArrayListExtra("MUSIC_LIST", musicBeanList);
                intent.putExtra("CURRENT_POSITION", currentPosition);
                //startService(intent);
                bindService(intent, conn, Service.BIND_AUTO_CREATE);
                binderFlag = true;
                JieMaDouble = null;
                iControl = 0;
                waveAgain = true;
                //mService2.updateCharts(32000);
                musicinfo.setText(musicBeanList.get(currentPosition).getTitle());
                    try {

                        FileInputStream in = new FileInputStream(musicBeanList.get(currentPosition).getMusicPath());
                        byte[] databuffer1 = new byte[in.available()];
                        while (in.read(databuffer1) != -1) {
                        }
                        Log.e(TAG, databuffer1.length+"");
                        JieMaDouble = decodeByte(Arrays.copyOfRange(databuffer1,60,databuffer1.length),true);
                        in.close();
                        //stringBuffer1 = stringBuffer;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                //获取数据后进行更新
               // mService2.updateCharts(,8000);
//                   //提取wav文件的数据,获取之后在界面进行实时的更新
//                    //将其提取到界面更新的handler进行处理
//                    try {
//                        byte[] databuffer1 = new byte[60000];
//                        FileInputStream in = new FileInputStream(musicBeanList.get(currentPosition).getMusicPath());
//                        while (in.read(databuffer1) != -1) {
//                        }
//                        StringBuilder stringBuffer = new StringBuilder(getRealLength(databuffer1));
//                        for (byte byteChar : databuffer1)
//                            stringBuffer.append(String.format("%02X", byteChar));
//                        mService2.updateCharts(8000);
//                        mService2.updateCharts(array(stringBuffer.toString()),8000);
//                        in.close();
//                        //stringBuffer1 = stringBuffer;
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    //播放的进度条和时间的开始和结束的标识控住
                    seekTime();
                    forSeekBar();
                }
                break;
            default:
                break;
        }
    }
    //播放音频的实时流文件的解码数据
    private double[] decodeByte(byte[] data,boolean startFlag) {
        //解析完成后实时显示
        int originalLen = data.length;
        Log.e(TAG,"originalLen:"+originalLen);
        //double [] decodeArray = new double[(originalLen-60)/256*505+(originalLen-60)%256 >0 ?(((originalLen-60)%256-4)*2+1):0];
        //double [] decodeArray = new double[originalLen/256*505];
        double [] decodeArray = new double[0];
        int len = (int) Math.ceil(data.length/256);
        int iloop = 0;
        boolean fin = false;
        while(len!=0){
            Log.e(TAG,"decodeArray.length:"+decodeArray.length+" ");
            byte [] tempByte;
            if(!fin && (iloop+256)<originalLen)
                tempByte = Arrays.copyOfRange(data,iloop,iloop+256);
            else
                tempByte = Arrays.copyOfRange(data,iloop,originalLen);
            //获取其数据后进行相应的存储
            decodeArray = concat(decodeArray,Decode_IMA_ADPCM_4BIT_MONO(tempByte, tempByte.length, true));
            //mService2.updateCharts(Decode_IMA_ADPCM_4BIT_MONO(tempByte, tempByte.length, true), 32000);
            iloop = iloop+256;
            len = len-1;
        }
        return decodeArray;
    }
    double [] concat(double[] a, double[] b) {
        double[] c= new double[a.length+b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }
    //求得byte数组的逻辑长度
    public int getRealLength(byte[] a){
        int i=0;
        for(;i<a.length;i++)
        {
            if(a[i]=='\0')
                break;
        }
        return i;
    }
    /**
     * 播放音乐通过Binder接口实现
     */
    public void playerMusicByIBinder() {
        boolean playerState = callBack.isPlayerMusic();
        if (playerState) {
            bt_play.setImageResource(R.drawable.pause);
        } else {
            bt_play.setImageResource(R.drawable.play);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(binderFlag){
            unbindService(conn);
            callBack = null;
        }
        unregisterReceiver(mGattUpdateReceiver);
        //DeviceControlActivity.releaseWakeLock();
        if (bindor) {
            unbindService(mServiceConnection);
            mBluetoothLeService = null;
        }
        //mBluetoothAdapter.stopLeScan(mLeScanCallback);
        if (mBluetoothAdapter.isEnabled())
            mBluetoothAdapter.disable();
        //android.os.Process.killProcess(android.os.Process.myPid());
        Log.d(TAG, "---onDestroy()---");
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //onDestroy();
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
