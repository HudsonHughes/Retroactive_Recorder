package com.hughes.retrorecord;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.hughes.retrorecord.messages.MessageEvent;
import com.hughes.retrorecord.playback.PlaybackFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    public Context context;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {

    };

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        
        getWritePermission();
        getRecordPermission();
        getReadPermission();
        getBootPermission();
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @NeedsPermission(Manifest.permission.RECORD_AUDIO)
    private void getRecordPermission() {}
    @OnShowRationale(Manifest.permission.RECORD_AUDIO)
    void showRationaleForRecord(final PermissionRequest request) {request.proceed();}
    @OnPermissionDenied(Manifest.permission.RECORD_AUDIO)
    void onDeniedRecord() {leaveApp();}
    @OnNeverAskAgain(Manifest.permission.RECORD_AUDIO)
    void onNeverAskRecord() {leaveApp();}

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    private void getReadPermission() {}
    @OnShowRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showRationaleForRead(final PermissionRequest request) {request.proceed();}
    @OnPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
    void onDeniedRead() {leaveApp();}
    @OnNeverAskAgain(Manifest.permission.READ_EXTERNAL_STORAGE)
    void onNeverAskRead() {leaveApp();}

    @NeedsPermission(Manifest.permission.RECEIVE_BOOT_COMPLETED)
    private void getBootPermission() {}
    @OnShowRationale(Manifest.permission.RECEIVE_BOOT_COMPLETED)
    void showRationaleForBoot(final PermissionRequest request) {request.proceed();}
    @OnPermissionDenied(Manifest.permission.RECEIVE_BOOT_COMPLETED)
    void onDeniedBoot() {leaveApp();}
    @OnNeverAskAgain(Manifest.permission.RECEIVE_BOOT_COMPLETED)
    void onNeverAskBoot() {leaveApp();}

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private void getWritePermission() {}
    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showRationaleForWrite(final PermissionRequest request) {request.proceed();}
    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void onDeniedWrite() {leaveApp();}
    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void onNeverAskWrite() {leaveApp();}

    public void leaveApp(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.permission_denied_title)
                .setMessage(R.string.permission_denied_message)
                .setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID)));
                        finish();
                        System.exit(0);
                    }
                })
                .setNegativeButton(R.string.quit_app, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                        System.exit(0);
                    }
                })
                .show();
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(PlaybackFragment.newInstance(), "Playback");
        adapter.addFragment(MiddleFragment.newInstance(), "Main");
        adapter.addFragment(SettingsFragment.newInstance(), "Settings");
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}