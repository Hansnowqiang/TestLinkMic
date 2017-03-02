package com.han.testlinkmic;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.ITXLivePushListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayConfig;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.TXLivePushConfig;
import com.tencent.rtmp.TXLivePusher;
import com.tencent.rtmp.ui.TXCloudVideoView;

/**
 * 大主播端
 */
public class PushActivity extends AppCompatActivity implements ITXLivePushListener {
    private PushActivity.TCPlayItem mPlayItem1;

    private class TCPlayItem {
        public boolean              mPending = false;
        public String               mUserID = "";
        public String               mPlayUrl = "";
        public TXCloudVideoView mVideoView;

        public TXLivePlayer mTXLivePlayer;
        public TXLivePlayConfig mTXLivePlayConfig = new TXLivePlayConfig();

        public void empty() {
            mPending = false;
            mUserID = "";
            mPlayUrl = "";
        }
    }

    private TXCloudVideoView mTXCloudVideoView;
    private TXLivePusher mTXLivePusher;
    protected TXLivePushConfig mTXPushConfig = new TXLivePushConfig();
    private String TAG = PlayActivity.class.getSimpleName();
    private String mPushUrl ="rtmp://4005.livepush.myqcloud.com/live/4005_da?bizid=4005&txSecret=09d053589f32daf9e5e13d12a56be32d&txTime=58B8417F";//大主播推流地址
    private String mPlayUrl = "rtmp://4005.liveplay.myqcloud.com/live/4005_xiao?bizid=4005&txTime=58B8417F";//小主播拉流地址
    private String mSessionID = "1095220622628398746";

    private String key = "ebdddf810e5e4aed5fbda3c172028d89";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_push);
        mPushUrl += String.format("&mix=layer:b;t_id:1;session_id:%s", mSessionID);
        mPlayUrl = new StringBuilder(mPlayUrl).append("&txSecret=").append(getSecretKey()).append("&session_id=").append(mSessionID).toString();
        Log.e(TAG,"大主播推流地址："+mPushUrl);
        Log.e(TAG,"小主播拉流地址："+mPlayUrl);
        initView();
        startPublish();
    }

    public String getSecretKey(){
        String secretKey = "";
        secretKey = MD5Utils.getMD5(key + "4005_xiao"+"58B8417F");
        return secretKey;
    }

    private void initView() {
        mTXCloudVideoView = (TXCloudVideoView) findViewById(R.id.video_view);

        initLinkMicView();
    }

    private void initLinkMicView() {
        findViewById(R.id.btn_push_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPlayItem1.mPlayUrl = mPlayUrl;
                Log.e(TAG,"小主播地址："+mPlayUrl);
                mPlayItem1.mTXLivePlayer.startPlay(mPlayItem1.mPlayUrl, TXLivePlayer.PLAY_TYPE_LIVE_RTMP_ACC);
            }
        });

        mPlayItem1 = new PushActivity.TCPlayItem();
        mPlayItem1.mVideoView = (TXCloudVideoView) findViewById(R.id.play_video_view1);
        mPlayItem1.mTXLivePlayer = new TXLivePlayer(this);
        mPlayItem1.mTXLivePlayer.setPlayListener(new PushActivity.TXLivePlayListener(mPlayItem1));
        mPlayItem1.mTXLivePlayer.setPlayerView(mPlayItem1.mVideoView);
        mPlayItem1.mTXLivePlayer.enableHardwareDecode(true);

        mPlayItem1.mTXLivePlayConfig = new TXLivePlayConfig();
        mPlayItem1.mTXLivePlayConfig.enableAEC(true);
        mPlayItem1.mTXLivePlayConfig.setAutoAdjustCacheTime(true);
        mPlayItem1.mTXLivePlayConfig.setMinAutoAdjustCacheTime(0.2f);
        mPlayItem1.mTXLivePlayConfig.setMaxAutoAdjustCacheTime(0.2f);
        mPlayItem1.mTXLivePlayer.setConfig(mPlayItem1.mTXLivePlayConfig);
    }

    protected void startPublish() {

        mTXPushConfig.enableAEC(true);
        mTXPushConfig.setAutoAdjustBitrate(true);
        mTXPushConfig.setAutoAdjustStrategy(TXLiveConstants.AUTO_ADJUST_BITRATE_STRATEGY_2);
        if (Build.VERSION.SDK_INT < 18) {
            mTXPushConfig.setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_360_640);
            mTXPushConfig.setMinVideoBitrate(400);
            mTXPushConfig.setMaxVideoBitrate(1000);
            mTXPushConfig.setHardwareAcceleration(false);
        } else {
            mTXPushConfig.setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_540_960);
            mTXPushConfig.setMinVideoBitrate(800);
            mTXPushConfig.setMaxVideoBitrate(1200);
            mTXPushConfig.setHardwareAcceleration(true);
        }

        if (mTXLivePusher == null) {
            mTXLivePusher = new TXLivePusher(PushActivity.this);
            mTXLivePusher.setPushListener(PushActivity.this);

            mTXPushConfig.setPauseFlag(TXLiveConstants.PAUSE_FLAG_PAUSE_VIDEO|TXLiveConstants.PAUSE_FLAG_PAUSE_AUDIO);
            mTXLivePusher.setConfig(mTXPushConfig);
        }
        if (mTXCloudVideoView != null) {
            mTXCloudVideoView.setVisibility(View.VISIBLE);
            mTXCloudVideoView.clearLog();
        }
        mTXLivePusher.startCameraPreview(mTXCloudVideoView);
        mTXLivePusher.startPusher(mPushUrl);
        Log.e(TAG,"主播端推流地址：" + mPushUrl);
    }

    @Override
    public void onPushEvent(int event, Bundle bundle) {
        if (mTXCloudVideoView != null) {
            mTXCloudVideoView.setLogText(null,bundle,event);
        }
        if (event < 0) {
            if (event == TXLiveConstants.PUSH_ERR_NET_DISCONNECT) {//网络断开，弹对话框强提醒，推流过程中直播中断需要显示直播信息后退出
                Log.e(TAG,"网络断开，弹对话框强提醒，推流过程中直播中断需要显示直播信息后退出");
            } else if (event == TXLiveConstants.PUSH_ERR_OPEN_CAMERA_FAIL) {//未获得摄像头权限，弹对话框强提醒，并退出
                Log.e(TAG,"未获得摄像头权限，弹对话框强提醒，并退出");
            } else if (event == TXLiveConstants.PUSH_ERR_OPEN_MIC_FAIL) { //未获得麦克风权限，弹对话框强提醒，并退出
                Log.e(TAG,"/未获得麦克风权限，弹对话框强提醒，并退出");
                Toast.makeText(getApplicationContext(), bundle.getString(TXLiveConstants.EVT_DESCRIPTION), Toast.LENGTH_SHORT).show();
            } else {
                //其他错误弹Toast弱提醒，并退出
                Toast.makeText(getApplicationContext(), bundle.getString(TXLiveConstants.EVT_DESCRIPTION), Toast.LENGTH_SHORT).show();
                mTXCloudVideoView.onPause();
                finish();
            }
        }

        if (event == TXLiveConstants.PUSH_WARNING_HW_ACCELERATION_FAIL) {
            Log.d(TAG, "当前机型不支持视频硬编码");
            mTXPushConfig.setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_360_640);
            mTXPushConfig.setVideoBitrate(700);
            mTXPushConfig.setHardwareAcceleration(false);
            mTXLivePusher.setConfig(mTXPushConfig);
        }
    }

    @Override
    public void onNetStatus(Bundle bundle) {
        if (mTXCloudVideoView != null) {
            mTXCloudVideoView.setLogText(bundle,null,0);
        }
    }

    private class TXLivePlayListener implements ITXLivePlayListener {
        private PushActivity.TCPlayItem item;

        public TXLivePlayListener(PushActivity.TCPlayItem item) {
            this.item = item;
        }

        public void onPlayEvent(final int event, final Bundle param) {
            TXCloudVideoView videoView = item.mVideoView;
            if (videoView != null) {
                videoView.setLogText(null,param,event);
            }
            //            if (event == TXLiveConstants.PLAY_ERR_NET_DISCONNECT || event == TXLiveConstants.PLAY_EVT_PLAY_END) {
            //                if (item.mPending == true) {
            //                    handleLinkMicFailed(item, "拉流失败，结束连麦");
            //                }
            //                else {
            //                    handleLinkMicFailed(item, "连麦观众视频断流，结束连麦");
            //                }
            //            }
            else if (event == TXLiveConstants.PLAY_EVT_PLAY_BEGIN) {
                if (item.mPending == true) {
                    item.mPending = false;
                }
            }
        }

        public void onNetStatus(final Bundle status) {
            TXCloudVideoView videoView = item.mVideoView;
            if (videoView != null) {
                videoView.setLogText(status,null,0);
            }
        }
    }




    @Override
    protected void onResume() {
        super.onResume();
        mTXCloudVideoView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mTXCloudVideoView.onPause();
    }
}
