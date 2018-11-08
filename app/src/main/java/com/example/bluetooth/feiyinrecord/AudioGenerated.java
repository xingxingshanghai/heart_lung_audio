package com.example.bluetooth.feiyinrecord;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

// 开启音频文件写入线程 new Thread(new AudioRecordThread()).start();
class AudioGenerated  {
    // 缓冲区字节大小
    private int bufferSizeInBytes = 1024;
    private Context context;
    private String AudioName = AudioFileFunc.getRawFilePath();
    private String NewAudioName = AudioFileFunc.getWavFilePath();

    public AudioGenerated(Context context) {
        this.context = context;
    }

//    public void run() {
//        //进程控制读写文件的生成
//        writeDateTOFile();//往文件中写入裸数据
//        copyWaveFile(AudioName, NewAudioName);//给裸数据加上头文件
//    }

    /**
     * 这里将数据写入文件，但是并不能播放，因为AudioRecord获得的音频是原始的裸音频，
     * 如果需要播放就必须加入一些格式或者编码的头信息。但是这样的好处就是你可以对音频的 裸数据进行处理，比如你要做一个爱说话的TOM
     * 猫在这里就进行音频的处理，然后重新封装 所以说这样得到的音频比较容易做一些音频的处理。
     */
    public void writeDateTOFile(byte[] audiodata) {
        //是一个字节存储还是字节流形式存储，效率如何？
        // new一个byte数组用来存一些字节数据，大小为缓冲区大小
        //byte[] audiodata = new byte[bufferSizeInBytes];
        //如何获取蓝牙的原始数据
        FileOutputStream fos = null;
        try {
            File file = new File(AudioName);
            if (file.exists()) {
                file.delete();
            }
            fos = new FileOutputStream(file);// 建立一个可存取字节的文件
        } catch (Exception e) {
            e.printStackTrace();
        }
        //有个停止标志位来控制isRecord == true
//        while (MainActivity.isRecord == true) {
            try {
                fos.write(audiodata);
                fos.close();
                //saveAppend(AudioName, audiodata);
            } catch (Exception e) {
                e.printStackTrace();
            }
//        }
//        //什么时候关闭流？
//        try {
//            if (fos != null)
//                fos.close();// 关闭写入流
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    // 这里得到可播放的音频文件
    public void copyWaveFile(String inFilename, String outFilename) {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = AudioFileFunc.AUDIO_SAMPLE_RATE;
        int channels = 1;
        long byteRate = 16 * AudioFileFunc.AUDIO_SAMPLE_RATE * channels / 8;
        byte[] data = new byte[bufferSizeInBytes];
        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            //totalDataLen = totalAudioLen + 36;
            WriteWaveFileHeader(out, totalAudioLen, totalDataLen, longSampleRate, channels, byteRate);
            while (in.read(data) != -1) {
                out.write(data);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
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

    /**
     * 以追加的方式保存文件内容
     *
     * @param filename 文件名称
     * @param content  文件内容
     * @throws Exception
     */
    public void saveAppend(String filename, byte[] content) throws Exception {
        FileOutputStream outStream = context.openFileOutput(filename, Context.MODE_APPEND);
        outStream.write(content);
        outStream.close();
    }
}
