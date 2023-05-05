package com.thesis.deliverytracking;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.thesis.deliverytracking.models.Delivery;
import com.thesis.deliverytracking.models.UserInfo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class ContentActivity extends AppCompatActivity {

    BottomNavigationView navigationView;
    private Toolbar myToolbar;
    UserInfo userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            return;
        }

        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
            if (firebaseAuth.getCurrentUser() == null){
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
        .get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                userData = task.getResult().toObject(UserInfo.class);
                if (userData != null && userData.role.equals("admin")) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.body_container, new DeliveryFormFragment()).commit();
                }
                else{
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                    Bundle bundle = new Bundle();
                    bundle.putParcelable("userData", userData);
                    DeliveryListFragment fragment = new DeliveryListFragment();
                    fragment.setArguments(bundle);
                    transaction.replace(R.id.body_container, fragment, "deliveryList");
                    transaction.addToBackStack("deliveryList");
                    transaction.commit();
                }
                invalidateOptionsMenu();
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_deliveries:
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                Bundle bundle = new Bundle();
                bundle.putParcelable("userData", userData);
                DeliveryListFragment fragment = new DeliveryListFragment();
                fragment.setArguments(bundle);
                transaction.replace(R.id.body_container, fragment, "deliveryList");
                transaction.addToBackStack("deliveryList");
                transaction.commit();
                break;
            case R.id.nav_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.body_container, new DeliveryFormFragment()).commit();
                break;
            case R.id.nav_logout:
                FirebaseAuth.getInstance().signOut();
                break;
            case R.id.nav_drivers:
                getSupportFragmentManager().beginTransaction().replace(R.id.body_container, new DriversFragment()).commit();
                break;
            case R.id.nav_vehicles:
                getSupportFragmentManager().beginTransaction().replace(R.id.body_container, new VehiclesFragment()).commit();
                break;
            case R.id.nav_locations:
                getSupportFragmentManager().beginTransaction().replace(R.id.body_container, new LocationsFragment()).commit();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        if (userData != null && userData.role.equals("admin")) {
            inflater.inflate(R.menu.item_menu, menu);
        } else {
            inflater.inflate(R.menu.item_menu_driver, menu);
        }

        return true;
    }
}