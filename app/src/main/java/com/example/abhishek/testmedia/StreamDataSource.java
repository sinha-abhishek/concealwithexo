package com.example.abhishek.testmedia;

import android.util.Log;

import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;
import com.google.android.exoplayer.C;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DataSpec;
import com.google.android.exoplayer.upstream.FileDataSource;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * Created by abhishek on 24/02/16.
 */
public class StreamDataSource implements DataSource {
    private long readBytes;
    private InputStream stream;

    private String path;
    private String baseFileName;
    private RandomAccessFile file;

    private long bytesRemaining;
    private boolean opened;
    private int encryptedBlockSize;
    private long nextReadPos;
    private byte[] decryptedBuffer;

    public StreamDataSource(String path, String fileName, int encryptedBlockSize) {
        this.path = path;
        baseFileName = fileName;
        readBytes = 0;
        this.encryptedBlockSize = encryptedBlockSize;
        nextReadPos = 0;
        Encrypter enc = Encrypter.GetInstance();
        try {
            InputStream inputStream = enc.GetDecryptedBlockData(path,fileName,encryptedBlockSize);
            //decryptedBuffer = new byte[encryptedBlockSize];
            int read;
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            while ((read = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            inputStream.close();
            decryptedBuffer = out.toByteArray();
            String s = new String(decryptedBuffer);
            Log.i("StreamData", "#decrypt="+s);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyChainException e) {
            e.printStackTrace();
        } catch (CryptoInitializationException e) {
            e.printStackTrace();
        }
    }
    @Override
    public long open(DataSpec dataSpec) throws IOException {
        //return file.length();

        try {
            long totalFileSize;
            RandomAccessFile file2 = new RandomAccessFile(path+File.separator+"1_"+baseFileName, "r");
            totalFileSize = encryptedBlockSize + file2.length();
            nextReadPos = dataSpec.position;
//            if (nextReadPos < encryptedBlockSize) {
//
//            }
//            file = new RandomAccessFile(path, "r");
//            file.seek(dataSpec.position);
            bytesRemaining = dataSpec.length == C.LENGTH_UNBOUNDED ? totalFileSize - dataSpec.position
                    : dataSpec.length;
            if (bytesRemaining < 0) {
                throw new EOFException();
            }
        } catch (IOException e) {
            throw new FileDataSource.FileDataSourceException(e);
        }


        opened = true;
        return bytesRemaining;
    }

    @Override
    public void close() throws IOException {
        //stream.close();
    }

    @Override
    public int read(byte[] buffer, int offset, int readLength) throws IOException {
        if (bytesRemaining == 0) {
            return -1;
        } else {
            int bytesRead = 0;
            try {
                if (nextReadPos < encryptedBlockSize && nextReadPos + readLength < encryptedBlockSize) {
                    byte[] temp = new byte[readLength];
                    for (int i = 0 ; i < readLength ; i++ ) {
                        temp[i] = decryptedBuffer[(int)nextReadPos+i];
                        buffer[offset+i] = temp[i];
                        bytesRead++;
                    }
                    nextReadPos += bytesRead;

                    //bytesRead = stream.read(buffer,offset,readLength);
                    //stream.close();
                }
                else if (nextReadPos >= encryptedBlockSize) {
                    RandomAccessFile file = new RandomAccessFile(path+File.separator+"1_"+baseFileName, "r");
                    file.seek(nextReadPos - encryptedBlockSize);
                    bytesRead = file.read(buffer, offset, (int) Math.min(bytesRemaining, readLength));
                    file.close();
                    nextReadPos += bytesRead;
                } else {
                    //this is a little complex
                    byte[] temp = new byte[readLength];
                    int readFromEnc = encryptedBlockSize - (int)nextReadPos;
                    for (int i = 0 ; i < readFromEnc ; i++ ) {
                        temp[i] = decryptedBuffer[(int)nextReadPos+i];
                        buffer[offset+i] = temp[i];
                        bytesRead++;
                    }

                    //stream.close();
                    if ( bytesRead < readLength) {
                        int bytesLeft = readLength - bytesRead;
                        RandomAccessFile file = new RandomAccessFile(path+File.separator+"1_"+baseFileName, "r");
                        file.seek(0);
                        bytesRead += file.read(buffer, offset+bytesRead, (int) Math.min(bytesRemaining, bytesLeft));
                        file.close();
                    }
                    nextReadPos += bytesRead;
                }

            } catch (IOException e) {
                throw new FileDataSource.FileDataSourceException(e);
            }

            if (bytesRead > 0) {
                bytesRemaining -= bytesRead;
                readBytes += bytesRead;
            }

            return bytesRead;
        }
    }
}
