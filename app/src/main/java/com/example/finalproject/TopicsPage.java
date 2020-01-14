package com.example.finalproject;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

public class TopicsPage extends AppCompatActivity
{
    private ActionBarDrawerToggle actBarToggle;
    protected ArrayList<Topic> topics = new ArrayList<Topic>();
    protected ArrayList<Comment> comments = new ArrayList<Comment>();
    protected ArrayAdapter<String> arrayAdapter;
    protected int topicPage;
    protected String sort;
    protected boolean hasNextPage;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.topics_page);

        DrawerLayout drawLayout;
        Button nextButton;
        Button previousButton;
        Button sortingButton;
        TextView pageNoView;

        sort = "Popular";
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ListView topicsList = findViewById(R.id.discussion_posts);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        hasNextPage = true;

        System.out.println("DEBUG: Creating listview click listener");
        topicsList.setAdapter(arrayAdapter);
        topicsList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                System.out.println("DEBUG: Item: " + id + " Pressed");
                //create handler for item clicked
                new CommentPaginationHandler(TopicsPage.this).execute(position);

            }
        });

        drawLayout = findViewById(R.id.drawerLayoutHome);
        actBarToggle = new ActionBarDrawerToggle(this, drawLayout, toolbar, R.string.Open, R.string.Close);
        actBarToggle.setDrawerIndicatorEnabled(true);

        drawLayout.addDrawerListener(actBarToggle);
        actBarToggle.syncState();
        NavigationView navView = findViewById(R.id.nav_host);

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
            {
                int id = menuItem.getItemId();

                if(id == R.id.messages)
                {
                    //uses the commentsPage class to open the new page
                    Intent commentPage = new Intent(TopicsPage.this, CommentsPage.class);
                    TopicsPage.this.startActivity(commentPage);
                }

                return true;
            }
        });

        //get buttons references and add listeners
        nextButton = findViewById(R.id.nextButton);
        previousButton = findViewById(R.id.previousButton);
        sortingButton = findViewById(R.id.sortingButton);

        System.out.println("DEBUG: Next button on click listener");
        nextButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                System.out.println("DEBUG: Next button pressed");
                new TopicPaginationHandler(TopicsPage.this).execute("Next");
            }
        });

        System.out.println("DEBUG: Previous button on click listener");
        previousButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                System.out.println("DEBUG: Previous button pressed");
                new TopicPaginationHandler(TopicsPage.this).execute("Prev");
            }
        });

        System.out.println("DEBUG: Sorting button on click listener");
        sortingButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                System.out.println("DEBUG: Sorting button pressed");

                Button thisButton = findViewById(R.id.sortingButton);
                String thisButtonText = thisButton.getText().toString();
                TextView discussion = findViewById(R.id.discussions_title);

                topicPage = 1;

                //toggle the button text
                if(thisButtonText.equals("Newest"))
                {
                    thisButton.setText(getString(R.string.popular));
                    discussion.setText(getString(R.string.newest_discussions));
                }
                else
                {
                    thisButton.setText(getString(R.string.newest));
                    discussion.setText(getString(R.string.popular_discussions));
                }

                //get the new information
                new TopicPaginationHandler(TopicsPage.this).execute("refresh");
            }
        });

        //puts the app on page 1
        topicPage = 1;
        pageNoView = findViewById(R.id.pageNo);
        pageNoView.setText("" + topicPage);
        new TopicPaginationHandler(this).execute("refresh");

        /*NavController navController = Navigation.findNavController(this, R.id.nav_host);
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(navController.getGraph()).build();
        Toolbar toolbar = findViewById(R.id.toolbar);
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        return actBarToggle.onOptionsItemSelected(menuItem) || super.onOptionsItemSelected(menuItem);
    }

    private static class TopicPaginationHandler extends AsyncTask<String, Void, Void>
    {
        String response = "";
        String sortingButtonString;
        String buttonPagination;
        int newTopicPage;
        private WeakReference<TopicsPage> activityReference;

        TopicPaginationHandler(TopicsPage context)
        {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute()
        {
            //gets the weak reference to the activity - needed for the UI manipulation
            TopicsPage topicsPage = activityReference.get();

            if(topicsPage == null || topicsPage.isFinishing())
            {
                return;
            }

            //has access to UI thread for the button reference
            Button sortingButton = topicsPage.findViewById(R.id.sortingButton);
            sortingButtonString = sortingButton.getText().toString();

            //has to inverse for itself since the button's text switches before this is executed
            if(sortingButtonString.equals("Newest"))
            {
                sortingButtonString = "Popular";
            }
            else
            {
                sortingButtonString = "Newest";
            }
        }

        @Override
        protected Void doInBackground(String ...buttonText)
        {
            System.out.println("DEBUG: Sending next request");

            //gets the weak reference to the activity
            TopicsPage topicsPage = activityReference.get();

            if(topicsPage == null || topicsPage.isFinishing())
            {
                return null;
            }

            //need to store what button was pressed to know if they pressed prev or next
            buttonPagination = buttonText[0];

            //only update the global topicPage if the request was successful
            newTopicPage = topicsPage.topicPage;
            String urlString = "http://10.0.2.2:8080/finalYearProject/AppServer?searchType=";
            String urlSafeString;
            HttpURLConnection urlConnection;
            InputStream input;
            Gson gson = new Gson();

            if(buttonPagination.equals("Prev") && newTopicPage>1)
            {
                newTopicPage --;
            }
            else if(buttonPagination.equals("Next") && topicsPage.hasNextPage)
            {
                newTopicPage ++;
            }
            else
            {
                //else will refresh current page instead
                buttonPagination = "refresh";
            }

            try
            {
                urlSafeString = urlString + URLEncoder.encode(sortingButtonString, "UTF-8");
                urlSafeString = urlSafeString + "&topicPage=" + newTopicPage;

                System.out.println("DEBUG: Connecting to " + urlSafeString);
                // the url we wish to connect to
                URL url = new URL(urlSafeString);

                // open the connection to the specified URL
                urlConnection = (HttpURLConnection) url.openConnection();

                // get the response from the server in an input stream
                input = new BufferedInputStream(urlConnection.getInputStream());

                try
                {
                    // covert the input stream to a string
                    response = topicsPage.convertStreamToString(input);
                    System.out.println("DEBUG: response: " + response);
                }
                catch (Exception e)
                {
                    System.out.println("DEBUG: failed to turn response into string: " + e);
                    return null;
                }

                try
                {
                    topicsPage.topics = gson.fromJson(response, new TypeToken<ArrayList<Topic>>(){}.getType());
                }
                catch(Exception e)
                {
                    System.out.println("DEBUG: Invalid topic array list");
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
                System.out.println("DEBUG: Failed to connect");
            }
            return null;
        }

        //has access to UI thread for updates
        @Override
        protected void onPostExecute(Void voided)
        {
            //gets the weak reference to the activity
            TopicsPage topicsPage = activityReference.get();

            if(topicsPage == null || topicsPage.isFinishing())
            {
                return;
            }

            String topicTitle;
            String username;
            String comments;
            String dateTime;
            System.out.println("DEBUG Topics: " + topicsPage.topics);
            //set the button to the updated text
            if(topicsPage.topics != null)
            {
                System.out.println("DEBUG: Clearing list then populating it");
                topicsPage.arrayAdapter.clear();

                for(int count = 0; count < topicsPage.topics.size(); count++)
                {
                    if(count >= 10)
                    {
                        topicsPage.hasNextPage = true;
                        //dont allow count to go over 10
                        break;
                    }

                    topicTitle = topicsPage.topics.get(count).getTopicTitle();
                    username = topicsPage.topics.get(count).getUsername();
                    comments = "" + topicsPage.topics.get(count).getComments();
                    dateTime = topicsPage.topics.get(count).getDateTime();

                    topicsPage.arrayAdapter.add(topicTitle +" User: " + username +
                    " Comments: " + comments+ " Posted: " + dateTime);

                    //if there are less than 11 results there isn't a new page
                    topicsPage.hasNextPage = false;
                }

                //updating the global variable to reflect the current state of the app
                //updates the page number at the bottom too
                topicsPage.topicPage = newTopicPage;
                TextView pageNo = topicsPage.findViewById(R.id.pageNo);
                pageNo.setText("" + newTopicPage);
            }
            else
            {
                System.out.println("DEBUG: topics is null");
            }
        }
    }

    private static class CommentPaginationHandler extends AsyncTask<Integer, Void, ArrayList<Topic>>
    {
        String response = "";
        String sortingButtonString;
        String buttonPagination;
        int newTopicPage;
        private WeakReference<TopicsPage> activityReference;

        CommentPaginationHandler(TopicsPage context)
        {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute()
        {
            //gets the weak reference to the activity - needed for the UI manipulation
            TopicsPage topicsPage = activityReference.get();

            if(topicsPage == null || topicsPage.isFinishing())
            {
                return;
            }

            //has access to UI thread for the button reference
            Button sortingButton = topicsPage.findViewById(R.id.sortingButton);
            sortingButtonString = sortingButton.getText().toString();

            //has to inverse for itself since the button's text switches before this is executed
            if(sortingButtonString.equals("Newest"))
            {
                sortingButtonString = "Popular";
            }
            else
            {
                sortingButtonString = "Newest";
            }
        }

        @Override
        protected ArrayList<Topic> doInBackground(Integer ...topicId)
        {
            System.out.println("DEBUG: Sending next request");

            //gets the weak reference to the activity
            TopicsPage topicsPage = activityReference.get();

            if(topicsPage == null || topicsPage.isFinishing())
            {
                return null;
            }



            //only update the global topicPage if the request was successful
            newTopicPage = topicsPage.topicPage;
            String urlString = "http://10.0.2.2:8080/finalYearProject/AppServer?searchType=";
            String urlSafeString;
            HttpURLConnection urlConnection;
            InputStream input;
            Gson gson = new Gson();
            ArrayList<Topic> topics = new ArrayList<>();

            if(buttonPagination.equals("Prev") && newTopicPage>1)
            {
                newTopicPage --;
            }
            else if(buttonPagination.equals("Next") && topicsPage.hasNextPage)
            {
                newTopicPage ++;
            }
            else
            {
                //else will refresh current page instead
                buttonPagination = "refresh";
            }

            try
            {
                urlSafeString = urlString + URLEncoder.encode(sortingButtonString, "UTF-8");
                urlSafeString = urlSafeString + "&topicPage=" + newTopicPage;

                System.out.println("DEBUG: Connecting to " + urlSafeString);
                // the url we wish to connect to
                URL url = new URL(urlSafeString);

                // open the connection to the specified URL
                urlConnection = (HttpURLConnection) url.openConnection();

                // get the response from the server in an input stream
                input = new BufferedInputStream(urlConnection.getInputStream());

                try
                {
                    // covert the input stream to a string
                    response = topicsPage.convertStreamToString(input);
                    System.out.println("DEBUG: response: " + response);
                }
                catch (Exception e)
                {
                    System.out.println("DEBUG: failed to turn response into string: " + e);
                    return null;
                }

                try
                {
                    topics = gson.fromJson(response, new TypeToken<ArrayList<Topic>>(){}.getType());
                }
                catch(Exception e)
                {
                    System.out.println("DEBUG: Invalid topic array list");
                    return null;
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
                System.out.println("DEBUG: Failed to connect");
            }

            return topics;
        }

        //has access to UI thread for updates
        @Override
        protected void onPostExecute(ArrayList<Topic> topics)
        {
            //gets the weak reference to the activity
            TopicsPage topicsPage = activityReference.get();

            if(topicsPage == null || topicsPage.isFinishing())
            {
                return;
            }

            String topicTitle;
            String username;
            String comments;
            String dateTime;
            System.out.println("DEBUG Topics: " + topics);
            //set the button to the updated text
            if(topics != null)
            {
                System.out.println("DEBUG: Clearing list then populating it");
                topicsPage.arrayAdapter.clear();

                for(int count = 0; count < topics.size(); count++)
                {
                    if(count >= 10)
                    {
                        topicsPage.hasNextPage = true;
                        //dont allow count to go over 10
                        break;
                    }

                    topicTitle = topics.get(count).getTopicTitle();
                    username = topics.get(count).getUsername();
                    comments = "" + topics.get(count).getComments();
                    dateTime = topics.get(count).getDateTime();

                    topicsPage.arrayAdapter.add(topicTitle +" User: " + username +
                            " Comments: " + comments+ " Posted: " + dateTime);

                    //if there are less than 11 results there isn't a new page
                    topicsPage.hasNextPage = false;
                }

                //updating the global variable to reflect the current state of the app
                //updates the page number at the bottom too
                topicsPage.topicPage = newTopicPage;
                TextView pageNo = topicsPage.findViewById(R.id.pageNo);
                pageNo.setText("" + newTopicPage);
            }
            else
            {
                System.out.println("DEBUG: topics is null");
            }
        }
    }

    //needed for the server responses
    private String convertStreamToString(InputStream inputStream)
    {
        java.util.Scanner scan = new java.util.Scanner(inputStream).useDelimiter("\\A");
        return scan.hasNext() ? scan.next() : "";
    }
}

