package gcr.cli.android;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import gcr.cli.android.validatiors.RelayValidator;
import gcr.cli.android.validatiors.errorkeys.RelayErrorKeys;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class RelayValidatorUnitTest {

    @Mock
    private RelayValidator validator;
    @Mock
    private RelayErrorKeys error;

    public RelayValidatorUnitTest() {
        validator = new RelayValidator();
    }

    @Test
    public void id() throws Exception {
        error = validator.validateId(1);
        assertEquals(null, error);
        error = validator.validateId(0);
        assertNotEquals(error, RelayErrorKeys.ID);
        error = validator.validateId(-1);
        assertNotEquals(error, RelayErrorKeys.ID);
    }

    @Test
    public void name() throws Exception {
        error = validator.validateName("Relay name");
        assertEquals(null, error);
        error = validator.validateName("");
        assertEquals(error, RelayErrorKeys.NAME);
        error = validator.validateName(null);
        assertNotEquals(error, RelayErrorKeys.NAME);
    }

    @Test
    public void gpio() throws Exception {
        error = validator.validateGpio(1);
        assertEquals(null, error);
        error = validator.validateGpio(27);
        assertEquals(null, error);

        error = validator.validateGpio(-1);
        assertEquals(error, RelayErrorKeys.GPIO);
        error = validator.validateGpio(28);
        assertEquals(error, RelayErrorKeys.GPIO);
        error = validator.validateGpio("1.2");
        assertEquals(error, RelayErrorKeys.GPIO);
    }
}