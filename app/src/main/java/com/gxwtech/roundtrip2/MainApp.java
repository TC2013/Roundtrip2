package com.gxwtech.roundtrip2;

import android.app.Application;

import com.gxwtech.roundtrip2.ServiceData.ServiceClientConnection;

/**
 * Created by Tim on 15/06/2016.
 */
public class MainApp extends Application {

    private static MainApp sInstance;
    private static ServiceClientConnection serviceClientConnection;

    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;
        serviceClientConnection = new ServiceClientConnection();

        // TODO: 09/07/2016 @TIM are we going to use ActiveAndroid?
        //Manually initialize ActiveAndroid
        // TODO: 05/11/2015 appears to be a bug in Active Android where DB version is ignored in Manifest, must be added here as well
        // http://stackoverflow.com/questions/33164456/update-existing-database-table-with-new-column-not-working-in-active-android
        //Configuration configuration = new Configuration.Builder(this).setDatabaseVersion(1).create();
        //ActiveAndroid.initialize(configuration); //// TODO: 06/01/2016 change to this?
    }




    public static MainApp instance() {
        return sInstance;
    }

    public static ServiceClientConnection getServiceClientConnection(){
        if (serviceClientConnection == null) {
            serviceClientConnection = new ServiceClientConnection();
        }
        return serviceClientConnection;
    }

    // TODO: 09/07/2016 @TIM uncomment ServiceClientConnection once class is added

}