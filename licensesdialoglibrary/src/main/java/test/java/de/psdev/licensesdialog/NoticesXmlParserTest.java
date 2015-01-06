package test.java.de.psdev.licensesdialog;

import java.io.InputStream;

import main.java.de.psdev.licensesdialog.NoticesXmlParser;
import main.java.de.psdev.licensesdialog.model.Notices;


public class NoticesXmlParserTest {

    public void testParse() throws Exception {
        final InputStream noticesXmlStream = getClass().getResourceAsStream("notices.xml");
        final Notices parse = NoticesXmlParser.parse(noticesXmlStream);
    }
}
