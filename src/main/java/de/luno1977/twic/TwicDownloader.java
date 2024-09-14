package de.luno1977.twic;

import org.apache.commons.cli.*;
import org.jsoup.Jsoup;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;


public class TwicDownloader {

    private static final String helpLongOpt = "help";
    private static final String twicPageLongOpt = "twicPage";
    private static final String zipFolderLongOpt = "zipFolder";
    private static final String extractionBaseFolderLongOpt = "extBaseFolder";
    private static final String extractExtensionsLongOpt = "extractExtensions";

    private static final char fs = File.separatorChar;
    private static final String lSep = System.lineSeparator();
    private static final Path userHome = Paths.get(System.getProperty("user.home"));
    private static final Path defaultDocumentsPath = Paths.get(
            FileSystemView.getFileSystemView().getDefaultDirectory().getPath());

    private static final Path downloadBaseFolder = userHome
            .resolve("Downloads")
            .resolve("TWIC");

    private static final Path cbDefaultPath = defaultDocumentsPath
            .resolve("ChessBase");

    private String twicBasePage = "https://theweekinchess.com/twic";
    private Path zipFolder = downloadBaseFolder
            .resolve("zips");
    private Path extractionBaseFolder = cbDefaultPath
            .resolve("Bases")
            .resolve("TWIC");
    private String[] extractExtensions = new String[] { "pgn", "cbv" };

    public static void main(String[] args) throws Exception {
        // create Options object
        Options options = new Options();

        // add t option
        options.addOption("h", "help", false, "Displays this help.");

        options.addOption("d", twicPageLongOpt, true,
                "The landing page where TWIC downloads are available. " +
                        "As default, 'https://theweekinchess.com/twic' is used.");

        options.addOption("z", zipFolderLongOpt, true,
                "The folder used on your local file system to download zip files to (original sources). " +
                        "As default, '" + userHome+ fs + "Downloads" + fs + "TWIC" + fs + "zips" + fs + "' is used");

        options.addOption("x", extractionBaseFolderLongOpt, true,
                          "The folder used on your local file system to extract TWIC resources to. " +
                                  "This value is set to '" + defaultDocumentsPath + fs +
                                  "ChessBase" + fs + "Bases" + fs + "TWIC" + fs + "', if the folder " +
                                  "exists on your system (ChessBase user). Otherwise, '" + userHome +
                                  fs + "Downloads" + fs + "' is used. Depending on the file extensions you want to " +
                                  "extract, corresponding sub-folders (pgn, cbv, txt) are created automatically.");

        options.addOption(null, extractExtensionsLongOpt, true,
                "The extensions to extract seperated by |. As default, 'pgn|cbv' is used. " +
                        "Within TWIC zip files, you will also find 'txt' files. For each file extension you " +
                        "choose, a corresponding folder under <"+extractionBaseFolderLongOpt+"> is created.");

        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            TwicDownloader downloader = new TwicDownloader();

            if (cmd.hasOption(twicPageLongOpt)) {
                downloader.twicBasePage = cmd.getOptionValue(twicPageLongOpt);
            }

            if (cmd.hasOption(zipFolderLongOpt)) {
                downloader.zipFolder = Paths.get(cmd.getOptionValue(zipFolderLongOpt));
            }

            if (cmd.hasOption(extractionBaseFolderLongOpt)) {
                downloader.extractionBaseFolder = Paths.get(cmd.getOptionValue(extractionBaseFolderLongOpt));
            } else {
                //if default ChessBase documents path is not found, fall-back to download base path
                if (! Files.exists(cbDefaultPath) ) {
                    downloader.extractionBaseFolder = downloadBaseFolder;
                }
            }

            if (cmd.hasOption(extractExtensionsLongOpt)) {
                downloader.extractExtensions = cmd.getOptionValue(extractExtensionsLongOpt).split("\\|");
            }

            if (cmd.hasOption(helpLongOpt)) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.setWidth(120);
                formatter.setNewLine(lSep);
                formatter.printHelp("./TWICDownloader [Options]... \n" +
                        "  (or: java -jar TWICDownloader.jar [Options]...)\n\n" +
                        "  Downloads TheWeekInChess magazine publications to your local system.\n" +
                        "  Updates your local system based on files found in <"+zipFolderLongOpt+">.\n" +
                        "  After downloading, all extracted pgn files are joined to\n" +
                        "  '<extractBaseFolder>" + fs + "twic_until_<yyyy-MM-dd>.pgn'\n\n", options);
            } else {
                downloader.downloadTwic();
                downloader.joinPGNFiles();
            }
        } catch (ParseException pEx) {
            System.out.println("Could not parse command line: " + pEx.getMessage());
        }
    }

    public void downloadTwic() throws Exception {
        String html = Jsoup.connect(twicBasePage).get().html();

        Matcher m = Pattern.compile("\"(.*\\.zip)\"").matcher(html);
        while (m.find()) {
            String downloadUrl = m.group(1);
            String[] parts = downloadUrl.split("/");
            String filename = parts[parts.length-1];

            System.out.println("Zip: " + downloadUrl + " : " + filename);

            if (!Files.exists(zipFolder)) {
                Files.createDirectories(zipFolder);
            }

            Path targetZipFilePath = zipFolder.resolve(filename);
            if (!Files.exists(targetZipFilePath)) {
                try(FileOutputStream fOut = new FileOutputStream(targetZipFilePath.toString())) {
                     fOut.getChannel().transferFrom(Channels.newChannel(
                             new URL(downloadUrl).openStream()), 0, Long.MAX_VALUE);
                }

            }

            //check and create folders
            List<Path> extensionTargetFolders = new ArrayList<>();

            for (String extractExtension : extractExtensions) {
                Path targetFolder = extractionBaseFolder.resolve(extractExtension);
                if (!Files.exists(targetFolder)) {
                    Files.createDirectories(targetFolder);
                }
                extensionTargetFolders.add(targetFolder);
            }

            try (ZipFile zipFile = new ZipFile(targetZipFilePath.toString())) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();

                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    System.out.println("\t" + entry.getName());

                    boolean copied = false;
                    for (int i = 0; i < extractExtensions.length; i++) {
                        String extractExtension = extractExtensions[i];
                        Path targetFolder = extensionTargetFolders.get(i);
                        if (entry.getName().endsWith(extractExtension)) {
                            Files.copy(
                                    zipFile.getInputStream(entry),
                                    targetFolder.resolve(entry.getName()),
                                    StandardCopyOption.REPLACE_EXISTING);
                            copied = true;
                        }
                    }

                    if (!copied) {
                        System.out.println("\tnot extracted: " + entry.getName());
                    }
                }
            } catch (ZipException zip) {
                Files.delete(targetZipFilePath);
            }
        }
    }

    public void joinPGNFiles() throws Exception {
        if (Arrays.asList(extractExtensions).contains("pgn")) {
            Path pgnTargetFolder = extractionBaseFolder.resolve("pgn");
            String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            Path targetAllPgnFile = Files.createFile(extractionBaseFolder.resolve("twic_until_"+dateStr+".pgn"));

            try ( Stream<Path> pgnFiles = Files.list(pgnTargetFolder);
                  FileOutputStream allPgnOut = new FileOutputStream(targetAllPgnFile.toFile(), true) ) {

                FileChannel allPgnCh = allPgnOut.getChannel();
                pgnFiles.forEach(pgnFile -> {
                    try (FileInputStream pgnIn = new FileInputStream(pgnFile.toFile())) {
                        FileChannel pgnInCh = pgnIn.getChannel();
                        allPgnCh.transferFrom(pgnInCh, allPgnCh.size(), pgnInCh.size());
                    } catch (FileNotFoundException e) {
                        System.err.println("The pgn file (" + pgnFile + ") could not be found and is not added.");
                    } catch (IOException e) {
                        System.err.println("The pgn file (" + pgnFile + ") could not be read and is not added. "
                                + e.getMessage());
                    }
                });
            }
        }
    }
}
