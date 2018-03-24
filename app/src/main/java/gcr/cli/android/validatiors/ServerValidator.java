package gcr.cli.android.validatiors;

import gcr.cli.android.models.IServerModel;

public class ServerValidator extends ModelValidator<IServerModel> {

    public String validateId(int id) {
        final String errorMsg = "Invalid id";
        final String regex = "^[1-9][0-9]*$";
        final String idStr = id + "";
        return validateData(regex, errorMsg, idStr);
    }

    public String validateName(String name) {
        final String errorMsg = "Invalid name";
        final String regex = ".+";
        return validateData(regex, errorMsg, name);
    }

    public String validateAddress(String address) {
        final String errorMsg = "Invalid hostname address";
        // Hostname specification: [RFC 1123] http://tools.ietf.org/html/rfc1123
        final String hostnameRegex = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$";
        final String ipAddressRegex = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
        if(validateData(hostnameRegex, errorMsg, address) != null) {
            return validateData(ipAddressRegex, errorMsg, address);
        }
        return null;
    }

    public String validateSocketPort(int socketPort) {
        final String errorMsg = "Invalid socket port";
        final String regex = "^(6553[0-5]|655[0-2][0-9]|65[0-4][0-9]{2}|6[0-4][0-9]{3}|[1-5][0-9]{4}|[0-9]{1,4})$|^(6553[0-5]|655[0-2][0-9]|65[0-4][0-9]{2}|6[0-4][0-9]{3}|[1-5][0-9]{4}|[0-9]{1,4})$";
        final String socketPortStr = socketPort + "";
        return validateData(regex, errorMsg, socketPortStr);
    }

    @Override
    public String validate(IServerModel model) {
        String msg = validateId(model.getId());
        if(msg != null) {
            return msg;
        }
        msg = validateName(model.getName());
        if(msg != null) {
            return msg;
        }
        msg = validateAddress(model.getAddress());
        if(msg != null) {
            return msg;
        }
        msg = validateSocketPort(model.getSocketPort());
        return msg;
    }
}
