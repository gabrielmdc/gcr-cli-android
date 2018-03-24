package gcr.cli.android;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import gcr.cli.android.validatiors.ServerValidator;
import gcr.cli.android.validatiors.errorkeys.ServerErrorKeys;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class ServerValidatorUnitTest {

    @Mock
    private ServerValidator validator;
    private ServerErrorKeys error;

    public ServerValidatorUnitTest() {
        validator = new ServerValidator();
    }

    @Test
    public void id() throws Exception {
        error = validator.validateId(1);
        assertEquals(null, error);
        error = validator.validateId(0);
        assertNotEquals(error, ServerErrorKeys.ID);
        error = validator.validateId(-1);
        assertNotEquals(error, ServerErrorKeys.ID);
    }

    @Test
    public void name() throws Exception {
        error = validator.validateName("Server name");
        assertEquals(null, error);
        error = validator.validateName("");
        assertEquals(error, ServerErrorKeys.NAME);
        error = validator.validateName(null);
        assertNotEquals(error, ServerErrorKeys.NAME);
    }

    @Test
    public void address() throws Exception {
        error = validator.validateAddress("0.0.0.0");
        assertEquals(null, error);
        error = validator.validateAddress("address");
        assertEquals(null, error);

        error = validator.validateAddress("add ress");
        assertEquals(error, ServerErrorKeys.ADDRESS);
        error = validator.validateAddress("");
        assertEquals(error, ServerErrorKeys.ADDRESS);
        error = validator.validateAddress(null);
        assertEquals(error, ServerErrorKeys.ADDRESS);
    }

    @Test
    public void socketPort() throws Exception {
        error = validator.validateSocketPort(0);
        assertEquals(null, error);
        error = validator.validateSocketPort(65535);
        assertEquals(null, error);

        error = validator.validateSocketPort(-1);
        assertEquals(error, ServerErrorKeys.SOCKET_PORT);
        error = validator.validateSocketPort(65536);
        assertEquals(error, ServerErrorKeys.SOCKET_PORT);
    }
}