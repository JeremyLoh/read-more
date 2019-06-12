package edu.u.nus.readmore;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        AsyncArticleResponse {
    private boolean TESTING_DB = false;

    private DrawerLayout drawer;
    private Menu optionsMenu;
    private MenuItem logoutItem;
    private boolean isLoggedIn;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mFirebaseAuthStateListener;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private NavigationView navigationView;
    private TextView articleTextView;
//    private String currentPageid;

    // onCreateOptionsMenu is called once
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Initialize logout_menu xml file (android:visible="false" at start)
        getMenuInflater().inflate(R.menu.logout_menu, menu);
        optionsMenu = menu;
        logoutItem = optionsMenu.findItem(R.id.logout_item);
        if (isLoggedIn) {
            logoutItem.setVisible(true);
        } else {
            logoutItem.setVisible(false);
        }
        return true;
    }

    // For MenuItem selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout_item:
                new AlertDialog.Builder(this)
                        .setTitle("Logout")
                        .setMessage("Do you want to logout?")
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                logout();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mFirebaseAuth.signOut();
        Toast
                .makeText(this,
                        "You have successfully signed out",
                        Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (TESTING_DB) {
            FetchData fd = new FetchData();
            fd.execute();
        }

        // Initialise article components
        articleTextView = findViewById(R.id.articleContentTextView);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialise Firebase components
        mFirebaseAuth = FirebaseAuth.getInstance();
        // Checking user status for displaying different menu options
        mFirebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // user is signed in
                    navigationView.getMenu().clear();
                    navigationView.inflateMenu(R.menu.drawer_menu_user);
                    isLoggedIn = true;
                } else {
                    // user is signed out
                    navigationView.getMenu().clear();
                    navigationView.inflateMenu(R.menu.drawer_menu_login);
                    isLoggedIn = false;
                }
                // declare that the options menu has changed, so should be recreated.
                // calls onCreateOptionsMenu method when menu needs to be displayed again
                invalidateOptionsMenu();
            }
        };

        // Navigation drawer bar set-up
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    /**
     * Starts a new Intent, with extra information given based on key and value passed to
     * Function.
     *
     * @param key   Used to identify intent
     * @param value Used to identify intent
     */
    private void startIntermediateActivity(String key, String value) {
        Intent startIntent = new Intent(getApplicationContext(), IntermediateActivity.class);
        startIntent.putExtra(key, value);
        startActivity(startIntent);
    }

    private void getNewArticle() {
        // Random generated number for retrieving article
        String checker = Util.autoId();

        // String storing the collection name of topic
        String randomTopic = randomTopicGenerator();
        CollectionReference topicRef = db.collection(randomTopic);
        Query subTopic = topicRef.whereGreaterThan("ID", checker).limit(1);
        if (subTopic == null) {
            subTopic = topicRef.whereLessThanOrEqualTo("ID", checker).limit(1);
        }
        subTopic.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> documentList = task.getResult().getDocuments();
                    if (documentList == null || documentList.size() == 0) {
                        getNewArticle();
                    } else {
                        DocumentSnapshot document = documentList.get(0);
                        Map<String, Object> docContent = document.getData();
                        List<String> listOfPageID = (List<String>) (docContent.get("pageid"));
                        int randomIndex = new Random().nextInt(listOfPageID.size());
                        String pageid = listOfPageID.get(randomIndex);
                        generateArticleContent(pageid);
                    }
                } else {
                    getNewArticle();
                }
            }
        });
    }

    private void generateArticleContent(String pageid) {
        // set interface (AsyncArticleResponse) in FetchArticleData back to this class
        (new FetchArticleData(this)).execute(pageid);
    }

    private String randomTopicGenerator() {
        //TODO
        return "Science";
    }

    // For AsyncArticleResponse interface, to setup new articles obtained from getNewArticle()
    @Override
    public void processFinish(Map<String, String> output) {
        Article currentArticle = new Article(output.get("title"),
                output.get("description"),
                output.get("pageid"),
                output.get("URL"),
                output.get("imageURL"));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_login:
                String loginKey = getString(R.string.login_key);
                String loginValue = "login";
                startIntermediateActivity(loginKey, loginValue);
                break;
            case R.id.nav_settings:
                String settingsKey = getString(R.string.settings_key);
                String settingsValue = "settings";
                startIntermediateActivity(settingsKey, settingsValue);
                break;
            case R.id.log_out:
                new AlertDialog.Builder(this)
                        .setTitle("Logout")
                        .setMessage("Do you want to logout?")
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                logout();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mFirebaseAuthStateListener);
        getNewArticle();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFirebaseAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mFirebaseAuthStateListener);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //closing activity
            new AlertDialog.Builder(this)
                    .setTitle("Quit")
                    .setMessage("Are you sure?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }
}
