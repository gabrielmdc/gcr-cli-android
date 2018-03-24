package gcr.cli.android;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import gcr.cli.android.models.IRelay;
import gcr.cli.android.models.Relay;
import gcr.cli.android.validatiors.RelayValidator;
import gcr.cli.android.validatiors.errorkeys.RelayErrorKeys;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class RelayUnitTest {

    @Mock
    private RelayValidator validator;

    public RelayUnitTest() {
        validator = new RelayValidator();
    }

    @Test
    public void all() throws Exception {
        IRelay relay = new Relay(1, "relay name", 2, false, false, false);
        List<RelayErrorKeys> errors = validator.validate(relay);
        assertEquals(0, errors.size());

        IRelay relay2 = new Relay(0, "", 30, false, false, false);
        List<RelayErrorKeys> errors2 = validator.validate(relay2);
        assertEquals(3, errors.size());
    }
}