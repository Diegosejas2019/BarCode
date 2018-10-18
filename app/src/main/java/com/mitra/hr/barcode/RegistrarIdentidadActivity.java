package com.mitra.hr.barcode;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class RegistrarIdentidadActivity extends AppCompatActivity {
    //private static String url = "http://10.0.2.2/api/Employees/GetEmployees/63";
    //private static String url_barcode = "http://10.0.2.2/api/Employees/RegisterBarCode";
    private static String url = "https://www.mitra.com.ar/barcode/api/Employees/GetEmployees/";
    private static String url_barcode = "https://www.mitra.com.ar/barcode/api/Employees/RegisterBarCode";

    private ProgressDialog pDialog;
    ArrayList<Empleados> empleados;
    ArrayList<String> ListaEmpleados;
    JSONArray jsonarray;
    JSONObject jsonobject;
    JSONParser jParser = new JSONParser();
    Spinner spinner;
    Button Capturar;
    private Integer Item;
    private Integer IdUser;
    private Integer IdEnterprise;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_identidad);
        new ObtenerEmpleados().execute();

        Bundle b = getIntent().getExtras();
        int value = -1; // or other values
        if(b != null){
            IdUser = b.getInt("key");
            IdEnterprise = b.getInt("idEnterprise");
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

        Capturar = (Button) findViewById(R.id.btnCapturar);
        Capturar.setVisibility(View.GONE);
        Capturar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator scanIntegrator = new IntentIntegrator(RegistrarIdentidadActivity.this);
                scanIntegrator.setPrompt("Scan a barcode");
                scanIntegrator.setBeepEnabled(true);
                scanIntegrator.setOrientationLocked(true);
                scanIntegrator.setBarcodeImageEnabled(true);
                scanIntegrator.initiateScan();
            }
        });
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

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        String scanContent = scanningResult.getContents().toString();
        Empleados empleado = empleados.get(Item);
        Integer IDEmpleado = empleado.getIdEmpleado();

        ArrayList<String> passing = new ArrayList<String>();
        passing.add(scanContent);
        passing.add(IDEmpleado.toString());

        String resultado = "";
        try {
            if (isConnectedToInternet())
            {
                resultado = new GuardarRegistro().execute(passing).get();
            }else
                {
                    Toast.makeText(RegistrarIdentidadActivity.this, "Necesita conexión", Toast.LENGTH_LONG).show();
                }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace() ;
        }
        Toast.makeText(RegistrarIdentidadActivity.this, resultado, Toast.LENGTH_LONG).show();
        finish();
    }

    class GuardarRegistro extends AsyncTask<ArrayList<String>, String, String> {
        protected String doInBackground(ArrayList<String>... args) {
            ArrayList<String> result = args[0];
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
            nameValuePairs.add(new BasicNameValuePair("ScanIdentification", result.get(0)));
            nameValuePairs.add(new BasicNameValuePair("IdEmployee", result.get(1)));
            nameValuePairs.add(new BasicNameValuePair("IdUser", IdUser.toString()));

            String Resultado="";
            JSONObject json = jParser.makeHttpRequest(url_barcode, "POST", nameValuePairs);

            Log.d("All Products: ", json.toString());
            try {
                Boolean success = json.getBoolean("Success");

                if (success) {
                    Resultado = "Registro guardado!";
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
    }

    class ObtenerEmpleados extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RegistrarIdentidadActivity.this);
            pDialog.setMessage("Obteniendo empleados...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... args) {
            empleados = new ArrayList<Empleados>();
            ListaEmpleados = new ArrayList<String>();
            List params = new ArrayList();

            JSONObject json = jParser.makeHttpRequest(url + IdEnterprise.toString(), "GET", params);
            ListaEmpleados.add("Seleccione empleado..");
            Empleados listaEmpleados1 = new Empleados();

            listaEmpleados1.setIdEmpleado(0);
            listaEmpleados1.setNombre("Seleccione empleado..");
            empleados.add(listaEmpleados1);

            try {
                jsonarray = json.getJSONArray("Employees");
                for (int i = 0; i < jsonarray.length(); i++) {
                    jsonobject = jsonarray.getJSONObject(i);

                    Empleados listaEmpleados = new Empleados();
                    listaEmpleados.setIdEmpleado(jsonobject.optInt("IdEmployee"));
                    listaEmpleados.setNombre(jsonobject.optString("NameEmployee"));
                    listaEmpleados.setApellido(jsonobject.optString("SurNameEmployee"));
                    empleados.add(listaEmpleados);

                    ListaEmpleados.add(jsonobject.optString("EmployeeFullName"));
                }
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
            spinner = (Spinner) findViewById(R.id.spinner);
            spinner
                    .setAdapter(new ArrayAdapter<String>(RegistrarIdentidadActivity.this,
                            android.R.layout.simple_spinner_dropdown_item,
                            ListaEmpleados));
            spinner.setSelection(0,false);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1,
                                           int position, long arg3) {
                    if(position > 0){
                        //String Item = spinner.getItemAtPosition(position).toString();
                        Item = position;
                        Capturar.setVisibility(View.VISIBLE);
                    }
                    else {
                        Toast.makeText(RegistrarIdentidadActivity.this, "Debe Seleccionar una empresa.", Toast.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
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
