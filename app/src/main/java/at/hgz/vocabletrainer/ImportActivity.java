package at.hgz.vocabletrainer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import at.hgz.vocabletrainer.csv.CsvUtil;
import at.hgz.vocabletrainer.json.JsonUtil;
import at.hgz.vocabletrainer.xml.XmlUtil;

public class ImportActivity extends ListActivity {
	
	private State state;

	private static class FileRow {
		public File file;
		public String dictionary;
	}

	private List<FileRow> list = new ArrayList<>();
	private FileArrayAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_import);

		Intent intent = getIntent();
		state = TrainingApplication.getState(intent.getIntExtra(State.STATE_ID, -1));
				 
		if (state.getCurrentDirectory() == null) {
			File dir = getSDCardDir(this);
			state.setCurrentDirectory(dir);
		}
		TextView currentPath = (TextView) findViewById(R.id.currentPath);
		currentPath.setText("" + state.getCurrentDirectory());
		loadFiles();
		adapter = new FileArrayAdapter(this, R.layout.import_item, list);
		setListAdapter(adapter);
	}

	public static File getSDCardDir(Context context) {
		// return context.getExternalFilesDir(null);
		File[] dirs = context.getExternalFilesDirs(null);
		return dirs[dirs.length - 1];
	}

	private void loadFiles() {
		File dir = state.getCurrentDirectory();
		list.clear();
		File[] files = dir.listFiles(new FilenameFilter() {
			private Pattern p = Pattern.compile("^.*\\.(vt|vtj|vtc)$", Pattern.CASE_INSENSITIVE);
			@Override
			public boolean accept(File dir, String filename) {
				return p.matcher(filename.toLowerCase(Locale.US)).matches();
			}

		});
		if (files == null) {
			files = new File[0];
		}
		Arrays.sort(files, new Comparator<File>() {
			@Override
			public int compare(File c1, File c2) {
				return c2.compareTo(c1);
			}
		});
		for (File file : files) {
			FileRow fileRow = new FileRow();
			fileRow.file = file;
			try {
				InputStream in = new FileInputStream(file);
				byte[] dictionaryBytes = IOUtils.toByteArray(in);
				String dictionaryName;
				if (file.getName().toLowerCase().endsWith(".vtj")) {
					dictionaryName = JsonUtil.getInstance().unmarshall(dictionaryBytes).getDictionary().getName();
				} else if (file.getName().toLowerCase().endsWith(".vtc")) {
					dictionaryName = CsvUtil.getInstance().unmarshall(dictionaryBytes).getDictionary().getName();
				} else {
					dictionaryName = XmlUtil.getInstance().unmarshall(dictionaryBytes).getDictionary().getName();
				}
				fileRow.dictionary = dictionaryName;
			} catch (Exception ex) {
				fileRow.dictionary = "(X_X)";
				Log.d("VocableTrainer", "Error loading dictionary: " + ex.getMessage(), ex);
			}
			list.add(fileRow);
		}
	}

	private void deleteFile(final FileRow fileRow) {

		Resources resources = getApplicationContext().getResources();
		String confirmDeleteDictionaryTitle = resources.getString(R.string.confirmDeleteDictionaryTitle);
		String confirmDeleteDictionaryText = resources.getString(R.string.confirmDeleteDictionaryText);
		
		new AlertDialog.Builder(this)
		.setTitle(confirmDeleteDictionaryTitle)
		.setMessage(confirmDeleteDictionaryText)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
				if (fileRow.file.delete()) {
					Resources resources = getApplicationContext().getResources();
					String text = resources.getString(R.string.deletingDictionary);
					Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
					toast.show();
					adapter.remove(fileRow);
				}
		    }})
		.setNegativeButton(android.R.string.no, null).show();
	}
	
	private class FileArrayAdapter extends ArrayAdapter<FileRow> {

		public FileArrayAdapter(Context context, int resource,
				List<FileRow> objects) {
			super(context, resource, objects);
		}

		private class ViewHolder {
			public ImageButton buttonDelete;
			public View listItem;
			public TextView listItemName;
			public TextView listItemDictionary;
			public FileRow fileRow;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			FileRow fileRow = getItem(position);

			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(
						R.layout.import_item, parent, false);
				final ViewHolder vh = new ViewHolder();
				vh.buttonDelete = (ImageButton) convertView.findViewById(R.id.buttonDelete);
				vh.listItem = (View) convertView.findViewById(R.id.listItem);
				vh.listItemName = (TextView) convertView.findViewById(R.id.listItemName);
				vh.listItemDictionary = (TextView) convertView.findViewById(R.id.listItemDictionary);
				
				vh.buttonDelete.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						deleteFile(vh.fileRow);
					}
				});
				OnClickListener selectFileListener = new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent resultIntent = new Intent();
						String mimeType = VocableTrainerProvider.getMimeType(vh.fileRow.file.getName());
						resultIntent.setDataAndType(Uri.fromFile(vh.fileRow.file), mimeType);
						setResult(Activity.RESULT_OK, resultIntent);
						ImportActivity.this.finish();
					}
				};
				vh.listItem.setOnClickListener(selectFileListener);
				vh.listItemName.setOnClickListener(selectFileListener);
				vh.listItemDictionary.setOnClickListener(selectFileListener);
				convertView.setTag(vh);
			}

			ViewHolder vh = (ViewHolder) convertView.getTag();
			vh.fileRow = fileRow;
			vh.listItemName.setText(fileRow.file.getName());
			vh.listItemDictionary.setText(fileRow.dictionary);

			return convertView;
		}

	}
}
