package com.mitra.hr.barcode;
import android.app.ActionBar;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import static android.content.Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP;

public class Successful extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_successful);

        try {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
           //getSupportActionBar().setCustomView(R.layout.home_menu);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        catch (Exception e )
        {
            String mensaje = e.getMessage();
        }

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent myIntent = new Intent(Successful.this, MainActivity.class);
        Successful.this.startActivity(myIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            Intent myIntent = new Intent(Successful.this, MainActivity.class);
            myIntent.addFlags(FLAG_ACTIVITY_PREVIOUS_IS_TOP);

            startActivity(myIntent);
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }
}
