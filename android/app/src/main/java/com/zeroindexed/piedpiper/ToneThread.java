package com.zeroindexed.piedpiper;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class ToneThread extends Thread {
    public interface ToneCallback {
        public void onProgress(int current, int total);

        public void onDone();
    }

    static final int sample_rate = 44100;
    static final float duration = 1f;
    static final int sample_size = Math.round(duration * sample_rate);

    final float[] frequencies;
    final ToneCallback callback;

    public ToneThread(float[] frequencies, ToneCallback callback) {
        this.frequencies = frequencies;
        this.callback = callback;
        setPriority(Thread.MAX_PRIORITY);
    }

    @Override
    public void run() {
        final AudioTrack track = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                sample_rate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                2 * sample_size,
                AudioTrack.MODE_STREAM
        );

        final int total_samples = frequencies.length * sample_rate;

        track.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
            @Override
            public void onMarkerReached(AudioTrack track) {
                callback.onDone();
            }

            @Override
            public void onPeriodicNotification(AudioTrack track) {
                callback.onProgress(track.getPlaybackHeadPosition(), total_samples);
            }
        });
        track.setPositionNotificationPeriod(sample_rate / 10);

        track.play();

        for (float freq : frequencies) {
            short[] samples = generate(freq);
            track.write(samples, 0, samples.length);
        }

        track.setNotificationMarkerPosition(sample_size);
    }

    static short[] generate(float frequency) {
        final short sample[] = new short[sample_size];
        final double increment = 2 * Math.PI * frequency / sample_rate;

        double angle = 0;
        for (int i = 0; i < sample.length; ++i) {
            sample[i] = (short) (Math.sin(angle) * Short.MAX_VALUE);
            angle += increment;
        }

        return sample;
    }
}