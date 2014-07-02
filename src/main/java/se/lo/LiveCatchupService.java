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
        DateTimeFormatter fmt = DateTimeFormat.forPattern("YYYYMMdd_HHmmss");
        int linkedFiles = 0;
        for (File f: liveFilesCollection) {
            DateTime lastModified = new DateTime(f.lastModified());
            String linkFileName = "LIVE_" + lastModified.toString(fmt) + ".mp4";
            File linkFile = new File(vodPath + linkFileName);
            if (!linkFile.exists()) {
                System.out.println("[" + DateTime.now() + "]: Creating link from " + livePath + f.getName() + " to " + vodPath + linkFileName);
                try {
                    Process p = Runtime.getRuntime().exec("/bin/ln -s " + livePath + f.getName() + " " + vodPath + linkFileName);
                    p.waitFor();
                    int exitValue = p.exitValue();
                    if (exitValue != 0) {
                        System.out.println("Link command exited with a non-zero exit code: " + exitValue);
                    }
                    linkedFiles++;
                } catch (IOException e) {
                    System.out.println("Failed to create link. Reason: " + e.getMessage());
                } catch (InterruptedException e) {
                    System.out.println("Link command was interrupted: " + e.getMessage());
                }
            }
        }
        System.out.println("[" + DateTime.now() + "]: Linked " + linkedFiles + " files");
    }
}
