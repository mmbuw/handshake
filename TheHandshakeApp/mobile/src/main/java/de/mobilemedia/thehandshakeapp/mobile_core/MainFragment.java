package de.mobilemedia.thehandshakeapp.mobile_core;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import de.mobilemedia.thehandshakeapp.R;
import de.mobilemedia.thehandshakeapp.detection.WatchListenerService;


public class MainFragment extends Fragment {

    MainActivity parentActivity;

    private Button mShakeButton;
    private ImageView mImageView;
    private TextView mYouAreView;

    private Handler mHandler;

    private Animation mShakeAnimation;
    private int mCurrentAnimationRepeatCount;
    private int mAnimationRepeats;

    public MainFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = (MainActivity) getActivity();
        parentActivity.setTitle(R.string.app_name);

        mHandler = new Handler();

        mShakeAnimation = AnimationUtils.loadAnimation(parentActivity, R.anim.handshake_animation);
        mShakeAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {

                if (mCurrentAnimationRepeatCount < mAnimationRepeats-1) {
                    Animation repeat = AnimationUtils.loadAnimation(parentActivity, R.anim.handshake_animation);
                    repeat.setAnimationListener(this);
                    mImageView.startAnimation(repeat);
                    mCurrentAnimationRepeatCount++;
                }
                else {
                    mCurrentAnimationRepeatCount = 0;
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        mCurrentAnimationRepeatCount = 0;
        mAnimationRepeats = 5;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        mShakeButton = (Button) view.findViewById(R.id.shakeButton);
        mImageView = (ImageView) view.findViewById(R.id.handshakeImageView);
        mYouAreView = (TextView) view.findViewById(R.id.you_are_value);

        mShakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WatchListenerService.ACTIVITY_TO_SERVICE_NAME);
                LocalBroadcastManager.getInstance(new Activity()).sendBroadcast(intent);
                updateUiOnHandshake();
            }
        });

        displayOwnHandshakeData();

        return view;
    }

    public void displayOwnHandshakeData() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(parentActivity);
        String shortUrl = prefs.getString(parentActivity.getString(R.string.url_short_pref_id),
                parentActivity.getString(R.string.url_short_pref_default));
        mYouAreView.setText(shortUrl);
    }

    public void updateUiOnHandshake() {
        greyOutButton();
        mImageView.startAnimation(mShakeAnimation);
    }

    public void updateUiOnValuesReceived() {

        /* Update visualization */
        mImageView.setColorFilter(0xff444444);

        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mImageView.setColorFilter(Color.BLACK);
            }
        }, 200);

    }

    public void greyOutButton() {
        mShakeButton.setEnabled(false);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mShakeButton.setEnabled(true);
            }
        }, Config.BLE_SCAN_AND_ADVERTISE_PERIOD);
    }

}
