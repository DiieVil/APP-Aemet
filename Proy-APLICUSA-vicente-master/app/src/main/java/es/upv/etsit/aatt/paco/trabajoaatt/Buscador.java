package es.upv.etsit.aatt.paco.trabajoaatt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Buscador extends AppCompatActivity {

    String localidad;
    String dia;
    int numeroDia;
    Button searchButton;

    ArrayList<String> municipios = new ArrayList<String>();

    AutoCompleteTextView atv;

    Map<String,String> munList = new HashMap<String,String>();
    String codigoLocalidad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscador);

        // Autocompletado de buscador de municipios
        atv = findViewById(R.id.atv);
        atv.setAdapter(new ArrayAdapter<>(Buscador.this, android.R.layout.simple_list_item_1, municipios));

        //Spinner
        ArrayAdapter adaptador = ArrayAdapter.createFromResource( this, R.array.dias, android.R.layout.simple_spinner_item );
        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final Spinner desplegable = (Spinner) findViewById( R.id.spinner );
        desplegable.setAdapter(adaptador);

        desplegable.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, android.view.View v, int position, long id) {
                        dia = parent.getItemAtPosition(position).toString();
                        switch (dia){
                            case "Hoy": numeroDia = 0; break;
                            case "Mañana": numeroDia = 1; break;
                            case "Pasado Mañana": numeroDia = 2; break;
                        }
                    }
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                }
        );


        //Botón envío
        searchButton = (Button) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                localidad = atv.getText().toString();
                if (munList.containsKey(localidad)){
                    codigoLocalidad = munList.get(localidad);
                    Log.i("DIA DE LA SEMANA", dia);
                    Intent intent = new Intent(Buscador.this,MainActivity.class);
                    intent.putExtra("codigoLocalidad", codigoLocalidad);
                    intent.putExtra("diaSemana", numeroDia);
                    startActivity(intent);
                }else{
                    Toast.makeText(Buscador.this , "Municipio no encontrado", Toast.LENGTH_SHORT).show();
                }
            }
        });

        RotateAnimation anim = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(15000);

        // Start animating the image
        final ImageView solGiratorio = (ImageView) findViewById(R.id.imageView3);
        solGiratorio.startAnimation(anim);

        readCode();
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("codMun.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public void readCode(){
        try {
            JSONArray raiz = new JSONArray(loadJSONFromAsset());
            munList = new HashMap<String,String>();

            for(int ind=0; ind<= raiz.length()-2 ; ind++){

                JSONObject codMunlist = raiz.getJSONObject(ind);

                String ciudad = codMunlist.getString("NOMBRE");
                municipios.add(ciudad);

                String cpro = codMunlist.getString("CPRO");

                String cmun = codMunlist.getString("CMUN");

                String codigo = cpro+cmun;

                munList.put(ciudad,codigo);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}