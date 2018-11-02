package astrax.dermionics;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.widget.Toast;

/**
 * Created by Brian on 10/8/2017.
 */

public class WebView extends Activity {
    private String link = "";
    MainActivity mA = new MainActivity();

    android.webkit.WebView wv;
    @Override
    public void onBackPressed(){
        if(wv.canGoBack()){

            wv.goBack();
        }else {
            mA.disconnect();
            Toast.makeText(this,"Super",Toast.LENGTH_LONG).show();
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.webview);

        link = getIntent().getStringExtra("link");
        Toast.makeText(this,"Link: " +link,Toast.LENGTH_LONG).show();

        wv = (android.webkit.WebView)findViewById(R.id.wv);

        wv.getSettings().setJavaScriptEnabled(true);
        wv.setFocusable(true);
        wv.setFocusableInTouchMode(true);
        //Set Render Priority to High
        wv.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        wv.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        wv.getSettings().setDomStorageEnabled(true);
        wv.getSettings().setDatabaseEnabled(true);
        wv.getSettings().setAppCacheEnabled(true);
        wv.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        //load url
        wv.loadUrl(link);
        wv.setWebViewClient(new WebViewClient());
    }
}
