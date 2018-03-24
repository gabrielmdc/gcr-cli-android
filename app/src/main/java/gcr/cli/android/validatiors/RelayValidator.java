package gcr.cli.android.validatiors;

import java.util.ArrayList;
import java.util.List;

import gcr.cli.android.models.IRelay;
import gcr.cli.android.validatiors.errorkeys.RelayErrorKeys;

public class RelayValidator extends ModelValidator<IRelay, RelayErrorKeys> {

    public static final int ERROR_KEY_ID = 0;

    public RelayErrorKeys validateId(int id) {
        final String regex = "^[1-9]+[0-9]*$";
        final String idStr = id + "";
        return validateData(regex, RelayErrorKeys.ID, idStr);
    }

    public RelayErrorKeys validateName(String name) {
        final String regex = ".+";
        return validateData(regex, RelayErrorKeys.NAME, name);
    }

    public RelayErrorKeys validateGpio(int gpio) {
        final String gpioStr = gpio + "";
        return validateGpio(gpioStr);
    }

    public RelayErrorKeys validateGpio(String gpioStr) {
        final String regex = "^((2[0-7])|(1[0-9])|[1-9])$";
        return validateData(regex, RelayErrorKeys.GPIO, gpioStr);
    }

    @Override
    public List<RelayErrorKeys> validate(IRelay model) {
        List<RelayErrorKeys> errorKeys = new ArrayList<>();

        RelayErrorKeys errorKey = validateId(model.getId());
        if(errorKey != null) {
            errorKeys.add(errorKey);
        }
        errorKey = validateName(model.getName());
        if(errorKey != null) {
            errorKeys.add(errorKey);
        }
        errorKey = validateGpio(model.getGpio());
        if(errorKey != null) {
            errorKeys.add(errorKey);
        }
        return errorKeys;
    }

    @Override
    public String getErrorMessage(RelayErrorKeys key) {
        switch(key) {
            case ID:
                return "Invalid id";
            case NAME:
                return "Invalid name";
            case GPIO:
                return "Invalid gpio";
        }
        return null;
    }
}