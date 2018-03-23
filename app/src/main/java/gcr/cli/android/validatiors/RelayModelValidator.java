package gcr.cli.android.validatiors;

import gcr.cli.android.models.IRelay;

public class RelayModelValidator extends ModelValidator<IRelay> {

    public String validateId(int id) {
        final String errorMsg = "Invalid id";
        final String regex = "^[1-9]+[0-9]*$";
        final String idStr = id + "";
        return validateData(regex, errorMsg, idStr);
    }

    public String validateName(String name) {
        final String errorMsg = "Invalid name";
        final String regex = ".+";
        return validateData(regex, errorMsg, name);
    }

    public String validateGpio(int gpio) {
        final String gpioStr = gpio + "";
        return validateGpio(gpioStr);
    }

    public String validateGpio(String gpioStr) {
        final String errorMsg = "Invalid gpio";
        final String regex = "^((2[0-7])|(1?[0-9]))$";
        return validateData(regex, errorMsg, gpioStr);
    }

    @Override
    public String validate(IRelay model) {
        String msg = validateId(model.getId());
        if(msg != null) {
            return msg;
        }
        msg = validateName(model.getName());
        if(msg != null) {
            return msg;
        }
        msg = validateGpio(model.getGpio());
        return msg;
    }
}