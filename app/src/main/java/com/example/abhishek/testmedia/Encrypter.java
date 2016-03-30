package com.example.abhishek.testmedia;

import android.content.Context;
import android.util.Log;

import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.Entity;
import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;
import com.facebook.crypto.util.SystemNativeCryptoLibrary;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.crypto.CipherOutputStream;

/**
 * Created by abhishek on 25/02/16.
 */
public class Encrypter {
    private Crypto _crypto;
    private Entity _entity;
    private boolean isInited = false;
    private static Encrypter instance = null;

    private Encrypter(){
        isInited = false;
    }

    public static Encrypter GetInstance() {
        if(instance == null) {
            instance = new Encrypter();
        }
        return instance;
    }

    public void Init(Context context, String saltString) {
        _crypto = new Crypto(new SharedPrefsBackedKeyChain(context),
                new SystemNativeCryptoLibrary());
        _entity = new Entity(saltString);
        isInited = true;
    }

    public boolean IsInitialized() {
        return isInited;
    }

    public void BreakAndEncrypt(String fileLoc, String fileName, int encBlockSize) throws IOException, KeyChainException, CryptoInitializationException {
        String encBlockFileName = "0_"+fileName;
        String plainBlockFileName = "1_"+fileName;
        File src = new File(fileLoc+File.separator + fileName);
        InputStream fis = new FileInputStream(src);
        FileOutputStream encfileStream = new FileOutputStream(fileLoc+ File.separator + encBlockFileName);
        FileOutputStream plainfileStream = new FileOutputStream(fileLoc+ File.separator + plainBlockFileName);

// Creates an output stream which encrypts the data as
// it is written to it and writes it out to the file.
        OutputStream cryptoCipherOutputStream = _crypto.getCipherOutputStream(
                encfileStream,
                _entity);
        byte[] buf = new byte[encBlockSize];
        fis.read(buf);
        String s = new String(buf);
        Log.i("Encrypter","#decrypt="+s);
        cryptoCipherOutputStream.write(buf);
        byte[] buffer = new byte[1024];
        int len;
        while ((len =  fis.read(buffer)) != -1) {
            plainfileStream.write(buffer, 0, len);
        }
        fis.close();
        cryptoCipherOutputStream.close();
        plainfileStream.close();
        //src.delete();
    }

    public InputStream GetDecryptedBlockData(String fileLoc , String baseFileName , int encBlockSize ) throws IOException, KeyChainException, CryptoInitializationException {
        String filePath = fileLoc + File.separator + "0_"+baseFileName;
        FileInputStream fileInputStream = new FileInputStream(filePath);
        InputStream inputStream = _crypto.getCipherInputStream(
                fileInputStream,
                _entity);
        return inputStream;
    }
}
