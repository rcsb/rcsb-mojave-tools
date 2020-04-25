package org.rcsb.mojave.tools.utils;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Created on 10/14/19.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class CommandOptions {

    private List<String> options = new ArrayList<>();
    private List<String> arguments = new ArrayList<>();

    public CommandOptions(String[] args) {
        parse(args);
    }

    private void parse(String[] args) {
        arguments.addAll(asList(args));
        arguments.stream().filter(o -> o.startsWith("-"))
                .forEach(o -> options.add(o));
    }

    public int size() {
        return arguments.size();
    }

    public boolean hasOption(String option) {
        boolean hasValue = false;
        for (String argument : arguments) {
            if (argument.equalsIgnoreCase(option)) {
                hasValue = true;
                break;
            }
        }
        return hasValue;
    }

    public List<String> valueOf(String option) {

        if (!hasOption(option))
            return new ArrayList<>();

        List<String> value = new ArrayList<>();
        String argument;
        for ( int i = 0; i < arguments.size(); i++ ) {
            argument = arguments.get(i);
            if (argument.equalsIgnoreCase(option)) {
                for (int j=i+1; j<arguments.size(); j++ ) {
                    if (options.contains(arguments.get(j)) && !option.equals(arguments.get(j)))
                        break;
                    value.add(arguments.get(j));
                }
                break;
            }
        }
        return value;
    }
}
