package pt.rmartins.battleships;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;

import com.nuggeta.NuggetaContext;
import com.nuggeta.NuggetaPlug;
import com.nuggeta.network.Message;
import com.nuggeta.ngdl.nobjects.StartResponse;

public class MainActivity extends Activity {

	private NuggetaPlug nuggetaPlug;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//TODO: setContentView(R.layout.activity_main);

		// register main activity on NuggetaContext
		NuggetaContext.register(MainActivity.this);

		// start the NuggetaPlug
		nuggetaPlug = new NuggetaPlug("nuggeta://pt_rmartins_battleships_46e37c39-4062-43d3-9606-84882002aa94");
		nuggetaPlug.start();

		// start the GameLoop Thread which handle received Messages
		Thread gameLoopThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {

					// pump incoming messages
					List<Message> messages = nuggetaPlug.pump();

					for (Message message : messages) {

						if (message instanceof StartResponse) {
							handleStartResponse((StartResponse) message);
						} else {
							Log.i("Sample", "Received unhandled message : " + message);
						}
					}

					try {
						Thread.sleep(10);
					} catch (Exception e) {
					}
				}
			}
		});
		gameLoopThread.start();
	}

	private void handleStartResponse(final StartResponse startResponse) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				//TODO:
				/*
				if (startResponse.getStartStatus() == StartStatus.READY) {
					ImageView nuggetaImageView = (ImageView) findViewById(R.id.nuggetaImageView);
					nuggetaImageView.setImageResource(R.drawable.nuggeta_on);

					TextView nuggetaTextView = (TextView) findViewById(R.id.nuggetaTextView);
					nuggetaTextView.setText(R.string.connection_ready);
				} else if (startResponse.getStartStatus() == StartStatus.WARNED) {
					ImageView nuggetaImageView = (ImageView) findViewById(R.id.nuggetaImageView);
					nuggetaImageView.setImageResource(R.drawable.nuggeta_on);

					TextView nuggetaTextView = (TextView) findViewById(R.id.nuggetaTextView);
					nuggetaTextView.setText(R.string.connection_warned);
				} else if (startResponse.getStartStatus() == StartStatus.REFUSED) {
					TextView nuggetaTextView = (TextView) findViewById(R.id.nuggetaTextView);
					nuggetaTextView.setText(R.string.connection_refused);
				} else if (startResponse.getStartStatus() == StartStatus.FAILED) {
					TextView nuggetaTextView = (TextView) findViewById(R.id.nuggetaTextView);
					nuggetaTextView.setText(R.string.connection_fail);
				}
				*/
			}
		});
	}

	@Override
	public void onBackPressed() {

		AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

		alertDialog.setPositiveButton("Yes", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				doExit();
			}
		});

		alertDialog.setNegativeButton("No", null);

		alertDialog.setMessage("Do you want to exit?");
		alertDialog.setTitle("NuggetaSample");
		alertDialog.show();

	}

	protected void doExit() {
		if (nuggetaPlug != null) {
			nuggetaPlug.stop();
		}

		finish();

		System.exit(0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		com.facebook.Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}

}