package gcr.cli.android.validatiors;

import gcr.cli.android.models.IRelay;

public class RelayModelValidator extends ModelValidator<IRelay> {

    public static String validateId(int id) {
        String errorMsg = "Invalid id";
        String regex = "^[1-9]+[0-9]*$";
        String idStr = id + "";
        return validateData(regex, errorMsg, idStr);
    }

    public static String validateName(String name) {
        String errorMsg = "Invalid name";
        String regex = ".+";
        return validateData(regex, errorMsg, name);
    }

    public static String validateGpio(int gpio) {
        String errorMsg = "Invalid gpio";
        String regex = "^[1-2].[]$";
        String gpioStr = gpio + "";
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