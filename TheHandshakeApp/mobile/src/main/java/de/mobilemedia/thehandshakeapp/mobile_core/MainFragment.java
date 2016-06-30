package de.mobilemedia.thehandshakeapp.mobile_core;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import de.mobilemedia.thehandshakeapp.R;
import de.mobilemedia.thehandshakeapp.bluetooth.BTLEConnectionManager;
import de.mobilemedia.thehandshakeapp.detection.FileOutputWriter;
import de.mobilemedia.thehandshakeapp.bluetooth.Util;
import de.mobilemedia.thehandshakeapp.detection.MRDFeatureExtractor;


public class MainFragment extends Fragment {

    MainActivity parentActivity;

    private Button mShakeButton;
    private ImageView mImageView;
    private TextView mYouAreView;

    private FileOutputWriter fileOutputWriter;
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

        displayOwnHandshakeData();

        fileOutputWriter = null;
        mShakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onHandshake();
            }
        });

        return view;
    }

    public void displayOwnHandshakeData() {
        try {
            String youAreValue = parentActivity.getBleConnectionManager().getMyHandshakeData().getHash();
            mYouAreView.setText(youAreValue);
        } catch (Exception e) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    displayOwnHandshakeData();
                }
            }, 1000);
        }
    }

    public void onHandshake() {

        final BTLEConnectionManager bleConnectionManager = parentActivity.getBleConnectionManager();
        bleConnectionManager.setButtonToGreyOut(mShakeButton);
        bleConnectionManager.scanBTLE(true);
        bleConnectionManager.advertiseBTLE(true);
        MRDFeatureExtractor.myLastShakeTime = Util.getCurrentUnixTimestamp();

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

}
