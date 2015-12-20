package com.niedzielski.volleydemo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {
    private static final String ENDPOINT = "https://commons.wikimedia.org/w/api.php?action=query&prop=categories|coordinates|pageprops&format=json&clshow=!hidden&coprop=type%7Cname%7Cdim%7Ccountry%7Cregion%7Cglobe&codistancefrompoint=40.7127%7C-74.0059&generator=geosearch&redirects=&ggscoord=40.7127%7C-74.0059&ggsradius=10&ggslimit=5&ggsnamespace=6&ggsprop=type%7Cname%7Cdim%7Ccountry%7Cregion%7Cglobe&ggsprimary=all&formatversion=2";
    private static RequestQueue REQUEST_QUEUE;
    private static final Gson GSON = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        request();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void request() {
        JsonRequest<QueryResponse> request = new QueryRequest(ENDPOINT,
                new LogResponseListener<QueryResponse>(), new LogResponseErrorListener());
        getQueue().add(request);
    }

    private RequestQueue getQueue() {
        return getQueue(this);
    }

    private static RequestQueue getQueue(@NonNull Context context) {
        if (REQUEST_QUEUE == null) {
            REQUEST_QUEUE = Volley.newRequestQueue(context.getApplicationContext());
        }
        return REQUEST_QUEUE;
    }

    private static class LogResponseListener<T> implements Response.Listener<T> {
        private static final String TAG = LogResponseListener.class.getName();

        @Override
        public void onResponse(T response) {
            Log.d(TAG, response.toString());
        }
    }

    private static class LogResponseErrorListener implements Response.ErrorListener {
        private static final String TAG = LogResponseErrorListener.class.getName();

        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, error.toString());
        }
    }

    private static class QueryRequest extends JsonRequest<QueryResponse> {
        private static final String TAG = QueryRequest.class.getName();

        public QueryRequest(@NonNull String url,
                            @NonNull Response.Listener<QueryResponse> listener,
                            @Nullable Response.ErrorListener errorListener) {
            super(Request.Method.GET, url, null, listener, errorListener);
        }

        @Override
        protected Response<QueryResponse> parseNetworkResponse(@NonNull NetworkResponse response) {
            String json = parseString(response);
            //Log.d(TAG, "json=" + json);
            QueryResponse queryResponse = GSON.fromJson(json, QueryResponse.class);
            return Response.success(queryResponse, cacheEntry(response));
        }

        @NonNull
        private Cache.Entry cacheEntry(@NonNull NetworkResponse response) {
            return HttpHeaderParser.parseCacheHeaders(response);
        }

        @NonNull
        private String parseString(@NonNull NetworkResponse response) {
            try {
                return new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            } catch (UnsupportedEncodingException e) {
                return new String(response.data);
            }
        }
    }

    private static class QueryResponse {
        @NonNull private Query query;

        @Override
        public String toString() {
            return "query=" + query.toString();
        }
    }

    private static class Query {
        @NonNull private Page [] pages;

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("pages=");
            for (Page page : pages) {
                builder.append(page.toString());
                builder.append("\n");
            }
            builder.replace(builder.length() - 1, builder.length(), "");
            return builder.toString();
        }
    }

    private static class Page {
        private int pageid;
        private int ns;
        @NonNull private String title;

        @Override
        public String toString() {
            return "pageid=" + pageid + " ns=" + ns + " title=" + title;
        }
    }
}
