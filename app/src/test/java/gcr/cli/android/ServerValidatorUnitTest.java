package gcr.cli.android;

import android.content.Context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import gcr.cli.android.validatiors.ServerValidator;
import gcr.cli.android.validatiors.errorkeys.ServerErrorKeys;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Server validator unit test
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class ServerValidatorUnitTest {

    private ServerErrorKeys error;
    @Mock
    private Context context;

    @Test
    public void id() throws Exception {
        ServerValidator validator = new ServerValidator(context);
        error = validator.validateId(1);
        assertEquals(null, error);
        error = validator.validateId(0);
        assertEquals(ServerErrorKeys.ID, error);
        error = validator.validateId(-1);
        assertEquals(ServerErrorKeys.ID, error);
    }

    @Test
    public void name() throws Exception {
        ServerValidator validator = new ServerValidator(context);
        error = validator.validateName("Server name");
        assertEquals(null, error);
        error = validator.validateName("");
        assertEquals(ServerErrorKeys.NAME, error);
        error = validator.validateName(null);
        assertEquals(ServerErrorKeys.NAME, error);
    }

    @Test
    public void address() throws Exception {
        ServerValidator validator = new ServerValidator(context);
        error = validator.validateAddress("0.0.0.0");
        assertEquals(null, error);
        error = validator.validateAddress("address");
        assertEquals(null, error);

        error = validator.validateAddress("add ress");
        assertEquals(ServerErrorKeys.ADDRESS, error);
        error = validator.validateAddress("");
        assertEquals(ServerErrorKeys.ADDRESS, error);
        error = validator.validateAddress(null);
        assertEquals(ServerErrorKeys.ADDRESS, error);
    }

    @Test
    public void socketPort() throws Exception {
        ServerValidator validator = new ServerValidator(context);
        error = validator.validateSocketPort(0);
        assertEquals(null, error);
        error = validator.validateSocketPort(65535);
        assertEquals(null, error);

        error = validator.validateSocketPort(-1);
        assertEquals(ServerErrorKeys.SOCKET_PORT, error);
        error = validator.validateSocketPort(65536);
        assertEquals(ServerErrorKeys.SOCKET_PORT, error);
    }
}