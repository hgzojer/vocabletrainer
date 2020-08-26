package at.hgz.vocabletrainer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesUtil;

import androidx.core.content.FileProvider;

public class AboutActivity extends ListActivity {

	private List<License> list = new ArrayList<>();

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
			return IOUtils.toString(in, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		License license = list.get(position);
		if (license.getLicenseText().length() > 100) {
			Context context = AboutActivity.this;
			try {
				File licensePath = new File(context.getCacheDir(), "licenses");
				if (!licensePath.exists()) {
					if (!licensePath.mkdirs()) {
						throw new IOException("Error creating dir: " + licensePath);
					}
				}
				String fileName = license.getModuleName().replace('.', '_') + ".txt";
				File licenseFile = new File(licensePath, fileName);
				FileUtils.writeStringToFile(licenseFile, license.getLicenseText(), StandardCharsets.UTF_8);
				Uri contentUri = FileProvider.getUriForFile(context,
						"at.hgz.vocabletrainer.fileprovider",
						licenseFile);
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(contentUri, "text/plain");
				intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				AboutActivity.this.startActivity(intent);
			} catch (IOException e) {
				Log.e("VocableTrainer", "Error loading license: " + e.getMessage(), e);
				String text = context.getResources().getString(R.string.errorLoadingLicense);
				Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
				toast.show();
				e.printStackTrace();
			}
		} else {
			Intent intent = new Intent(AboutActivity.this, LicenseActivity.class);
			intent.putExtra("moduleName", license.getModuleName());
			intent.putExtra("licenseText", license.getLicenseText());
			AboutActivity.this.startActivity(intent);
		}
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
