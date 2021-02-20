package com.kagof.renamer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public class Main {
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_RESET = "\u001B[0m";

    private static final String INPUT = "input";
    private static final String PREVIOUS_PREFIX = "previousPrefix";
    private static final String NEW_PREFIX = "newPrefix";
    private static final String DRY_RUN = "dryRun";
    private static final String VERBOSE = "verbose";

    public static void main(String[] args) throws IOException {
        Options options = new Options()
                .addRequiredOption("i", INPUT, true, "directory to run in")
                .addRequiredOption("p", PREVIOUS_PREFIX, true, "prefix to be replaced")
                .addRequiredOption("n", NEW_PREFIX, true, "prefix to replace with")
                .addOption("d", DRY_RUN, false, "do not actually perform rename")
                .addOption("V", VERBOSE, false, "verbose output")
                .addOption("v", "version", false, "prints the version")
                .addOption("h", "help", false, "prints the usage guide")
                .addOption("u", "usage", false, "prints the usage guide");

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        if (hasOpt(args, options.getOption("version"))) {
            System.out.println(getVersion());
            return;
        }
        if (hasOpt(args, options.getOption("help")) || hasOpt(args, options.getOption("usage"))) {
            printHelp(options, formatter);
            return;
        }
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (Exception e) {
            System.out.println(colorize(e.getMessage(), ANSI_RED));
            printHelp(options, formatter);
            System.exit(1);
            return;
        }

        final String dirname = cmd.getOptionValue(INPUT);
        String oldPrefix = cmd.getOptionValue(PREVIOUS_PREFIX);
        String newPrefix = cmd.getOptionValue(NEW_PREFIX);
        final boolean dryRun = cmd.hasOption(DRY_RUN);
        final boolean verbose = cmd.hasOption(VERBOSE);

        if (oldPrefix.equals(newPrefix)) {
            System.out.println(colorize("newPrefix must be distinct from oldPrefix", ANSI_RED));
            System.exit(1);
            return;
        }

        if (verbose && dryRun) {
            System.out.println("running in dryRun mode");
        } else if (dryRun) {
            System.out.println(
                colorize("Running in dryRun mode - note that this is not very useful without verbose mode",
                    ANSI_YELLOW));
        }

        final File dir = new File(dirname);
        if (!dir.exists()) {
            System.out.println(colorize(dirname + " directory not found", ANSI_RED));
            System.exit(1);
            return;
        }
        if (!dir.isDirectory()) {
            System.out.println(colorize(dirname + " is not a directory", ANSI_RED));
            System.exit(1);
            return;
        }

        File[] files = Objects.requireNonNull(dir.listFiles((__, n) -> n.startsWith(oldPrefix)));
        int longest = -1;
        if (verbose) {
            System.out.println("found " + files.length + " matching files");
            longest = Arrays.stream(files).map(f -> f.getName().length()).max(Comparator.naturalOrder()).orElse(-1);
        }
        for (File file : files) {
            String oldName = file.getName();
            String newName = newPrefix + oldName.substring(oldPrefix.length());
            if (verbose) {
                System.out.printf("'%s'%" + (longest - oldName.length() + 1) + "s >  '%s'", oldName, "", newName);
            }
            if (dryRun) {
                if (verbose) {
                    System.out.println();
                }
            } else {
                Files.move(file.toPath(), file.toPath().resolveSibling(newName));
                printIfVerbose(verbose);
            }
        }
        if (verbose) {
            if (dryRun) {
                System.out.println("run without flag -d/--dryMode to rename " + files.length + " files");
            } else {
                System.out.println(colorize("renamed " + files.length + " files", ANSI_GREEN));
            }
        }
    }

    private static void printIfVerbose(boolean verbose) {
        if (verbose) {
            System.out.println(colorize(" done", ANSI_GREEN));
        }
    }

    private static String getVersion() {
        return Main.class.getPackage().getImplementationVersion();
    }

    private static void printHelp(Options options, HelpFormatter formatter) {
        formatter.printHelp("renamer <options>", options);
    }

    private static String colorize(final String toColor, final String ansiColor) {
        return ansiColor + toColor + ANSI_RESET;
    }

    private static boolean hasOpt(final String[] args, final Option option) {
        return Arrays.stream(args).anyMatch(a -> a.equals("-" + option.getOpt()) || a.equals("--" + option.getLongOpt()));
    }
}
