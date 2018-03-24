package gcr.cli.android;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import gcr.cli.android.models.IRelay;
import gcr.cli.android.models.Relay;
import gcr.cli.android.validatiors.IModelValidator;
import gcr.cli.android.validatiors.ModelValidator;
import gcr.cli.android.validatiors.RelayValidator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class RelayUnitTest {

    @Mock
    IRelay relay;

    @Test
    public void id_isCorrect() throws Exception {
        relay = new Relay(1, "relay name", 2, false, false, false);
        RelayValidator<IRelay> relayValidator = new RelayModelValidator();
        String res1 = relayValidator.validate(relay);
        assertEquals(null, res1);
        String res2 = ((RelayValidator)relayValidator).validateId(1);
        assertEquals(res1, res2);
        String res3 = ((RelayValidator)relayValidator).validateId(0);
        assertNotEquals(res1, res3);
        String res4 = ((RelayValidator)relayValidator).validateId(-1);
        assertNotEquals(res1, res4);
        assertEquals(res3, res4);
    }

    @Test
    public void name_isCorrect() throws Exception {
        relay = new Relay(1, "relay name", 2, false, false, false);
        IModelValidator<IRelay> relayValidator = new RelayValidator();
        String res1 = relayValidator.validate(relay);
        assertEquals(null, res1);
        String res2 = ((RelayValidator)relayValidator).validateName("relay name2");
        assertEquals(res1, res2);
        String res3 = ((RelayValidator)relayValidator).validateName("");
        assertNotEquals(res1, res3);
    }

    @Test
    public void gpio_isCorrect() throws Exception {
        relay = new Relay(1, "relay name", 2, false, false, false);
        IModelValidator<IRelay> relayValidator = new RelayValidator();
        String res1 = relayValidator.validate(relay);
        assertEquals(null, res1);
        String res2 = ((RelayValidator)relayValidator).validateGpio(1);
        assertEquals(res1, res2);
        String res3 = ((RelayValidator)relayValidator).validateGpio(0);
        assertNotEquals(res1, res3);
        String res4 = ((RelayValidator)relayValidator).validateGpio(28);
        assertNotEquals(res1, res4);
        String res5 = ((RelayValidator)relayValidator).validateGpio("-1");
        assertNotEquals(res1, res5);
        String res6 = ((RelayValidator)relayValidator).validateGpio("1.2");
        assertNotEquals(res1, res6);
        assertEquals(res3, res6);
    }
}