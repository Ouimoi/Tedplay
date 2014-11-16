package tedcheng.example.tedplay;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

public class SimpleMusicPlayerService extends Service {
	MediaPlayer mPlayer = new MediaPlayer();

	enum STATE {
		PLAYING, PAUSE, IDLE
	};
	STATE state=STATE.IDLE;
	private final SMPlayerBinder binder = new SMPlayerBinder();

	public void playOrPause(String file){
		//Initial play
		if(state==STATE.IDLE)
			try{
				mPlayer.reset();
				mPlayer.setDataSource(file);
				Toast.makeText(getBaseContext(),"loading music" , Toast.LENGTH_LONG).show();
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
		try {
			mPlayer.setDataSource("/storage/sdcard1/Kugou/");
			mPlayer.prepare();
		} catch (IOException e) {
			// don't know what to do
		}
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}
