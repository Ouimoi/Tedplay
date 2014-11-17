package tedcheng.example.tedplay;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class MainActivity extends Activity {
	private SimpleMusicPlayerService smpService=null;
	ImageButton lsib, nsib, pib;
	TextView mntv;
	Handler handler=new Handler();
	SeekBar seekbar;
	int count=0;
	private List<Map<String,Object>> mDataList = new ArrayList<Map<String,Object>>();
	
	private ServiceConnection sc = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			smpService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			smpService = ((SimpleMusicPlayerService.SMPlayerBinder) service)
					.getService();
			// TODO: updateByStatus();
		}
	};
	
	//Generate Music List
	private void generateListView(){
		List<File> list =new ArrayList<File>();
		int temp = 1;
		//Acquire all the songs from sdcard
		//findAll(Environment.getExternalStorageDirectory().toString(),list);
		findAll("/storage/sdcard1/Kugou",list);
		//sort out the songs
		Collections.sort(list);
		for(File file:list){
			Map<String,Object> map =new HashMap<String, Object>();
			map.put("name", temp+"."+file.getName());
			map.put("path",file.getAbsoluteFile());
			mDataList.add(map);
			temp++;
		}
	}
	private void findAll(String path,List<File> list){
		File mFile=new File(path);
		File[] subFiles=mFile.listFiles();
		if(subFiles != null)
			for(File subFile: subFiles){
				if(subFile.isFile()&&subFile.getName().endsWith(".mp3")){
					list.add(subFile);
					}
			}
		else Toast.makeText(getBaseContext(), "sd¿¨¶ÁÈ¡Îª¿Õ", Toast.LENGTH_LONG).show();
	}
	//Update UI
	Runnable r=new Runnable(){
		
		@Override
		public void run() {
			seekbar.setProgress(seekbar.getMax()*smpService.getCurrentPosition()/smpService.getDuration());
			handler.postDelayed(r,1000);	
		}
		
	};
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		lsib = (ImageButton) findViewById(R.id.lastoneib);
		nsib = (ImageButton) findViewById(R.id.nextoneib);
		pib = (ImageButton) findViewById(R.id.play);
		mntv=(TextView)findViewById(R.id.mntv);		
		seekbar=(SeekBar)findViewById(R.id.seekbar);
		generateListView();
		
		//use adapter inflate listview
		SimpleAdapter adapter= new SimpleAdapter(this, mDataList,
				R.layout.listitem,
				new String[]{"name"},
				new int[]{R.id.name});
		ListView list = (ListView)findViewById(R.id.lv);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				count=position;
				mntv.setText(mDataList.get(position).get("name").toString());
				smpService.playOrPause(mDataList.get(position).get("path").toString(),true);
				handler.post(r);
			}
		//TODO:longclick	
		});

		// bind the service
		Intent bindintent = new Intent(getBaseContext(),
				SimpleMusicPlayerService.class);
		startService(bindintent);
		bindService(bindintent, sc, BIND_AUTO_CREATE);
		
		pib.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				smpService.playOrPause(mDataList.get(count).get("path").toString(),false);
				mntv.setText(mDataList.get(count).get("name").toString());
				handler.post(r);
				// TODO: updateByStatus();
			}
		});
		lsib.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(count!=0)
						count--;
				smpService.playOrPause(mDataList.get(count).get("path").toString(),true);
				mntv.setText(mDataList.get(count).get("name").toString());
				handler.post(r);
				// TODO: updateByStatus();
			}
		});
		nsib.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(count!=0)
					count++;
				smpService.playOrPause(mDataList.get(count).get("path").toString(),true);
				mntv.setText(mDataList.get(count).get("name").toString());
				handler.post(r);
				// TODO: updateByStatus();
			}
		});
		//Sychronize seekbar
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(fromUser){
					float temp=(float)progress/(float)seekbar.getMax();
					smpService.setCurrentPosition(temp);
					Toast.makeText(getBaseContext(), ""+progress+"%", Toast.LENGTH_SHORT).show();}
				
			}
		});
	}

	@Override
	protected void onDestroy() {
		unbindService(sc);
		handler.removeCallbacks(r);
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);
		menu.add(Menu.NONE, 0, Menu.NONE, "Exit");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return false;
//		switch (item.getItemId()) {
//		case 0:
//			AlertDialog.Builder builder = new Builder(MainActivity.this);
//			builder.setMessage("exit");
//
//			builder.setPositiveButton("Yes", new OnClickListener() {
//
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					dialog.dismiss();
//					onDestroy();
//					System.exit(0);
//				}
//			});
//			builder.setNegetiveButton("cancel", new OnClickListener() {
//
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					dialog.dismiss();
//				}
//			});
//			builder.create().show();
//			return true;
//		default:
//			return super.onOptionsItemSelected(item);
//		}

	}
}
