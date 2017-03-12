package org.apache.stegocasket.stego.bitmap;

import java.io.IOException;
import java.io.OutputStream;

public class PixelDemux {

    private OutputStream payloadStream;

    private int buffer;

    private int curr;

    private int size;

    private int counter;

    public PixelDemux(OutputStream outStream, int size) {
        payloadStream = outStream;
        this.size = size;
        curr = 0;
        buffer = 0;
        counter = 0;
    }

    public boolean demux(Pixel pixel)
            throws IOException {

        int guard = 8;
        switch (size) {
            case 0:
                return false;
            case 1:
                guard = 3;
                break;
            case 2:
                guard = 6;
        }

        int tmpi = (pixel.getPixel() & 0x10000) >> 14;
        tmpi |= (pixel.getPixel() & 0x100) >> 7;
        tmpi |= (pixel.getPixel() & 0x1);

        buffer |= tmpi << (21 - 3 * curr);
        curr++;

        if (curr == guard) {

            byte[] tmpbuf = new byte[3];
            tmpbuf[0] = (byte) ((buffer & 0xff0000) >> 16);
            tmpbuf[1] = (byte) ((buffer & 0xff00) >> 8);
            tmpbuf[2] = (byte) (buffer & 0xff);

            switch (size) {
                case 1:
                    payloadStream.write(tmpbuf, 0, 1);
                    size = 0;
                    break;
                case 2:
                    payloadStream.write(tmpbuf, 0, 2);
                    size = 0;
                    break;
                default:
                    payloadStream.write(tmpbuf);
                    size -= 3;
            }

            curr = 0;
            buffer = 0;
        }

        counter++;
        return true;

    }

    public void close()
            throws IOException {
        payloadStream.close();
        System.out.println("Counter for demux " + counter);
        if (size > 0) {
            throw new IOException("Payload incomplete " + size);
        }
    }

}