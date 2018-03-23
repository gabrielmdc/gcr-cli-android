package gcr.cli.android.validatiors;

import gcr.cli.android.models.IModel;

public interface IModelValidator<T extends IModel> {
    String validate(T model);
}
