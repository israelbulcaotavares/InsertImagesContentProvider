package com.tavares.pets;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tavares.pets.data.PetContract;


public class PetCursorAdapter extends CursorAdapter {

    /* Constructs a new {@link PetCursorAdapter}.
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public PetCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /* Makes a new blank list item view. No data is set (or bound) to the views yet.
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /* This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView summaryTextView = (TextView) view.findViewById(R.id.summary);
        ImageView imageView =  view.findViewById(R.id.editImagem);

        int nameColumnIndex = cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_NAME);
        int breedColumnIndex = cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_BREED);
        int imagemColumnIndex = cursor.getColumnIndex(PetContract.PetEntry.COLUMN_IMAGEM);

        String petName = cursor.getString(nameColumnIndex);
        String petBreed = cursor.getString(breedColumnIndex);
        String petImagem = cursor.getString(imagemColumnIndex);
        Glide.with(imageView.getContext())
                .load(petImagem)
                .asBitmap()
                .error(android.R.drawable.ic_menu_report_image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);


        nameTextView.setText(petName);
        if (TextUtils.isEmpty(petBreed)) {
            petBreed = context.getString(R.string.unknown);
        }




        nameTextView.setText(petName);
        summaryTextView.setText(petBreed);
        //imageView.setTag(petImagem);
    }
}