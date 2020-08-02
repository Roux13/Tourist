package ru.nehodov.tourist.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ru.nehodov.tourist.R;
import ru.nehodov.tourist.entities.UserRoute;

public class RouteListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final RouteListAdapterListener listener;

    private final List<UserRoute> routes = new ArrayList<>();

    public RouteListAdapter(RouteListAdapterListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.route_list_item, parent, false);
        return new RecyclerView.ViewHolder(view) { };
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final UserRoute route = routes.get(position);
        TextView startPointName = holder.itemView.findViewById(R.id.startPointName);
        TextView endPointName = holder.itemView.findViewById(R.id.endPointName);
        startPointName.setText(route.getStartPointName());
        endPointName.setText(route.getEndPointName());
        holder.itemView.setOnClickListener(v -> listener.selectRoute(route));
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    public void setRoutes(List<UserRoute> routes) {
        this.routes.clear();
        this.routes.addAll(routes);
        notifyDataSetChanged();
    }

    public interface RouteListAdapterListener {
        void selectRoute(UserRoute route);
    }
}
