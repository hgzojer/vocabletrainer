package at.hgz.vocabletrainer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
		list.addAll(Arrays.asList(dir.listFiles()));
		adapter = new FileArrayAdapter(this, R.layout.import_item, list);
		setListAdapter(adapter);
	}
	
	private class FileArrayAdapter extends ArrayAdapter<File> {

		public FileArrayAdapter(Context context, int resource,
				List<File> objects) {
			super(context, resource, objects);
		}

	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {

	       File file = getItem(position);    

	       if (convertView == null) {
	          convertView = LayoutInflater.from(getContext()).inflate(R.layout.import_item, parent, false);
	       }

	       TextView listItemName = (TextView) convertView.findViewById(R.id.listItemName);
	        
	       listItemName.setText(file.getName());

	       return convertView;
	   }		

	}
}
