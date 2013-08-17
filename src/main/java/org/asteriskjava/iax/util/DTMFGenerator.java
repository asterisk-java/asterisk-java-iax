package org.asteriskjava.iax.util;


import javax.sound.sampled.*;


public class DTMFGenerator {

    private int NUM_BANDS = 10;

    private float frequencyArray[] = {697.0F, 770.0F, 852.0F, 941.0F, 1209.0F, 1336.0F, 1477.0F, 1633.0F, 440.0F, 523.25F};

    private float Q1[] = new float[NUM_BANDS];
    private float Q2[] = new float[NUM_BANDS];
    private float freqCoeffValueArray[] = new float[NUM_BANDS];
    private static boolean ringing = true;

    public void init(float sps) {
        for (int i = 0; i < NUM_BANDS; ++i) {
            Q1[i] = (float) java.lang.Math.sin(0.0F);
            Q2[i] = (float) java.lang.Math.sin(2.0F * java.lang.Math.PI * frequencyArray[i] / sps);
            freqCoeffValueArray[i] = (float) (2 * java.lang.Math.cos(2.0F * java.lang.Math.PI * frequencyArray[i] / sps));
        }
    }

    public float getLow(String chr) {
        int idx = 0;
        if ("1".equals(chr)) {
            idx = 0;
        } else if ("2".equals(chr)) {
            idx = 0;
        } else if ("3".equals(chr)) {
            idx = 0;
        } else if ("4".equals(chr)) {
            idx = 1;
        } else if ("5".equals(chr)) {
            idx = 1;
        } else if ("6".equals(chr)) {
            idx = 1;
        } else if ("7".equals(chr)) {
            idx = 2;
        } else if ("8".equals(chr)) {
            idx = 2;
        } else if ("9".equals(chr)) {
            idx = 2;
        } else if ("*".equals(chr)) {
            idx = 3;
        } else if ("0".equals(chr)) {
            idx = 3;
        } else if ("#".equals(chr)) {
            idx = 3;
        } else if ("Ring".equals(chr)) {
            idx = 8;
        }
        float g;
        g = Q1[idx];
        Q1[idx] = Q2[idx];
        Q2[idx] = freqCoeffValueArray[idx] * Q1[idx] - g;
        return g;
    }

    public float getHigh(String chr) {
        int idx = 0;
        if ("1".equals(chr)) {
            idx = 4;
        } else if ("2".equals(chr)) {
            idx = 5;
        } else if ("3".equals(chr)) {
            idx = 6;
        } else if ("4".equals(chr)) {
            idx = 4;
        } else if ("5".equals(chr)) {
            idx = 5;
        } else if ("6".equals(chr)) {
            idx = 6;
        } else if ("7".equals(chr)) {
            idx = 4;
        } else if ("8".equals(chr)) {
            idx = 5;
        } else if ("9".equals(chr)) {
            idx = 6;
        } else if ("*".equals(chr)) {
            idx = 4;
        } else if ("0".equals(chr)) {
            idx = 5;
        } else if ("#".equals(chr)) {
            idx = 6;
        } else if ("Ring".equals(chr)) {
            idx = 9;
        }
        float g;
        g = Q1[idx];
        Q1[idx] = Q2[idx];
        Q2[idx] = freqCoeffValueArray[idx] * Q1[idx] - g;
        return g;
    }

    public float getDTMF(String chr) {
        return getLow(chr) + getHigh(chr);
    }

    public static void StopRing() {
        ringing = false;
    }


    public static void playRing() {
        ringing = true;
        Thread ring = new Thread() {

            @Override
            public void run() {
                while (ringing) {
                    try {
                        DTMFGenerator generator = new DTMFGenerator();
                        generator.init(8000.0F);
                        AudioFormat pcmFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 8000.0F, 8, 1, 1, 8000.0F, false);
                        DataLine.Info info = new DataLine.Info(SourceDataLine.class, pcmFormat);
                        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
                        line.open(pcmFormat);
                        line.start();
                        byte[] abData = new byte[160];
                        for (int i = 0; i < 150; ++i) {
                            int available = line.available();
                            if (available < 1600) {
                                Thread.sleep(150);
                            } else {
                                if (i < 50) {
                                    for (int j = 0; j < 160; ++j) {
                                        if ((i % 2) == 0) {
                                            abData[j] = (byte) ((generator.getLow("Ring") * 128.0F) * 0.1);
                                        } else {
                                            abData[j] = (byte) ((generator.getHigh("Ring") * 128.0F) * 0.1);
                                        }
                                    }
                                } else {
                                    for (int j = 0; j < 160; ++j) {
                                        abData[j] = (byte) 0;
                                    }
                                }
                                line.write(abData, 0, abData.length);
                            }
                        }
                        line.drain();
                        line.close();
                    } catch (LineUnavailableException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        ring.start();
    }


    public static void playTone(final String tone) {
        Thread toneThread = new Thread() {
            @Override
            public void run() {
                try {
                    DTMFGenerator generator = new DTMFGenerator();
                    generator.init(8000.0F);
                    AudioFormat pcmFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 8000.0F, 8, 1, 1, 8000.0F, false);
                    DataLine.Info info = new DataLine.Info(SourceDataLine.class, pcmFormat);
                    SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
                    line.open(pcmFormat);
                    line.start();
                    byte[] abData = new byte[160];
                    for (int i = 0; i < 10; ++i) {
                        int available = line.available();
                        if (available < 1600) {
                            Thread.sleep(150);
                        } else {
                            for (int j = 0; j < 160; ++j) {
                                abData[j] = (byte) ((generator.getDTMF(tone) * 128.0F) * 0.1);
                            }
                            line.write(abData, 0, abData.length);
                        }
                    }
                    line.drain();
                    line.close();
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        toneThread.start();
    }

}
