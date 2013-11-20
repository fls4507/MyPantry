package com.example.mptester;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import android.view.MenuItem;

import com.example.mptester.dummy.DummyContent;



/**
 * A list fragment representing a list of PantryItems. This fragment also
 * supports tablet devices by allowing list items to be given an 'activated'
 * state upon selection. This helps indicate which item is currently being
 * viewed in a {@link PantryItemDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class PantryItemListFragment extends ListFragment {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";
	private static final int CAPTURE_IMAGE_ACTIVITY_REQ = 0;
	private Uri fileUri;

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = sDummyCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;
	
	private Context mContext;
	public static final String LOG_NAME = "My Pantry";
	SoapServiceHandler soapServ;

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(String id);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(String id) {
		}
	};

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public PantryItemListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// TODO: replace with a real list adapter.
		setListAdapter(new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
				android.R.layout.simple_list_item_activated_1,
				android.R.id.text1, DummyContent.ITEMS));
		
		setHasOptionsMenu(true); //Control visibility of menu items this fragment
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		mContext = getActivity();
		soapServ = new SoapServiceHandler(mContext);
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		mCallbacks.onItemSelected(DummyContent.ITEMS.get(position).id);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	   inflater.inflate(R.menu.menu_list, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected (MenuItem item){
		// Handle item selection
	    switch (item.getItemId()) {
	        case R.id.menu_import:
	        	//Initalize Camera Intent
	        	launchCamera();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}

	}

	@Override
	public void onPrepareOptionsMenu(Menu menu){
		super.onPrepareOptionsMenu(menu);
		//		((MenuItem)menu.findItem(R.id.menu_filter)).setTitle(filterToString(m_nFilter));
		//		m_vwMenu = menu;
	}
	
	
	/// ************************************ CAMERA STUFF *****************************************
	private void launchCamera(){
		Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File file = getOutputPhotoFile();
		fileUri = Uri.fromFile(getOutputPhotoFile());
		i.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
		startActivityForResult(i, CAPTURE_IMAGE_ACTIVITY_REQ);

		Log.d(LOG_NAME, file.getAbsolutePath());
	}
	private File getOutputPhotoFile() {
		File directory = new File(
				Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				mContext.getPackageName());
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				Log.e(LOG_NAME, "Failed to create storage directory.");
				return null;
			}
		}
		String timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss", Locale.US)
		.format(new Date(0));
		return new File(directory.getPath() + File.separator + "IMG_"
				+ timeStamp + ".jpg");
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQ) {
			if (resultCode == Activity.RESULT_OK) {
				Uri photoUri = null;
				if (data == null) {
					// A known bug here! The image should have saved in fileUri
					toastIt("Image saved successfully");
					photoUri = fileUri;
				} else {
					photoUri = data.getData();
					toastIt("Image saved successfully in: " + data.getData());
				}
				//Make Request to OCR it
				soapServ.getSoapResponse(fileUri.toString(), getByteArrayOfStoredPhoto());
				
			} else if (resultCode == Activity.RESULT_CANCELED) {
				toastIt("Cancelled");
			} else {
				toastIt("Callout for image capture failed!");
			}
		}
	}
	
	public static byte[] convertPhotoToByteArray(String sourcePath) throws IOException {
		File f = new File(sourcePath);
		long l = f.length();
		byte [] buf = new byte[(int) l];
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			InputStream fis = new FileInputStream(sourcePath);

			for (int readNum; (readNum = fis.read(buf)) != -1;) {
				bos.write(buf, 0, readNum);
				Log.i("","read num bytes: "+readNum);
			}
		} catch (IOException e) {
			System.out.println("IO Ex"+e);
		}
		byte[] bytes = bos.toByteArray();
		return bytes;
	}

	public byte[] getByteArrayOfStoredPhoto(){
		byte[] bytes = null;
		String filePath = fileUri.getEncodedPath();
		File imageFile = new File(filePath);
		if (imageFile.exists()){
			try {
				bytes = convertPhotoToByteArray(imageFile.getAbsolutePath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}   	
		return bytes;
	}
	/// ************************************ CAMERA STUFF END *****************************************
	
	private void toastIt(String toastText){
		Toast.makeText(mContext, toastText,
				Toast.LENGTH_LONG).show();
	}
}
