package es.upv.etsit.aatt.paco.trabajoaatt;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    TextView puebloT;
    TextView temperaturaT;
    TextView probPrecipitacionT;
    TextView dirVientoT;
    TextView velVientoT;
    TextView cieloT;

    TextView infPrecipitacionT;
    TextView infdirVientoT;
    TextView infvelVientoT;

    TextView maxtempT;
    TextView mintempT;

    ViewGroup tContainer;
    Button animBtn;

    String cLocalidad;
    int dSemana;

    Animation translateAnim;

    String [] tiempoSol = {"Despejado","Nubes altas","Calima"};

    String [] tiempoNuboso = {"Poco nuboso","Intervalos nubosos","Nuboso","Muy nuboso","Cubierto","Intervalos nubosos con lluvia escasa","Nuboso con lluvia escasa",
    "Muy nuboso con lluvia escasa","Cubierto con lluvia escasa","Intervalos nubosos con nieve escasa","Niebla","Bruma"};

    String [] tiempoLluvia = {"Intervalos nubosos con lluvia","Nuboso con lluvia","Muy nuboso con lluvia","Cubierto con lluvia"};

    String [] tiempoTormenta = {"Intervalos nubosos con tormenta","Nuboso con tormenta","Muy nuboso con tormenta","Cubierto con tormenta","Intervalos nubosos con tormenta y lluvia escasa",
    "Muy nuboso con tormenta y lluvia escasa","Cubierto con tormenta y lluvia escasa"};

    String [] tiempoNieve = {"Nuboso con nieve","Muy nuboso con nieve","Cubierto con nieve"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        cLocalidad = (String) getIntent().getStringExtra("codigoLocalidad");
        dSemana = (Integer) getIntent().getIntExtra("diaSemana",0);
        Log.i("NUMERO DIA SEMANA", String.valueOf(dSemana));

        String url = "https://opendata.aemet.es/opendata/api/prediccion/especifica/municipio/diaria/"+cLocalidad+"?api_key=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkdmljdmlsQHRlbGVjby51cHYuZXMiLCJqdGkiOiJmMjc4NjgwZi0xMzg3LTRhOGQtOWY3YS00MGIyYTJhNzEzY2UiLCJpc3MiOiJBRU1FVCIsImlhdCI6MTYyMDI4MTYxNCwidXNlcklkIjoiZjI3ODY4MGYtMTM4Ny00YThkLTlmN2EtNDBiMmEyYTcxM2NlIiwicm9sZSI6IiJ9.PhGmTY4jCvQ3bMzARFdMyqf_O2MEzOOgKX0Wb3pR0C0";
        ServiciosWebEncadenados servicioWeb = new ServiciosWebEncadenados(url);
        servicioWeb.start();

        puebloT = findViewById(R.id.pueblo);
        temperaturaT = findViewById(R.id.temperatura);
        probPrecipitacionT = findViewById(R.id.probPreci);
        dirVientoT = findViewById(R.id.DirViento);
        velVientoT = findViewById(R.id.VelViento);
        cieloT = findViewById(R.id.cielo);
        infPrecipitacionT = findViewById(R.id.textView4);
        infdirVientoT = findViewById(R.id.textView5);
        infvelVientoT = findViewById(R.id.textView6);
        mintempT = findViewById(R.id.mintemp);
        maxtempT = findViewById(R.id.maxtemp);

        tContainer = findViewById(R.id.tContainer);
        animBtn = findViewById(R.id.animButton);

        animBtn.setOnClickListener(new View.OnClickListener() {
            boolean visible;
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                TransitionManager.beginDelayedTransition(tContainer);
                visible = !visible;

                infPrecipitacionT.setVisibility(visible ? View.VISIBLE: View.GONE);
                infdirVientoT.setVisibility(visible ? View.VISIBLE: View.GONE);
                infvelVientoT.setVisibility(visible ? View.VISIBLE: View.GONE);

                probPrecipitacionT.setVisibility(visible ? View.VISIBLE: View.GONE);
                dirVientoT.setVisibility(visible ? View.VISIBLE: View.GONE);
                velVientoT.setVisibility(visible ? View.VISIBLE: View.GONE);
            }
        });

    }


    // LLeva a cabo dos peticiones de servicios web encadenadas
    class ServiciosWebEncadenados extends Thread {

        String url_inicial;

        // constructor
        ServiciosWebEncadenados(String url_inicial) {
            this.url_inicial = url_inicial;
        }

        // tarea a ejecutar en hilo paralelo e independiente
        @Override public void run(){

            // Gestiónese oportunamente las excepciones

            try {
                // Primera peticion
                String respuesta = API_REST(url_inicial);
                JSONObject raiz = new JSONObject(respuesta);
                String urldef = raiz.getString("datos");
                Log.i("URL",urldef);
                // Segunda peticion
                final String respuesta2 = API_REST(urldef);

                // Impresión de resultados en el hilo de la UI (User Interface thread): runOnUiThread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        printParametros(respuesta2);
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(TAG, "Error JSON" + e.toString());
            }
        } // run
    } // ServiciosWebEncadenados

    // Imprime parámetros meteorológicos en pantalla: esto ya se ejecutará en la UI
    public void printParametros(String respuesta2){
        try {
            JSONArray raiz2 = new JSONArray(respuesta2);
            JSONObject obj1 = raiz2.getJSONObject(0);

            JSONObject predic = obj1.getJSONObject("prediccion");
            JSONArray semana = predic.getJSONArray("dia");
            JSONObject dia = semana.getJSONObject(dSemana);
            Log.i("JSON DIA =", String.valueOf(semana));

            JSONObject temperatura = dia.getJSONObject("temperatura");

            /* Franjas no necesarias
            JSONArray franjasTemp = temperatura.getJSONArray("dato");
            JSONObject franja18 = franjasTemp.getJSONObject(2);
            */

            JSONArray precipitacion = dia.getJSONArray("probPrecipitacion");
            JSONObject franjaPrecipitacion = precipitacion.getJSONObject(2);

            JSONArray viento = dia.getJSONArray("viento");
            JSONObject franjaViento = viento.getJSONObject(2);

            JSONArray cielo = dia.getJSONArray("estadoCielo");
            JSONObject franjaCielo = cielo.getJSONObject(2);

            String fecha = obj1.getString("elaborado");
            String pueblo = obj1.getString("nombre");

            int tempeMax = temperatura.getInt("maxima");
            int tempeMin = temperatura.getInt("minima");
            int tempe = (tempeMax+tempeMin)/2;

            int pPrec = franjaPrecipitacion.getInt("value");
            String dirV = franjaViento.getString("direccion");
            int velV = franjaViento.getInt("velocidad");
            String estCielo = franjaCielo.getString("descripcion");


            final ImageView iconoTiempo = (ImageView) findViewById(R.id.iv_image);
            translateAnim = AnimationUtils.loadAnimation(this,R.anim.translate_anim);
            //Animacion
           if(Arrays.asList(tiempoSol).contains(estCielo)){
               RotateAnimation anim = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
               anim.setInterpolator(new LinearInterpolator());
               anim.setRepeatCount(Animation.INFINITE);
               anim.setDuration(15000);

               // Start animating the image

               iconoTiempo.startAnimation(anim);

           }else if (Arrays.asList(tiempoNuboso).contains(estCielo)){
                iconoTiempo.setImageResource(R.drawable.cloudy);
                iconoTiempo.startAnimation(translateAnim);
           }else if (Arrays.asList(tiempoLluvia).contains(estCielo)){
               iconoTiempo.setImageResource(R.drawable.rain);
               iconoTiempo.startAnimation(translateAnim);
           }else if (Arrays.asList(tiempoTormenta).contains(estCielo)){
               iconoTiempo.setImageResource(R.drawable.heavyrain);
               iconoTiempo.startAnimation(translateAnim);
           }else if (Arrays.asList(tiempoNieve).contains(estCielo)){
               iconoTiempo.setImageResource(R.drawable.snowflake);
               iconoTiempo.startAnimation(translateAnim);
           }


            // Muestra de datos
            puebloT.setText(pueblo);

            temperaturaT.setText(tempe+"º");

            probPrecipitacionT.setText(pPrec+"%");

            dirVientoT.setText(dirV);

            velVientoT.setText(velV+" m/s");

            cieloT.setText(estCielo);

            maxtempT.setText(tempeMax+"º");
            mintempT.setText(tempeMin+"º");


        } catch (Exception e){
            e.printStackTrace();
            Log.d(TAG,"Exception Results:" + e.toString());

        }

    }

    /** La peticion del argumento es recogida y devuelta por el método API_REST
     *  Método ya completado y supuestamente correcto */
    public String API_REST(String uri){

        StringBuffer response = null;

        try {
            URL url = new URL(uri);
            Log.d(TAG, "URL: " + uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // Detalles de HTTP
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            Log.d(TAG, "Codigo de respuesta: " + responseCode);
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream() , "ISO-8859-15" ));
                String output;
                response = new StringBuffer();

                while ((output = in.readLine()) != null) {
                    response.append(output);
                }
                in.close();
            } else {
                Log.d(TAG, "responseCode: " + responseCode);
                return null; // retorna null anticipadamente si hay algun problema

            }
        } catch(Exception e) { // Posibles excepciones: MalformedURLException, IOException y ProtocolException
            e.printStackTrace();
            Log.d(TAG, "Error conexión HTTP:" + e.toString());
            return null;
        }

        return new String(response); // de StringBuffer -response- pasamos a String

    } // API_REST

}
