package com.mitra.hr.barcode;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;


import static android.content.Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP;


public class HomeActivity extends AppCompatActivity {
    Spinner spinner;
    ArrayList<EnterPrises> enterprises;
    ArrayList<String> ListaEnterprises;
    JSONArray jsonarray;
    JSONObject jsonobject;
    private Integer IDUser;
    private Integer IDEnterprise;
    //private static String url = "http://10.0.2.2/api/enterprises/getenterprises/96/5";
    //private static String url_Accesos = "http://10.0.2.2/api/Acceses/HasAccess";
    private static String url_Accesos = "https://www.mitra.com.ar/barcode/api/Acceses/HasAccess";
    private static String url = "https://www.mitra.com.ar/barcode/api/enterprises/getenterprises/";
    private ProgressDialog pDialog;
    JSONParser jParser = new JSONParser();
    Button btnRegistrar;
    Button btnLeer;
    TextView txtTitulo;
    public static final String MY_PREFS_NAME = "MyPrefsFile";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        try {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            //setSupportActionBar(toolbar);

            //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            //getSupportActionBar().setDisplayShowHomeEnabled(true);
           // getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        }
        catch (Exception e )
        {
            String mensaje = e.getMessage();
        }


        Bundle b = getIntent().getExtras();
        int value = -1; // or other values
        if(b != null){
            IDUser = b.getInt("key");
        }

        new ObtenerEmpresas().execute();

        btnRegistrar = (Button) findViewById(R.id.btnRegistrar);
        txtTitulo = (TextView) findViewById(R.id.txtTitulo);
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(HomeActivity.this, RegistrarIdentidadActivity.class);
                myIntent.addFlags(FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                myIntent.putExtra("key", IDUser);
                myIntent.putExtra("idEnterprise", IDEnterprise);
                HomeActivity.this.startActivity(myIntent);
            }
        }

        );
        btnRegistrar.setVisibility(View.GONE);
        btnLeer = (Button)  findViewById(R.id.btnLeerID);
        btnLeer.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent myIntent = new Intent(HomeActivity.this, ReadCodeActivity.class);
                                                myIntent.addFlags(FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                                                myIntent.putExtra("key", IDUser); //Optional parameters
                                                myIntent.putExtra("idEnterprise", IDEnterprise);
                                                HomeActivity.this.startActivity(myIntent);
                                            }
                                        });
        btnLeer.setVisibility(View.GONE);
        //btnLeer.setVisibility(View.VISIBLE);
    }

    class ObtenerEmpresas extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(HomeActivity.this);
            pDialog.setMessage("Obteniendo empresas...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... args) {
            enterprises = new ArrayList<EnterPrises>();

            ListaEnterprises = new ArrayList<String>();
            List params = new ArrayList();

            JSONObject json = jParser.makeHttpRequest(url  + IDUser + "/5", "GET", params);
            ListaEnterprises.add("Seleccione..");
            EnterPrises listaEnterprises1 = new EnterPrises();

            listaEnterprises1.setID(0);
            listaEnterprises1.setDescripcion("Seleccione empresa..");
            enterprises.add(listaEnterprises1);
            try {

                jsonarray = json.getJSONArray("Enterprises");
                for (int i = 0; i < jsonarray.length(); i++) {
                    jsonobject = jsonarray.getJSONObject(i);

                    EnterPrises listaEnterprises = new EnterPrises();

                    listaEnterprises.setID(jsonobject.optInt("IdEnterprise"));
                    listaEnterprises.setDescripcion(jsonobject.optString("Name"));
                    enterprises.add(listaEnterprises);

                    ListaEnterprises.add(jsonobject.optString("Name"));
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
                    .setAdapter(new ArrayAdapter<String>(HomeActivity.this,
                            android.R.layout.simple_spinner_dropdown_item,
                            ListaEnterprises));
            spinner.setSelection(0, false);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1,
                                           int position, long arg3) {
                    if(position > 0){
                        String Item = spinner.getItemAtPosition(position).toString();
                        btnRegistrar.setVisibility(View.GONE);
                        btnLeer.setVisibility(View.GONE);
                        ConsultarAccesos(position);
                    }
                    else {
                        Toast.makeText(HomeActivity.this, "Debe Seleccionar una empresa.", Toast.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            if(ListaEnterprises.size() == 2){
                btnRegistrar.setVisibility(View.VISIBLE);
                btnLeer.setVisibility(View.VISIBLE);
                spinner.setVisibility(View.GONE);
                txtTitulo.setText("Seleccionar Operación");
            }
        }
    }

    private void ConsultarAccesos(Integer Item){
        EnterPrises empresa = enterprises.get(Item);
        IDEnterprise = empresa.getID();
        ArrayList<String> passing = new ArrayList<String>();
        passing.add(IDUser.toString());
        passing.add(IDEnterprise.toString());
        passing.add("MobileBarCodeReader");
        passing.add("idMobileBarCodeReaderRegister");
        passing.add("5");
        passing.add("RegisterFirstTime");

        try {
            if(new ConsultarAccesos().execute(passing).get()){
            btnRegistrar.setVisibility(View.VISIBLE);}
        } catch (Exception e) {
            e.printStackTrace();
        }
        passing.clear();

        passing.add(IDUser.toString());
        passing.add(IDEnterprise.toString());
        passing.add("MobileBarCodeReader");
        passing.add("idMobileBarCodeReaderRegisterPresence");
        passing.add("5");
        passing.add("RegisterPresence");

        try {
            if(new ConsultarAccesos().execute(passing).get()){
                btnLeer.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (btnRegistrar.getVisibility() == View.GONE && btnLeer.getVisibility() == View.GONE){
            Toast.makeText(HomeActivity.this,"No posee accesos",Toast.LENGTH_LONG).show();
        }
    }

    class ConsultarAccesos extends AsyncTask<ArrayList<String>, Boolean, Boolean> {

        protected Boolean doInBackground(ArrayList<String>... args) {
            ArrayList<String> result = args[0];

            Boolean flag = false;
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(6);
            nameValuePairs.add(new BasicNameValuePair("IdUser", result.get(0)));
            nameValuePairs.add(new BasicNameValuePair("IdEnterprise", result.get(1)));
            nameValuePairs.add(new BasicNameValuePair("NameWebForm", result.get(2)));
            nameValuePairs.add(new BasicNameValuePair("NameAcceso", result.get(3)));
            nameValuePairs.add(new BasicNameValuePair("IdGroupService", result.get(4)));
            nameValuePairs.add(new BasicNameValuePair("HasAccessKey", result.get(5)));

            JSONObject json = jParser.makeHttpRequest(url_Accesos, "POST", nameValuePairs);

            try {

                flag = json.getBoolean("HasAccess");
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return flag;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                SharedPreferences spreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor spreferencesEditor = spreferences.edit();
                spreferencesEditor.clear();
                spreferencesEditor.commit();

                SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                spreferencesEditor = prefs.edit();
                spreferencesEditor.clear();
                spreferencesEditor.commit();
                Intent myIntent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(myIntent);
                finish();
                return true;
            case android.R.id.home:
                /*Intent myIntent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(myIntent);
                finish();*/
                return super.onOptionsItemSelected(item);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public  boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.password_menu, menu);
        return  super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }


}
