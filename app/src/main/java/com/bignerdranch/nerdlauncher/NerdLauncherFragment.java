package com.bignerdranch.nerdlauncher;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NerdLauncherFragment extends Fragment {

    private static final String LOG_TAG = "NerdLauncherFragment";

    private RecyclerView mRecyclerView;

    public NerdLauncherFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance() {
        return new NerdLauncherFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nerd_launcher, container, false);
        mRecyclerView = view.findViewById(R.id.app_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        setUpAdapter();
        return view;
    }

    // SOS: irrelevant but good info: startActivity(intent) secretly adds a CATEGORY_DEFAULT to the
    // intent,so it will only match activities that also contain that category. Not all app launcher
    // activities care about being the default for anything, so they don't specify CATEGORY_DEFAULT.
    private void setUpAdapter() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        if (getActivity() == null) return;
        final PackageManager packageManager = getActivity().getPackageManager();
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, 0);

        Collections.sort(resolveInfos, new Comparator<ResolveInfo>() {
            @Override
            public int compare(ResolveInfo info1, ResolveInfo info2) {
                return String.CASE_INSENSITIVE_ORDER.compare(
                        info1.loadLabel(packageManager).toString(),
                        info2.loadLabel(packageManager).toString());
            }
        });

        Log.i(LOG_TAG, "Found " + resolveInfos.size() + " activities.");

        mRecyclerView.setAdapter(new ActivityAdapter(resolveInfos));
    }

    private class ActivityHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView mNameTextView;
        private ResolveInfo mResolveInfo;

        ActivityHolder(@NonNull View itemView) {
            super(itemView);
            mNameTextView = (TextView) itemView;
        }

        void bindActivity(ResolveInfo resolveInfo) {
            mResolveInfo = resolveInfo;

            if (getActivity() == null) return;
            PackageManager packageManager = getActivity().getPackageManager();
            String appName = mResolveInfo.loadLabel(packageManager).toString();
            mNameTextView.setText(appName);
            mNameTextView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            ActivityInfo activityInfo = mResolveInfo.activityInfo;
            // SOS: we include an action because some activities present different UIs depending on
            // whether they were started as Main or not.
            Intent intent = new Intent(Intent.ACTION_MAIN)
                    // SOS: note that we need the packageName; when we write 'new Intent(this, Foo.class)'
                    // the packageName is inferred from the 'this' argument.
                    .setClassName(activityInfo.applicationInfo.packageName, activityInfo.name)
                    // SOS: By default, the new unrelated activity will be added to THIS app's back-
                    // stack! If we want the new activity to get its own process/task, we add this.
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private class ActivityAdapter extends RecyclerView.Adapter<ActivityHolder> {

        private final List<ResolveInfo> mActivityInfos;

        ActivityAdapter(List<ResolveInfo> activityInfos) {
            mActivityInfos = activityInfos;
        }

        @NonNull
        @Override
        public ActivityHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ActivityHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ActivityHolder activityHolder, int position) {
            ResolveInfo activityInfo = mActivityInfos.get(position);
            activityHolder.bindActivity(activityInfo);
        }

        @Override
        public int getItemCount() {
            return mActivityInfos.size();
        }
    }
}
