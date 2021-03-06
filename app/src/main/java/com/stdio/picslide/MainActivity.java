package com.stdio.picslide;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private String section = "latest";
    ImageView imageView;
    TextView tvDescription;
    RelativeLayout mainContent;
    LinearLayout offlineContent;
    private int [] pageNumbers = {0, 0,0 };
    private int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        if (!isNetworkConnected()) {
            mainContent.setVisibility(View.GONE);
            offlineContent.setVisibility(View.VISIBLE);
        }
        else {
            requestData();
        }
    }

    private void initViews() {
        imageView = findViewById(R.id.imageView);
        tvDescription = findViewById(R.id.tvDescription);
        mainContent = findViewById(R.id.mainContent);
        offlineContent = findViewById(R.id.offlineContent);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fabNext:
                pageNumbers[currentIndex]++;
                requestData();
                break;
            case R.id.fabBack:
                if (pageNumbers[currentIndex] > 0) {
                    pageNumbers[currentIndex]--;
                    requestData();
                }
                break;
            case R.id.btnTryAgain:
                if (isNetworkConnected()) {
                    mainContent.setVisibility(View.VISIBLE);
                    offlineContent.setVisibility(View.GONE);
                    requestData();
                }
                break;
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    currentIndex = 0;
                    section = "latest";
                    break;
                case R.id.navigation_dashboard:
                    currentIndex = 1;
                    section = "top";
                    break;
                case  R.id.navigation_notifications:
                    currentIndex = 2;
                    section = "hot";
                    break;
            }
            requestData();
            return true;
        }
    };

    private void requestData() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://developerslife.ru/" + section + "/" + pageNumbers[currentIndex] + "?json=true";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject obj;
                        try {
                            obj = new JSONObject(response);
                            JSONObject gifObj = obj.getJSONArray("result").getJSONObject(0);
                            System.out.println(gifObj.getString("gifURL"));
                            Glide.with(MainActivity.this) //Takes the context
                                    .asGif()
                                    .load(gifObj.getString("gifURL"))
                                    .apply(new RequestOptions()
                                            .placeholder(R.drawable.progress_animation))
                                    .into(imageView);
                            tvDescription.setText(gifObj.getString("description"));
                        } catch (JSONException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("response " + error.getMessage());
                if (!isNetworkConnected()) {
                    mainContent.setVisibility(View.GONE);
                    offlineContent.setVisibility(View.VISIBLE);
                }
            }
        });
        queue.add(stringRequest);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}