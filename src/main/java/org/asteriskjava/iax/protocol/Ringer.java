/**
 *
 * Benaiad@gmail.com
 */
package org.asteriskjava.iax.protocol;

import javax.sound.sampled.*;
import java.io.IOException;

public class Ringer implements Runnable {

    String fileLocation = "ring.wav";
    boolean stop = false;

    Ringer() {
    }

    public void start() {
        stop = false;
        Thread t = new Thread(this);
        t.start();
    }

    public void run() {
        while (!stop) {
            playSound(fileLocation);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ;
            }
        }
    }

    public void stop() {
        stop = true;
    }

    private void playSound(String fileName) {
        //File soundFile = new File(fileName);
        AudioInputStream audioInputStream = null;
        try {
            audioInputStream = AudioSystem.getAudioInputStream(getClass().getResourceAsStream(fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        AudioFormat audioFormat = audioInputStream.getFormat();
        SourceDataLine line = null;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        try {
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(audioFormat);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        line.start();
        int nBytesRead = 0;
        byte[] abData = new byte[128000];

        while (nBytesRead != -1 && !stop) {
            try {
                nBytesRead = audioInputStream.read(abData, 0, abData.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (nBytesRead >= 0) {
                int nBytesWritten = line.write(abData, 0, nBytesRead);
            }
        }

        line.drain();
        line.close();
    }
}