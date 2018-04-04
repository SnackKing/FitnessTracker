package com.harshil.zach.fitnesstracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.Circle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Zach on 2/21/2018.
 */
public class FriendArrayAdapter extends ArrayAdapter<User>
{
    private Context mContext;
    List<User> friendList = new ArrayList<>();


    public FriendArrayAdapter(Context context, List<User> list)
    {
        super(context,0,list);
        friendList = list;
        this.mContext = context;

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View listItem = convertView;

        if (convertView == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.friend_item, parent, false);
        }
        final User currentFriend = friendList.get(position);


        TextView name = (TextView) listItem.findViewById(R.id.name);
        name.setText(currentFriend.getName());


        TextView email = (TextView) listItem.findViewById(R.id.email);
        email.setText(currentFriend.getEmail());

        TextView rank = (TextView) listItem.findViewById(R.id.rank);
        rank.setText("Rank " + Integer.toString(currentFriend.getRank()));

        TextView runRank = listItem.findViewById(R.id.runRank);
        runRank.setText("Run Rank " + Integer.toString(currentFriend.getRunRank()));

        CircleImageView profile = listItem.findViewById(R.id.profile_image);
        String img = currentFriend.getProfile();
        if(!img.equals("")){
            byte[] imageAsBytes = Base64.decode(img.getBytes(), Base64.DEFAULT);
            profile.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
        }
        ImageButton delete = listItem.findViewById(R.id.remove);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog dialog = new AlertDialog.Builder(getContext())
                        .setTitle("Delete Friend")
                        .setMessage("Are you sure that you want to delete " + currentFriend.name + " from your friend list")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                final String altEmail = currentFriend.email.replace('.',',');
                                final String userAltEmail = user.getEmail().replace('.',',');
                                DatabaseReference node = FirebaseDatabase.getInstance().getReference().getRoot().child("Users").child(user.getUid()).child("Friends").child(altEmail);
                                node.setValue(null);
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().getRoot().child("email_uid");
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String friendId = dataSnapshot.child(altEmail).getValue(String.class);
                                        FirebaseDatabase.getInstance().getReference().getRoot().child("Users").child(friendId).child("FriendedBy").child(userAltEmail).setValue(null);
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {}
                                });




                                friendList.remove(position);
                                Toast.makeText(getContext(),"Friend Removed",Toast.LENGTH_SHORT).show();
                                notifyDataSetChanged();

                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });
        return listItem;



    }

    static class ViewHolder
    {

        TextView tv;
    }
}