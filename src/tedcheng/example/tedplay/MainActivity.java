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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class MainActivity extends Activity {
	private SimpleMusicPlayerService smpService = null;
	ImageView lsib, nsib, pib;
	TextView mntv;
	Handler handler = new Handler();
	SeekBar seekbar;
	int count;
	private List<Map<String, Object>> mDataList = new ArrayList<Map<String, Object>>();

	private ServiceConnection sc = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			smpService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			smpService = ((SimpleMusicPlayerService.SMPlayerBinder) service)
					.getService();
			// restore current playing info
			if (smpService.getState() == true) {
				count = smpService.getCount();
				mntv.setText(mDataList.get(count).get("name").toString());
				handler.post(r);
				pib.setImageResource(R.drawable.stopmusic);
			}
			// TODO: updateByStatus();
		}
	};

	// Generate Music List
	private void generateListView() {
		List<File> list = new ArrayList<File>();
		int temp = 1;
		// Acquire all the songs from sdcard
		// findAll(Environment.getExternalStorageDirectory().toString(),list);
		findAll("/storage/sdcard1/Kugou", list);
		//findAll("/mnt/sdcard", list);
		// sort out the songs
		Collections.sort(list);
		for (File file : list) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("name", temp + "." + file.getName());
			map.put("path", file.getAbsoluteFile());
			mDataList.add(map);
			temp++;
		}
	}

	private void findAll(String path, List<File> list) {
		File mFile = new File(path);
		File[] subFiles = mFile.listFiles();
		if (subFiles != null)
			for (File subFile : subFiles) {
				if (subFile.isFile() && subFile.getName().endsWith(".mp3")) {
					list.add(subFile);
				}
			}
		else
			Toast.makeText(getBaseContext(), "No mp3 files found,check your sdcard", Toast.LENGTH_LONG)
					.show();
	}

	// Update UI
	Runnable r = new Runnable() {

		@Override
		public void run() {
			seekbar.setProgress(seekbar.getMax()
					* smpService.getCurrentPosition()
					/ smpService.getDuration());
			handler.postDelayed(r, 1000);
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		lsib = (ImageView) findViewById(R.id.lastoneib);
		nsib = (ImageView) findViewById(R.id.nextoneib);
		pib = (ImageView) findViewById(R.id.play);
		mntv = (TextView) findViewById(R.id.mntv);
		seekbar = (SeekBar) findViewById(R.id.seekbar);
		generateListView();

		// use adapter inflate listview
		final SimpleAdapter adapter = new SimpleAdapter(this, mDataList,
				R.layout.listitem, new String[] { "name" },
				new int[] { R.id.name });
		final ListView list = (ListView) findViewById(R.id.lv);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				count = position;
				if (!mDataList.isEmpty()) {
					count=position;
					mntv.setText(mDataList.get(position).get("name").toString());
					smpService.playOrPause(mDataList.get(position).get("path")
							.toString(), true);
					handler.post(r);
					pib.setImageResource(R.drawable.stopmusic);
				}
			}
		});
		list.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					final int position, long id) {
				AlertDialog.Builder builder = new Builder(MainActivity.this);
				builder.setMessage("Do you want to delete this song?");

				builder.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								if(!mDataList.isEmpty()){
									if(position==count){
										smpService.stop();
										count=0;
										handler.removeCallbacks(r);
										pib.setImageResource(R.drawable.playmusic);
										Toast.makeText(getBaseContext(), "Song is deleted", Toast.LENGTH_SHORT).show();
									}
									mDataList.remove(position);
									adapter.notifyDataSetChanged();	
								}
							}
						});
				builder.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
				builder.create().show();
				return true;
			}
		});

		// bind the service
		Intent bindintent = new Intent(getBaseContext(),
				SimpleMusicPlayerService.class);
		startService(bindintent);
		bindService(bindintent, sc, BIND_AUTO_CREATE);

		pib.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!mDataList.isEmpty()) {
					smpService.playOrPause(mDataList.get(count).get("path")
							.toString(), false);
					mntv.setText(mDataList.get(count).get("name").toString());
					handler.post(r);
					if (smpService.getState())
						pib.setImageResource(R.drawable.stopmusic);
					else
						pib.setImageResource(R.drawable.playmusic);
				}
				else Toast.makeText(getBaseContext(), "list is empty", Toast.LENGTH_SHORT).show();
				// TODO: updateByStatus();
			}
		});
		lsib.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!mDataList.isEmpty()) {
					if (count != 0)
						count--;
					smpService.playOrPause(mDataList.get(count).get("path")
							.toString(), true);
					mntv.setText(mDataList.get(count).get("name").toString());
					handler.post(r);
					pib.setImageResource(R.drawable.stopmusic);
				}
				else Toast.makeText(getBaseContext(), "list is empty", Toast.LENGTH_SHORT).show();
				// TODO: updateByStatus();
			}
		});
		nsib.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!mDataList.isEmpty()) {
					if (count+1 < mDataList.size())
						count++;
					smpService.playOrPause(mDataList.get(count).get("path")
							.toString(), true);
					mntv.setText(mDataList.get(count).get("name").toString());
					handler.post(r);
					pib.setImageResource(R.drawable.stopmusic);
				}
				else Toast.makeText(getBaseContext(), "list is empty", Toast.LENGTH_SHORT).show();
				// TODO: updateByStatus();
			}
		});

		// Sychronize seekbar
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (fromUser) {
					float temp = (float) progress / (float) seekbar.getMax();
					smpService.setCurrentPosition(temp);
					Toast.makeText(getBaseContext(), "" + progress + "%",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		// TODO:if(smpService==null)
		// Toast.makeText(getBaseContext(), "NULL", Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onDestroy() {
		smpService.backupCount(count);
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
		switch (item.getItemId()) {
		case 0:
			AlertDialog.Builder builder = new Builder(MainActivity.this);
			builder.setMessage("Are you sure to exit?");

			builder.setPositiveButton("Yes",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							Intent intent = new Intent(getBaseContext(),
									SimpleMusicPlayerService.class);
							stopService(intent);
							onDestroy();
							System.exit(0);
						}
					});
			builder.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});

			builder.create().show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}
}
