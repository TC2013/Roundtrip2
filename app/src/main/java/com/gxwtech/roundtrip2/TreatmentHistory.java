package com.gxwtech.roundtrip2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.gxwtech.roundtrip2.HappService.Objects.Basal;
import com.gxwtech.roundtrip2.HappService.Objects.Treatment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class TreatmentHistory extends AppCompatActivity {
    private static final String TAG = "TreatmentHistory";

    SectionsPagerAdapter mSectionsPagerAdapter;                                                     //will provide fragments for each of the sections
    ViewPager mViewPager;
    Fragment bolusFragmentObject;
    Fragment basalFragmentObject;
    BroadcastReceiver refreshTreatments;
    BroadcastReceiver refreshBasal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treatment_history);

        // Create the adapter that will return a fragment for each of the 4 primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) this.findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        bolusFragmentObject = new bolusFragment();
        basalFragmentObject = new basalFragment();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (refreshTreatments != null){
            unregisterReceiver(refreshTreatments);
        }
        if (refreshBasal != null){
            unregisterReceiver(refreshBasal);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

        //Refresh the treatments list
        refreshTreatments = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                bolusFragment.update();
            }
        };
        registerReceiver(refreshTreatments, new IntentFilter("UPDATE_TREATMENTS"));
        bolusFragment.update();

        //Refresh the Basal list
        refreshBasal = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                basalFragment.update();
            }
        };
        registerReceiver(refreshBasal, new IntentFilter("UPDATE_BASAL"));
        basalFragment.update();
    }

    /* Page layout Fragments */

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            switch (position){
                case 0:
                    return bolusFragmentObject;
                case 1:
                    return basalFragmentObject;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Bolus Requests";
                case 1:
                    return "Basal Requests";
            }
            return null;
        }
    }

    public static class bolusFragment extends Fragment {
        public bolusFragment(){}
        private static ListView list;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_treatments_list, container, false);
            list    =   (ListView) rootView.findViewById(R.id.treatmentsFragmentList);

            update();
            return rootView;
        }

        public static void update(){

            if (list != null) {
                ArrayList<HashMap<String, String>> treatmentsList = new ArrayList<>();
                List<Treatment> treatments = Treatment.getLatestTreatments(10);
                Calendar treatmentDate = Calendar.getInstance();
                SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd MMM HH:mm", MainApp.instance().getResources().getConfiguration().locale);

                for (Treatment treatment : treatments) {                                                    //Convert from a List<Object> Array to ArrayList
                    HashMap<String, String> treatmentItem = new HashMap<String, String>();

                    if (treatment.date_requested != null) {
                        treatmentDate.setTime(new Date(treatment.date_requested));
                    } else {
                        treatmentDate.setTime(new Date(0));                                                 //Bad Treatment
                    }
                    treatmentItem.put("type", treatment.type);
                    treatmentItem.put("value", treatment.value.toString() + "U");
                    treatmentItem.put("dateTime", sdfDateTime.format(treatmentDate.getTime()));
                    treatmentItem.put("state", "State:" + treatment.state);
                    treatmentItem.put("delivered", "Delivered:" + treatment.delivered);
                    treatmentItem.put("rejected", "Rejected:" + treatment.rejected);
                    treatmentItem.put("happ_id", "HAPP Integration ID:" + treatment.happ_int_id);
                    treatmentItem.put("happ_update", "Update Needed:" + treatment.happ_update);
                    treatmentItem.put("details", treatment.details);

                    treatmentsList.add(treatmentItem);
                }

                SimpleAdapter adapter = new SimpleAdapter(MainApp.instance(), treatmentsList, R.layout.treatments_list_layout,
                        new String[]{"type", "value", "dateTime", "state", "delivered", "rejected", "happ_id", "happ_update", "details"},
                        new int[]{R.id.treatmentTypeLayout, R.id.treatmentValueLayout, R.id.treatmentDateTimeLayout, R.id.treatmentStateLayout, R.id.treatmentDeliveredLayout, R.id.treatmentRejectedLayout, R.id.treatmentHAPPIDLayout, R.id.treatmentHAPPUpdateLayout, R.id.treatmentDetailsLayout});
                list.setAdapter(adapter);
            }
        }
    }

    public static class basalFragment extends Fragment {
        public basalFragment() {
        }

        private static ListView list;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_treatments_list, container, false);
            list = (ListView) rootView.findViewById(R.id.treatmentsFragmentList);

            update();
            return rootView;
        }

        public static void update() {
            if (list != null) {
                ArrayList<HashMap<String, String>> basalList = new ArrayList<>();
                List<Basal> basals = Basal.getLatest(10);
                Calendar basalDate = Calendar.getInstance();
                SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd MMM HH:mm", MainApp.instance().getResources().getConfiguration().locale);

                for (Basal basal : basals) {                                                    //Convert from a List<Object> Array to ArrayList
                    HashMap<String, String> basalItem = new HashMap<String, String>();

                    basalDate.setTime(basal.start_time);

                    basalItem.put("type", basal.action);
                    basalItem.put("value", basal.rate + "U/h (" + basal.ratePercent + "%) " + basal.duration + "mins");
                    basalItem.put("dateTime", sdfDateTime.format(basalDate.getTime()));
                    basalItem.put("state", "State:" + basal.state);
                    basalItem.put("delivered", "Set:" + basal.been_set);
                    basalItem.put("rejected", "Rejected:" + basal.rejected);
                    basalItem.put("happ_id", "HAPP Integration ID:" + basal.happ_int_id);
                    basalItem.put("happ_update", "Update Needed:" + basal.happ_update);
                    basalItem.put("details", basal.details);

                    basalList.add(basalItem);
                }

                SimpleAdapter adapter = new SimpleAdapter(MainApp.instance(), basalList, R.layout.treatments_list_layout,
                        new String[]{"type", "value", "dateTime", "state", "delivered", "rejected", "happ_id", "happ_update", "details"},
                        new int[]{R.id.treatmentTypeLayout, R.id.treatmentValueLayout, R.id.treatmentDateTimeLayout, R.id.treatmentStateLayout, R.id.treatmentDeliveredLayout, R.id.treatmentRejectedLayout, R.id.treatmentHAPPIDLayout, R.id.treatmentHAPPUpdateLayout, R.id.treatmentDetailsLayout});
                list.setAdapter(adapter);
            }
        }
    }
}
