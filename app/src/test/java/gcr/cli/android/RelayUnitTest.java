package gcr.cli.android;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import gcr.cli.android.models.IRelay;
import gcr.cli.android.models.Relay;
import gcr.cli.android.validatiors.IModelValidator;
import gcr.cli.android.validatiors.RelayModelValidator;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class RelayUnitTest {

    @Test
    public void id_isCorrect() throws Exception {
        IRelay relay = new Relay(1, "relay name", 2, false, false, false);
        IModelValidator<IRelay> relayValidator = new RelayModelValidator();
        String res = relayValidator.validate(relay);
        assertEquals(null, res);
    }
}