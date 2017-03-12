package org.apache.stegocasket.stego.bitmap;

import java.io.IOException;
import java.io.InputStream;

public class PixelMux {

    private static int[] maskTable = { 0x0, 0x1, 0x100, 0x101, 0x10000, 0x10001, 0x10100, 0x10101 };

    private static int[] filterTable = { 0xe00000, 0x1c0000, 0x38000, 0x7000, 0xe00, 0x1c0, 0x38, 0x7 };

    private InputStream payloadStream;

    private int buffer;

    private int guard;

    private int curr;

    private int size;

    private int counter;

    public PixelMux(InputStream inStream) {
        payloadStream = inStream;
        curr = 8;
        guard = 8;
        size = 0;
        counter = 0;
    }

    /*
     * http://stackoverflow.com/questions/4872206/most-elegant-way-to-convert-a- byte-to-an-int-in-java
     */

    public Pixel mux(Pixel pixel)
            throws IOException {

        if (curr == guard) {

            byte[] tmpbuf = new byte[3];
            int res = payloadStream.read(tmpbuf);
            int tmpd = 0;

            buffer = 0;

            switch (res) {
                case 3:
                    tmpd = tmpbuf[2] & 0xff;
                    buffer |= tmpd;
                case 2:
                    tmpd = tmpbuf[1] & 0xff;
                    buffer |= (tmpd << 8);
                case 1:
                    tmpd = tmpbuf[0] & 0xff;
                    buffer |= (tmpd << 16);
                    break;
                default:
                    return null;
            }

            switch (res) {
                case 1:
                    guard = 3;
                    break;
                case 2:
                    guard = 6;
                    break;
                case 3:
                    guard = 8;
            }

            curr = 0;
            size += res;
        }

        int tmpi = (buffer & filterTable[curr]) >> (21 - 3 * curr);

        curr++;

        int stegoMask = maskTable[tmpi];

        counter++;
        return new Pixel((pixel.getPixel() & 0xfffefefe) | stegoMask);
    }

    public void close()
            throws IOException {
        System.out.println("Counter for mux " + counter);
        payloadStream.close();
    }

    public int getSize() {
        return size;
    }

}