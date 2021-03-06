package tedcheng.example.tedplay;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

public class SimpleMusicPlayerService extends Service {
	MediaPlayer mPlayer = new MediaPlayer();
	int countbackup=0;
	static enum STATE {
		PLAYING, PAUSE, IDLE
	};
	STATE state=STATE.IDLE;
	private final SMPlayerBinder binder = new SMPlayerBinder();

	public void playOrPause(String file,boolean restart){
		//Initial play
		if(state==STATE.IDLE||restart)
			try{
				mPlayer.reset();
				mPlayer.setDataSource(file);
				mPlayer.prepare();
				mPlayer.start();
				state=STATE.PLAYING;
			}
		 catch(IOException e){
			//don't know what to do
		}
		else if(state == STATE.PLAYING){
			mPlayer.pause();
			state=STATE.PAUSE;
		}
		else {
			mPlayer.start();
			state=STATE.PLAYING;
		}
	}
	public void stop(){
		mPlayer.reset();
		state=STATE.IDLE;
	}
	
	//SeekBar Refresh
	public int getDuration(){
		if(state==STATE.IDLE)
			return 0;
		return mPlayer.getDuration();
	}
	public int getCurrentPosition(){
		if(state==STATE.IDLE)
			return 0;
		return mPlayer.getCurrentPosition();
	}
	public boolean getState(){
		return mPlayer.isPlaying();
	}
	public void setCurrentPosition(float percentage){
		mPlayer.seekTo((int)(mPlayer.getDuration()*percentage));
	}
	public void backupCount(int count){
		countbackup=count;
	}
	public int getCount(){
		return countbackup;
	}
	
	//Bind service
	@Override
	public IBinder onBind(Intent intent) {
		return this.binder;
	}

	public class SMPlayerBinder extends Binder {
		SimpleMusicPlayerService getService() {
			return SimpleMusicPlayerService.this;
		}
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		mPlayer.release();
		super.onDestroy();
	}

}
