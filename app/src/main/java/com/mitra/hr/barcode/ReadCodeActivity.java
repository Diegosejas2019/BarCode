package com.mitra.hr.barcode;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.view.View;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class ReadCodeActivity extends AppCompatActivity {
    private Button mButton;
    private ProgressDialog pDialog;
    JSONParser jParser = new JSONParser();
    private String Contenido;
    private static String url_Barcode = "https://www.mitra.com.ar/barcode/api/Employees/RegisterPresence";
    //private static String url_Barcode = "http://10.0.2.2/api/Employees/RegisterPresence";
    private Integer IdUser;
    // JSON Node names
    private static final String TAG_SUCCESS = "Success";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_code);

        Bundle b = getIntent().getExtras();
        int value = -1; // or other values
        if(b != null){
            IdUser = b.getInt("key");
        }

        try {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        catch (Exception e )
        {
            String mensaje = e.getMessage();
        }

        mButton = (Button) findViewById(R.id.btnCapturar);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator scanIntegrator = new IntentIntegrator(ReadCodeActivity.this);
                scanIntegrator.setPrompt("Scan a barcode");
                scanIntegrator.setBeepEnabled(true);
                scanIntegrator.setOrientationLocked(true);
                scanIntegrator.setBarcodeImageEnabled(true);
                scanIntegrator.initiateScan();
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        String scanContent = scanningResult.getContents().toString();
        Contenido = scanContent;
        String resultado = "";
        try {
            if (isConnectedToInternet())
            {
                resultado = new LoadAllProducts().execute().get();
            }
            else{
                Toast.makeText(ReadCodeActivity.this, "Necesita conexión", Toast.LENGTH_LONG).show();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace() ;
        }
        Toast.makeText(ReadCodeActivity.this, resultado, Toast.LENGTH_LONG).show();
    }

    public boolean isConnectedToInternet(){
        ConnectivityManager connectivity = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
        }
        return false;
    }
    class LoadAllProducts extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ReadCodeActivity.this);
            pDialog.setMessage("Cargando comercios. Por favor espere...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("IdUser", IdUser.toString()));
            nameValuePairs.add(new BasicNameValuePair("ScanIdentification", Contenido));

            String Resultado="";
            JSONObject json = jParser.makeHttpRequest(url_Barcode, "POST", nameValuePairs);

            Log.d("All Products: ", json.toString());
            try {
                 Boolean success = json.getBoolean(TAG_SUCCESS);

                if (success) {
                    Resultado = "Presencia exitosa!";
                }
                else{
                    Resultado = json.getString("ErrorMessage");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Resultado = e.getMessage();
            }
            return Resultado;
        }

        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

}
