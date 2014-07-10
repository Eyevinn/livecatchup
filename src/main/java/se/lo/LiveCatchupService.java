package se.lo;

/**
 * Copyright (c) 2014 jonas.birme@eyevinn.se
 *
 * This file is part of Live Catchup Service.
 *
 * Live Catchup Service is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Live Catchup Service is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Live Catchup Service.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class LiveCatchupService {
    private final static String LIVEPATH = "/mnt/resource/livevod/";
    private final static String VODPATH = "/mnt/resource/vod/";
    private class LiveFile {
        public File q720p;
        public File q360p;
        public File q160p;
        public DateTime lastModified;
    }
    private Map<String, LiveFile> liveFilesCollection = new HashMap<String, LiveFile>();
    private String livePath;
    private String vodPath;

    public static void main(String[] args) {
        LiveCatchupService catchupService = new LiveCatchupService(LIVEPATH, VODPATH);
        catchupService.createLinks();
        catchupService.createSMILFiles();
    }

    public LiveCatchupService(String pathToLiveFiles, String pathToVodFiles) {
        vodPath = pathToVodFiles;
        livePath = pathToLiveFiles;
        File liveFiles = new File(livePath);
        if (liveFiles.isDirectory()) {
            for (File f : liveFiles.listFiles()) {
                String id = getID(f.getName());
                if (id != null) {
                    LiveFile lf = liveFilesCollection.get(id);
                    if (lf == null) {
                        lf = new LiveFile();
                    }
                    if (f.getName().contains("HD_720p_")) {
                        lf.q720p = f;
                        lf.lastModified = new DateTime(f.lastModified());
                    } else if (f.getName().contains("HD_360p_")) {
                        lf.q360p = f;
                    } else if (f.getName().contains("HD_160p_")) {
                        lf.q160p = f;
                    }
                    liveFilesCollection.put(id, lf);
                }
            }
        }
    }

    private String getID(String filename) {
        Pattern p = Pattern.compile("HD_(.*p)_(\\d+).mp4");
        Matcher m = p.matcher(filename);
        if (m.matches()) {
            return m.group(2);
        }
        return null;
    }

    private int linkFile(File src, String destName) {
        // System.out.println("DEBUG Source file: " + src.getName());
        File linkFile = new File(vodPath + destName);
        if (!linkFile.exists()) {
            System.out.println("[" + DateTime.now() + "]: Creating link from " + livePath + src.getName() + " to " + vodPath + destName);

            try {
                Process p = Runtime.getRuntime().exec("/bin/ln -s " + livePath + src.getName() + " " + vodPath + destName);
                p.waitFor();
                int exitValue = p.exitValue();
                if (exitValue != 0) {
                    System.out.println("Link command exited with a non-zero exit code: " + exitValue);
                }
            } catch (IOException e) {
                System.out.println("Failed to create link. Reason: " + e.getMessage());
            } catch (InterruptedException e) {
                System.out.println("Link command was interrupted: " + e.getMessage());
            }
            return 1;
        }
        return 0;
    }

    private Document generateSMILFile(LiveFile f) {
        Document doc = null;
        DateTimeFormatter fmt = DateTimeFormat.forPattern("YYYYMMdd_HHmmss");
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.newDocument();
            Element rootElement = doc.createElement("smil");
            rootElement.setAttribute("title", f.lastModified.toString(fmt));
            doc.appendChild(rootElement);

            Element bodyElement = doc.createElement("body");
            rootElement.appendChild(bodyElement);

            Element switchElement = doc.createElement("switch");
            bodyElement.appendChild(switchElement);

            /* 720p */
            Element video720 = doc.createElement("video");
            switchElement.appendChild(video720);
            video720.setAttribute("height", "720");
            video720.setAttribute("src", "LIVE_" + f.lastModified.toString(fmt) + ".mp4");
            video720.setAttribute("width", "1280");
            Element video720param = doc.createElement("param");
            video720param.setAttribute("name", "videoBitrate");
            video720param.setAttribute("value", "1100000");
            video720param.setAttribute("valuetype", "data");
            video720.appendChild(video720param);
            Element audio720param = doc.createElement("param");
            audio720param.setAttribute("name", "audioBitrate");
            audio720param.setAttribute("value", "44100");
            audio720param.setAttribute("valuetype", "data");
            video720.appendChild(audio720param);

            /* 360p */
            Element video360 = doc.createElement("video");
            switchElement.appendChild(video360);
            video360.setAttribute("height", "360");
            video360.setAttribute("src", "LIVE_" + f.lastModified.toString(fmt) + "_360p.mp4");
            video360.setAttribute("width", "640");
            Element video360param = doc.createElement("param");
            video360param.setAttribute("name", "videoBitrate");
            video360param.setAttribute("value", "750000");
            video360param.setAttribute("valuetype", "data");
            video360.appendChild(video360param);
            Element audio360param = doc.createElement("param");
            audio360param.setAttribute("name", "audioBitrate");
            audio360param.setAttribute("value", "44100");
            audio360param.setAttribute("valuetype", "data");
            video360.appendChild(audio360param);

            /* 160p */
            Element video160 = doc.createElement("video");
            switchElement.appendChild(video160);
            video160.setAttribute("height", "160");
            video160.setAttribute("src", "LIVE_" + f.lastModified.toString(fmt) + "_160p.mp4");
            video160.setAttribute("width", "284");
            Element video160param = doc.createElement("param");
            video160param.setAttribute("name", "videoBitrate");
            video160param.setAttribute("value", "240000");
            video160param.setAttribute("valuetype", "data");
            video160.appendChild(video160param);
            Element audio160param = doc.createElement("param");
            audio160param.setAttribute("name", "audioBitrate");
            audio160param.setAttribute("value", "44100");
            audio160param.setAttribute("valuetype", "data");
            video160.appendChild(audio160param);


        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        }
        return doc;
    }

    public void createSMILFiles() {
        int smilFiles = 0;
        DateTimeFormatter fmt = DateTimeFormat.forPattern("YYYYMMdd_HHmmss");

        for (LiveFile f: liveFilesCollection.values()) {
            Document smilXMLDoc = generateSMILFile(f);
            if (smilXMLDoc != null) {
                try {
                    TransformerFactory factory = TransformerFactory.newInstance();
                    Transformer transformer = factory.newTransformer();
                    DOMSource source = new DOMSource(smilXMLDoc);
                    String smilFileName = "LIVE_" + f.lastModified.toString(fmt) + ".smil";
                    File smilFile = new File(vodPath + smilFileName);
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                    StreamResult result = new StreamResult(smilFile);
                    transformer.transform(source, result);
                    smilFiles++;
                } catch (TransformerException tfe) {
                    tfe.printStackTrace();
                }
            }
        }
        System.out.println("[" + DateTime.now() + "]: Generated " + smilFiles + " SMIL files");
    }

    public void createLinks() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("YYYYMMdd_HHmmss");
        int linkedFiles = 0;
        for (LiveFile f: liveFilesCollection.values()) {
            DateTime lastModified = f.lastModified;

            if (f.q720p != null) {
                linkedFiles += linkFile(f.q720p, "LIVE_" + lastModified.toString(fmt) + ".mp4");
            }
            if (f.q360p != null) {
                linkedFiles += linkFile(f.q360p, "LIVE_" + lastModified.toString(fmt) + "_360p.mp4");
            }
            if (f.q160p != null) {
                linkedFiles += linkFile(f.q160p, "LIVE_" + lastModified.toString(fmt) + "_160p.mp4");
            }
        }
        System.out.println("[" + DateTime.now() + "]: Linked " + linkedFiles + " files");
    }
}
