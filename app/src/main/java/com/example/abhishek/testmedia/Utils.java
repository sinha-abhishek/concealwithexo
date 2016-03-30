package com.example.abhishek.testmedia;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by abhishek on 30/03/16.
 */
public class Utils {
    public static void findFilesRecursively(String directoryName, List<File> files, Pattern pattern) {
        File directory = new File(directoryName);

        // get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                if(pattern.matcher(file.getName()).find()) {
                    files.add(file);
                }
            } else if (file.isDirectory()) {
                findFilesRecursively(file.getAbsolutePath(), files, pattern);
            }
        }
    }
}
