package com.davidhampgonsalves.contactidenticons;

import java.util.TreeSet;

import android.view.View;


public class ApplyIdenticonsClickListener implements View.OnClickListener {

	private MainActivity mainActivity;

	public ApplyIdenticonsClickListener(TreeSet<Integer> selectedContacts, MainActivity mainActivity) {

		this.mainActivity = mainActivity;
	}

	@Override
	public void onClick(View view) {
		IdenticonSetterTask task = new IdenticonSetterTask(mainActivity);
		task.execute(mainActivity.selectedContacts);
		
		
	}
}

