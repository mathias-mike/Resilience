package com.psyclone.resilience.groups;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.psyclone.resilience.R;
import com.psyclone.resilience.models.RGroup;

import java.util.ArrayList;
import java.util.List;

public class ResilienceGroupsViewModel extends AndroidViewModel {

    private Application application;
    private MutableLiveData<List<RGroup>> groups;

    public ResilienceGroupsViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
    }

    public LiveData<List<RGroup>> getGroups() {
        if (groups == null) {
            groups = new MutableLiveData<>();
            loadGroups();
        }
        return groups;
    }

    private void loadGroups() {
        String[] groupArray = application.getResources().getStringArray(R.array.groups);
        String[] descArray = application.getResources().getStringArray(R.array.groups_desc);

        List<RGroup> rGroups = new ArrayList<>();
        for(int i=0; i<groupArray.length; i++){
            RGroup group = new RGroup();
            group.setName(groupArray[i]);
            group.setDesc(descArray[i]);

            switch (i){
                case 0:
                    group.setResourceId(R.raw.addiction);
                    break;
                case 1:
                    group.setResourceId(R.raw.aids);
                    break;
                case 2:
                    group.setResourceId(R.raw.anger);
                    break;
                case 3:
                    group.setResourceId(R.raw.anxiety);
                    break;
                case 4:
                    group.setResourceId(R.raw.cancer);
                    break;
                case 5:
                    group.setResourceId(R.raw.grief);
                    break;
            }
            rGroups.add(group);
        }

        groups.postValue(rGroups);
    }
}
