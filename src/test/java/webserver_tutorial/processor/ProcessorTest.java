package processor;

import connector.Request;
import org.junit.Assert;
import org.junit.Test;
import util.TestUtils;

import javax.servlet.Servlet;
import java.net.MalformedURLException;
import java.net.URLClassLoader;

public class ProcessorTest {

    private static final String servletRequest = "GET /servlet/TimeServlet HTTP/1.1";

    @Test
    public void givenServletRequest_thenLoadServlet() throws MalformedURLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        Request request = TestUtils.createRequest(servletRequest);
        ServletProcessor processor = new ServletProcessor();
        URLClassLoader loader = processor.getServletLoader();
        Servlet servlet = processor.getServlet(loader, request);

        Assert.assertEquals("TimeServlet", servlet.getClass().getName());
    }
}
