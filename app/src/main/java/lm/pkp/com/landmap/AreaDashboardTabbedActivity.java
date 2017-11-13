package lm.pkp.com.landmap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

import lm.pkp.com.landmap.area.AreaContext;
import lm.pkp.com.landmap.area.AreaElement;
import lm.pkp.com.landmap.area.dashboard.AreaDashboardOwnedFragment;
import lm.pkp.com.landmap.area.dashboard.AreaDashboardPublicFragment;
import lm.pkp.com.landmap.area.dashboard.AreaDashboardSharedFragment;
import lm.pkp.com.landmap.area.db.AreaDBHelper;
import lm.pkp.com.landmap.area.res.disp.AreaItemAdaptor;
import lm.pkp.com.landmap.custom.AsyncTaskCallback;
import lm.pkp.com.landmap.custom.GenericActivityExceptionHandler;
import lm.pkp.com.landmap.sync.LocalDataRefresher;
import lm.pkp.com.landmap.util.ColorProvider;

public class AreaDashboardTabbedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //new GenericActivityExceptionHandler(this);

        setContentView(R.layout.activity_area_tabbed_dashboard);
        // Setup Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.areas_display_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(ColorProvider.getDefaultToolBarColor());

        ViewPager viewPager = (ViewPager) findViewById(R.id.areas_display_tab_pager);
        // Assign created adapter to viewPager
        viewPager.setAdapter(new DisplayAreasPagerAdapter(getSupportFragmentManager()));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.areas_display_tab_layout);
        // This method setup all required method for TabLayout with Viewpager
        tabLayout.setupWithViewPager(viewPager);

        ImageView createAreaView = (ImageView) findViewById(R.id.action_area_create);
        createAreaView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.splash_panel).setVisibility(View.VISIBLE);
                new AreaDBHelper(getApplicationContext(), new DataInsertCallback()).insertAreaLocally();
            }
        });

    }

    public static class DisplayAreasPagerAdapter extends FragmentPagerAdapter {
        // As we are implementing two tabs
        private static final int NUM_ITEMS = 3;

        public DisplayAreasPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        // For each tab different fragment is returned
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new AreaDashboardOwnedFragment();
                case 1:
                    return new AreaDashboardSharedFragment();
                case 2:
                    return new AreaDashboardPublicFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;

        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Owned";
                case 1:
                    return "Shared";
                case 2:
                    return "Public";
                default:
                    return null;
            }
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Exit")
                .setMessage("Are you sure?")
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                }).setNegativeButton("no", null).show();
    }

    private class DataInsertCallback implements AsyncTaskCallback {

        @Override
        public void taskCompleted(Object result) {
            AreaElement ae = (AreaElement) result;
            AreaContext.getInstance().setAreaElement(ae, getApplicationContext());

            AreaDBHelper adh = new AreaDBHelper(getApplicationContext(), new DataInsertServerCallback());
            adh.insertAreaToServer(ae);
        }
    }

    private class DataInsertServerCallback implements AsyncTaskCallback {

        @Override
        public void taskCompleted(Object result) {
            findViewById(R.id.splash_panel).setVisibility(View.INVISIBLE);
            finish();

            Intent intent = new Intent(getApplicationContext(), AreaDetailsActivity.class);
            startActivity(intent);
        }
    }

}