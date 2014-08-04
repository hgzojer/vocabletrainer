package at.hgz.vocabletrainer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesUtil;

public class AboutActivity extends ListActivity {

	private List<License> list = new ArrayList<License>();

	private LicenseArrayAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		list.clear();
		list.add(new License("vocabletrainer", getLicense(R.raw.vocabletrainer_license)));
		list.add(new License("google-gson", getLicense(R.raw.googlegson_license)));
		list.add(new License("commons-io", getLicense(R.raw.commonsio_license)));
		list.add(new License("simple-xml", getLicense(R.raw.simplexml_license)));
		list.add(new License("google-play-services-lib", GooglePlayServicesUtil
				.getOpenSourceSoftwareLicenseInfo(this)));
		adapter = new LicenseArrayAdapter(this, R.layout.about_item, list);
		setListAdapter(adapter);
	}

	private String getLicense(int id) {
		InputStream in = getResources().openRawResource(id);
		try {
			return IOUtils.toString(in);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		License license = list.get(position);
		Intent intent = new Intent(AboutActivity.this, LicenseActivity.class);
		intent.putExtra("moduleName", license.getModuleName());
		intent.putExtra("licenseText", license.getLicenseText());
		AboutActivity.this.startActivity(intent);
	}

	private class LicenseArrayAdapter extends ArrayAdapter<License> {

		public LicenseArrayAdapter(Context context, int resource,
				List<License> objects) {
			super(context, resource, objects);
		}

		private class ViewHolder {
			public TextView listItemName;
			public License license;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			License license = getItem(position);

			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(
						R.layout.about_item, parent, false);
				final ViewHolder vh = new ViewHolder();
				vh.listItemName = (TextView) convertView
						.findViewById(R.id.listItemName);

				convertView.setTag(vh);
			}

			ViewHolder vh = (ViewHolder) convertView.getTag();
			vh.license = license;
			vh.listItemName.setText(vh.license.getModuleName());

			return convertView;
		}

	}
}
