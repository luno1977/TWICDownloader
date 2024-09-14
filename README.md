# TWICDownloader

Command line tool to download The Week In Chess (TWIC - https://theweekinchess.com/twic) zip files 
and extract contents. Keeps you local system up-to-date if you run it via 
[cron job](https://en.wikipedia.org/wiki/Cron) or 
[windows task scheduler](https://en.wikipedia.org/wiki/Windows_Task_Scheduler).

# Build

Just install [maven](https://maven.apache.org/) to your local system and run
`mvn package` to create the jar and exe files locally. 

Just run TWICDownloader `target/TWICDownloader.exe` for basic usage. 

Incomplete downloads are completed when running the tool again.

# Usage

```
usage: ./TWICDownloader [Options]...
  (or: java -jar TWICDownloader.jar [Options]...)

  Downloads TheWeekInChess magazine publications to your local system.
  Updates your local system based on files found in <zipFolder>.
  After downloading, all extracted pgn files are joined to
  '<extractBaseFolder>\twic_until_<yyyy-MM-dd>.pgn'

 -d,--twicPage <arg>            The landing page where TWIC downloads are available. As default,
                                'https://theweekinchess.com/twic' is used.
    --extractExtensions <arg>   The extensions to extract seperated by |. As default, 'pgn|cbv' is used. Within TWIC zip
                                files, you will also find 'txt' files. For each file extension you choose, a
                                corresponding folder under <extBaseFolder> is created.
 -h,--help                      Displays this help.
 -x,--extBaseFolder <arg>       The folder used on your local file system to extract TWIC resources to. This value is
                                set to '<document-base-folder>\ChessBase\Bases\TWIC\', if the folder
                                exists on your system (ChessBase user). Otherwise, '<download-base>' is used. 
                                Depending on the file extensions you want to extract, corresponding sub-folders
                                (pgn, cbv, txt) are created automatically.
 -z,--zipFolder <arg>           The folder used on your local file system to download zip files to (original sources).
                                As default, '<download-base>\zips\' is used

<document-base-folder> is determined by:
Paths.get(FileSystemView.getFileSystemView().getDefaultDirectory().getPath());
# on Windows e.g. '<user_home>/Documents/' or '<OneDrive-Folder>/Documents'

<download-base> is determined by:
Paths.get(System.getProperty("user.home")).resolve("Downloads").resolve("TWIC");
# on windows e.g. <user_home>/Downloads/TWIC/

```