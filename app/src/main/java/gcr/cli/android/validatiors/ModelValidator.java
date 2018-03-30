package gcr.cli.android.validatiors;


import android.content.Context;

import java.util.List;

import gcr.cli.android.models.IModel;

public abstract class ModelValidator<T extends IModel, E extends Enum<E>> {

    public abstract List<E> validate(T model);
    public abstract String getErrorMessage(E key);
    protected Context context;

    protected E validateData(String regex, E key, String data, boolean nullable) {
        if(nullable && data == null) {
            return null;
        }
        if(data != null && data.matches(regex)) {
            return null;
        }
        return key;
    }

    protected E validateData(String regex, E key, String data) {
        return validateData(regex, key, data, false);
    }
}
