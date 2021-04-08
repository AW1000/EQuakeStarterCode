/*
Name: Aidan Watret
Matriculation Number:S1803674
 */

package org.me.gcu.equakestartercode;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import static org.me.gcu.equakestartercode.R.layout.activity_listview;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, OnClickListener {
    private TextView expandedList;
    private Button startButton;
    private Button searchButton;
    private Button filterSearchButton;
    private Button mapButton;
    private Button clear;
    private ProgressBar progressBar;
    private ListView listView;
    private TextView txt_percentage;
    private TextView acknowledgement;
    private TextView searchResults;
    private TextView searchError;
    private String result = "";
    private String urlSource = "http://quakes.bgs.ac.uk/feeds/MhSeismology.xml";
    ArrayList<EQDataClass> alist;
    ArrayList<String> stringLocationMagnitude;
    private ViewFlipper flip;
    private EditText startDate;
    private EditText endDate;
    final private Calendar myCalendar = Calendar.getInstance();
    private boolean isStartDate;
    private Date filterStartDate = null;
    private Date filterEndDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Log.e("MyTag", "in onCreate");

        expandedList = (TextView) findViewById(R.id.expandedList);
        startButton = (Button) findViewById(R.id.startButton);
        searchButton = (Button) findViewById(R.id.searchButton);
        filterSearchButton = (Button) findViewById(R.id.filterSearch);
        mapButton = (Button) findViewById(R.id.mapButton);
        clear = (Button) findViewById(R.id.clear);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        txt_percentage = (TextView) findViewById(R.id.txt_percentage);
        acknowledgement = (TextView) findViewById(R.id.acknowledgement);
        searchResults = (TextView) findViewById(R.id.searchResults);
        searchError = (TextView) findViewById(R.id.searchError);
        listView = (ListView) findViewById(R.id.list);
        flip = (ViewFlipper) findViewById(R.id.flip);
        startDate = (EditText) findViewById(R.id.startDate);
        endDate = (EditText) findViewById(R.id.endDate);
        startButton.setOnClickListener(this);
        searchButton.setOnClickListener(this);
        filterSearchButton.setOnClickListener(this);
        mapButton.setOnClickListener(this);
        clear.setOnClickListener(this);
        startDate.setOnClickListener(this);
        endDate.setOnClickListener(this);
        listView.setOnItemClickListener(this);
        flip.setInAnimation(this, android.R.anim.fade_in);
        flip.setOutAnimation(this, android.R.anim.fade_out);

        //this section allows for app to stay on its current screen when screen orientation is changed by getting values passed at save instance state
        if (savedInstanceState != null) {
            int flipperPosition = savedInstanceState.getInt("TAB_NUMBER");
            flip.setDisplayedChild(flipperPosition);
            String listDetails = savedInstanceState.getString("LISTDETAIL");
            if (flipperPosition == 2) {
                expandedList.setText(listDetails);
            }
            String searchDetails = savedInstanceState.getString("SEARCHDETAIL");
            if (flipperPosition == 3) {
                searchResults.setText(searchDetails);
                if (searchResults.getText().equals("")) {
                    startDate.setText("");
                    endDate.setText("");
                    filterStartDate = null;
                    filterEndDate = null;
                }
                else {
                    startDate.setEnabled(false);
                    endDate.setEnabled(false);
                }
            }
            if (flipperPosition > 0) {
                alist = new ArrayList<EQDataClass>();
                stringLocationMagnitude = new ArrayList<String>();
                alist = (ArrayList<EQDataClass>) savedInstanceState.getSerializable("ARRAYLIST");
                updateList();
            }
        }
    }

    //this method activates when app saves state (usually after orientation change) and passes values to onCreate method to allow for view to stay the same after orientation change
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        int position = flip.getDisplayedChild();
        savedInstanceState.putInt("TAB_NUMBER", position);
        savedInstanceState.putString("LISTDETAIL", (String) expandedList.getText());
        savedInstanceState.putString("SEARCHDETAIL", (String) searchResults.getText());
        savedInstanceState.putSerializable("ARRAYLIST", (Serializable) alist);

    }

    //code to send app to homescreen
    public void goToHomeScreen() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    //dictates what each view does when android back button is pressed
    @Override
    public void onBackPressed() {

        if (flip.getDisplayedChild() > 1) {
            flip.setDisplayedChild(1);
        } else {
            goToHomeScreen();
        }
    }

    //This method details what should occur during all onClick events
    public void onClick(View aview) {
        //shows progress bar and launches async task
        if (aview == startButton) {
            startButton.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            txt_percentage.setVisibility(View.VISIBLE);
            new LoadXMLDataAsyncTask(urlSource).execute();

            //timer used to make automatic updates to xml data every hour. timer is created after app gets to main page
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                                      @Override
                                      public void run() {
                                          listView.setEnabled(false);
                                          Toast.makeText(MainActivity.this, "App is updating", Toast.LENGTH_SHORT).show();
                                      }
                                  });
                    try {
                        TimeUnit.MILLISECONDS.sleep(50); //sleep is added as if app updated while user was scrolling listview the app would crash with fatal error
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    updateXMLData();
                    updateList();

                }
            }, 1000 * 60 * 60, 1000 * 60 * 60); //this sets updates to happen every hour to change the number of minutes I just chang one of the 60s I decided on 1 hour as earthquakes are relatively infrequent
        } else if (aview == searchButton) {
            flip.setDisplayedChild(3);
        } else if (aview == startDate) {
            new DatePickerDialog(this, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();

            isStartDate = true;
        } else if (aview == endDate) {
            startDate.setEnabled(false);
            new DatePickerDialog(this, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();

            isStartDate = false;
        } else if (aview == clear) {
            startDate.setText("");
            endDate.setText("");
            filterStartDate = null;
            filterEndDate = null;
            startDate.setEnabled(true);
            endDate.setEnabled(false);
            filterSearchButton.setEnabled(false);
            searchResults.setText("");
            searchError.setText("");

        } else if (aview == filterSearchButton) {

            endDate.setEnabled(false);

            String strongestPosition;
            String northPosition;
            String southPosition;
            String westPosition;
            String eastPosition;
            String deepestPosition;
            String shallowestPosition;
            ArrayList<EQDataClass> filteredList = new ArrayList<EQDataClass>();

            for (EQDataClass item : alist) {
                Log.e("MyTag", item.getEqDate().toString());
                if ((item.getEqDate().before(filterEndDate) || item.getEqDate().equals(filterEndDate)) && (item.getEqDate().after(filterStartDate) || item.getEqDate().equals(filterStartDate))) {
                    filteredList.add(item);
                }
            }

            if (filteredList.size() == 0) {
                searchResults.setText("There are no earthquakes registered between the given dates. Please try a different set of dates");
            }
            else {

                Log.e("MyTag", filterStartDate.toString());
                Log.e("MyTag", filterEndDate.toString());
                strongestPosition = filteredList.get(maxValueArray(filteredList, "magnitude")).getTitle();
                northPosition = filteredList.get(maxValueArray(filteredList, "latitude")).getTitle();
                southPosition = filteredList.get(minValueArray(filteredList, "latitude")).getTitle();
                westPosition = filteredList.get(minValueArray(filteredList, "longitude")).getTitle();
                eastPosition = filteredList.get(maxValueArray(filteredList, "longitude")).getTitle();
                deepestPosition = filteredList.get(maxValueArray(filteredList, "depth")).getTitle();
                shallowestPosition = filteredList.get(minValueArray(filteredList, "depth")).getTitle();

                searchResults.setText("Most Northerly Earthquake:\n\n" + northPosition + "\n\nMost Southerly Earthquake:\n\n" + southPosition + "\n\nMost Westerly Earthquake:\n\n" + westPosition + "\n\nMost Easterly Earthquake:\n\n" + eastPosition + "\n\nLargest Magnitude Earthquake:\n\n" + strongestPosition + "\n\nDeepest Earthquake:\n\n" + deepestPosition + "\n\nShallowest Earthquake:\n\n" + shallowestPosition);
            }

        }
        //starts new activity and passes alist arraylist to MapsActivity
        else if (aview == mapButton) {
            Intent intent = new Intent(this, MapsActivity.class);
            Bundle args = new Bundle();
            args.putSerializable("ARRAYLIST", (Serializable) alist);
            intent.putExtra("BUNDLE", args);
            startActivity(intent);
        }
    }

    //onClick event for listView items
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        String s = String.valueOf(position);
        Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
        EQDataClass o = alist.get(position);
        expandedList.setText(o.displayData());
        flip.showNext();
        Log.e("MyTag", "on list click");

    }

    //beginning of async task
    private class LoadXMLDataAsyncTask extends AsyncTask<Void, Integer, Void> {

        int progress_status;
        private String url;

        public LoadXMLDataAsyncTask(String aurl) {
            url = aurl;
        }

        //updates the UI before task
        @Override
        public void onPreExecute() {
            super.onPreExecute();

            progress_status = 0;
            txt_percentage.setText("Downloading 0%");

        }

        //code for xmlpullparser and progress update
        @Override
        protected Void doInBackground(Void... params) {
            double countItem = 0;
            double progress;

                URL aurl;
                URLConnection yc;
                BufferedReader in = null;
                String inputLine = "";


                Log.e("MyTag", "in run");

                try {
                    Log.e("MyTag", "in try");
                    aurl = new URL(url);
                    yc = aurl.openConnection();
                    in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
                    Log.e("MyTag", "after ready");

                    while ((inputLine = in.readLine()) != null) {
                        result = result + inputLine;
                        Log.e("MyTag", inputLine);

                    }
                    in.close();

                    //removes unnecessary xml data
                    int xmlIndex = result.indexOf("<item>");
                    result = result.substring(xmlIndex);
                    xmlIndex = result.lastIndexOf("</channel>");
                    result = result.substring(0, xmlIndex);


                } catch (IOException ae) {
                    Log.e("MyTag", "ioexception in run");
                }

                //counts the number of items in the xml document to make working progress bar
                String findStr = "<item>";
                int lastIndex = 0;

                while (lastIndex != -1) {

                    lastIndex = result.indexOf(findStr, (lastIndex));

                    if (lastIndex != -1) {
                        countItem++;
                        lastIndex += findStr.length();
                    }
                }


                try {
                    EQDataClass item = null;
                    alist = new ArrayList<EQDataClass>();

                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    factory.setNamespaceAware(false);
                    XmlPullParser xpp = factory.newPullParser();
                    xpp.setInput(new StringReader(result));
                    int eventType = xpp.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        // Found a start tag
                        if (eventType == XmlPullParser.START_TAG) {
                            if (xpp.getName().equalsIgnoreCase("item")) {
                                Log.e("MyTag", "Item Start Tag found");
                                item = new EQDataClass();
                            } else if (xpp.getName().equalsIgnoreCase("title")) {
                                // Now just get the associated text
                                String temp = xpp.nextText();
                                // Do something with text
                                Log.e("MyTag", "title is " + temp);
                                item.setTitle(temp);
                            } else
                                // Check which Tag we have
                                if (xpp.getName().equalsIgnoreCase("description")) {
                                    // Now just get the associated text
                                    String temp = xpp.nextText();
                                    // Do something with text
                                    Log.e("MyTag", "description is " + temp);
                                    item.setDescription(temp);
                                    item.setLocation(temp.substring(temp.indexOf("Location: ") + 10, temp.indexOf(" ; Lat/long")));
                                    item.setMagnitude(Double.parseDouble(temp.substring(temp.indexOf("Magnitude: ") + 11)));
                                    item.setDepth(Integer.parseInt(temp.substring(temp.indexOf("Depth: ") + 7, temp.indexOf(" km ;"))));
                                } else
                                    // Check which Tag we have
                                    if (xpp.getName().equalsIgnoreCase("pubDate")) {
                                        // Now just get the associated text
                                        String temp = xpp.nextText();
                                        // Do something with text
                                        Log.e("MyTag", "pubDate is " + temp);
                                        item.setPubDate(temp);
                                        temp = temp.substring(5, 16);
                                        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM yyyy");
                                        try {
                                            item.setEqDate(dateFormatter.parse(temp));
                                            dateFormatter.applyPattern("dd-MM-yyyy");
                                        } catch (ParseException e) {
                                            System.out.println(e);
                                        }
                                    } else
                                        // Check which Tag we have
                                        if (xpp.getName().equalsIgnoreCase("geo:lat")) {
                                            // Now just get the associated text
                                            String temp = xpp.nextText();
                                            // Do something with text
                                            Log.e("MyTag", "latitude is " + temp);
                                            item.setLatitude(Double.parseDouble(temp));
                                        } else
                                            // Check which Tag we have
                                            if (xpp.getName().equalsIgnoreCase("geo:long")) {
                                                // Now just get the associated text
                                                String temp = xpp.nextText();
                                                // Do something with text
                                                Log.e("MyTag", "longitude is " + temp);
                                                item.setLongitude(Double.parseDouble(temp));
                                            }
                        } else if (eventType == XmlPullParser.END_TAG) {
                            if (xpp.getName().equalsIgnoreCase("item")) {
                                Log.e("MyTag", "item is " + item.toString());
                                alist.add(item);

                                //code to make working progress bar with number of items parsed divided by total number of items
                                progress_status += 1;
                                progress = (int) Math.round((progress_status/countItem)*100);
                                publishProgress((int) Math.round(progress));

                                Log.e("MyTag", "progress is " + progress);
                            }
                        }


                        // Get the next event
                        eventType = xpp.next();

                    } // End of while

                } catch (XmlPullParserException ae1) {
                    Log.e("MyTag", "Parsing error" + ae1.toString());
                } catch (IOException ae1) {
                    Log.e("MyTag", "IO error during parsing");
                }

                Log.e("MyTag", "End document");
            Log.e("MyTag", "total list items is " + alist.size());

            return null;
        }

        //updates progress as task is being completed
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            progressBar.setProgress(values[0]);

            txt_percentage.setText("Downloading " + values[0] + "%");

        }


        //makes changes to UI after task is completed
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            flip.showNext();

            Log.d("UI thread", "I am the UI thread");

            stringLocationMagnitude = new ArrayList<String>();
            for (EQDataClass item : alist) {
                stringLocationMagnitude.add(item.locationMagnitude());
            }

            //creation of adapter to allow location and magnitude values to be colour coded and added to listView
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.activity_listview, stringLocationMagnitude) {

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {

                    View view = super.getView(position, convertView, parent);

                    EQDataClass item = alist.get(position);
                    double i = item.getMagnitude();
                    Log.e("MyTag", "magnitude is " + i);

                    if (i >= 2) {
                        view.setBackgroundColor(getResources().getColor(
                                android.R.color.holo_red_dark
                        ));
                    } else if (i >= 1 && i < 2) {
                        view.setBackgroundColor(getResources().getColor(
                                android.R.color.holo_orange_dark
                        ));
                    } else {
                        view.setBackgroundColor(getResources().getColor(
                                R.color.yellow
                        ));

                    }
                    return view;
                }
            };
            listView.setAdapter(adapter);
        }
    }

    //changes calendar values to those picked by user in datepicker dialog
    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            myCalendar.set(Calendar.HOUR_OF_DAY, 0);
            myCalendar.set(Calendar.MINUTE, 0);
            myCalendar.set(Calendar.SECOND, 0);
            myCalendar.set(Calendar.MILLISECOND, 0);
            setDates();
        }

    };

    //checks user calendar dates for valid values and sets calendar to values useable in search
    private void setDates() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.UK);
        Date currentDate = new Date();

        Log.e("MyTag","current date is " + currentDate.toString());

        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);
        cal.add(Calendar.DATE, -50);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date dateBefore50Days = cal.getTime();

        Log.e("MyTag","current date is " + dateBefore50Days.toString());

        if (isStartDate == true) {
            if (myCalendar.getTime().before(dateBefore50Days)) {
                searchError.setVisibility(View.VISIBLE);
                searchError.setText("Please enter a date less than 50 days from today");
            } else if (myCalendar.getTime().after(currentDate)) {
                searchError.setVisibility(View.VISIBLE);
                searchError.setText("Please enter a date today or before");
            } else {
                filterStartDate = myCalendar.getTime();
                startDate.setText(sdf.format(myCalendar.getTime()));
                searchError.setVisibility(View.INVISIBLE);
                endDate.setEnabled(true);
            }
        } else {
            if (myCalendar.getTime().after(currentDate)) {
                searchError.setVisibility(View.VISIBLE);
                searchError.setText("Please enter a date today or before");
            } else if (myCalendar.getTime().before(filterStartDate)) {
                searchError.setVisibility(View.VISIBLE);
                searchError.setText("End date cannot be before the start date");
            } else {
                filterEndDate = myCalendar.getTime();
                endDate.setText(sdf.format(myCalendar.getTime()));
                searchError.setVisibility(View.INVISIBLE);
                filterSearchButton.setEnabled(true);
            }
        }
    }

    //method for returning maximum attribute value for specified attribute of EQDataClass object
    public int maxValueArray(ArrayList<EQDataClass> filteredList, String variableName) {
        int position = 0;
        double maxValue = 0;

        if (variableName.equals("magnitude")) {
            maxValue = filteredList.get(0).getMagnitude();
            for (EQDataClass item : filteredList) {
                if (item.getMagnitude() > maxValue) {
                    maxValue = item.getMagnitude();
                    position = filteredList.indexOf(item);
                }
            }
        }
        if (variableName.equals("longitude")) {
            maxValue = filteredList.get(0).getLongitude();
            for (EQDataClass item : filteredList) {
                if (item.getLongitude() > maxValue) {
                    maxValue = item.getLongitude();
                    position = filteredList.indexOf(item);
                }
            }
        }
        if (variableName.equals("latitude")) {
            maxValue = filteredList.get(0).getLatitude();
            for (EQDataClass item : filteredList) {
                if (item.getLatitude() > maxValue) {
                    maxValue = item.getLatitude();
                    position = filteredList.indexOf(item);
                }
            }
        }
        if (variableName.equals("depth")) {
            maxValue = filteredList.get(0).getDepth();
            for (EQDataClass item : filteredList) {
                if (item.getDepth() > maxValue) {
                    maxValue = item.getDepth();
                    position = filteredList.indexOf(item);
                }
            }
        }
        return position;
    }

    //method for returning minimum attribute value for specified attribute of EQDataClass object
    public int minValueArray(ArrayList<EQDataClass> filteredList, String variableName) {
        int position = 0;
        double minValue = 0;

        if (variableName.equals("longitude")) {
            minValue = filteredList.get(0).getLongitude();
            for (EQDataClass item : filteredList) {
                if (item.getLongitude() < minValue) {
                    minValue = item.getLongitude();
                    position = filteredList.indexOf(item);
                }
            }
        } else if (variableName.equals("latitude")) {
            minValue = filteredList.get(0).getLatitude();
            for (EQDataClass item : filteredList) {
                if (item.getLatitude() < minValue) {
                    minValue = item.getLatitude();
                    position = filteredList.indexOf(item);
                }
            }
        } else if (variableName.equals("depth")) {
            minValue = filteredList.get(0).getDepth();
            for (EQDataClass item : filteredList) {
                if (item.getDepth() < minValue) {
                    minValue = item.getDepth();
                    position = filteredList.indexOf(item);
                }
            }
        }
        return position;
    }

    //this method is is similar to xml pullparser from earlier async task only is used to update xml data in background
    public void updateXMLData() {

        alist.clear();
        stringLocationMagnitude.clear();

        URL aurl;
        URLConnection yc;
        BufferedReader in = null;
        String inputLine = "";


        Log.e("MyTag", "in run");

        try {
            Log.e("MyTag", "in try");
            aurl = new URL(urlSource);
            yc = aurl.openConnection();
            in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
            Log.e("MyTag", "after ready");
            //
            // Throw away the first 2 header lines before parsing
            //
            //
            //
            while ((inputLine = in.readLine()) != null) {
                result = result + inputLine;
                Log.e("MyTag", inputLine);

            }
            in.close();

            int xmlIndex = result.indexOf("<item>");
            result = result.substring(xmlIndex);
            xmlIndex = result.lastIndexOf("</channel>");
            result = result.substring(0, xmlIndex);

        } catch (IOException ae) {
            Log.e("MyTag", "ioexception in run");
        }


        //
        // Now that you have the xml data you can parse it
        //

        try {
            EQDataClass item = null;

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(result));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                // Found a start tag
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equalsIgnoreCase("item")) {
                        Log.e("MyTag", "Item Start Tag found");
                        item = new EQDataClass();
                    } else if (xpp.getName().equalsIgnoreCase("title")) {
                        // Now just get the associated text
                        String temp = xpp.nextText();
                        // Do something with text
                        Log.e("MyTag", "title is " + temp);
                        item.setTitle(temp);
                    } else
                        // Check which Tag we have
                        if (xpp.getName().equalsIgnoreCase("description")) {
                            // Now just get the associated text
                            String temp = xpp.nextText();
                            // Do something with text
                            Log.e("MyTag", "description is " + temp);
                            item.setDescription(temp);
                            item.setLocation(temp.substring(temp.indexOf("Location: ") + 10, temp.indexOf(" ; Lat/long")));
                            item.setMagnitude(Double.parseDouble(temp.substring(temp.indexOf("Magnitude: ") + 11)));
                            item.setDepth(Integer.parseInt(temp.substring(temp.indexOf("Depth: ") + 7, temp.indexOf(" km ;"))));
                        } else
                            // Check which Tag we have
                            if (xpp.getName().equalsIgnoreCase("pubDate")) {
                                // Now just get the associated text
                                String temp = xpp.nextText();
                                // Do something with text
                                Log.e("MyTag", "pubDate is " + temp);
                                item.setPubDate(temp);
                                temp = temp.substring(5, 16);
                                SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM yyyy");
                                try {
                                    item.setEqDate(dateFormatter.parse(temp));
                                    dateFormatter.applyPattern("dd-MM-yyyy");
                                } catch (ParseException e) {
                                    System.out.println(e);
                                }
                            } else
                                // Check which Tag we have
                                if (xpp.getName().equalsIgnoreCase("geo:lat")) {
                                    // Now just get the associated text
                                    String temp = xpp.nextText();
                                    // Do something with text
                                    Log.e("MyTag", "latitude is " + temp);
                                    item.setLatitude(Double.parseDouble(temp));
                                } else
                                    // Check which Tag we have
                                    if (xpp.getName().equalsIgnoreCase("geo:long")) {
                                        // Now just get the associated text
                                        String temp = xpp.nextText();
                                        // Do something with text
                                        Log.e("MyTag", "longitude is " + temp);
                                        item.setLongitude(Double.parseDouble(temp));
                                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (xpp.getName().equalsIgnoreCase("item")) {
                        Log.e("MyTag", "item is " + item.toString());
                        alist.add(item);
                    }
                }


                // Get the next event
                eventType = xpp.next();

            } // End of while

        } catch (XmlPullParserException ae1) {
            Log.e("MyTag", "Parsing error" + ae1.toString());
        } catch (IOException ae1) {
            Log.e("MyTag", "IO error during parsing");
        }

        Log.e("MyTag", "End document");
    }

    //similar to async task updates the listView UI with updated XML data, must implement runnable in order to make changes to UI
    public void updateList() {

        for (EQDataClass item : alist) {
            stringLocationMagnitude.add(item.locationMagnitude());
        }


        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.activity_listview, stringLocationMagnitude) {

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {

                        View view = super.getView(position, convertView, parent);

                        EQDataClass item = alist.get(position);
                        double i = item.getMagnitude();
                        Log.e("MyTag", "magnitude is " + i);

                        if (i >= 2) {
                            view.setBackgroundColor(getResources().getColor(
                                    android.R.color.holo_red_dark
                            ));
                        } else if (i >= 1 && i < 2) {
                            view.setBackgroundColor(getResources().getColor(
                                    android.R.color.holo_orange_dark
                            ));
                        } else {
                            view.setBackgroundColor(getResources().getColor(
                                    R.color.yellow
                            ));

                        }
                        return view;
                    }
                };
                listView.setAdapter(adapter);
                listView.setEnabled(true);
            }
        });
    }
}
