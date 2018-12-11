# gsubdl
Command line tool to download subtitles from [OpenSubtitles.org]

## Usage
    usage: gsubdl [options] [args]
      -d, --destination <location>   unzip file destination. Default: script location
      -h, --help                     usage information
      -l, --language <arg>           subtitle's languages (separated by comma). Default: eng
      -q, --query <text>             fulltext search by text

### Installing Groovy
1. Access [Groovy's download site] and download the latest version
2. Unzip the downloaded zip to a folder of your choosing
3. Add the Groovy executable to your System Enviroment Variables

    #### Windows
    Go to System Properties and then in System Variables:

    a. Add a GROOVY_HOME variable and the value should be the folder where you unzip Groovy 

    b. Append the ;%GROOVY_HOME%\bin to your *Path* variable
    
    #### Linux
    a. Add a GROOVY_HOME variable and the value should be the folder where you unzip Groovy 
    
    b. On your .profile file, add this:
    
    ```
    GROOVY_HOME=/path/to/groovy/folder
    PATH=$PATH:$GROOVY_HOME/bin
    
    export PATH GROOVY_HOME
    ```

### Adding script to your path
To use the script as command-line you should add it to your PATH just as GROOBY_HOME above.

  #### Windows
  Go to System Properties and then in System Variables:

  a. Append the ;</path/to/script/folder> to your *Path* variable

  #### Linux
  a. On your .profile file, add this:
  ```
  PATH=$PATH:</path/to/script/folder>

  export PATH
  ```

  [OpenSubtitles.org]: <https://www.opensubtitles.org>
  [Groovy's download site]: <http://groovy-lang.org/download.html>
