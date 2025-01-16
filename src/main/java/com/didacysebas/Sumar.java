package com.didacysebas;


import picocli.CommandLine;

@CommandLine.Command(
        name = "sumar",
        mixinStandardHelpOptions = true,
        description = "Sumar dos valores."
)
public class Sumar implements Runnable {

    @CommandLine.Parameters(index="0", description = "primer valor a sumar")
    private int num1;
    @CommandLine.Parameters(index="1",description = "Segundo valor a sumar")
    private int num2;

    @Override
    public void run() {
        System.out.printf("%d + %d = %d%n", num1, num2, num1 + num2);
    }

}
