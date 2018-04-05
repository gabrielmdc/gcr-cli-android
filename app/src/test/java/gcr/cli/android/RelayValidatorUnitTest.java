package gcr.cli.android;

import android.content.Context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import gcr.cli.android.validatiors.RelayValidator;
import gcr.cli.android.validatiors.errorkeys.RelayErrorKeys;

import static org.junit.Assert.assertEquals;

/**
 * Relay validator unit test
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class RelayValidatorUnitTest {

    private RelayErrorKeys error;
    @Mock
    private Context context;

    @Test
    public void id() throws Exception {
        RelayValidator validator = new RelayValidator(context);
        error = validator.validateId(1);
        assertEquals(null, error);
        error = validator.validateId(0);
        assertEquals(RelayErrorKeys.ID, error);
        error = validator.validateId(-1);
        assertEquals(RelayErrorKeys.ID, error);
    }

    @Test
    public void name() throws Exception {
        RelayValidator validator = new RelayValidator(context);
        error = validator.validateName("Relay name");
        assertEquals(null, error);
        error = validator.validateName("");
        assertEquals(RelayErrorKeys.NAME, error);
        error = validator.validateName(null);
        assertEquals(RelayErrorKeys.NAME, error);
    }

    @Test
    public void gpio() throws Exception {
        RelayValidator validator = new RelayValidator(context);
        error = validator.validateGpio(1);
        assertEquals(null, error);
        error = validator.validateGpio(27);
        assertEquals(null, error);

        error = validator.validateGpio(-1);
        assertEquals(RelayErrorKeys.GPIO, error);
        error = validator.validateGpio(28);
        assertEquals(RelayErrorKeys.GPIO, error);
        error = validator.validateGpio("1.2");
        assertEquals(RelayErrorKeys.GPIO, error);
    }
}