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

 -d,--twicPage <arg>            The landing page where TWIC downloads are available. 
                                This value defaults to https://theweekinchess.com/twic.
    --extractExtensions <arg>   The extensions to extract seperated by |.
                                This value defaults to 'pgn|cbv'. Within
                                TWIC zip files you will also find 'txt'
                                files. For each file extension you choose,
                                a corresponding folder under
                                <extBaseFolder> is created.
 -h,--help                      Displays this help.
 -x,--extBaseFolder <arg>       The folder used on your local file system
                                to extract TWIC resources to. This value
                                defaults to 
                                <user.home='...'>\Documents\ChessBase\Bases\TWIC\,
                                if the folder 
                                <user.home='...'>\Documents\ChessBase\ exists on
                                your system (ChessBase user). Otherwise,
                                <user.home='...'>\Downloads\ is used. Depending on
                                the file extensions you want to extract,
                                corresponding sub-folders (pgn, cbv, txt)
                                are created.
 -z,--zipFolder <arg>           The folder used on your local file system
                                to download zip files to (original
                                sources). This value defaults to
                                <user.home='...'>\Downloads\TWIC\zips\

```