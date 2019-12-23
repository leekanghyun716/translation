package com.example.j.kokoko;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.GoogleAPIException;
import com.google.api.translate.Language;
import com.google.api.translate.Translate;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;

import static android.speech.tts.TextToSpeech.ERROR;
public class MainActivity extends AppCompatActivity implements View.OnFocusChangeListener, TextWatcher {
    private static final int REQUEST_CODE = 1234;
    ImageButton Start, button,translateButton,IbtnSound;
    TextView input_text;
    EditText Speech;
    Dialog match_text_dialog;
    private TextToSpeech tts;
    String s;
    ListView textlist;
    ArrayList<String> matches_text;
    private String result;
    Spinner spinner2,spinner1;
    private  String translate1,translate2;
    private  String translate3,translate4;
    Translate translate=new Translate() {
        @Override
        public String execute(String s, Language language, Language language1) throws GoogleAPIException {
            return null;
        }

        @Override
        public String[] execute(String[] strings, Language language, Language language1) throws GoogleAPIException {
            return new String[0];
        }

        @Override
        public String[] execute(String s, Language language, Language[] languages) throws GoogleAPIException {
            return new String[0];
        }

        @Override
        public String[] execute(String[] strings, Language[] languages, Language[] languages1) throws GoogleAPIException {
            return new String[0];
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Start = findViewById(R.id.start_reg);
        button = findViewById(R.id.button);
        IbtnSound = findViewById(R.id.IbtnSound);
        translateButton = findViewById(R.id.translateButton);
        Speech =findViewById(R.id.speech);
        input_text = (TextView) findViewById(R.id.input_text);
        ////////////////////////////////////////////////////////
        spinner1 = (Spinner)findViewById(R.id.spinner1);
        ArrayAdapter yearAdapter = ArrayAdapter.createFromResource(this,
                R.array.spinnerArray, android.R.layout.simple_spinner_item);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(yearAdapter);

        spinner2 = (Spinner)findViewById(R.id.spinner2);
        ArrayAdapter monthAdapter = ArrayAdapter.createFromResource(this,
                R.array.spinnerArray2, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(monthAdapter);
        ///////////////////////////////////////////////////////
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != ERROR) {
                    // 언어를 선택한다.
                    tts.setLanguage(Locale.KOREA);
                }
            }
        });
        IbtnSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // editText에 있는 문장을 읽는다.
                if(spinner2.getSelectedItem().toString().equals("한국어")){
                    tts.setLanguage(Locale.KOREA);
                }else if(spinner2.getSelectedItem().toString().equals("일본어")){
                    tts.setLanguage(Locale.JAPAN);
                }else if(spinner2.getSelectedItem().toString().equals("영어")){
                    tts.setLanguage(Locale.ENGLISH);
                }
                tts.speak(input_text.getText().toString(),TextToSpeech.QUEUE_FLUSH, null);
            }
        });
        ///////////////////////////////////////////////////////
        Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected()) {
                    getTranslate();
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    if(translate3.equals("ko")){
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
                        //RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                        //recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
                    }else if(translate3.equals("ja")) {
                         intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getPackageName());
                         intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ja");
                    }else if(translate3.equals("en")) {
                        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getPackageName());
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
                    }
                    startActivityForResult(intent, REQUEST_CODE);
                } else {
                    Toast.makeText(getApplicationContext(), "Plese Connect to Internet", Toast.LENGTH_LONG).show();
                }
            }

        });
        Speech.addTextChangedListener(this);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, input_text.getText().toString());
                startActivity(intent);
            }
        });
        translateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Speech.getText().toString().equals("")){
                    Toast.makeText(MainActivity.this, "번역할 문장이 없어요!", Toast.LENGTH_SHORT).show();
                    return;
                }
                BackgroundTask backgroundTask=new BackgroundTask();
                backgroundTask.execute();
            }
        });
    }

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net = cm.getActiveNetworkInfo();
        if (net != null && net.isAvailable() && net.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {

            match_text_dialog = new Dialog(MainActivity.this);
            match_text_dialog.setContentView(R.layout.dialog_matches_frag);
            match_text_dialog.setTitle("Select Matching Text");
            textlist = (ListView) match_text_dialog.findViewById(R.id.list);
            matches_text = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, matches_text);
            textlist.setAdapter(adapter);
            textlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    s = matches_text.get(position);
                    Speech.setText(matches_text.get(position));
                    match_text_dialog.hide();
                }
            });
            match_text_dialog.show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //button.callOnClick();
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {

    }
    class BackgroundTask extends AsyncTask<Integer, Integer, Integer> {
        protected void onPreExecute() {
        }

        @Override
        protected Integer doInBackground(Integer... arg0) {
            StringBuilder output = new StringBuilder();
            String clientId = "01oWHo2LQNWyICk9_5vk"; // 애플리케이션 클라이언트 아이디 값";
            String clientSecret = "_sMBJJhSD4"; // 애플리케이션 클라이언트 시크릿 값";
            try {
                // 번역문을 UTF-8으로 인코딩합니다.
                String text = URLEncoder.encode(Speech.getText().toString(), "UTF-8");
                String apiURL = "https://openapi.naver.com/v1/papago/n2mt";

                // 파파고 API와 연결을 수행합니다.
                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("X-Naver-Client-Id", clientId);
                con.setRequestProperty("X-Naver-Client-Secret", clientSecret);

                getTranslate();
                // 번역할 문장을 파라미터로 전송합니다.
                String postParams = "source="+translate3+"&target="+translate4+"&text=" + text;
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(postParams);
                wr.flush();
                wr.close();

                // 번역 결과를 받아옵니다.
                int responseCode = con.getResponseCode();
                BufferedReader br;
                if(responseCode == 200) {
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                } else {
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                }
                String inputLine;
                while ((inputLine = br.readLine()) != null) {
                    output.append(inputLine);
                }
                br.close();
            } catch(Exception ex) {
                Log.e("SampleHTTP", "Exception in processing response.", ex);
                ex.printStackTrace();
            }
            result = output.toString();
            return null;
        }

        protected void onPostExecute(Integer a) {
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(result);
            if(element.getAsJsonObject().get("errorMessage") != null) {
                Log.e("번역 오류", "번역 오류가 발생했습니다. " +
                        "[오류 코드: " + element.getAsJsonObject().get("errorCode").getAsString() + "]");
            } else if(element.getAsJsonObject().get("message") != null) {
                // 번역 결과 출력
                input_text.setText(element.getAsJsonObject().get("message").getAsJsonObject().get("result")
                        .getAsJsonObject().get("translatedText").getAsString());
            }

        }

    }

    public void getTranslate() {
       translate1=spinner1.getSelectedItem().toString();
       translate2=spinner2.getSelectedItem().toString();

       switch (translate1){
           case "한국어":translate3="ko";break;
           case "영어":translate3="en";break;
           case "일본어":translate3="ja";break;
       }
        switch (translate2){
            case "한국어":translate4="ko";break;
            case "영어":translate4="en";break;
            case "일본어":translate4="ja";break;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TTS 객체가 남아있다면 실행을 중지하고 메모리에서 제거한다.
        if(tts != null){
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }
}