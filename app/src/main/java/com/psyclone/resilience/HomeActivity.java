package com.psyclone.resilience;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.psyclone.resilience.databinding.ActivityHomeBinding;
import com.psyclone.resilience.messages.GroupMessageFragment;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActivityHomeBinding binding;

    private DrawerLayout drawerLayout;

    private GoogleSignInClient mGoogleSignInClient;

    private NavController navController;

    private boolean isAboutFragment = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewPump.init(ViewPump.builder().addInterceptor(new CalligraphyInterceptor(new CalligraphyConfig.Builder()
                .setFontAttrId(R.attr.fontPath).build())).build());
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        drawerLayout = binding.drawerLayout;
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                binding.menuBar.setImageResource(R.drawable.ic_baseline_arrow_back_24);
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                if(!isAboutFragment) binding.menuBar.setImageResource(R.drawable.ic_baseline_menu_24);
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if(destination.getId() == controller.getGraph().getStartDestination()) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                binding.menuBar.setImageResource(R.drawable.ic_baseline_menu_24);
                binding.menuBar.setOnClickListener(v -> openCloseDrawer());
                binding.title.setText(("Resilience Groups"));
                isAboutFragment = false;
            } else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                binding.menuBar.setImageResource(R.drawable.ic_baseline_arrow_back_24);
                binding.menuBar.setOnClickListener(v -> onBackPressed());
            }
        });

        binding.navView.setNavigationItemSelectedListener(this);

        GroupMessageFragment.getObservableTopic().observe(this, topic -> binding.title.setText(topic));
    }

    // This attaches the font setting to the base of the activity
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    private void openCloseDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.closeDrawer(GravityCompat.START);
        else drawerLayout.openDrawer(GravityCompat.START);
    }

    public void closeNavigationDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.about_fragment:
                isAboutFragment = true;
                closeNavigationDrawer();
                binding.title.setText(("About"));
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                binding.menuBar.setOnClickListener(v -> onBackPressed());
                navController.navigate(R.id.action_reseilienceGroups_to_aboutFragment);
                break;
            case R.id.sign_out:
                mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                    startActivity(new Intent(this, LoginActivity.class));
                    this.finish();
                });
                break;
        }

        return true;
    }
}