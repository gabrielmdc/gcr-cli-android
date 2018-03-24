package gcr.cli.android.validatiors;

import java.util.ArrayList;
import java.util.List;

import gcr.cli.android.models.IServerModel;
import gcr.cli.android.validatiors.errorkeys.ServerErrorKeys;

public class ServerValidator extends ModelValidator<IServerModel, ServerErrorKeys> {

    public ServerErrorKeys validateId(int id) {
        final String regex = "^[1-9][0-9]*$";
        final String idStr = id + "";
        return validateData(regex, ServerErrorKeys.ID, idStr);
    }

    public ServerErrorKeys validateName(String name) {
        final String regex = ".+";
        return validateData(regex, ServerErrorKeys.NAME, name);
    }

    public ServerErrorKeys validateAddress(String address) {
        // Hostname specification: [RFC 1123] http://tools.ietf.org/html/rfc1123
        final String hostnameRegex = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$";
        final String ipAddressRegex = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
        if(validateData(hostnameRegex, ServerErrorKeys.ADDRESS, address) != null) {
            return validateData(ipAddressRegex, ServerErrorKeys.ADDRESS, address);
        }
        return null;
    }

    public ServerErrorKeys validateSocketPort(int socketPort) {
        final String regex = "^(6553[0-5]|655[0-2][0-9]|65[0-4][0-9]{2}|6[0-4][0-9]{3}|[1-5][0-9]{4}|[0-9]{1,4})$|^(6553[0-5]|655[0-2][0-9]|65[0-4][0-9]{2}|6[0-4][0-9]{3}|[1-5][0-9]{4}|[0-9]{1,4})$";
        final String socketPortStr = socketPort + "";
        return validateData(regex, ServerErrorKeys.SOCKET_PORT, socketPortStr);
    }

    @Override
    public List<ServerErrorKeys> validate(IServerModel model) {
        List<ServerErrorKeys> errorKeys = new ArrayList<>();

        ServerErrorKeys errorKey = validateId(model.getId());
        if(errorKey != null) {
            errorKeys.add(errorKey);
        }
        errorKey = validateName(model.getName());
        if(errorKey != null) {
            errorKeys.add(errorKey);
        }
        errorKey = validateAddress(model.getAddress());
        if(errorKey != null) {
            errorKeys.add(errorKey);
        }
        errorKey = validateSocketPort(model.getSocketPort());
        if(errorKey != null) {
            errorKeys.add(errorKey);
        }
        return errorKeys;
    }

    @Override
    public String getErrorMessage(ServerErrorKeys key) {
        switch(key) {
            case ID:
                return "Invalid id";
            case NAME:
                return "Invalid name";
            case ADDRESS:
                return "Invalid hostname address";
            case SOCKET_PORT:
                return "Invalid socket port";
        }
        return null;
    }
}
