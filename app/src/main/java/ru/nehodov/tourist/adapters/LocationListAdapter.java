package ru.nehodov.tourist.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ru.nehodov.tourist.R;
import ru.nehodov.tourist.model.UserLocation;

public class LocationListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final LocationListAdapterListener listener;

    private final List<UserLocation> locations = new ArrayList<>();

    public LocationListAdapter(LocationListAdapterListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.location_list_item, parent, false);
        return new RecyclerView.ViewHolder(view) { };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final UserLocation location = locations.get(position);
        holder.itemView.setOnClickListener((view -> listener.selectLocation(location)));
        TextView locationName = holder.itemView.findViewById(R.id.locationName);
        TextView time = holder.itemView.findViewById(R.id.time);
        TextView latitude = holder.itemView.findViewById(R.id.latitude);
        TextView longitude = holder.itemView.findViewById(R.id.longitude);
        locationName.setText(location.getName());
        time.setText(location.getTime());
        latitude.setText(String.format(Locale.getDefault(), "%.5f", location.getLatitude()));
        longitude.setText(String.format(Locale.getDefault(), "%.5f", location.getLongitude()));
    }

    @Override
    public int getItemCount() {
        if (locations == null) {
            return 0;
        } else {
            return locations.size();
        }
    }

    public void setLocations(List<UserLocation> locations) {
        this.locations.clear();
        this.locations.addAll(locations);
        notifyDataSetChanged();
    }

    public interface LocationListAdapterListener {
        void selectLocation(UserLocation location);
    }
}
