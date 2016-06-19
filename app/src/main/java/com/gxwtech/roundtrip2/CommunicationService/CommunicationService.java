package com.gxwtech.roundtrip2.CommunicationService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gxwtech.roundtrip2.MainApp;
import com.gxwtech.roundtrip2.util.Check;

import com.gxwtech.roundtrip2.CommunicationService.Objects.ObjectToSync;
import com.gxwtech.roundtrip2.CommunicationService.Objects.Basal;
import com.gxwtech.roundtrip2.CommunicationService.Objects.Treatment;


import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by Tim on 07/06/2016.
 * This service listens out for requests from HAPP and processes them
 */
public class CommunicationService extends android.app.Service {

    public CommunicationService(){}
    final static String TAG = "CommunicationService";

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            String action="";
            Long requested=0L;
            String dataString="";
            String safteyCheck="";
            Bundle data = new Bundle();

            Log.d(TAG,"START");
            try {
                data = msg.getData();
                action = data.getString("ACTION");
                Log.d("RECEIVED: ACTION", action);
                requested = data.getLong("DATE_REQUESTED", 0);
                Log.d("RECEIVED: DATE", requested.toString());
                dataString = data.getString("DATA");
                Log.d("RECEIVED: DATA", dataString);

            } catch (Exception e){
                e.printStackTrace();
                // TODO: 16/01/2016 Issue getting treatment details from APS app msg for user

            } finally {
                //Toast.makeText(MainApp.instance(), action, Toast.LENGTH_LONG).show();

                switch (action){
                    case "TEST_MSG":
                        Resources appR = MainApp.instance().getResources();
                        CharSequence txt = appR.getText(appR.getIdentifier("app_name", "string", MainApp.instance().getPackageName()));
                        Toast.makeText(MainApp.instance(), txt + ": Pump Driver App has connected successfully. ", Toast.LENGTH_LONG).show();
                        Log.d(TAG,txt + ": APS app has connected successfully.");

                        break;
                    case "temp_basal":
                    case "cancel_temp_basal":
                        ObjectToSync basalSync = new Gson().fromJson(dataString, ObjectToSync.class);

                        Basal basal = new Basal();
                        basal.rate          =   basalSync.value1;
                        basal.ratePercent   =   Integer.parseInt(basalSync.value2);
                        basal.duration      =   Integer.parseInt(basalSync.value3);
                        basal.start_time    =   basalSync.requested;

                        basal.action        =   basalSync.action;
                        basal.been_set      =   false;
                        basal.aps_int_id    =   basalSync.aps_integration_id;
                        basal.auth_code     =   basalSync.integrationSecretCode;

                        safteyCheck         =   Check.NewObjectToSync(basalSync);
                        if (safteyCheck.equals("")) {
                            basal.state     =   "received";
                        } else {
                            basal.state      =   "error";
                            basal.details    =   safteyCheck;
                            basal.rejected   =   true;
                            basal.aps_update =   true;
                        }
                        basal.save();

                        //We have now saved the requested treatments from APS app to our local DB, now action them
                        connect_to_aps_app();
                        actionBasal();

                        break;
                    case "bolus_delivery":
                        Type type = new TypeToken<List<ObjectToSync>>(){}.getType();
                        List<ObjectToSync> bolusList = new Gson().fromJson(dataString, type);

                        for (ObjectToSync newBolus : bolusList){
                            try {
                                Treatment newTreatment = new Treatment();
                                newTreatment.type           =   newBolus.value3;
                                newTreatment.date_requested =   newBolus.requested.getTime();
                                newTreatment.value          =   newBolus.value1;

                                newTreatment.delivered      =   false;
                                newTreatment.aps_int_id     =   newBolus.aps_integration_id;
                                newTreatment.auth_code      =   newBolus.integrationSecretCode;

                                safteyCheck                 = Check.NewObjectToSync(newBolus);
                                if (safteyCheck.equals("")){
                                    newTreatment.state      =   "received";
                                } else {
                                    newTreatment.state      =   "error";
                                    newTreatment.details    =   safteyCheck;
                                    newTreatment.rejected   =   true;
                                    newTreatment.aps_update =   true;
                                }
                                newTreatment.save();

                            } catch (Exception e) {
                                e.printStackTrace();
                                // TODO: 16/01/2016 Issue getting treatment details
                            }
                        }


                        //We have now saved the requested treatments from APS App to our local DB, now action them
                        connect_to_aps_app();
                        actionBolus();
                        break;
                }
            }
        }
    }



    public void actionBolus(){

        List<Treatment> boluses = Treatment.getToBeActioned();                                      //Get all boluses yet to be processed

        for (Treatment bolus : boluses){

            if (Check.IsBolusSafeToAction(bolus)) {

                String actionResult = "delivered";                                                  // TODO: 16/01/2016 a function should be here to action the treatment and get result

                if (actionResult.equals("delivered")) {
                    bolus.state         =   "delivered";
                    bolus.details       =   "Treatment has been sent to the pump";
                    bolus.delivered     =   true;
                    bolus.aps_update   =   true;
                    bolus.save();
                } else {
                    bolus.state         =   "error";
                    bolus.details       =   actionResult;
                    bolus.delivered     =   false;
                    bolus.rejected      =   true;
                    bolus.aps_update   =   true;
                    bolus.save();
                }
            }

        }
        if (boluses.size() > 0) {
            connect_to_aps_app();
            Intent intent = new Intent("UPDATE_TREATMENTS");                                        //sends result to update UI if loaded
            MainApp.instance().sendBroadcast(intent);
        }
    }

    public void actionBasal(){

        Basal recentRequest = Basal.lastRequested();                                                //Basal most recently requested and has not been set

        if (recentRequest != null){

            if (Check.IsBasalSafeToAction(recentRequest)) {
                String actionResult = "";

                switch (recentRequest.action) {
                    case "new":

                        actionResult = "set";                                                       // TODO: 16/01/2016 a function should be here to action the TBR and get result

                        if (actionResult.equals("set")) {
                            recentRequest.state         =   "set";
                            recentRequest.details       =   "Temp Basal Set " + recentRequest.rate + "U (" + recentRequest.ratePercent + "%)";
                            recentRequest.been_set      =   true;
                            recentRequest.aps_update    =   true;
                            recentRequest.save();
                        } else {
                            recentRequest.state         =   "error";
                            recentRequest.details       =   actionResult;
                            recentRequest.been_set      =   false;
                            recentRequest.rejected      =   true;
                            recentRequest.aps_update    =   true;
                            recentRequest.save();
                        }

                        break;

                    case "cancel":

                        actionResult = "canceled";                                                  // TODO: 16/01/2016 a function should be here to action the TBR and get result

                        if (actionResult.equals("canceled")) {
                            recentRequest.state         =   "canceled";
                            recentRequest.details       =   "This Temp Basal was running and has now been Canceled";
                            recentRequest.been_set      =   true;
                            recentRequest.aps_update    =   true;
                            recentRequest.save();
                        } else {
                            recentRequest.state         =   "error";
                            recentRequest.details       =   actionResult;
                            recentRequest.been_set      =   false;
                            recentRequest.aps_update    =   true;
                            recentRequest.rejected      =   true;
                            recentRequest.save();
                        }

                        break;
                }
            }

            connect_to_aps_app();
            Intent intent = new Intent("UPDATE_BASAL");                                             //sends result to update UI if loaded
            //MainApp.instance().sendBroadcast(intent);
            sendBroadcast(intent); // TODO: 07/06/2016 this ok?
        }
    }

    final Messenger myMessenger = new Messenger(new IncomingHandler());

    @Override
    public IBinder onBind(Intent intent) {
        return myMessenger.getBinder();
    }

    public void updateAPSAppBolus(){

        List<Treatment> treatments = Treatment.getToUpdateAPSApp();
        Log.d(TAG, "UPDATE APS App: " + treatments.size() + " treatments");

        for (Treatment bolus : treatments) {

            ObjectToSync bolusSync = new ObjectToSync(bolus,null);

            Message msg = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putString("ACTION", "bolus_delivery");
            bundle.putString("UPDATE", bolusSync.asJSONString());
            msg.setData(bundle);

            try {
                Log.d(TAG, "UPDATE APS App: INT ID " + bolusSync.aps_integration_id);
                myService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
                Log.d(TAG, "UPDATE APS App: Update FAILED for " + bolusSync.aps_integration_id);
            } finally {
                Log.d(TAG, "UPDATE APS App: Update sent for " + bolusSync.aps_integration_id);
                bolus.aps_update = false;
                bolus.save();
            }
        }

        try {
            if (isBound) CommunicationService.this.unbindService(myConnection);
        } catch (IllegalArgumentException e) {
            //catch if service was killed in a unclean way
        }
    }

    public void updateAPSAppBasal(){

        List<Basal> basals = Basal.getToUpdateHAPP();
        Log.d(TAG, "UPDATE APS App:" + basals.size() + " basals");

        for (Basal basal : basals) {

            ObjectToSync basalSync = new ObjectToSync(null,basal);

            Message msg = Message.obtain();
            Bundle bundle = new Bundle();
            bundle.putString("ACTION", "temp_basal");
            bundle.putString("UPDATE", basalSync.asJSONString());
            msg.setData(bundle);

            try {
                Log.d(TAG, "UPDATE APS App: INT ID " + basalSync.aps_integration_id);
                myService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
                Log.d(TAG, "UPDATE APS App: Update FAILED for " + basalSync.aps_integration_id);
            } finally {
                Log.d(TAG, "UPDATE APS App: Update sent for " + basalSync.aps_integration_id);
                basal.aps_update = false;
                basal.save();
            }
        }

        try {
            if (isBound) CommunicationService.this.unbindService(myConnection);
        } catch (IllegalArgumentException e) {
            //catch if service was killed in a unclean way
        }
    }

    //Connect to the APS App Treatments Service
    private void connect_to_aps_app(){
        // TODO: 16/06/2016 should be able to pick the APS app from UI not hardcoded 
        Intent intent = new Intent("com.hypodiabetic.happ.services.TreatmentService");
        intent.setPackage("com.hypodiabetic.happ");
        CommunicationService.this.bindService(intent, myConnection, Context.BIND_AUTO_CREATE);
    }
    //Our Service that APS App will connect to
    private Messenger myService = null;
    private Boolean isBound = false;
    private ServiceConnection myConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            myService = new Messenger(service);
            isBound = true;

            updateAPSAppBolus();
            updateAPSAppBasal();
        }

        public void onServiceDisconnected(ComponentName className) {
            myService = null;
            isBound = false;
        }
    };


}

