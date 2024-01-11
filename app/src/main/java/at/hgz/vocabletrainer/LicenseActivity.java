package at.hgz.vocabletrainer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LicenseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);
        setSupportActionBar(findViewById(R.id.license_toolbar));

        Intent intent = getIntent();
        String moduleName = intent.getStringExtra("moduleName");
        String licenseText = intent.getStringExtra("licenseText");

        TextView textViewName = (TextView) findViewById(R.id.textViewName);
        TextView textViewDescription = (TextView) findViewById(R.id.textViewDescription);

        textViewName.setText(moduleName);
        textViewDescription.setText(licenseText);
    }

}
