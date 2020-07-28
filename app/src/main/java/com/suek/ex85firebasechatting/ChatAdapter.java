package com.suek.ex85firebasechatting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends BaseAdapter {    //listview 는 baseAdapter 상속

    Context context;
    ArrayList<MessageItem> messageItems;


    public ChatAdapter(Context context, ArrayList<MessageItem> messageItems) {
        this.context = context;
        this.messageItems = messageItems;
    }




    @Override
    public int getCount() {
        return messageItems.size();
    }

    @Override
    public Object getItem(int position) {
        return messageItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        MessageItem item= messageItems.get(position);

        //1. create view [ my_msgbox 또는 other_msgbox ]
        View itemView= null;
        if( G.nickName.equals(item.name) ) itemView= LayoutInflater.from(context).inflate(R.layout.listview_my_msgbox, parent, false);
        else itemView= LayoutInflater.from(context).inflate(R.layout.listview_other_msgbox, parent, false);

        //2. bind view
        CircleImageView civ= itemView.findViewById(R.id.civ);
        TextView tvName= itemView.findViewById(R.id.tv_name);
        TextView tvMsg= itemView.findViewById(R.id.tv_msg);
        TextView tvTime= itemView.findViewById(R.id.tv_time);

        Glide.with(context).load(item.profileUrl).into(civ);

        tvName.setText(item.name);
        tvMsg.setText(item.message);
        tvTime.setText(item.time);

        return itemView;
    }
}
