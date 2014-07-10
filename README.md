This is a simple script that can run on a Wowza streaming engine to
create links in VOD directory pointing to previously recorded live
sessions and automatically create SMIL files.

Developed for LO Play

To enable adaptive streaming such as:

    http://<wowza>:1935/vod/smil:LIVE_20140703_053926/playlist.m3u8
    http://<wowza>:1935/vod/smil:LIVE_20140703_053926/manifest.f4m

This program can be run as a cron job and executed like this:

    java -jar livecatchup-1.1-shadow.jar

Assumes that the live recorded files are located at /mnt/resource/livevod/ and
that the vod clips should be stored in /mnt/resource/vod/
