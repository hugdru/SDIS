package pinypon.cli.parser;

import java.util.HashMap;

public class Parser {

    static final private String PORT_LONG = "--port";
    static final private String PORT_SHORT = "-p";

    final private String []args;
    private HashMap<Option, Object> parsed = new HashMap<>();

    public enum Option {
        PORT
    }

    public Parser(String []args) {
        this.args = args;
    }

    public HashMap<Option, Object> parse() {
        int args_length = this.args.length;
        for (int index = 0; index < args_length; ++index) {
            switch(this.args[index]) {
                case PORT_LONG:
                case PORT_SHORT:
                    if ((index = index + 1) >= args_length) {
                        throw new IllegalArgumentException(args[index - 1] + " port");
                    }
                    int port = Integer.parseInt(args[index]);
                    if (port < 1 || port > 65535) {
                        throw new IllegalArgumentException(args[index - 1] + " 1-65535");
                    }
                    this.parsed.put(Option.PORT, port);
                    break;
                default:
                    throw new IllegalStateException(args[index] + " invalid option");
            }
        }
        return this.parsed;
    }
}