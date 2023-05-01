package va.vanthe.app_chat_2.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import va.vanthe.app_chat_2.R;
import va.vanthe.app_chat_2.adapters.ChatAdapter;
import va.vanthe.app_chat_2.databinding.ActivityChatMessage3Binding;
import va.vanthe.app_chat_2.entity.ChatMessage;
import va.vanthe.app_chat_2.entity.Conversation;
import va.vanthe.app_chat_2.entity.User;
import va.vanthe.app_chat_2.fragment.DialogViewImageFragment;
import va.vanthe.app_chat_2.ulitilies.Constants;
import va.vanthe.app_chat_2.ulitilies.HelperFunction;
import va.vanthe.app_chat_2.ulitilies.PreferenceManager;

public class ChatMessageActivity3 extends AppCompatActivity {
    private ActivityChatMessage3Binding binding;
    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();


    private PreferenceManager account;
    private Conversation mConversation = new Conversation();

    private List<ChatMessage> chatMessageList = new ArrayList<>();
    private User mUser = new User();

    private ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatMessage3Binding.inflate(getLayoutInflater());


        setContentView(binding.getRoot());


        init();
        setListener();
        listenChatMessage();

    }
    private void init(){
        account = new PreferenceManager(getApplicationContext());
        try {
            mConversation = (Conversation) getIntent().getSerializableExtra(Constants.KEY_CONVERSATION);
            mUser = getIntent().getParcelableExtra(Constants.KEY_USER);

        } catch (Exception e){
            e.printStackTrace();
        }
        if(mConversation != null){
            if (mConversation.getStyleChat()==Constants.KEY_CONVERSATION_STYLE_CHAT_SINGLE){
                binding.textName.setText(mUser.getLastName());
                binding.imageInfo.setImageBitmap(HelperFunction.getBitmapFromEncodedImageString(mUser.getImage()));
            } else if (mConversation.getStyleChat()==Constants.KEY_TYPE_CHAT_GROUP) {
                binding.textName.setText(mConversation.getConversationName());
                if(mConversation.getConversationAvatar()!= null){
                    binding.imageProfile.setImageBitmap(HelperFunction.getBitmapFromEncodedImageString(mConversation.getConversationAvatar()));
                }
                
            }
        }
        if (mConversation.getQuickEmotions()!= null){
            binding.textviewSend.setText(mConversation.getQuickEmotions());
        } else {
            binding.textviewSend.setText(getString(R.string.text_detail));
        }

        ChatAdapter chatAdapter = new ChatAdapter(chatMessageList, account.getString(Constants.KEY_ACCOUNT_USER_ID), mConversation.getStyleChat(), new ChatAdapter.OnItemClickListener(){

            @Override
            public void onItemClick(Uri uri) {
                Bundle bundle= new Bundle();
                bundle.putString("image_path", uri.toString());

                DialogViewImageFragment dialogViewImageFragment = new DialogViewImageFragment();
                dialogViewImageFragment.setArguments(bundle);
                dialogViewImageFragment.show(getSupportFragmentManager(),"dialogViewImage");
            }
        });
        binding.chatRCV.setAdapter(chatAdapter);
        chatAdapter.notifyDataSetChanged();
        binding.chatRCV.setVisibility(View.VISIBLE);


    }

    private void setListener() {
        binding.imageBack.setOnClickListener(view -> onBackPressed());
        binding.imageInfo.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), InfoChatActivity.class);
            intent.putExtra(Constants.KEY_CONVERSATION, mConversation);
            startActivity(intent);
        });

        binding.imageImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
      binding.inputMessage.addTextChangedListener(new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

          }

          @Override
          public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

          }

          @Override
          public void afterTextChanged(Editable editable) {
              String mess = editable.toString().trim();
              if(!mess.equals("")){
                  binding.textviewMore.setVisibility(View.GONE);

                  Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_send);

                  Drawable wrapperDrawable = DrawableCompat.wrap(drawable);
                  DrawableCompat.setTint(wrapperDrawable, ContextCompat.getColor(getApplicationContext(), R.color.blue));

                  binding.textviewSend.setText("");
                  binding.textviewSend.setCompoundDrawablesRelativeWithIntrinsicBounds(wrapperDrawable,null, null,null);

              } else{
                  binding.textviewMore.setVisibility(View.VISIBLE);
                  binding.textviewSend.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null,null,null);
                  if(mConversation.getQuickEmotions() != null){
                      binding.textviewSend.setText(mConversation.getQuickEmotions());
                  }else {
                      binding.textviewSend.setText(R.string.text_detail);
                  }
              }

          }
      });
      binding.textviewSend.setOnClickListener(view -> {
        sendMessage(binding.inputMessage.getText().toString().trim(),Constants.KEY_CHAT_MESSAGE_STYLE_MESSAGE_TEXT);
      });

    }



    private void sendMessage(String message, int StyleMessage){
        binding.inputMessage.setText("");
        ChatMessage chatMessage = new ChatMessage();

        chatMessage.setMessage(message);
        chatMessage.setSenderId(account.getString(Constants.KEY_ACCOUNT_USER_ID));
        chatMessage.setDataTime(new Date());
        chatMessage.setConversationId(mConversation.getId());
        chatMessage.setStyleMessage(mConversation.getStyleChat());

        HashMap<String, Object> hashMap= chatMessage.toHashMap();
        database.collection(Constants.KEY_CHAT_MESSAGE)
                .add(hashMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        updateConversation(message, mConversation.getStyleChat());

                    }
                });

    }

    private void updateConversation(String message, int styleChat) {
        DocumentReference documentReference = database.collection(Constants.KEY_CONVERSATION).document(mConversation.getId());

        mConversation.setMessageTime(new Date());
        mConversation.setNewMessage(message);
        mConversation.setSenderId(account.getString(Constants.KEY_ACCOUNT_USER_ID));
        documentReference.update(mConversation.toHashMap());


    }
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        Intent intent2 = new Intent(getApplicationContext(), EditImageActivity.class);
                        intent2.putExtra("DATA", imageUri.toString());
                        intent2.putExtra("style", Constants.KEY_REQUEST_CODE_IMAGE_MESSAGE);
                        startActivityForResult(intent2, Constants.KEY_REQUEST_CODE_IMAGE_MESSAGE);
                    }
                }
            });
    private void listenChatMessage(){
        if(mConversation != null){
            database.collection(Constants.KEY_CHAT_MESSAGE)
                    .whereEqualTo(Constants.KEY_CHAT_MESSAGE_CONVERSATION_ID, mConversation.getId())
                    .addSnapshotListener(eventListenerChatMessage);
        }
    }


    private  final EventListener<QuerySnapshot> eventListenerChatMessage = (value, error) -> {
        if(error !=null){
            return;
        }
        if (value != null){
            int count = chatMessageList.size();
            for(DocumentChange documentChange : value.getDocumentChanges()){
                if (documentChange.getType() == DocumentChange.Type.ADDED) {

                    DocumentSnapshot chatMessageSnap = documentChange.getDocument();
                    ChatMessage chatMessage = chatMessageSnap.toObject(ChatMessage.class);
                    chatMessage.setId(chatMessageSnap.getId());

                    chatMessageList.add(chatMessage);
                }else if(documentChange.getType()== DocumentChange.Type.MODIFIED){
                    ///
                }
            }

            chatMessageList.sort((obj1, obj2) -> obj1.getDataTime().compareTo(obj2.getDataTime()));
            if(count ==0){
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessageList.size(), chatMessageList.size());
                binding.chatRCV.smoothScrollToPosition(chatMessageList.size()-1);
            }
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1 && requestCode == Constants.KEY_REQUEST_CODE_IMAGE_MESSAGE) {
            String result = data.getStringExtra("RESULT");
            Uri imageUri= null;
            if(result != null){
                imageUri = Uri.parse(result);

            }
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String typeFile = imageUri.getPath().substring(imageUri.getPath().lastIndexOf(".")); //VD: jpg, png , ....

            String fileName = String.format("%s_%s.%s", account.getString(Constants.KEY_ACCOUNT_USER_ID), timestamp, typeFile);
            String path = String.format("images/conversation/%s/%s", mConversation.getId(), fileName);

            StorageReference imageRef = storage.getReference().child(path);
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            sendMessage(fileName,Constants.KEY_CHAT_MESSAGE_STYLE_MESSAGE_IMAGE);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
                        }
                    });
        }
    }
}
