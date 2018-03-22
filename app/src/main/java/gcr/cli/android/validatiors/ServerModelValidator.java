package gcr.cli.android.validatiors;

import gcr.cli.android.models.IServerModel;

public class ServerModelValidator extends ModelValidator<IServerModel> {

    public static String validateId(int id) {
        String errorMsg = "Invalid id";
        String regex = "^[1-9][0-9]*$";
        String idStr = id + "";
        return validateData(regex, errorMsg, idStr);
    }

    public static String validateName(String name) {
        String errorMsg = "Invalid name";
        String regex = ".+";
        return validateData(regex, errorMsg, name);
    }

    public static String validateAddress(String address) {
        String errorMsg = "Invalid hostname address";
        // Hostname specification: [RFC 1123] http://tools.ietf.org/html/rfc1123
        String hostnameRegex = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$";
        String ipAddressRegex = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";
        if(validateData(hostnameRegex, errorMsg, address) != null) {
            return validateData(ipAddressRegex, errorMsg, address);
        }
        return null;
    }

    public static String validateSocketPort(int socketPort) {
        String errorMsg = "Invalid socket port";
        String regex = "^((2[0-7])|(1?[0-9]))$";
        String socketPortStr = socketPort + "";
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
