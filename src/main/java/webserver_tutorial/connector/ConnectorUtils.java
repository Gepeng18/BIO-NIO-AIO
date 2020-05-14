package webserver_tutorial.connector;

import java.io.File;

public class ConnectorUtils {

    public static final String WEB_ROOT =
            new ConnectorUtils().getClass().getResource("/").getPath() + File.separator + "webroot";

    public static final String PROTOCOL = "HTTP/1.1";

    public static final String CARRIAGE = "\r";

    public static final String NEWLINE = "\n";

    public static final String SPACE = " ";

    public static String renderStatus(HttpStatus status) {
        StringBuilder sb = new StringBuilder(PROTOCOL)
                .append(SPACE)
                .append(status.getStatusCode())
                .append(SPACE)
                .append(status.getReason())
                .append(CARRIAGE).append(NEWLINE)
                .append(CARRIAGE).append(NEWLINE);

        return sb.toString();
    }

}
