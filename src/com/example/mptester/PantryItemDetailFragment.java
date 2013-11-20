package com.example.mptester;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mptester.dummy.DummyContent;

/**
 * A fragment representing a single PantryItem detail screen. This fragment is
 * either contained in a {@link PantryItemListActivity} in two-pane mode (on
 * tablets) or a {@link PantryItemDetailActivity} on handsets.
 */
public class PantryItemDetailFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	/**
	 * The dummy content this fragment is presenting.
	 */
	private DummyContent.DummyItem mItem;
	
	private Activity mContext;
	
	

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public PantryItemDetailFragment() {
	}
	
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
		
		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
			mItem = DummyContent.ITEM_MAP.get(getArguments().getString(
					ARG_ITEM_ID));
		}

		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_pantryitem_detail,
				container, false);

		// Show the dummy content as text in a TextView.
		if (mItem != null) {
			//			((TextView) rootView.findViewById(R.id.pantryitem_detail_container))
			//					.setText(mItem.content);
		}

		





		return rootView;
	}
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_details, menu);
	}
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
		//Have to do stuff usually done in onCreate in here.
		
		Spinner spinner = (Spinner) mContext.findViewById(R.id.unit_selector);

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				mContext, R.array.units, android.R.layout.simple_spinner_item);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				int index = arg0.getSelectedItemPosition();

				// storing string resources into Array
				String[] units = getResources().getStringArray(R.array.units);

				Toast.makeText(mContext, "You have selected : " + units[index], 
						Toast.LENGTH_SHORT).show();

			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// do nothing

			}

		});     
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
	}
}
