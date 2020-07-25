package ru.nehodov.tourist;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import ru.nehodov.tourist.adapters.LocationListAdapter;
import ru.nehodov.tourist.model.UserLocation;

public class LocationListFragment extends Fragment
        implements LocationListAdapter.LocationListAdapterListener {

    private LocationListFragmentListener listener;

    private RecyclerView recycler;
    private LocationListAdapter adapter;

    public static LocationListFragment newInstance() {
        LocationListFragment fragment = new LocationListFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location_list, container, false);
        recycler = view.findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(requireActivity()));
        adapter = new LocationListAdapter(this);
        adapter.setLocations(listener.getLocations());
        recycler.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.setLocations(listener.getLocations());
    }

    @Override
    public void selectLocation(UserLocation location) {
        listener.selectLocation(location);
    }

    public interface LocationListFragmentListener {
        List<UserLocation> getLocations();

        void selectLocation(UserLocation location);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (LocationListFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(String.format(
                    "Class %s must implement %s",
                    context.getClass().getSimpleName(), listener.getClass().getSimpleName()));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.listener = null;
    }
}