package com.dayz.sapientiacloud_edupivot.auth.constant;

public class OAuth2Constants {

    public static final String GITHUB_AUTHORIZE_URL = "https://github.com/login/oauth/authorize";
    public static final String GITHUB_ACCESS_TOKEN_URL = "https://github.com/login/oauth/access_token";
    public static final String GITHUB_USER_API_URL = "https://api.github.com/user";
    
    public static final String PARAM_CLIENT_ID = "client_id";
    public static final String PARAM_CLIENT_SECRET = "client_secret";
    public static final String PARAM_CODE = "code";
    public static final String PARAM_REDIRECT_URI = "redirect_uri";
    public static final String PARAM_STATE = "state";
    public static final String PARAM_SCOPE = "scope";
    
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String ACCEPT_JSON = "application/json";
    public static final String ACCEPT_GITHUB_V3 = "application/vnd.github.v3+json";
    public static final String AUTHORIZATION_BEARER_PREFIX = "Bearer ";
    
    public static final String RESPONSE_ACCESS_TOKEN = "access_token";
    public static final String RESPONSE_ERROR = "error";
    public static final String RESPONSE_ERROR_DESCRIPTION = "error_description";
    public static final String ERROR_BAD_VERIFICATION_CODE = "bad_verification_code";
    
    public static final String GITHUB_API_USER_ID = "id";
    public static final String GITHUB_API_USER_LOGIN = "login";
    public static final String GITHUB_API_USER_EMAIL = "email";
    public static final String GITHUB_API_USER_NAME = "name";
    public static final String GITHUB_API_USER_AVATAR_URL = "avatar_url";
    
    public static final String USER_INFO_GITHUB_ID = "githubId";
    public static final String USER_INFO_USERNAME = "username";
    public static final String USER_INFO_EMAIL = "email";
    public static final String USER_INFO_NAME = "name";
    public static final String USER_INFO_AVATAR_URL = "avatarUrl";
    
    public static final String RESULT_ACCESS_TOKEN = "accessToken";
    public static final String RESULT_REFRESH_TOKEN = "refreshToken";
    public static final String RESULT_USER = "user";
    public static final String RESULT_REGISTRATION_STATUS = "registrationStatus";
    public static final String RESULT_IS_NEW_USER = "isNewUser";
    public static final String RESULT_NEED_BIND_MOBILE = "needBindMobile";
    public static final String RESULT_NEED_SELECT_IDENTITY = "needSelectIdentity";
    public static final String RESULT_NEED_COMPLETE_INFO = "needCompleteInfo";
    public static final String RESULT_CURRENT_STEP = "currentStep";
    
    public static final String PROVIDER_GITHUB = "github";
    
    public static final int MAX_RETRIES = 3;
    public static final long RETRY_DELAY_BASE_MS = 1000L;
    
    public static final int HTTP_STATUS_BAD_REQUEST = 400;
    
    public static final String REDIS_STATE_KEY_PREFIX = "oauth2:state:";
    public static final int STATE_EXPIRE_MINUTES = 10;
    
    private OAuth2Constants() {
        // 禁止实例化
    }
}

