package com.psyclone.resilience.groups;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.messaging.FirebaseMessaging;
import com.psyclone.resilience.R;
import com.psyclone.resilience.databinding.FragmentResilienceGroupBinding;
import com.psyclone.resilience.models.RGroup;
import com.psyclone.resilience.models.User;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static com.psyclone.resilience.utils.Constants.TAG;

public class ResilienceGroupFragment extends Fragment implements ResilienceGroupAdapter.GroupClickListener{

    private ResilienceGroupAdapter adapter;

    private User me;

    private String token;

    public ResilienceGroupFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentResilienceGroupBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_resilience_group, container, false);

        adapter = new ResilienceGroupAdapter(getActivity(), new ArrayList<>(), this);
        binding.resilienceGroupList.setAdapter(adapter);

        ResilienceGroupsViewModel resilienceGroupsViewModel = new ViewModelProvider(this)
                .get(ResilienceGroupsViewModel.class);

        resilienceGroupsViewModel.getGroups().observe(getViewLifecycleOwner(), groups -> adapter.swapData(groups));

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireActivity());
        if(account != null) {
            me = new User();
            me.set_id(account.getEmail());
            me.setName(account.getDisplayName());
            if(account.getPhotoUrl() != null) me.setUrl(account.getPhotoUrl().toString());
        } else {
            Toast.makeText(requireActivity(), "Unable to get your data!", Toast.LENGTH_SHORT).show();
        }

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                return;
            }
            // Get new FCM registration token
            token = task.getResult();
        });

        return binding.getRoot();
    }

    @Override
    public void onGroupClicked(View view, RGroup group) {
        if(me != null && token != null) {
            Bundle bundle = new Bundle();
            bundle.putString("topic", group.getName());
            bundle.putSerializable("me", me);
            bundle.putSerializable("token", token);
            Navigation.findNavController(view).navigate(R.id.action_reseilienceGroups_to_groupMessages, bundle);
        }
    }
}