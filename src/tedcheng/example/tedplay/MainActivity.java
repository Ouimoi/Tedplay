package tedcheng.example.tedplay;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class MainActivity extends Activity {
	private SimpleMusicPlayerService smpService = null;
	ImageButton lsib, nsib, pib;
	TextView mntv;
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
		findAll("/mnt/sdcard",list);
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
		else Toast.makeText(getBaseContext(), "sdø®∂¡»° ß∞‹", Toast.LENGTH_LONG).show();
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		lsib = (ImageButton) findViewById(R.id.lastoneib);
		nsib = (ImageButton) findViewById(R.id.nextoneib);
		pib = (ImageButton) findViewById(R.id.play);
		mntv=(TextView)findViewById(R.id.mntv);
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
				mntv.setText(mDataList.get(position).get("name").toString());
				//TODO:how to restart?
				smpService.playOrPause(mDataList.get(position).get("path").toString());
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
				smpService.playOrPause(mDataList.get(0).get("name").toString());
				// TODO: updateByStatus();
			}
		});
		pib.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				smpService.playOrPause(mDataList.get(0).get("name").toString());
				// TODO: updateByStatus();
			}
		});
	}

	@Override
	protected void onDestroy() {
		unbindService(sc);
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
