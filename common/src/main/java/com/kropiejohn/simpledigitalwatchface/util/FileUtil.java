package com.kropiejohn.simpledigitalwatchface.util;

import java.io.File;

/**
 * Collection of file utilities.
 * <p>
 * Created by jonat on 4/16/2017.
 */

public class FileUtil {

    /**
     * Will join paths and return the joined path.
     *
     * @param path1 The beginning section of the path
     * @param path2 The ending section of path.
     * @return The joined path.
     */
    public static String joinPaths(String path1, String path2) {
        return new File(path1, path2).toString();
    }
}
