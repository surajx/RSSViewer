package in.surajx.rssviewer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import in.surajx.rssviewer.xml.RSSFeed;
import in.surajx.rssviewer.xml.RSSFeedXMLParser;


public class SplashScreenActivity extends ActionBarActivity {

    private final String RSS_FEED_URL = "http://indianote.asia/feed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conMgr.getActiveNetworkInfo() == null
                && !conMgr.getActiveNetworkInfo().isConnected()
                && !conMgr.getActiveNetworkInfo().isAvailable()) {
// No connectivity - Show alert
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(
                    "Unable to reach server, \nPlease check your connectivity.")
                    .setTitle("TD RSS Reader")
                    .setCancelable(false)
                    .setPositiveButton("Exit",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    finish();
                                }
                            });

            AlertDialog alert = builder.create();
            alert.show();

        } else {
            new XMLFeedLoadingTask().execute();
        }
    }

    private class XMLFeedLoadingTask extends AsyncTask<Void,Void, RSSFeed> {

        @Override
        protected RSSFeed doInBackground(Void... params) {
            try {
                return loadXmlFromNetwork(RSS_FEED_URL);
            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }
            return null;
        }

        private RSSFeed loadXmlFromNetwork(String urlString) throws IOException, XmlPullParserException {
            RSSFeedXMLParser rssXMLParser = new RSSFeedXMLParser();
            InputStream stream = null;
            RSSFeed feed;
            try {
                stream = downloadUrl(RSS_FEED_URL);
                feed = rssXMLParser.parse(stream);
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
            return feed;
        }


        private InputStream downloadUrl(String rss_feed_url) throws IOException {
            URL url = new URL(rss_feed_url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            return conn.getInputStream();
        }

        @Override
        protected void onPostExecute(RSSFeed result) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("feed", result);
            Intent intent = new Intent(SplashScreenActivity.this, ListActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }
    }
}
