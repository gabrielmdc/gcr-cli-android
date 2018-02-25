package gcr.cli.android.repositories;

import android.content.Context;

public interface IRepositories {
    IServerRepository getServerRepository();
    void closeConnection();
    void openConnection(Context context);
}
