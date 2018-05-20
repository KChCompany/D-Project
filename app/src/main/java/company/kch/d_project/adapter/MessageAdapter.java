package company.kch.d_project.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import company.kch.d_project.R;
import company.kch.d_project.model.MessageModel;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<MessageModel> list;

    public MessageAdapter(List<MessageModel> list) {
        this.list = list;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false));
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        final MessageModel message = list.get(position);

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

        holder.textName.setText(message.userName);
        holder.textMessage.setText(message.massage);
        holder.textTime.setText(dateFormat.format(message.time));

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textMessage, textTime;


        public MessageViewHolder(View itemView) {
            super(itemView);

            textName = itemView.findViewById(R.id.textName);
            textMessage = itemView.findViewById(R.id.textMessage);
            textTime = itemView.findViewById(R.id.textTime);

        }
    }
}
