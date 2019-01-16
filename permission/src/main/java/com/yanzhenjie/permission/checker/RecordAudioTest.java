/*
 * Copyright © Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yanzhenjie.permission.checker;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.File;

/**
 * Created by YanZhenjie on 2018/1/14.
 */
class RecordAudioTest implements PermissionTest {
    // 音频获取源
    public static int audioSource = MediaRecorder.AudioSource.MIC;
    // 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    public static int sampleRateInHz = 44100;
    // 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
    public static int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    // 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
    public static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    // 缓冲区字节大小
    public static int bufferSizeInBytes = 0;

    private Context mContext;

    RecordAudioTest(Context context) {
        this.mContext = context;
    }

    @Override
    public boolean test() throws Throwable {
        File mTempFile = null;
        bufferSizeInBytes = 0;
        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
                channelConfig, audioFormat);
        AudioRecord audioRecord = null;
        try {
//            mTempFile = File.createTempFile("permission", "test");
            audioRecord =  new AudioRecord(audioSource, sampleRateInHz,
                    channelConfig, audioFormat, bufferSizeInBytes);
            audioRecord.startRecording();

            if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING
                    && audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_STOPPED) {
//            AVLogUtils.e(TAG, "audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING : " + audioRecord.getRecordingState());
                return false;
            }

            if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED) {
                //如果短时间内频繁检测，会造成audioRecord还未销毁完成，此时检测会返回RECORDSTATE_STOPPED状态，再去read，会读到0的size，可以更具自己的需求返回true或者false
                return false;
            }

            byte[] bytes = new byte[1024];
            int readSize = audioRecord.read(bytes, 0, 1024);
            if (readSize == AudioRecord.ERROR_INVALID_OPERATION || readSize <= 0) {
//            AVLogUtils.e(TAG, "readSize illegal : " + readSize);
                return false;
            }
            return true;
        } catch (Throwable e) {
            PackageManager packageManager = mContext.getPackageManager();
            return !packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
        } finally {
            try {
                audioRecord.stop();
                audioRecord.release();
            } catch (Exception ignored) {
            }

//            if (mTempFile != null && mTempFile.exists()) {
//                mTempFile.delete();
//            }
        }
    }
}