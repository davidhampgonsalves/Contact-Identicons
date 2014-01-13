package com.davidhampgonsalves.contactidenticons;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import com.davidhampgonsalves.identicon.HashGeneratorInterface;
import com.davidhampgonsalves.identicon.IdenticonGenerator;
import com.davidhampgonsalves.identicon.MessageDigestHashGenerator;

import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.widget.Adapter;
import android.widget.Toast;

public class IdenticonSetterTask extends AsyncTask<Set<Integer>, Integer, Void> {

	MainActivity mainActivity;
	ProgressDialog progressBar;

	public IdenticonSetterTask(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();

		int selectedContactCount = mainActivity.selectedContacts.size();
		progressBar = new ProgressDialog(mainActivity);
		progressBar.setMessage("Generating high quality identicons");
		progressBar.setMax(selectedContactCount);
		progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressBar.setCancelable(false);
		progressBar.setIndeterminate(false);
		progressBar.show();
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);

		//hide loader
		progressBar.dismiss();
		
		Toast.makeText(mainActivity, "Identicons were applied to selected contacts.", 2);
		
		// reselect selected elements
		mainActivity.restartLoader();
	}
	
	@Override
	protected void onProgressUpdate(Integer... progress) {
		super.onProgressUpdate(progress);

		// update progress bar
		progressBar.incrementProgressBy(progress[0]);
	}

	@Override
	protected Void doInBackground(Set<Integer>... selectedContacts) {

		Adapter mAdapter = mainActivity.getListAdapter();
		ContentResolver contentResolver = mainActivity.getContentResolver();
		Iterator<Integer> it = mainActivity.selectedContacts.iterator();
		while (it.hasNext()) {
			Integer i = it.next();
			ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

			Cursor c = (Cursor) mAdapter.getItem(i);
			int rawContactId = Integer.parseInt(c.getString(c
					.getColumnIndex("name_raw_contact_id")));
			byte[] identiconData = generateIdenticonBytes(c.getString(c
					.getColumnIndex(Contacts.DISPLAY_NAME_PRIMARY)));

			int photoRow = -1;
			String where = ContactsContract.Data.RAW_CONTACT_ID + " = "
					+ rawContactId + " AND " + ContactsContract.Data.MIMETYPE
					+ " =='"
					+ ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
					+ "'";

			Cursor cursor = mainActivity.getContentResolver().query(
					ContactsContract.Data.CONTENT_URI, null, where, null, null);
			if (cursor.moveToFirst())
				photoRow = cursor.getInt(cursor
						.getColumnIndex(ContactsContract.Data._ID));
			cursor.close();

			// Build content operations to insert/update contact photos
			ContentProviderOperation.Builder opsBuilder;
			if (photoRow == 0) {
				opsBuilder = ContentProviderOperation.newInsert(
						ContactsContract.Data.CONTENT_URI).withValue(
						ContactsContract.CommonDataKinds.Photo.PHOTO,
						identiconData);
			} else {
				opsBuilder = ContentProviderOperation
						.newUpdate(ContactsContract.Data.CONTENT_URI)
						.withSelection(ContactsContract.Data._ID + " = ?",
								new String[] { Integer.toString(photoRow) })
						.withValue(ContactsContract.Data.IS_SUPER_PRIMARY, 1)
						.withValue(ContactsContract.Data.DATA15, identiconData);
			}

			opsBuilder.withValue(ContactsContract.Data.RAW_CONTACT_ID,
					rawContactId).withValue(ContactsContract.Data.MIMETYPE,
					ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);

			ops.add(opsBuilder.withYieldAllowed(true).build());

			try {
				contentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
				publishProgress(1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	private byte[] generateIdenticonBytes(String contactName) {

		HashGeneratorInterface hashGenerator = new MessageDigestHashGenerator(
				"MD5");
		Bitmap identicon = IdenticonGenerator.generate(contactName,
				hashGenerator);

		identicon = Bitmap.createScaledBitmap(identicon, 400, 400, false);

		ByteArrayOutputStream identiconByteStream = new ByteArrayOutputStream();
		identicon.compress(Bitmap.CompressFormat.PNG, 20, identiconByteStream);

		return identiconByteStream.toByteArray();
	}
}
