package com.didacysebas;


import com.didacysebas.commands.DecryptCommand;
import com.didacysebas.commands.ReplaceCommand;
import picocli.CommandLine;


/**
 * @author sebastian y dídac
 */
@CommandLine.Command(
        name = "keybreaker",
        description = "KeyBreaker: Desxifra i modifica missatges interceptats.",
        version = "1.0",
        mixinStandardHelpOptions = true,
        subcommands = {DecryptCommand.class, ReplaceCommand.class}
)
public class KeyBreaker implements Runnable {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new KeyBreaker()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        System.out.println("Use -h or --help for more information.");
    }
}
