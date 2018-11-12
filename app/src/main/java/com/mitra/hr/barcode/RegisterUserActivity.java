package com.mitra.hr.barcode;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static android.content.Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP;

public class RegisterUserActivity extends AppCompatActivity {

    JSONParser jParser = new JSONParser();
    private static String url_Servicio = "https://www.mitra.com.ar/pharma/api/login/";
    //private static String url_Servicio = "http://10.0.2.2/api/login/";
    private static final String TAG_SUCCESS = "StatusCode";
    private static final String TAG_USER = "UserName";
    private ProgressDialog pDialog;
    private String Resultado="";
    private AutoCompleteTextView mEmailView;
    private EditText mUserView;
    private EditText mPasswordView;
    private EditText mRePasswordView;
    public static final String MY_PREFS_NAME = "MyPrefsFile";
    private final Pattern hasUppercase = Pattern.compile("[A-Z]");
    private final Pattern hasLowercase = Pattern.compile("[a-z]");
    private final Pattern hasNumber = Pattern.compile("\\d");
    private String mItem;
    Spinner spinner;
    ArrayList<EnterPrises> enterprises;
    ArrayList<String> ListaEnterprises;
    JSONArray jsonarray;
    JSONObject jsonobject;
    private static String url_Accesos = "https://www.mitra.com.ar/barcode/api/Acceses/HasAccess";
    private static String url_enterprises = "https://www.mitra.com.ar/barcode/api/enterprises/getenterprisesgroup";
    Button btnRegistrar,btnAcceder;
    TextView txtTitulo;

    private String mEmail;
    private String mPassword;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.contraseña);
        mRePasswordView = (EditText) findViewById(R.id.reIngresoContraseña);
        mUserView = (EditText) findViewById(R.id.nombreUusario);

        @SuppressLint("WrongViewCast") ImageView imgBtn = (ImageView) findViewById(R.id.imageView1);
        imgBtn.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                switch ( event.getAction() ) {
                    case MotionEvent.ACTION_DOWN:
                        mPasswordView.setInputType(InputType.TYPE_CLASS_TEXT);
                        break;
                    case MotionEvent.ACTION_UP:
                        mPasswordView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        break;
                }
                int pos = mPasswordView.getText().length();
                mPasswordView.setSelection(pos);
                return true;
            }
        });

        @SuppressLint("WrongViewCast") ImageView imgBtn2 = (ImageView) findViewById(R.id.imageView2);
        imgBtn2.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                switch ( event.getAction() ) {
                    case MotionEvent.ACTION_DOWN:
                        mRePasswordView.setInputType(InputType.TYPE_CLASS_TEXT);
                        break;
                    case MotionEvent.ACTION_UP:
                        mRePasswordView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        break;
                }
                int pos = mRePasswordView.getText().length();
                mRePasswordView.setSelection(pos);
                return true;
            }
        });

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
        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnectedToInternet()) {
                    attemptLogin();
                }
            }
        });
    }

    class ObtenerEmpresas extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RegisterUserActivity.this);
            pDialog.setMessage("Obteniendo empresas...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... args) {
            enterprises = new ArrayList<EnterPrises>();

            ListaEnterprises = new ArrayList<String>();
            List params = new ArrayList();

            JSONObject json = jParser.makeHttpRequest(url_enterprises, "GET", params);
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
                    .setAdapter(new ArrayAdapter<String>(RegisterUserActivity.this,
                            android.R.layout.simple_spinner_dropdown_item,
                            ListaEnterprises));
            spinner.setSelection(0, false);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1,
                                           int position, long arg3) {
                    if(position > 0){
                        mItem = spinner.getItemAtPosition(position).toString();
                    }
                    else {
                        Toast.makeText(RegisterUserActivity.this, "Debe Seleccionar una empresa.", Toast.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
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

    private void attemptLogin() {
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mRePasswordView.setError(null);
        mUserView.setError(null);

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String rePassword = mRePasswordView.getText().toString();
        String usuario = mUserView.getText().toString();


        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(rePassword)) {
            mRePasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mRePasswordView;
            cancel = true;
        }else if (!rePassword.equals(password)) {
            mRePasswordView.setError("Las contraseñas no coincide");
            focusView = mRePasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }else{
            if(!isValidPassword(password)){
                mPasswordView.setError(getString(R.string.error_invalid_password));
                focusView = mPasswordView;
                cancel = true;
            }
        }

        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isValidEmail(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (TextUtils.isEmpty(usuario)) {
            mUserView.setError(getString(R.string.error_field_required));
            focusView = mUserView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            new UserAddTask(email, password, usuario).execute();
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    public boolean isValidPassword(final String password) {
        Boolean flag = true;
        if (!hasUppercase.matcher(password).find()) {
            flag = false;
        }
        if (!hasLowercase.matcher(password).find()) {
            flag = false;
        }
        if (!hasNumber.matcher(password).find()) {
            flag = false;
        }
        return flag;
    }

    private ProgressDialog progressDialog;

    public class UserAddTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        private final String mEmail;
        private final String mPassword;
        private final String mUsuario;

        UserAddTask(String email, String password, String usuario) {
            mEmail = email;
            mPassword = password;
            mUsuario = usuario;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Boolean flag = false;
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
            nameValuePairs.add(new BasicNameValuePair("Email", mEmail));
            nameValuePairs.add(new BasicNameValuePair("Password", mPassword));
            nameValuePairs.add(new BasicNameValuePair("UserName", mUsuario));


            JSONObject json = jParser.makeHttpRequest(url_Servicio + "RegisterUser", "POST", nameValuePairs);

            Log.d("All Products: ", json.toString());
            try {
                if(json != null){
                    int success = json.getInt(TAG_SUCCESS);
                    if (success == 200){
                        //IDuser = json.getString(TAG_USER);
                        flag = true;}
                    else{
                        if(success == 500){
                            if(json.getString("ErrorMessage") != "null"){
                                Resultado = json.getString("ErrorMessage");
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Resultado = e.getMessage();
            }
            return flag;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(progressDialog != null && progressDialog.isShowing())
            {
                progressDialog.dismiss();
            }
            if (success) {
                Intent myIntent = new Intent(RegisterUserActivity.this, Successful.class);
                RegisterUserActivity.this.startActivity(myIntent);
            } else {
                if(Resultado != ""){
                    Toast.makeText(RegisterUserActivity.this,Resultado, Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(RegisterUserActivity.this,"Error al procesar la operación", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            Intent myIntent = new Intent(RegisterUserActivity.this, MainActivity.class);
            myIntent.addFlags(FLAG_ACTIVITY_PREVIOUS_IS_TOP);

            startActivity(myIntent);
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }
}
