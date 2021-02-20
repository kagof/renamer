# renamer

Java command line tool to rename prefixes of multiple files.

## Building

```shell
mvn package
```

## Usage

```shell
usage: renamer <options>
-d,--dryRun                 do not actually perform rename
-h,--help                   prints the usage guide
-i,--input <arg>            directory to run in
-n,--newPrefix <arg>        prefix to replace with
-p,--previousPrefix <arg>   prefix to be replaced
-u,--usage                  prints the usage guide
-V,--verbose                verbose output
-v,--version                prints the version
```

`-n`, `-p`, and `-i` options are required.
