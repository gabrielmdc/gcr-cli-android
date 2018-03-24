package gcr.cli.android;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import gcr.cli.android.models.IServer;
import gcr.cli.android.models.realm.Server;
import gcr.cli.android.validatiors.ServerValidator;
import gcr.cli.android.validatiors.errorkeys.ServerErrorKeys;

import static org.junit.Assert.assertEquals;

/**
 * Server unit test
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class ServerUnitTest {

    @Test
    public void all() throws Exception {
        ServerValidator validator = new ServerValidator();
        IServer server = new Server(1, "server name", "0.0.0.0", 0);
        List<ServerErrorKeys> errors = validator.validate(server);
        assertEquals(0, errors.size());

        IServer server2 = new Server(0, "", null, -1);
        List<ServerErrorKeys> errors2 = validator.validate(server2);
        assertEquals(4, errors2.size());
    }
}