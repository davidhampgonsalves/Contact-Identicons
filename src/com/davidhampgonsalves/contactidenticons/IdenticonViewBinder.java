package com.davidhampgonsalves.contactidenticons;

import java.util.TreeSet;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter.ViewBinder;

import com.davidhampgonsalves.identicon.HashGeneratorInterface;
import com.davidhampgonsalves.identicon.IdenticonGenerator;
import com.davidhampgonsalves.identicon.MessageDigestHashGenerator;


public class IdenticonViewBinder implements ViewBinder {

	private TreeSet<Integer> selectedContacts;

	public IdenticonViewBinder(TreeSet<Integer> selectedContacts) {
		this.selectedContacts = selectedContacts;
	}

	@Override
	public boolean setViewValue(View view, Cursor cursor, int index) {

		int viewId = view.getId();
		String currentValue = cursor.getString(index);

		if (viewId == R.id.identicon) {
			HashGeneratorInterface hashGenerator = new MessageDigestHashGenerator("MD5");
			Bitmap identicon = IdenticonGenerator.generate(currentValue, hashGenerator);

			identicon = Bitmap.createScaledBitmap(identicon, 50, 50, false);

			((ImageView) view).setImageBitmap(identicon);
			return true;
		}

		if (viewId == R.id.contactPhoto) {
			// use the thumbnail if it exists and use the photo uri if
			// not
			if (currentValue == null)
				currentValue = cursor.getString(cursor.getColumnIndex(Contacts.PHOTO_URI));

			// show the contact photo if one exists or hide the
			// imageView if now
			if (currentValue != null) {
				((ImageView) view).setImageURI(Uri.parse(currentValue));
				((ImageView) view).setVisibility(View.VISIBLE);
			} else
				((ImageView) view).setVisibility(View.INVISIBLE);

			return true;
		}

		if (viewId == R.id.applyIdenticonCheckBox) {
			((CheckBox) view).setChecked(selectedContacts.contains(cursor.getPosition()));
			return true;
		}

		return false;
	}
}