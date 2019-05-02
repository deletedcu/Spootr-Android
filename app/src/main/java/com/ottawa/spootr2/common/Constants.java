package com.ottawa.spootr2.common;

/**
 * Created by king on 21/01/16.
 */
public interface Constants {

    static final String WEBSERVICE_URL           = "http://spootr.com/api/db_api_v2_1.php";
    static final String DOWNLOAD_URL             = "http://spootr.com/upload_files/";
    static final String ACTION                   = "action";
    static final int REQUEST_TIMEOUT_SECOND      = 18000;

    // GCM constants
    static final String GCMSERVER_URL            = "http://spootr.com/api/gcm/register.php";
    static final String GOOGLE_SENDER_ID         = "";
    static final String GCM_TAG                  = "GCM Example";
    static final String DISPLAY_MESSAGE_ACTION   = "com.ottawa.spootr.DISPLAY_MESSAGE";

    static final String REQUEST_WAITING          = "please wait...";
    static final String WEB_FAILED               = "Failed. Try again.";

    // SharedPreference info
    static final String PREFS_NAME               = "my_prefs";
    static final String USER_ID                  = "user_id";
    static final String FACEBOOK_ID              = "facebook_id";
    static final String PICTURE_URL              = "picture_url";
    static final String STATUS                   = "status";
    static final String STATUS_SUCCESS           = "200";
    static final String MY_CIRCLES               = "my_circles";
    static final String SELECED_CIRCLE           = "selected_circle";

    // Device info
    static final String DEVICE_TOKEN             = "device_token";
    static final String DEVICE_TYPE              = "device_type";
    static final int DEVICE_ANDROID              = 2;

    // User info
    static final String ID                       = "id";
    static final String NAME                     = "name";
    static final String USER_NAME                = "user_name";
    static final String PASSWORD                 = "password";
    static final String EMAIL                    = "email";
    static final String PICTURE_NAME             = "picture_name";

    // User_Circle info
    static final String CIRCLE_ID                = "circle_id";
    static final String USER_COUNT               = "user_count";
    static final String CIRCLE_TYPE              = "circle_type";

    // Global Search
    static final int CIRCLE_SEARCH               = 1;
    static final int TRENDING_SEARCH             = 2;

    // Post info
    static final String POST_ID                  = "post_id";
    static final String CONTENT                  = "content";
    static final String POST_TIME                = "post_time";
    static final String COMMENT_TIME             = "comment_time";
    static final String LIKE_COUNT               = "like_count";
    static final String COMMENT_COUNT            = "comment_count";
    static final String LIKE_TYPE                = "like_type";
    static final String IS_COMMENTED             = "is_commented";
    static final String POST_IMAGE               = "image_name";
    static final String NOTIFICATION_COUNT       = "ncount";
    static final String NOTIFICATION_TYPE        = "notification_type";
    static final String NOTIFICATION_TIME        = "notification_time";
    static final String LIKE1                    = "like1_sum";
    static final String LIKE2                    = "like2_sum";
    static final String LIKE3                    = "like3_sum";
    static final String LIKE4                    = "like4_sum";
    static final String LIKE5                    = "like5_sum";


    static final String MAX_ID                   = "max_id";
    static final String CIRCLE_IDS               = "circle_ids";
    static final String DISTANCE                 = "distance";
    static final String OFFSET                   = "offset";
    static final String POST_PAGESIZE            = "page_size";
    static final int PAGE_SIZE                   = 30;
    static final int SEARCH_DISTANCE             = 2;

    static final String COMMENT_ID               = "comment_id";

    // Chat
    static final String CHATROOM_ID              = "chatroom_id";
    static final String SENDER_ID                = "sender_id";
    static final String CONVERSATION_ID          = "conversation_id";
    static final String CHAT_TIME                = "chat_time";
    static final String IS_NEW                   = "is_new";
    static final String MESSAGE                  = "message";
    static final String MESSAGE_COUNT            = "message_count";
    static final String MESSAGE_TIME             = "message_time";
    static final String FROMME                   = "fromme";
    static final String USER1_ID                 = "user1_id";
    static final String USER2_ID                 = "user2_id";

    // Location
    static final String LATITUDE                 = "latitude";
    static final String LONGITUDE                = "longitude";

    // Push notification
    static final String IS_SETTING               = "is_setting";
    static final String NEW_POST                 = "new_post";
    static final String LIKE_POST                = "like_post";
    static final String COMMENT_POST             = "comment_post";
    static final String CHAT_NOTIFICATION        = "chat";

    static final int RESULT_REMOVE               = 3000;
    static final int REQUEST_CODE_NOTIFCATION    = 2001;
    static final int REQUEST_CODE_ADDPOST        = 2002;
    static final int REQUEST_CODE_EDITPOST       = 2003;
    static final int REQUEST_CODE_COMMENT        = 2004;
    static final int REQUEST_CODE_COMMENTPOST    = 2005;
    static final int REQUEST_CODE_PROFILE        = 2006;

    static final int REQUEST_TAKE_PICTURE        = 3001;
    static final int REQUEST_SELECT_PICTURE      = 3002;
    static final int REQUEST_CROP_IMAGE          = 3003;

    static final int NOTIFICATION_ID             = 505;

    static final String GET_CIRCLES              = "getCirclesByLocation";
    static final int IMAGE_COMPRESS_RATE         = 30;

    static final int FACEBOOK_TYPE               = 1;
    static final int TWITTER_TYPE                = 2;
    static final int APP_TYPE                    = 3;

}
