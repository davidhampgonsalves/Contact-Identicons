package com.davidhampgonsalves.contactidenticons;

import java.util.TreeSet;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;

@SuppressLint("NewApi")
public class MainActivity extends ListActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {

	// This is the Adapter being used to display the list's data
	SimpleCursorAdapter mAdapter;
	TreeSet<Integer> selectedContacts = new TreeSet<Integer>();

	// These are the Contacts rows that we will retrieve
	static final String[] PROJECTION = new String[] { Contacts._ID,
			Contacts.DISPLAY_NAME_PRIMARY, Contacts.PHOTO_THUMBNAIL_URI,
			Contacts.PHOTO_URI, "name_raw_contact_id" };

	// This is the select criteria
	static final String SELECTION = "((" + Contacts.DISPLAY_NAME_PRIMARY
			+ " NOTNULL) AND (" + Contacts.DISPLAY_NAME_PRIMARY
			+ " != '' )) AND " + Contacts.HAS_PHONE_NUMBER + " == 1";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		
		getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		Button applyIdenticons = (Button) findViewById(R.id.applyIdenticonButton);
		applyIdenticons.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				IdenticonSetterTask task = new IdenticonSetterTask(MainActivity.this);
				task.execute(selectedContacts);
			}
		});

		// For the cursor adapter, specify which columns go into which views
		String[] fromColumns = { Contacts.DISPLAY_NAME_PRIMARY,
				Contacts.DISPLAY_NAME_PRIMARY, Contacts.PHOTO_THUMBNAIL_URI,
				Contacts.PHOTO_THUMBNAIL_URI };
		int[] toViews = { R.id.contactName, R.id.identicon, R.id.contactPhoto,
				R.id.applyIdenticonCheckBox };

		// Create an empty adapter we will use to display the loaded data.
		// We pass null for the cursor, then update it in onLoadFinished()
		mAdapter = new SimpleCursorAdapter(this, R.layout.list_item, null,
				fromColumns, toViews, 0);

		mAdapter.setViewBinder(new IdenticonViewBinder(selectedContacts));

		setListAdapter(mAdapter);

		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(0, null, this);
	}

	// Called when a new Loader needs to be created
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// Now create and return a CursorLoader that will take care of
		// creating a Cursor for the data being displayed.
		return new CursorLoader(this, Contacts.CONTENT_URI, PROJECTION,
				SELECTION, null, Contacts.DISPLAY_NAME_PRIMARY);
	}

	// ArrayList<ContactData> contacts = new ArrayList<ContactData>();
	// Called when a previously created loader has finished loading
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		// iterate over the contact results to auto select cotnacts without a
		// display picture
		c.moveToFirst();
		c.moveToPrevious();
		while(c.moveToNext()) {
			String photoUri = c.getString(c.getColumnIndexOrThrow(Contacts.PHOTO_THUMBNAIL_URI));

			if (photoUri == null)
				photoUri = c.getString(c.getColumnIndexOrThrow(Contacts.PHOTO_URI));

			if (photoUri == null)
				selectedContacts.add(c.getPosition());
		}

		mAdapter.swapCursor(c);
	}

	// Called when a previously created loader is reset, making the data
	// unavailable
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		CheckBox checkbox = (CheckBox) v
				.findViewById(R.id.applyIdenticonCheckBox);

		boolean isChecked = !checkbox.isChecked();
		if (isChecked)
			selectedContacts.add(position);
		else
			selectedContacts.remove(position);

		checkbox.setChecked(isChecked);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.select_all_contacts:
			Cursor c = mAdapter.getCursor();
			for (int i = 0; i < c.getCount(); i++)
				selectedContacts.add(i);

			mAdapter.notifyDataSetChanged();
			//getListView().invalidateViews();
			return true;
		case R.id.deselect_all_contacts:
			selectedContacts.clear();
			mAdapter.notifyDataSetChanged();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void restartLoader() {
		selectedContacts.clear();
		getLoaderManager().restartLoader(0, null, this);
		
		mAdapter.notifyDataSetChanged();
	}
}