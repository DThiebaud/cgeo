package cgeo.geocaching.connector.trackable;

import static org.assertj.core.api.Assertions.assertThat;

import cgeo.geocaching.CgeoApplication;
import cgeo.geocaching.Trackable;
import cgeo.geocaching.test.AbstractResourceInstrumentationTestCase;
import cgeo.geocaching.test.R;

import org.xml.sax.InputSource;

import java.util.List;

public class GeokretyParserTest extends AbstractResourceInstrumentationTestCase {

    public void testParse() throws Exception {
        final CgeoApplication app = CgeoApplication.getInstance();

        final List<Trackable> trackables = GeokretyParser.parse(new InputSource(getResourceStream(R.raw.geokret141_xml)));
        assertThat(trackables).hasSize(2);

        // Check first GK in list
        final Trackable trackable1 = trackables.get(0);
        assertThat(trackable1).isNotNull();
        assertThat(trackable1.getName()).isEqualTo("c:geo One");
        assertThat(trackable1.getGeocode()).isEqualTo("GKB580");
        assertThat(trackable1.getDistance()).isEqualTo(0);
        assertThat(trackable1.getType()).isEqualTo(app.getString(cgeo.geocaching.R.string.geokret_type_traditional));

        // Check second GK in list
        final Trackable trackable2 = trackables.get(1);
        assertThat(trackable2).isNotNull();
        assertThat(trackable2.getName()).isEqualTo("c:geo Two");
        assertThat(trackable2.getGeocode()).isEqualTo("GKB581");
        assertThat(trackable2.getDistance()).isEqualTo(0);
        assertThat(trackable2.getType()).isEqualTo(app.getString(cgeo.geocaching.R.string.geokret_type_post));
    }
}
