package se.lo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Created by jobi on 2014-07-01.
 */
public class LiveCatchupService {
    private final static String LIVEPATH = "/mnt/resource/livevod/";
    private final static String VODPATH = "/mnt/resource/vod/";
    private Collection<File> liveFilesCollection = new ArrayList<File>();
    private String livePath;
    private String vodPath;

    public static void main(String[] args) {
        LiveCatchupService catchupService = new LiveCatchupService(LIVEPATH, VODPATH);
        catchupService.createLinks();
    }

    public LiveCatchupService(String pathToLiveFiles, String pathToVodFiles) {
        vodPath = pathToVodFiles;
        livePath = pathToLiveFiles;
        File liveFiles = new File(livePath);
        if (liveFiles.isDirectory()) {
            for (File f : liveFiles.listFiles()) {
                if (f.getName().contains("HD_720p_")) {
                    // System.out.println("Found live file: " + f.getName());
                    liveFilesCollection.add(f);
                }
            }
        }
    }

    public void createLinks() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("YYYYMMdd_HHmm");
        for (File f: liveFilesCollection) {
            DateTime lastModified = new DateTime(f.lastModified());
            String linkFileName = "LIVE_" + lastModified.toString(fmt) + ".mp4";
            System.out.println("Create link from " + livePath + f.getName() + " to " + vodPath + linkFileName);
            try {
                Runtime.getRuntime().exec("/bin/ln -s " + livePath + f.getName() + " " + vodPath + linkFileName);
            } catch (IOException e) {
                System.out.println("Failed to create link. Reason: " + e.getMessage());
            }
        }
    }
}
