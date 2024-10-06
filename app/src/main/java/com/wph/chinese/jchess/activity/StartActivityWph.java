package com.wph.chinese.jchess.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.hzy.chinese.jchess.R;
import com.wph.chinese.jchess.game.GameConfigWph;
import com.wph.chinese.jchess.xqwlight.PositionWph;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class StartActivityWph extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        findViewById(R.id.btnStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadBookAndStartGame();
            }
        });
    }


    private void loadBookAndStartGame() {
        Observable.create(new ObservableOnSubscribe<Boolean>() {
                    @Override
                    public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                        InputStream is = getAssets().open(GameConfigWph.DAT_ASSETS_PATH);
                        boolean result = PositionWph.loadBook(is);
                        emitter.onNext(result);
                        emitter.onComplete();
                    }
                }).delay(GameConfigWph.SPLASH_DELAY_MILLISECONDS, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean loadOk) throws Exception {
                        Intent intent = new Intent(StartActivityWph.this, MainActivityWph.class);
                        startActivity(intent);
                        finish();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
    }
}
