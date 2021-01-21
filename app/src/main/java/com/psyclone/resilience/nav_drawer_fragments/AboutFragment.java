package com.psyclone.resilience.nav_drawer_fragments;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.psyclone.resilience.R;
import com.psyclone.resilience.databinding.FragmentAboutBinding;

import org.jetbrains.annotations.NotNull;

public class AboutFragment extends Fragment {

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentAboutBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_about, container, false);

        Glide.with(requireContext())
                .load(R.raw.resilience_logo)
                .into(binding.aboutImage);

        return binding.getRoot();
    }
}