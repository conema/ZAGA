package com.bupsolutions.polaritydetection;

import com.martiansoftware.jsap.*;

public class ArgumentsHandler {

    public static final String TEST_LONG = "test";
    public static final char TEST_SHORT = 't';

    public static final String HELP_LONG = "help";
    public static final char HELP_SHORT = 'h';

    private JSAP jsap;
    private JSAPResult result;


    public ArgumentsHandler(String[] args) throws JSAPException {
        defineArgs();
        result = parseArgs(args);
    }

    private void defineArgs() throws JSAPException {
        this.jsap = new JSAP();
        Switch flag;

        flag = new Switch(TEST_LONG);
        flag.setLongFlag(TEST_LONG);
        flag.setShortFlag(TEST_SHORT);
        flag.setDefault("false");
        flag.setHelp("Show accuracy measured by holdout validation.");
        jsap.registerParameter(flag);

        flag = new Switch(HELP_LONG);
        flag.setLongFlag(HELP_LONG);
        flag.setShortFlag(HELP_SHORT);
        flag.setDefault("false");
        flag.setHelp("Print this help message.");
        jsap.registerParameter(flag);

    }

    public JSAPResult parseArgs(String[] args) throws JSAPException {
        defineArgs();

        if (args == null) {
            args = new String[0];
        }

        JSAPResult arguments = jsap.parse(args);

        if (arguments.getBoolean(HELP_LONG)) {
            printHelp();
            System.exit(0);
        }

        if (!arguments.success()) {
            // print out specific error messages describing the problems
            java.util.Iterator<?> errs = arguments.getErrorMessageIterator();
            while (errs.hasNext()) {
                System.err.println("Error: " + errs.next());
            }

            printHelp();
            System.exit(-1);
        }


        return arguments;
    }

    private void printHelp() {
        printUsage();
        System.err.println("Options:");
        System.err.println();
        System.err.println(jsap.getHelp());
    }

    private void printUsage() {
        System.err.println("Usage:");
        System.err.println("./polarityDetection [OPTIONS]");
        System.err.println();
    }

    public boolean holdout() {
        return result.getBoolean(TEST_LONG);
    }
}
