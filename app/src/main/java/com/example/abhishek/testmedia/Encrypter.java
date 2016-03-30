package com.example.abhishek.testmedia;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.Entity;
import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;
import com.facebook.crypto.util.SystemNativeCryptoLibrary;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.crypto.CipherOutputStream;

/**
 * Created by abhishek on 25/02/16.
 */
public class Encrypter {
    private Crypto _crypto;
    private Entity _entity;
    private boolean isInited = false;
    private static Encrypter instance = null;
    public static final int ENC_BLOCK_SIZE = 4096;

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

    public void EncryptSDRecursive() throws IOException {
        File dir = Environment.getExternalStorageDirectory();
        List<File> filesToEncrypt = new ArrayList<File>();
        Utils.findFilesRecursively(dir.getAbsolutePath(), filesToEncrypt, Pattern.compile(".*\\.mp4$"));
        String infoFileName = ".mp4Info";
        Set<String> filesEncrypted = GetAllEncryptedFilePaths(dir.getAbsolutePath() + File.separator + infoFileName);
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(dir.getAbsoluteFile()+ File.separator+infoFileName,true));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (File file:filesToEncrypt) {
            if (filesEncrypted == null || !filesEncrypted.contains(file.getAbsolutePath())) {
                try {
                    Encrypt(file);
                    bw.write(file.getAbsolutePath());
                    bw.newLine();
                    if (filesEncrypted == null) {
                        filesEncrypted = new HashSet<String>();
                    }
                    filesEncrypted.add(file.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (KeyChainException e) {
                    e.printStackTrace();
                } catch (CryptoInitializationException e) {
                    e.printStackTrace();
                }
            }
        }
        if (bw != null) {
            bw.close();
        }

    }

    public Set<String> GetAllEncryptedFilePaths(String pathToInfoFile) throws IOException {
        File f = new File(pathToInfoFile);
        if (!f.exists()) {
            return null;
        }
        Set<String> filesEncrypted = new HashSet<String>();
        BufferedReader br = new BufferedReader(new FileReader(f));
        String line;
        while ((line = br.readLine()) != null) {
            filesEncrypted.add(line);
        }
        return filesEncrypted;
    }

    public String GetEditedFileName(File file, String token) {
        String path = file.getAbsolutePath();
        String extension;
        String fname;
        int i = path.lastIndexOf('.');
        if (i > 0) {
            extension = path.substring(i);
            fname = path.substring(0,i)+token+extension;
        } else {
            fname = path+token;
        }
        return fname;
    }

    private void Encrypt(File file) throws IOException, KeyChainException, CryptoInitializationException {
        String encBlockFileName = GetEditedFileName(file,"_0");
        String plainBlockFileName = GetEditedFileName(file,"_1");
        InputStream fis = new FileInputStream(file);
        FileOutputStream encfileStream = new FileOutputStream(encBlockFileName);
        FileOutputStream plainfileStream = new FileOutputStream(plainBlockFileName);

// Creates an output stream which encrypts the data as
// it is written to it and writes it out to the file.
        OutputStream cryptoCipherOutputStream = _crypto.getCipherOutputStream(
                encfileStream,
                _entity);
        byte[] buf = new byte[ENC_BLOCK_SIZE];
        fis.read(buf);
        String s = new String(buf);
        Log.i("Encrypter", "#decrypt=" + s);
        cryptoCipherOutputStream.write(buf);
        byte[] buffer = new byte[1024];
        int len;
        while ((len =  fis.read(buffer)) != -1) {
            plainfileStream.write(buffer, 0, len);
        }
        fis.close();
        cryptoCipherOutputStream.close();
        plainfileStream.close();
        //file.delete();
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

    public InputStream GetDecryptedBlockData(String baseFileName , int encBlockSize ) throws IOException, KeyChainException, CryptoInitializationException {
        String filePath =   GetEditedFileName(new File(baseFileName),"_0");
        FileInputStream fileInputStream = new FileInputStream(filePath);
        InputStream inputStream = _crypto.getCipherInputStream(
                fileInputStream,
                _entity);
        return inputStream;
    }
}
