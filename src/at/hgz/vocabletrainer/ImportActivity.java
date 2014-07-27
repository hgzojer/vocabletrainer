package at.hgz.vocabletrainer;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class ImportActivity extends ListActivity {

	private List<File> list = new ArrayList<File>();
	private FileArrayAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_import);

		File dir = getExternalFilesDir(null);
		list.clear();
		File[] files = dir.listFiles(new FilenameFilter() {
			private Pattern p = Pattern.compile("^.*\\.vt$");

			@Override
			public boolean accept(File dir, String filename) {
				return p.matcher(filename.toLowerCase(Locale.US)).matches();
			}

		});
		if (files == null) {
			files = new File[0];
		}
		list.addAll(Arrays.asList(files));
		adapter = new FileArrayAdapter(this, R.layout.import_item, list);
		setListAdapter(adapter);
	}

	private class FileArrayAdapter extends ArrayAdapter<File> {

		public FileArrayAdapter(Context context, int resource,
				List<File> objects) {
			super(context, resource, objects);
		}

		private class ViewHolder {
			public ImageButton buttonDelete;
			public ImageButton buttonImport;
			public TextView listItemName;
			public File file;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			File file = getItem(position);

			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(
						R.layout.import_item, parent, false);
				final ViewHolder vh = new ViewHolder();
				vh.buttonDelete = (ImageButton) convertView.findViewById(R.id.buttonDelete);
				vh.buttonImport = (ImageButton) convertView.findViewById(R.id.buttonImport);
				vh.listItemName = (TextView) convertView.findViewById(R.id.listItemName);
				
				vh.buttonDelete.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (vh.file.delete()) {
							FileArrayAdapter.this.remove(vh.file);
						}
					}
				});
				vh.buttonImport.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent resultIntent = new Intent();
						resultIntent.setData(Uri.fromFile(vh.file));
						setResult(Activity.RESULT_OK, resultIntent);
						ImportActivity.this.finish();
					}
				});
				convertView.setTag(vh);
			}

			ViewHolder vh = (ViewHolder) convertView.getTag();
			vh.file = file;
			vh.listItemName.setText(file.getName());

			return convertView;
		}

	}
}
