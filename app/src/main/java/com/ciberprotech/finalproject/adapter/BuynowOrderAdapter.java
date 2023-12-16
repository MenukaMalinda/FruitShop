package com.ciberprotech.finalproject.adapter;

        import android.annotation.SuppressLint;
        import android.content.Context;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.TextView;

        import androidx.annotation.NonNull;
        import androidx.cardview.widget.CardView;
        import androidx.recyclerview.widget.RecyclerView;

        import com.ciberprotech.finalproject.R;
        import com.ciberprotech.finalproject.listner.OrderSelectListner;
        import com.ciberprotech.finalproject.model.Order;

        import java.util.ArrayList;


public class BuynowOrderAdapter extends RecyclerView.Adapter<BuynowOrderAdapter.ViewHolder>{
    private ArrayList<Order> orders;
    private Context context;
    private OrderSelectListner orderSelectListner;

    public BuynowOrderAdapter(ArrayList<Order> orders, Context context, OrderSelectListner orderSelectListner) {
        this.orders = orders;
        this.context = context;
        this.orderSelectListner = orderSelectListner;
    }

    @NonNull
    @Override
    public BuynowOrderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.view_orders_layout, parent, false);
        return new BuynowOrderAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BuynowOrderAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Order order = orders.get(position);

        holder.orderIdTxt.setText("id : "+order.getId());
        holder.orderDateTxt.setText("Date : "+order.getDate_time());
        holder.orderPriceTxt.setText("Price : Rs. "+order.getTotal()+".00");
        holder.orderStatusTxt.setText("Status : "+(String.valueOf((order.getDeliver_status())).equals("1") ? "Delivered":"Pending"));

        holder.orderCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderSelectListner.selectOrder(orders.get(position));
            }
        });

    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdTxt, orderDateTxt,orderPriceTxt,orderStatusTxt;
        CardView orderCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdTxt = itemView.findViewById(R.id.orderid);
            orderDateTxt = itemView.findViewById(R.id.orderdate);
            orderPriceTxt = itemView.findViewById(R.id.orderprice);
            orderStatusTxt = itemView.findViewById(R.id.orderstatus);
            orderCard = itemView.findViewById(R.id.orderCard);
        }
    }
}
