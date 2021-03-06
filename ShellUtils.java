package com.macilias.tools;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * A wrapper to provide native access to cmd shell.
 *
 * @author - macilias@gmail.com
 */
public class ShellUtils {

    private static final Logger LOG = Logger.getLogger(ShellUtils.class);

    /**
     * This is a ultimate wrapper fro command line calls.
     * At its heart it uses Apache Commons Exec for not blocking calls and multi threading support.
     * Additionally pipe calls are supported.
     * Its dead simple to use.
     * Standard error is part of output, to enable calls like: java -version for example.
     *
     * @param command The command you would otherwise run in your command line.
     * @param dir     Optional directory where the command will get executed. If empty, current dir will be used.
     * @return Stdout + Stderr
     * @throws IOException
     */
    public static String runCommand(String command, Optional<File> dir) throws IOException {
        return runCommand(command, dir, false);
    }

    /**
     * This is a ultimate wrapper fro command line calls.
     * At its heart it uses Apache Commons Exec for not blocking calls and multi threading support.
     * Additionally pipe calls are supported.
     * Its dead simple to use.
     * Standard error is part of output, to enable calls like: java -version for example.
     *
     * @param command The command you would otherwise run in your command line.
     * @param dir     Optional directory where the command will get executed. If empty, current dir will be used.
     * @param stderr  Should Stderr be also a part of the output String or not.
     * @return Stdout + Stderr (depending on stderr boolean value)
     * @throws IOException
     */
    public static String runCommand(String command, Optional<File> dir, boolean stderr) throws IOException {
        LOG.debug(String.format("runCommand(): %s %s", command, dir.isPresent() ? "in dir: " + dir.get().getAbsolutePath() : ""));
        String[] commands = command.split("\\|");
        ByteArrayOutputStream output = null;
        for (String cmd : commands) {
            output = runSubCommand(output != null ? new ByteArrayInputStream(output.toByteArray()) : null, cmd.trim(), dir, stderr);
        }
        return output != null ? output.toString() : null;
    }

    private static ByteArrayOutputStream runSubCommand(ByteArrayInputStream input, String command, Optional<File> dir, boolean stderr) throws IOException {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        CommandLine cmd = CommandLine.parse(command);
        DefaultExecutor exec = new DefaultExecutor();
        if (dir.isPresent()) {
            exec.setWorkingDirectory(dir.get());
        }
        PumpStreamHandler streamHandler = new PumpStreamHandler(output, stderr ? output : null, input);
        exec.setStreamHandler(streamHandler);
        Map<String,String> environment = new HashMap<String, String>() {{
            put("HOME", "/services/home/" + System.getProperty("user.name"));
        }};
        int status = exec.execute(cmd, environment);
        if (status != 0) {
            throw new RuntimeException("The command: " + command + " could not be executed without error. The status is: " + status + ". Optional output is: " + output);
        }
        return output;
    }

}
