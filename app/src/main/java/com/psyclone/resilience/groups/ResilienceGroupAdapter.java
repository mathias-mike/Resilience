package com.psyclone.resilience.groups;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;
import com.psyclone.resilience.R;
import com.psyclone.resilience.models.RGroup;

import java.util.List;

public class ResilienceGroupAdapter extends RecyclerView.Adapter<ResilienceGroupAdapter.ResilienceGroupViewHolder> {

    private Context context;
    private List<RGroup> groups;
    private GroupClickListener groupClickListener;

    public interface GroupClickListener{
        void onGroupClicked(View view, RGroup group);
    }

    public ResilienceGroupAdapter(Context context, List<RGroup> groups,
                                  GroupClickListener groupClickListener){
        this.context = context;
        this.groups = groups;
        this.groupClickListener = groupClickListener;
    }

    @NonNull
    @Override
    public ResilienceGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.recycler_resilience_group_item, parent, false);
        return new ResilienceGroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResilienceGroupViewHolder holder, int position) {
        holder.itemView.setTag(position);

        Glide.with(context)
                .load(groups.get(position).getResourceId())
                .into(holder.group_image);

        holder.group_name.setText(groups.get(position).getName());
        holder.group_desc.setText(groups.get(position).getDesc());
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public class ResilienceGroupViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        RoundedImageView group_image;
        TextView group_name;
        TextView group_desc;

        public ResilienceGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            group_image = itemView.findViewById(R.id.group_image);
            group_name = itemView.findViewById(R.id.group_name);
            group_desc = itemView.findViewById(R.id.group_desc);
        }

        @Override
        public void onClick(View view) {
            int tag = (Integer) view.getTag();
            groupClickListener.onGroupClicked(view, groups.get(tag));
        }
    }

    public void swapData(List<RGroup> groups){
        this.groups = groups;
        notifyDataSetChanged();
    }
}
