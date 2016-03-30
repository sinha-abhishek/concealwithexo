package com.example.abhishek.testmedia;

import com.google.android.exoplayer.C;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DataSpec;
import com.google.android.exoplayer.upstream.FileDataSource;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * Created by abhishek on 26/02/16.
 */
public class CustomDataSource implements DataSource {
    private InputStream stream;
    private String fileName;
    private long bytesRemaining;

    public CustomDataSource(String fileName1, String fileName2) throws FileNotFoundException {
        this.fileName = fileName;
        //stream = new FileInputStream(this.fileName);
    }

    @Override
    public long open(DataSpec dataSpec) throws IOException {
        try {
            long totalFileSize;
            File file = new File(fileName);
            totalFileSize = file.length();
            long nextReadPos = dataSpec.position;
            //TODO: reset stream to beginning here rather than create
            stream = new FileInputStream(fileName);
            stream.skip(nextReadPos);
            bytesRemaining = dataSpec.length == C.LENGTH_UNBOUNDED ? totalFileSize - dataSpec.position
                    : dataSpec.length;
            if (bytesRemaining < 0) {
                throw new EOFException();
            }
        } catch (IOException e) {
            throw new FileDataSource.FileDataSourceException(e);
        }



        return bytesRemaining;
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

    @Override
    public int read(byte[] buffer, int offset, int readLength) throws IOException {
        if (bytesRemaining <= 0) {
            return -1;
        } else {
            int bytesRead = 0;
            try {
                bytesRead = stream.read(buffer, offset, (int) Math.min(bytesRemaining, readLength));
            } catch (IOException e) {
                throw e;
            }

            if (bytesRead > 0) {
                bytesRemaining -= bytesRead;
            }

            return bytesRead;
        }
    }
}
