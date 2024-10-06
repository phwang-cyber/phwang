package com.wph.chinese.jchess.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.blankj.utilcode.util.SnackbarUtils;
import com.blankj.utilcode.util.StringUtils;
import com.hzy.chinese.jchess.R;
import com.wph.chinese.jchess.game.GameConfigWph;
import com.wph.chinese.jchess.game.GameLogicWph;
import com.wph.chinese.jchess.game.IGameCallbackWph;
import com.wph.chinese.jchess.view.GameBoardViewWph;

import java.util.LinkedList;


public class MainActivityWph extends AppCompatActivity
        implements IGameCallbackWph {

    GameBoardViewWph mGameBoard;
    ProgressBar mGameProgress;
    private SoundPool mSoundPool;
    private LinkedList<Integer> mSoundList;
    private GameLogicWph mGameLogic;
    private SharedPreferences mPreference;
    private boolean mSoundEnable;
    private int mHandicapIndex;
    private boolean mComputerFlip;
    private int mPieceStyle;
    private int mAILevel;
/*
在重载的onCreate函数
*1. super.onCreate(savedInstanceState)是指调用父类的onCreate构造函数。
2. savedInstanceState参数是保存当前Activity的状态信息。
3. Bundle类型的数据与Map类型的数据相似，以key-value的形式存储数据。
4. saveInsanceState参数是指保存实例状态即保存Activity(活动)的状态。
 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGameBoard = (GameBoardViewWph) findViewById(R.id.game_board);
        mGameProgress = (ProgressBar) findViewById(R.id.game_progress);
        mPreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        loadDefaultConfig();
        initSoundPool();
        initGameLogic();
    }

    //重写Activity的onCreateOptionsMenu方法
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //根据菜单布局文件填充菜单项
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

//监听菜单项
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.main_menu_exit){
            finish();
        }else if (item.getItemId() == R.id.main_menu_retract){
            mGameLogic.retract();
        }else if (item.getItemId() == R.id.main_menu_restart){
            mGameLogic.restart(mComputerFlip, mHandicapIndex);
            showMessage(getString(R.string.new_game_started));
        }else if (item.getItemId()==R.id.main_menu_settings){
            startActivity(new Intent(this, SettingsActivityWph.class));
        }

        return super.onOptionsItemSelected(item);
    }

    //在 Activity 从 Pause 状态转换到 Active 状态时被调用。
    @Override
    protected void onResume() {
        super.onResume();
        loadDefaultConfig();
        mGameLogic.setLevel(mAILevel);
        mGameBoard.setPieceTheme(mPieceStyle);
        mGameBoard.invalidate();
    }

    @Override
    protected void onDestroy() {
        if (mSoundPool != null) {
            mSoundPool.release();
        }
        mPreference.edit().putString(GameConfigWph.PREF_LAST_FEN, mGameLogic.getCurrentFen()).apply();
        super.onDestroy();
    }

    /**
     * 获取游戏设置配置文件
     */
    private void loadDefaultConfig() {
        mSoundEnable = mPreference.getBoolean(getString(R.string.pref_sound_key), true);
        mHandicapIndex = Integer.parseInt(mPreference.getString(getString(R.string.pref_handicap_key), "0"));
        mComputerFlip = mPreference.getBoolean(getString(R.string.pref_who_first_key), false);
        mPieceStyle = Integer.parseInt(mPreference.getString(getString(R.string.pref_piece_style_key), "0"));
        mAILevel = Integer.parseInt(mPreference.getString(getString(R.string.pref_level_key), "0"));
    }

    /**
     * 音效
     */
    private void initSoundPool() {
        mSoundList = new LinkedList<>();
        int poolSize = GameConfigWph.SOUND_RES_ARRAY.length;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSoundPool = new SoundPool.Builder().setMaxStreams(poolSize).build();
        } else {
            mSoundPool = new SoundPool(poolSize, AudioManager.STREAM_MUSIC, 0);
        }
        for (int res : GameConfigWph.SOUND_RES_ARRAY) {
            mSoundList.add(mSoundPool.load(this, res, 1));
        }
    }

    private void initGameLogic() {
        mGameLogic = mGameBoard.getGameLogic();
        mGameLogic.setCallback(this);
        mGameLogic.setLevel(mAILevel);
        mGameBoard.setPieceTheme(mPieceStyle);
        String lastFen = mPreference.getString(GameConfigWph.PREF_LAST_FEN, "");
        if (StringUtils.isEmpty(lastFen)) {
            mGameLogic.restart(mComputerFlip, mHandicapIndex);
        } else {
            showMessage(getString(R.string.load_last_game_finish));
            mGameLogic.restart(mComputerFlip, lastFen);
        }
    }

    @Override
    public void postPlaySound(final int soundIndex) {
        if (mSoundPool != null && mSoundEnable) {
            int soundId = mSoundList.get(soundIndex);
            mSoundPool.play(soundId, 1, 1, 0, 0, 1);
        }
    }

    @Override
    public void postShowMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showMessage(message);
            }
        });
    }

    private void showMessage(String message) {
        SnackbarUtils.with(mGameBoard).setDuration(SnackbarUtils.LENGTH_LONG)
                .setMessage(message).show();
    }

    @Override
    public void postShowMessage(int messageId) {
        postShowMessage(getString(messageId));
    }

    @Override
    public void postStartThink() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGameProgress.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void postEndThink() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGameProgress.setVisibility(View.GONE);
            }
        });
    }
}
