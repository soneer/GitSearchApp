/*
 * Author: Soneer Sainion
 * Purpose: Demo app
 * Due to time restraint,
 * few items still need to be implemented.
 * 1. Need create custom adapter that stores list of drawables and list of strings.
 * 2. Find star ratings in the raw json parse data.
 * 3. Handle exceptions.
 */


package sainionapps.gitsearchapp;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import eu.amirs.JSON;

public class MainActivity extends AppCompatActivity {

    List<String> listOfGits = new ArrayList<String>();
    ArrayAdapter<String> adapter;

    private HttpURLConnection urlConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //TODO: Create custom adapter to avoid null initialization
        listOfGits.add("");
        ListView listViewOfGit = (ListView) findViewById(R.id.list_view_git);
        adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.custom_list, R.id.txt_view_git_info, listOfGits);
        listViewOfGit.setAdapter(adapter);
    }

    /**
     * Class that runs in the background to load data from GITHub
     */
    public class GitHubTask extends AsyncTask<Void, String, String> {
        ProgressDialog progressDialog;
        String urlString;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setCancelable(true);
            progressDialog.show();
            String urlQueryWord = ((EditText) findViewById(R.id.edit_txt_git_query)).getText().toString();
            urlString = "https://api.github.com/search/repositories?q=" + urlQueryWord + "&sort=stars&order=desc";
        }

        @Override
        protected String doInBackground(Void... voids) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }

            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            parseJson(result);
        }
    }

    /**
     * Handles search button logic
     *
     * @param v - Search button
     */
    public void gitSearchButton(View v) {
        if (!listOfGits.isEmpty()) {
            listOfGits.clear();
        }
        GitHubTask receiveString = new GitHubTask();
        receiveString.execute();
    }

    /**
     * Method to handle parsing raw Json data and updating adapter
     *
     * @param rawJson
     */
    public void parseJson(String rawJson) {
        JSON json = new JSON(rawJson);
        JSON items = json.key("items");
        json.getJsonObject().keys();
        for (int i = 0; i < items.count(); i++) {
            JSON productInfo = items.index(i);
            listOfGits.add(productInfo.key("name") + " \n" + productInfo.key("description") + " \n");
            adapter.notifyDataSetChanged();
            Picasso.with(MainActivity.this).load(productInfo.key("owner").key("avatar_url").stringValue()).into(((ImageView) findViewById(R.id.icon)));
        }
    }
}
