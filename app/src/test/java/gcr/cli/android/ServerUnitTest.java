package gcr.cli.android;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import gcr.cli.android.models.IServerModel;
import gcr.cli.android.models.realm.Server;
import gcr.cli.android.validatiors.IModelValidator;
import gcr.cli.android.validatiors.ServerValidator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class ServerUnitTest {

    @Mock
    IServerModel server;
    IModelValidator<IServerModel> validator;

    ServerUnitTest() {
        validator = new ServerValidator();
    }

    @Test
    public void id_isCorrect() throws Exception {
        server = new Server(1, "server name", "10.0.0.3", 10001);
        //validator = new ServerModelValidator();
        String res1 = validator.validate(server);
        assertEquals(null, res1);
        String res2 = ((ServerValidator)validator).validateId(1);
        assertEquals(res1, res2);
        String res3 = ((ServerValidator)validator).validateId(0);
        assertNotEquals(res1, res3);
        String res4 = ((ServerValidator)validator).validateId(-1);
        assertNotEquals(res1, res4);
        assertEquals(res3, res4);
    }

    @Test
    public void name_isCorrect() throws Exception {
        server = new Server(1, "server name", "10.0.0.3", 10001);
        String res1 = validator.validate(server);
        assertEquals(null, res1);
        String res2 = ((ServerValidator)validator).validateName("server name2");
        assertEquals(res1, res2);
        String res3 = ((ServerValidator)validator).validateName("");
        assertNotEquals(res1, res3);
    }

    @Test
    public void address_isCorrect() throws Exception {
        server = new Server(1, "server name", "10.0.0.3", 10001);
        String res1 = validator.validate(server);
        assertEquals(null, res1);
        String res2 = ((ServerValidator)validator).validateAddress("relay name2");
        assertEquals(res1, res2);
        String res3 = ((ServerValidator)validator).validateAddress("");
        assertNotEquals(res1, res3);
    }

//    @Test
//    public void socketPort_isCorrect() throws Exception {
//        server = new Server(1, "server name", "10.0.0.3", 10001);
//        String res1 = relayValidator.validate(relay);
//        assertEquals(null, res1);
//        String res2 = ((RelayModelValidator)relayValidator).validateGpio(1);
//        assertEquals(res1, res2);
//        String res3 = ((RelayModelValidator)relayValidator).validateGpio(0);
//        assertNotEquals(res1, res3);
//        String res4 = ((RelayModelValidator)relayValidator).validateGpio(28);
//        assertNotEquals(res1, res4);
//        String res5 = ((RelayModelValidator)relayValidator).validateGpio("-1");
//        assertNotEquals(res1, res5);
//        String res6 = ((RelayModelValidator)relayValidator).validateGpio("1.2");
//        assertNotEquals(res1, res6);
//        assertEquals(res3, res6);
//    }
}