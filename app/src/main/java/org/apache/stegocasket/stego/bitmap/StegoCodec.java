package org.apache.stegocasket.stego.bitmap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;

import org.apache.stegocasket.core.SecretException;
import org.apache.stegocasket.crypto.CryptoUtils;

public class StegoCodec {

    private static final int MD_SIZE = 48;

    public static byte[] encodeHeader(int size, String pwd)
            throws IOException, SecretException {

        Cipher cipher = CryptoUtils.setupCipher(Cipher.ENCRYPT_MODE, pwd);
        ByteArrayOutputStream boutStream = new ByteArrayOutputStream();
        CipherOutputStream cOut = new CipherOutputStream(boutStream, cipher);

        String tmps = UUID.randomUUID().toString();
        byte[] clearHdr = tmps.getBytes();

        clearHdr[4] = (byte) ((size & 0xff000000) >> 24);
        clearHdr[5] = (byte) ((size & 0xff0000) >> 16);
        clearHdr[6] = (byte) ((size & 0xff00) >> 8);
        clearHdr[7] = (byte) (size & 0xff);

        cOut.write(clearHdr);
        cOut.close();

        byte[] cryptoHdr = boutStream.toByteArray();
        Log.d("StegoCodec", "Encoded header with " + cryptoHdr.length + " bytes");

        return cryptoHdr;
    }

    public static int decodeHeader(byte[] cryptoHdr, String pwd)
            throws IOException, SecretException {

        byte[] clearHdr = new byte[MD_SIZE];

        Cipher cipher = CryptoUtils.setupCipher(Cipher.DECRYPT_MODE, pwd);
        ByteArrayInputStream binStream = new ByteArrayInputStream(cryptoHdr);
        CipherInputStream cIn = new CipherInputStream(binStream, cipher);
        cIn.read(clearHdr);
        cIn.close();

        int result = 0;
        result |= (clearHdr[4] & 0xff) << 24;
        result |= (clearHdr[5] & 0xff) << 16;
        result |= (clearHdr[6] & 0xff) << 8;
        result |= clearHdr[7] & 0xff;
        return result;
    }

    public static byte[] decode(Context ctx, Uri pictureURI, String pwd)
            throws IOException, SecretException {

        ParcelFileDescriptor parcelFileDescriptor = ctx.getContentResolver().openFileDescriptor(pictureURI, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap tgtImg = BitmapFactory.decodeFileDescriptor(fileDescriptor);

        ByteArrayOutputStream hdrStream = new ByteArrayOutputStream();
        ByteArrayOutputStream secretStream = new ByteArrayOutputStream();

        boolean readHdr = true;
        boolean stop = false;
        int size = 0;
        PixelDemux sDemux = new PixelDemux(hdrStream, MD_SIZE);
        PixelDemux pDemux = null;
        for (int y = 0; y < tgtImg.getHeight(); y++) {
            for (int x = 0; x < tgtImg.getWidth(); x++) {
                if (readHdr) {
                    readHdr = sDemux.demux(new Pixel(tgtImg.getPixel(x, y)));
                }

                if (!readHdr) {
                    if (size == 0) {
                        size = decodeHeader(hdrStream.toByteArray(), pwd);
                        if (size < 0)
                            throw new IOException("Cannot find header");

                        pDemux = new PixelDemux(secretStream, size);
                    }
                    if (!pDemux.demux(new Pixel(tgtImg.getPixel(x, y)))) {
                        stop = true;
                        break;
                    }
                }
            }
            if (stop)
                break;
        }

        sDemux.close();
        pDemux.close();
        secretStream.close();

        return secretStream.toByteArray();

    }

    public static void encode(Context ctx, Uri pictureURI, byte[] payload, String pwd)
            throws IOException, SecretException {

        ParcelFileDescriptor parcelFileDescriptor = ctx.getContentResolver().openFileDescriptor(pictureURI, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap srcImg = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        Bitmap tgtImg = srcImg.copy(Bitmap.Config.ARGB_8888, true);

        ByteArrayInputStream hdrStream = new ByteArrayInputStream(encodeHeader(payload.length, pwd));
        PixelMux sMux = new PixelMux(hdrStream);

        ByteArrayInputStream secretStream = new ByteArrayInputStream(payload);
        PixelMux pMux = new PixelMux(secretStream);

        boolean writeHdr = true;
        boolean stop = false;

        for (int y = 0; y < tgtImg.getHeight(); y++) {
            for (int x = 0; x < tgtImg.getWidth(); x++) {

                Pixel inPix = new Pixel(tgtImg.getPixel(x, y));

                if (writeHdr) {
                    Pixel outPix = sMux.mux(inPix);
                    if (outPix != null) {
                        tgtImg.setPixel(x, y, outPix.getPixel());
                    } else {
                        writeHdr = false;
                    }
                }

                if (!writeHdr) {
                    Pixel outPix = pMux.mux(inPix);
                    if (outPix != null) {
                        tgtImg.setPixel(x, y, outPix.getPixel());
                    } else {
                        stop = true;
                        break;
                    }
                }
            }
            if (stop)
                break;
        }

        hdrStream.close();
        sMux.close();
        secretStream.close();
        pMux.close();

        String pictureName = null;
        Cursor cursor = ctx.getContentResolver().query(pictureURI, null, null, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                pictureName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        } finally {
            cursor.close();
        }

        if (pictureName == null) {
            pictureName = UUID.randomUUID() + ".png";
        } else {
            pictureName = pictureName.subSequence(0, pictureName.lastIndexOf('.')) + ".png";
        }
        Log.d("StegoCodec", "Writing " + pictureName);

        File filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        filePath.mkdirs();
        File imageFile = new File(filePath, pictureName);
        FileOutputStream fOut = null;
        try {

            fOut = new FileOutputStream(imageFile);
            tgtImg.compress(Bitmap.CompressFormat.PNG, 90, fOut);

            MediaScannerConnection.scanFile(ctx, new String[] { imageFile.getAbsolutePath() }, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });

        } finally {
            if (fOut != null) {
                try {
                    fOut.close();
                } catch (IOException ioEx) {

                }
            }
        }

    }

}