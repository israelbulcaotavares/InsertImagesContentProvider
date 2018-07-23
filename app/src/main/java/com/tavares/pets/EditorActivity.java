/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tavares.pets;

import android.Manifest;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.tavares.pets.data.PetContract;
import com.tavares.pets.model.Pet;

import java.io.File;


/* Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PET_LOADER = 0;

    private Uri mCurrentPetUri;
    private EditText mNameEditText;
    private EditText mBreedEditText;
    private EditText mWeightEditText;
    private Spinner mGenderSpinner;

    private int mGender = PetContract.PetEntry.GENDER_UNKNOWN;

    private boolean mPetHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPetHasChanged = true;
            return false;
        }
    };


    private ImageView editImagem;
    private View layout;
    private final int CAMERA = 1;
    private final int GALERIA = 2;
    private String localArquivoFoto;
    private static final int TIRA_FOTO = 123;
    private boolean fotoResource = false;
    Pet pet;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        pet = new Pet();

        Intent intent = getIntent();
        if (intent != null) {
            mCurrentPetUri = intent.getData();
            if (mCurrentPetUri == null) {
                setTitle(getString(R.string.editor_activity_title_new_pet));
                invalidateOptionsMenu();
            } else {
                setTitle(getString(R.string.editor_activity_title_edit_pet));
                getLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);
            }
        }

        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);
        editImagem =  findViewById(R.id.editImagem);

        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();



        File imgFile = new File(pet.getImagem());
        if(imgFile.exists()){
            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            editImagem.setImageBitmap(bitmap);

        }

        editImagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertaImagem();
            }
        });
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetContract.PetEntry.GENDER_MALE;
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetContract.PetEntry.GENDER_FEMALE;
                    } else {
                        mGender = PetContract.PetEntry.GENDER_UNKNOWN;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = PetContract.PetEntry.GENDER_UNKNOWN;
            }
        });
    }

    private void setPet() {

        pet.setName(mNameEditText.getText().toString().trim());
        //String nameString = mNameEditText.getText().toString().trim();
        pet.setBreed(mBreedEditText.getText().toString().trim());
        //String breedString = mBreedEditText.getText().toString().trim();
        pet.setWeight(mWeightEditText.getText().toString().trim());
        //String weightString = mWeightEditText.getText().toString().trim();

        pet.setImagem((String) editImagem.getTag());

        int weight = 0;
        if (!TextUtils.isEmpty(pet.getWeight())) {
            weight = Integer.parseInt(pet.getWeight());
        }

        if (mCurrentPetUri == null &&
                TextUtils.isEmpty(pet.getName()) && TextUtils.isEmpty(pet.getBreed()) &&
                TextUtils.isEmpty(pet.getWeight()) && mGender == PetContract.PetEntry.GENDER_UNKNOWN) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(PetContract.PetEntry.COLUMN_PET_NAME, pet.getName());
        values.put(PetContract.PetEntry.COLUMN_PET_BREED, pet.getBreed());
        values.put(PetContract.PetEntry.COLUMN_PET_GENDER, mGender);
        values.put(PetContract.PetEntry.COLUMN_PET_WEIGHT, weight);
        values.put(PetContract.PetEntry.COLUMN_IMAGEM, pet.getImagem());

        if (mCurrentPetUri == null) {
            Uri uri = getContentResolver().insert(PetContract.PetEntry.CONTENT_URI, values);
            if (uri == null) {
                Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.pet_save), Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentPetUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.error),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.pet_save),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentPetUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                setPet();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mPetHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                PetContract.PetEntry._ID,
                PetContract.PetEntry.COLUMN_PET_NAME,
                PetContract.PetEntry.COLUMN_PET_BREED,
                PetContract.PetEntry.COLUMN_PET_GENDER,
                PetContract.PetEntry.COLUMN_PET_WEIGHT,
                PetContract.PetEntry.COLUMN_IMAGEM};

        return new CursorLoader(this,
                mCurrentPetUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            int nameColumnIndex = data.getColumnIndex(PetContract.PetEntry.COLUMN_PET_NAME);
            int breedColumnIndex = data.getColumnIndex(PetContract.PetEntry.COLUMN_PET_BREED);
            int genderColumnIndex = data.getColumnIndex(PetContract.PetEntry.COLUMN_PET_GENDER);
            int weightColumnIndex = data.getColumnIndex(PetContract.PetEntry.COLUMN_PET_WEIGHT);
            int imagemColumnIndex = data.getColumnIndex(PetContract.PetEntry.COLUMN_IMAGEM);

            String name = data.getString(nameColumnIndex);
            String breed = data.getString(breedColumnIndex);
            int gender = data.getInt(genderColumnIndex);
            int weight = data.getInt(weightColumnIndex);
            String imagem = data.getString(imagemColumnIndex);
            carregaImagem(imagem);

            mNameEditText.setText(name);
            mBreedEditText.setText(breed);
            String weightText = "" + weight;
            mWeightEditText.setText(weightText);
            switch (gender) {
                case PetContract.PetEntry.GENDER_MALE:
                    mGenderSpinner.setSelection(1);
                    break;
                case PetContract.PetEntry.GENDER_FEMALE:
                    mGenderSpinner.setSelection(2);
                    break;
                default:
                    mGenderSpinner.setSelection(0);
                    break;
            }
            editImagem.setTag("");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("");
        mGenderSpinner.setSelection(0);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!mPetHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /* Perform the deletion of the pet in the database.
     */
    private void deletePet() {
        if (mCurrentPetUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentPetUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_pet_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_pet_successful), Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    //TODO: PARTE DA CAMERA E GALERIA
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!fotoResource) {
            if (resultCode == RESULT_OK && null != data) {
                Uri imagemSel = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                Cursor cursor = getContentResolver().query(
                        imagemSel,
                        filePathColumn,
                        null,
                        null,
                        null);

                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String caminhoFoto = cursor.getString(columnIndex);
                cursor.close();

                carregaImagem(caminhoFoto);
            }

        }else{
            if (requestCode == TIRA_FOTO) {
                if(resultCode == Activity.RESULT_OK) {
                    carregaImagem(localArquivoFoto);
                } else {
                    localArquivoFoto = null;
                }
            }
        }

    }



    public void carregaImagem(String localArquivoFoto) {
        if(localArquivoFoto != null) {
            Bitmap imagemFoto = BitmapFactory.decodeFile(localArquivoFoto);
            editImagem.setImageBitmap(imagemFoto);
            editImagem.setTag(localArquivoFoto);
        }
    }

    private void alertaImagem(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecione a fonte da info");
        builder.setPositiveButton("Camera", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                clicaTirarFoto();
            }
        });
        builder.setNegativeButton("Galeria", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                clicaCarregaImagem();
            }
        });
        builder.create().show();
    }

    private void clicaTirarFoto(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED){
            requestCameraPermission();
        } else {
            abrirCamera();
        }
    }

    private void requestCameraPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)){

            Snackbar.make(layout, "É necessário permitir para utilizar a câmera!",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(EditorActivity.this,
                            new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            CAMERA);
                }
            }).show();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    CAMERA);
        }
    }



    public void abrirCamera(){
        fotoResource = true;
        localArquivoFoto = getExternalFilesDir(null) + "/"+ System.currentTimeMillis()+".jpg";

        Intent irParaCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        irParaCamera.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this,
                this.getApplicationContext().getPackageName() + ".provider", new File(localArquivoFoto)));
        startActivityForResult(irParaCamera, 123);
    }


    private void clicaCarregaImagem(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED){
            requestGaleriaPermission();
        } else {
            abrirGaleria();
        }
    }

    private void requestGaleriaPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)){

            Snackbar.make(layout, "É necessário permitir para utilizar a galeria!",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(EditorActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            GALERIA);
                }
            }).show();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    GALERIA);
        }
    }

    private void abrirGaleria(){
        fotoResource=false;
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    clicaTirarFoto();
                }
                break;
            case GALERIA:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    clicaCarregaImagem();
                }
                break;
        }
    }


}


