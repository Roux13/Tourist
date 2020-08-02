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

import ru.nehodov.tourist.adapters.RouteListAdapter;
import ru.nehodov.tourist.entities.UserRoute;

public class RouteListFragment extends Fragment
        implements RouteListAdapter.RouteListAdapterListener {

    private RouteListFragmentListener listener;

    private RouteListAdapter adapter;

    public static RouteListFragment newInstance() {
        return new RouteListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_route_list, container, false);
        RecyclerView recycler = view.findViewById(R.id.routesRecycler);
        recycler.setLayoutManager(new LinearLayoutManager(requireActivity()));
        adapter = new RouteListAdapter(this);
        adapter.setRoutes(listener.getRoutes());
        recycler.setAdapter(adapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.setRoutes(listener.getRoutes());
    }

    @Override
    public void selectRoute(UserRoute route) {
        listener.selectRoute(route);
    }

    public interface RouteListFragmentListener {
        List<UserRoute> getRoutes();

        void selectRoute(UserRoute route);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (RouteListFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(String.format("%s must implement %s",
                    context.getClass().getSimpleName(), listener.getClass().getSimpleName()));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}