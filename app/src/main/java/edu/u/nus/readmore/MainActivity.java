package edu.u.nus.readmore;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import edu.u.nus.readmore.Intermediate.IntermediateActivity;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        AsyncArticleResponse {
    private boolean TESTING_DB = false;

    private DrawerLayout drawer;
    private Menu optionsMenu;
    private MenuItem logoutItem;
    private ProgressBar progressBar;
    private boolean isLoggedIn;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mFirebaseAuthStateListener;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private NavigationView navigationView;
    private TextView articleContentTextView, articleTitleTextView, navHeaderUserEmail;
    private ImageView articleImageView;
    private ImageButton toolbarShareBtn;
    private ScrollView articleScrollView;
    private Button previousArticleBtn, nextArticleBtn;
    private Article currentArticle = null;
    private final List<String> listOfTopics = Arrays.asList("Science", "Math", "History", "Arts",
            "Computer Science", "Sports");
    private User currentUser = null;
    private boolean changedCurrentUser;
    static MainActivity INSTANCE;
    private long lastClickTime = 0;
    private View noInternetConnectionView;
    private Button retryInternetConnectionBtn;

    // onCreateOptionsMenu is called once
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Initialize toolbar_menu xml file (android:visible="false" at start)
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        optionsMenu = menu;
        logoutItem = optionsMenu.findItem(R.id.logout_item);
        logoutItem.setVisible(isLoggedIn);
        return true;
    }

    // For MenuItem selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (hasInternetConnection()) {
            int menuItemId = item.getItemId();
            if (menuItemId == R.id.logout_item) {
                showLogoutDialog();
            } else if (menuItemId == R.id.toolbar_share_item) {
                startActivity(Intent.createChooser(getSharingIntent(currentArticle), "Share Using"));
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Do you want to logout?")
                .setCancelable(true)
                .setPositiveButton("Yes", (DialogInterface dialog, int which) -> logout())
                .setNegativeButton("No", null)
                .show();
    }

    @NonNull
    private Intent getSharingIntent(Article article) {
        String shareTitle = article.getTitle();
        String shareBody = article.getUrl();
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, shareTitle);
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        return sharingIntent;
    }

    private void logout() {
        if (changedCurrentUser) {
            String userID = currentUser.getId();
            // Update database
            db.collection("Users")
                    .document(userID)
                    .set(currentUser)
                    .addOnSuccessListener((Void aVoid) -> mFirebaseAuth.signOut());
            changedCurrentUser = false;
        } else {
            mFirebaseAuth.signOut();
        }
        Toast.makeText(this, "You have successfully signed out", Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setDayNightTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // required for passing information from MainActivity to Filter
        INSTANCE = this;

        if (TESTING_DB) {
            UpdateDbTopics fd = new UpdateDbTopics(getApplicationContext());
            fd.execute();
        }
        setupToolbarAndAppDrawer();
        setupScreenElements();

        setupRetryInternetConnectionBtn();
        // Initialise Firebase components
        mFirebaseAuth = FirebaseAuth.getInstance();
        setupFirebaseAuthStateListener();

        // Add onClickListeners for article buttons
        nextArticleBtn.setOnClickListener((View v) -> {
            if (hasInternetConnection()) {
                if (isValidClick(1000)) {
                    lastClickTime = SystemClock.elapsedRealtime();
                    if (currentUser == null) {
                        // Guest mode, random article generated, displayed
                        getNewArticle();
                    } else {
                        Article nextArticle = currentUser.accessNextArticle();
                        if (nextArticle != null) {
                            currentArticle = nextArticle;
                            displayArticle(currentArticle);
                        } else {
                            addToReadList(currentArticle);
                            // Get new article, check user readList for duplicate article
                            // Add to user readlist
                            getNewArticle();
                        }
                        changedCurrentUser = true;
                    }
                }
            } else {
                noInternetConnectionView.setVisibility(View.VISIBLE);
                noInternetConnectionView.bringToFront();
            }
        });

        previousArticleBtn.setOnClickListener((View v) -> {
            if (isValidClick(1000)) {
                lastClickTime = SystemClock.elapsedRealtime();
                Article previousArticle = currentUser.accessPreviousArticle();
                if (previousArticle != null) {
                    currentArticle = previousArticle;
                    changedCurrentUser = true;
                }
                displayArticle(currentArticle);
            }
        });

        // Checking current display have any article,
        // if article present display back same article,
        // else generate new article for user to view
        // help to prevent generating new article if user change view Orientation
        if (savedInstanceState == null) {
            // Check if user is logged in
            FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
            // Get user object
            if (firebaseUser != null && hasInternetConnection()) {
                final String userID = firebaseUser.getUid();
                DocumentReference userDoc = db.collection("Users").document(userID);
                userDoc.get().addOnSuccessListener((DocumentSnapshot documentSnapshot) -> {
                    currentUser = documentSnapshot.toObject(User.class);
                    // Check read list for most recent Article
                    if (currentUser != null) {
                        Article latestArticle = currentUser.accessLatestArticle();
                        if (latestArticle == null) {
                            getNewArticle();
                        } else {
                            currentArticle = latestArticle;
                            displayArticle(currentArticle);
                        }
                    } else {
                        getNewArticle();
                    }
                });
            } else {
                if (hasInternetConnection()) {
                    getNewArticle();
                } else {
                    // no internet connection
                    noInternetConnectionView.setVisibility(View.VISIBLE);
                    noInternetConnectionView.bringToFront();
                }
            }
        } else {
            currentArticle = new Article(savedInstanceState.getString("title"),
                    savedInstanceState.getString("description"),
                    savedInstanceState.getString("pageid"),
                    savedInstanceState.getString("URL"),
                    savedInstanceState.getString("imageURL"));
            displayArticle(currentArticle);
            if (mFirebaseAuth.getCurrentUser() != null) {
                currentUser = (User) savedInstanceState.getSerializable("User");
            }
        }
    }

    private void setupScreenElements() {
        articleTitleTextView = findViewById(R.id.articleTitleTextView);
        articleContentTextView = findViewById(R.id.articleContentTextView);
        articleImageView = findViewById(R.id.articleImageView);
        previousArticleBtn = findViewById(R.id.previousArticleBtn);
        nextArticleBtn = findViewById(R.id.nextArticleBtn);
        articleScrollView = findViewById(R.id.articleScrollView);
        progressBar = findViewById(R.id.progressBar);
        navHeaderUserEmail = navigationView.getHeaderView(0).findViewById(R.id.nav_user_email);
        noInternetConnectionView = findViewById(R.id.no_internet_connection_view);
        retryInternetConnectionBtn = noInternetConnectionView.findViewById(R.id.retry_internet_connection_btn);
    }

    private void setupToolbarAndAppDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Remove default app name placement on action bar
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private boolean isValidClick(int delay) {
        return SystemClock.elapsedRealtime() - lastClickTime > delay;
    }

    private void setupRetryInternetConnectionBtn() {
        retryInternetConnectionBtn.setOnClickListener((View v) -> {
            if (hasInternetConnection()) {
                // refresh main activity
                Intent refreshActivity = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(refreshActivity);
                finish();
            }
        });
    }

    private void setupFirebaseAuthStateListener() {
        mFirebaseAuthStateListener = (@NonNull FirebaseAuth firebaseAuth) -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                setupUserNavigationView(user);
                isLoggedIn = true;
                if (currentUser == null) {
                    setCurrentUser();
                }
            } else {
                setupAnonymousNavigationView();
                isLoggedIn = false;
                currentUser = null;
                changedCurrentUser = false;
            }
            // declare that the options menu has changed, so should be recreated
            // calls onCreateOptionsMenu method when menu needs to be displayed again
            invalidateOptionsMenu();
        };
    }

    private void setupUserNavigationView(FirebaseUser user) {
        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.drawer_menu_user);
        navHeaderUserEmail.setText(user.getEmail());
        previousArticleBtn.setVisibility(View.VISIBLE);
    }

    private void setupAnonymousNavigationView() {
        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.drawer_menu_login);
        navHeaderUserEmail.setText(R.string.nav_header_guest);
        previousArticleBtn.setVisibility(View.GONE);
    }

    private void fetchMissingFilterCategories() {
        Map<String, Boolean> userFilter = currentUser.getUserFilter();
        boolean needToUpdateDB = false;
        for (String topic : listOfTopics) {
            // Check for missing filter categories
            Boolean topicExists = userFilter.get(topic);
            if (topicExists == null) {
                // create topic in user filter
                currentUser.addFilterTopic(topic, true);
                needToUpdateDB = true;
            }
        }
        if (needToUpdateDB) {
            updateUserDatabase(currentUser);
        }
    }

    private void setDayNightTheme() {
        SharedPreferences prefs = getSharedPreferences("myTheme", MODE_PRIVATE);
        boolean isNightTheme = prefs.getBoolean("isNightTheme", false);
        if (isNightTheme) {
            setTheme(R.style.AppThemeDark_NoActionBar);
        } else {
            setTheme(R.style.AppTheme_NoActionBar);
        }
    }

    private boolean hasInternetConnection() {
        // return a boolean corresponding to whether device is connected to internet
        ConnectivityManager connectivityManager = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    private void setCurrentUser() {
        // check if user is logged in
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            // Logged in
            final String userID = firebaseUser.getUid();
            DocumentReference userDoc = db.collection("Users").document(userID);
            userDoc.get().addOnSuccessListener((DocumentSnapshot documentSnapshot) -> {
                currentUser = documentSnapshot.toObject(User.class);
                Article latestArticle = currentUser.accessLatestArticle();
                if (latestArticle != null) {
                    currentArticle = latestArticle;
                    displayArticle(latestArticle);
                }
            });
        } else {
            currentUser = null;
        }
    }

    private void updateUserDatabase(User user) {
        String userID = user.getId();
        // Update database
        db.collection("Users")
                .document(userID)
                .set(user);
        changedCurrentUser = false;
    }

    private void addToReadList(Article article) {
        if (currentUser != null && article != null) {
            if (!currentUser.hasReadArticle(article)) {
                changedCurrentUser = true;
                currentUser.addReadArticle(article);
            }
        }
    }

    /**
     * Generates a new Article (from Wikipedia API), based on a randomTopic and random pageid.
     */
    private void getNewArticle() {
        // Random generated number for retrieving article
        String checker = Util.autoId();
        // String storing the collection name of topic
        String randomTopic = randomTopicGenerator();
        CollectionReference topicRef = db.collection(randomTopic);
        Query subTopic;
        subTopic = topicRef.whereGreaterThanOrEqualTo("ID", checker).limit(1);
        subTopic.get().addOnCompleteListener((@NonNull Task<QuerySnapshot> task) -> {
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
        });
    }

    private String randomTopicGenerator() {
        if (currentUser != null) {
            fetchMissingFilterCategories();
            Map<String, Boolean> userFilter = currentUser.getUserFilter();
            List<String> userFilteredList = new ArrayList<>();
            for (String topic : listOfTopics) {
                Boolean topicExists = userFilter.get(topic);
                if (topicExists != null && topicExists) {
                    userFilteredList.add(topic);
                }
            }
            int randomIndex = new Random().nextInt(userFilteredList.size());
            return userFilteredList.get(randomIndex);
        } else {
            int randomIndex = new Random().nextInt(listOfTopics.size());
            return listOfTopics.get(randomIndex);
        }
    }

    private void generateArticleContent(String pageid) {
        // set interface (AsyncArticleResponse) in FetchArticleData back to this class
        // executes AsyncTask, runs AsyncArticleResponse interface function at the end
        (new FetchArticleData(this)).execute(pageid);
    }

    // For AsyncArticleResponse interface, to setup new articles obtained from getNewArticle()
    @Override
    public void processFinish(Map<String, String> output) {
        currentArticle = new Article(output.get("title"),
                output.get("description"),
                output.get("pageid"),
                output.get("URL"),
                output.get("imageURL"));
        if (currentUser != null) {
            while (currentUser.hasReadArticle(currentArticle)) {
                getNewArticle();
            }
            // Save current article
            addToReadList(currentArticle);
        }
        displayArticle(currentArticle);
    }

    private void displayArticle(Article article) {
        articleTitleTextView.setText(article.getTitle());
        if (Build.VERSION.SDK_INT >= 24) {
            articleContentTextView.setText(Html.fromHtml(article.getDescription(), Html.FROM_HTML_MODE_LEGACY));
        } else {
            // For older SDK
            articleContentTextView.setText(Html.fromHtml(article.getDescription()));
        }
        String imageURL = article.getImageURL();
        // Show article image view
        articleImageView.setVisibility(View.VISIBLE);
        if (imageURL.equals("")) {
            // Show default app logo
            articleImageView.setImageResource(R.drawable.read_more_logo);
            articleImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        } else {
            // Retrieve image and set to article ImageView
            new DownloadImageTask((ImageView) findViewById(R.id.articleImageView)).
                    execute(article.getImageURL());
        }
        browserDirectView(articleImageView, article.getUrl());
        articleScrollView.setVisibility(View.VISIBLE);
        // Scroll to top
        articleScrollView.smoothScrollTo(0, 0);
    }

    private void browserDirectView(View view, final String URL) {
        view.setOnClickListener((View v) -> {
            Intent viewIntent =
                    new Intent("android.intent.action.VIEW",
                            Uri.parse(URL));
            viewIntent.addCategory(Intent.CATEGORY_BROWSABLE);
            startActivity(viewIntent);
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_login:
                String loginKey = "Login";
                String loginValue = "login";
                startIntermediateActivity(loginKey, loginValue);
                break;
            case R.id.nav_settings:
                String settingsKey = "Settings";
                if (mFirebaseAuth.getCurrentUser() != null) {
                    startInterActHashMap(settingsKey);
                } else {
                    String settingsValue = "settings";
                    startIntermediateActivity(settingsKey, settingsValue);
                }
                break;
            case R.id.nav_edit_profile:
                String editProfileKey = "Edit Profile";
                String editProfileValue = "Edit Profile";
                startIntermediateActivity(editProfileKey, editProfileValue);
                break;
            case R.id.nav_filter:
                String filterKey = "Filter";
                startInterActHashMap(filterKey);
                break;
            case R.id.nav_read_history:
                String readHistoryKey = getString(R.string.read_history_key);
                Intent startIntent = new Intent(getApplicationContext(), IntermediateActivity.class);
                startIntent.putExtra(readHistoryKey, currentUser);
                startActivity(startIntent);
                break;
            case R.id.nav_log_out:
                new AlertDialog.Builder(this)
                        .setTitle("Logout")
                        .setMessage("Do you want to logout?")
                        .setCancelable(true)
                        .setPositiveButton("Yes", (DialogInterface dialog, int which) -> logout())
                        .setNegativeButton("No", null)
                        .show();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startInterActHashMap(String key) {
        fetchMissingFilterCategories();
        HashMap<String, Boolean> userFilter =
                (HashMap<String, Boolean>) currentUser.getUserFilter();
        Intent startIntent = new Intent(getApplicationContext(), IntermediateActivity.class);
        startIntent.putExtra(key, userFilter);
        startActivity(startIntent);
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

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mFirebaseAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFirebaseAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mFirebaseAuthStateListener);
        }
        if (isFinishing()) {
            if (changedCurrentUser) {
                updateUserDatabase(currentUser);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                    .setPositiveButton("Yes", (DialogInterface dialog, int which) -> finish())
                    .setNegativeButton("No", null)
                    .show();
        }
    }

    public static MainActivity getActivityInstance() {
        return INSTANCE;
    }

    public void updateCurrentUserFilter(Map<String, Boolean> updatedFilter) {
        currentUser.updateUserFilter(updatedFilter);
        changedCurrentUser = true;
    }

    /*

     */
    public void enableProgressBar(Integer visibility) {
        progressBar.setVisibility(visibility);
        articleScrollView.setVisibility(View.INVISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
    }

    public void disableProgressBar(Integer visibility) {
        progressBar.setVisibility(visibility);
        articleScrollView.setVisibility(View.VISIBLE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    public void enableImage() {
        articleImageView.setVisibility(View.VISIBLE);
    }

    public void disableImage() {
        articleImageView.setVisibility(View.GONE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // recreate() has the same effect as a configuration change
        if (changedCurrentUser && currentUser != null) {
            updateUserDatabase(currentUser);
        }

        super.onSaveInstanceState(outState);
        if (currentArticle != null) {
            // storing article content into outState which retrieve back in onCreate
            outState.putString("title", currentArticle.getTitle());
            outState.putString("description", currentArticle.getDescription());
            outState.putString("pageid", currentArticle.getPageid());
            outState.putString("URL", currentArticle.getUrl());
            outState.putString("imageURL", currentArticle.getImageURL());
        }
        // For rotating screen plus changing user filter
        if (currentUser != null) {
            outState.putSerializable("User", currentUser);
        }
    }
}