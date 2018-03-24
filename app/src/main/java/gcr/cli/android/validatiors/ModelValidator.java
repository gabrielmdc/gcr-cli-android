package gcr.cli.android.validatiors;


import gcr.cli.android.models.IModel;

public abstract class ModelValidator<T extends IModel> {

    abstract String validate(T model);

    protected static String validateData(String regex, String errorMsg, String data, boolean nullable) {
        if(nullable && data == null) {
            return null;
        }
        if(data != null && data.matches(regex)) {
            return null;
        }
        return errorMsg;
    }

    protected static String validateData(String regex, String errorMsg, String data) {
        return validateData(regex, errorMsg, data, false);
    }
}
