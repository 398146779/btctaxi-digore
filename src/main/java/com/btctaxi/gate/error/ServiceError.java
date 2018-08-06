package com.btctaxi.gate.error;

public class ServiceError extends RuntimeException {
    public int code;
    public Object[] params;

    public ServiceError(int code, Object... params) {
        this.code = code;
        this.params = params;
    }

    public static final int SIGNUP_EMAIL_EXISTS = 1001;
    public static final int SIGNUP_PHONE_EXISTS = 1002;
    public static final int SIGNUP_EMAIL_NULL = 1003;
    public static final int SIGNUP_PHONE_NULL = 1009;
    public static final int SIGNUP_PASS_NULL = 1004;
    public static final int GEETEST_AUTH_FAIL = 1005;
    public static final int SIGNUP_PHONE_TIME_LIMIT = 1006;
    public static final int SIGNUP_PHONE_AUTH_FAIL = 1007;

    public static final int ACTIVATE_CODE_RESEND_TIME_TOO_SHORT = 1010;
    public static final int ACTIVATE_CODE_NOT_EXISTS = 1011;
    public static final int ACTIVATE_DONE_ERROR = 1012;

    public static final int SIGNIN_EMAIL_OR_PASS_NOT_EXISTS = 1021;
    public static final int SIGNIN_NAME_OR_PASS_NULL = 1022;
    public static final int SIGNIN_USER_FROZEN = 1024;
    public static final int SIGNIN_UNAUTH = 1025;
    public static final int SIGNIN_AUTH_GEETEST_FAIL = 1026;
    public static final int SIGNIN_CANT_SIGNUP = 1027;

    public static final int FOGGOT_EMAIL_NOT_EXISTS = 1031;
    public static final int FORGOT_EMAIL_TIME_LIMIT = 1032;
    public static final int FORGOT_AUTH_KEY_NOT_EXISTS = 1033;
    public static final int PASSWORD_MODIFY_UNAUTH = 1034;
    public static final int PASSWORD_MODIFY_UNAUTH_LIMIT = 1035;

    public static final int ORDER_CREATE_PAIR_NOT_EXISTS = 2001;
    public static final int ORDER_CREATE_OP_VALUE_ILLEGAL = 2002;
    public static final int ORDER_CREATE_AMOUNT_ILLEGAL_LT_MIN = 2003;
    public static final int ORDER_CREATE_AMOUNT_ILLEGAL_GT_MAX = 2004;
    public static final int ORDER_REMOVE_ORDER_NOT_EXISTS = 2005;
    public static final int ORDER_BALANCE_NOT_ENOUGH = 2006;
    public static final int ORDER_CREATE_TOTAL_ILLEGAL = 2007;
    public static final int ORDER_QUERY_ORDER_NOT_EXISTS = 2008;

    public static final int PAIR_NOT_EXISTS = 2011;

    public static final int WITHDRAW_ADDRESS_CREATE_CURRENCY_NOT_EXISTS = 3001;
    public static final int WITHDRAW_BALANCE_NOT_ENOUGH = 3002;
    public static final int WITHDRAW_CODE_NOT_EXISTS = 3003;
    public static final int WITHDRAW_FORBIDDEN_24_HOURS = 3004;

    public static final int KYC_SAME_ID_NO_LIMIT5 = 4001;

    public static final int FA2_KEY_NOT_EXISTS = 6001;
    public static final int FA2_GOOGLE_AUTH_FAIL = 6002;
    public static final int FA2_PHONE_AUTH_FAIL = 6003;
    public static final int FA2_PHONE_TIME_LIMIT = 6004;
    public static final int FA2_GOOGLE_AUTH_DUPLICTE = 6005;
    public static final int FA2_BIND_NEEDED = 6006;
    public static final int FA2_GOOGLE_AUTH_MAX_RETRY = 6007;
    public static final int FA2_PHONE_EXISTS = 6008;

    public static final int API_AUTH_NOT_EXISTS = 7001;
    public static final int API_AUTH_ILLEGAL = 7002;

    public static final int AUTH_TYPE_GOOGLE_VERIFY_FAIL = 8001;
    public static final int AUTH_TYPE_GOOGLE_NO_REQUESTED = 8002;
    public static final int AUTH_TYPE_GOOGLE_BINDED = 8003;
    public static final int AUTH_TYPE_GOOGLE_UNBINDED = 8004;
    public static final int AUTH_TYPE_SMS_VERIFY_FAIL = 8005;
    public static final int AUTH_TYPE_SMS_BINDED = 8006;
    public static final int AUTH_TYPE_SMS_UNBINDED = 8007;
    public static final int AUTH_TYPE_SMS_REQUEST_ILLEGAL = 8008;

    public static final int AUTH_PHONE_CODE_LIMIT = 8011;
    public static final int AUTH_CODE_EXPIRED = 8012;

    public static final int UPLOAD_FAILED = 9001;
    public static final int UPLOAD_FILE_FORMAT_ILLEGAL = 9002;
    public static final int FAVORITE_CREATE_PAIR_NOT_EXISTS = 9011;
    public static final int ACTIVITY_LOTTERY_CHANCE_LIMIT = 9101;
    public static final int SERVER_DATA_EXCEPTION = 9999;

}
