package org.kman.test.AndroidNContextMenu;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mListView = (ExpandableListView) findViewById(android.R.id.list);

		final TestAdapter adapter = new TestAdapter(this);
		mListView.setAdapter(adapter);

		registerForContextMenu(mListView);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		super.onCreateContextMenu(menu, v, menuInfo);

		if (menuInfo instanceof ExpandableListView.ExpandableListContextMenuInfo) {

			menu.setHeaderTitle("Context menu");

			final MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.context_menu, menu);

		}
	}

	private static class TestAdapter extends BaseExpandableListAdapter {
		TestAdapter(Context context) {
			mContext = context;
			mInflater = LayoutInflater.from(mContext);
		}

		@Override
		public int getGroupCount() {
			return 5;
		}

		@Override
		public int getChildrenCount(int i) {
			return 3;
		}

		@Override
		public Object getGroup(int i) {
			return this;
		}

		@Override
		public Object getChild(int i, int i1) {
			return this;
		}

		@Override
		public long getGroupId(int i) {
			return i;
		}

		@Override
		public long getChildId(int i, int i1) {
			return i + i1;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
			View v = view;
			if (v == null) {
				v = mInflater.inflate(android.R.layout.simple_expandable_list_item_1, viewGroup, false);
			}

			final TextView tv = (TextView) v.findViewById(android.R.id.text1);
			tv.setText("Group " + i);

			return v;
		}

		@Override
		public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
			View v = view;
			if (v == null) {
				v = mInflater.inflate(android.R.layout.simple_expandable_list_item_1, viewGroup, false);
			}

			final TextView tv = (TextView) v.findViewById(android.R.id.text1);
			tv.setText("Group " + i + ", child " + i1);

			return v;
		}

		@Override
		public boolean isChildSelectable(int i, int i1) {
			return true;
		}

		private Context mContext;
		private LayoutInflater mInflater;
	}

	private ExpandableListView mListView;
}
