package com.bignerdranch.nerdlauncher;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

        private final ImageView mImageView;
        private final TextView mTextView;
        private ResolveInfo mResolveInfo;

        ActivityHolder(@NonNull View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.app_icon);
            mTextView = itemView.findViewById(R.id.app_name);
        }

        void bindActivity(ResolveInfo resolveInfo) {
            mResolveInfo = resolveInfo;

            if (getActivity() == null) return;
            PackageManager packageManager = getActivity().getPackageManager();
            String appName = mResolveInfo.loadLabel(packageManager).toString();
            mTextView.setText(appName);
            Drawable appIcon = mResolveInfo.loadIcon(packageManager);
            mImageView.setImageDrawable(appIcon);
            mTextView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            ActivityInfo activityInfo = mResolveInfo.activityInfo;
            Intent intent = new Intent(Intent.ACTION_MAIN)
                    .setClassName(activityInfo.applicationInfo.packageName, activityInfo.name)
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
            View view = layoutInflater.inflate(R.layout.list_item_app, parent, false);
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
