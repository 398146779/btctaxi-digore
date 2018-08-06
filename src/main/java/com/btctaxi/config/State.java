package com.btctaxi.config;

public interface State {

     int WITHDRAW_CREATED = 1; //初始化
     int WITHDRAW_REVIEWED1 = 2; // 一审通过
     int WITHDRAW_REVIEWED2 = 3;//二审通过
     int WITHDRAW_SIGNED = 4;//签名成功
     int WITHDRAW_BROADCASTED = 5;//广播成功
     int SUCCESS = 6;//成功
     int WITHDRAW_REFUSE1 = -2;//一审拒绝
     int WITHDRAW_REFUSE2 = -3;//二审拒绝
     int WITHDRAW_SIGN_ERROR = -4; //签名失败
     int WITHDRAW_BROADCAST_ERROR = -5;//广播失败
     int WITHDRAW_CANCELED = -1;//用户取消
     int WITHDRAW_ERROR_RETRY = -6;//失败重申




//     int WITHDRAW_CREATED = 1;
//     int WITHDRAW_REVIEWED1 = 2;
//     int WITHDRAW_REVIEWED2 = 3;
//     int WITHDRAW_SIGNED = 4;
//     int WITHDRAW_BROADCASTED = 5;
//     int SUCCESS = 6;
//
//     int WITHDRAW_REFUSE1 = -2;
//     int WITHDRAW_REFUSE2 = -3;
//     int WITHDRAW_SIGN_ERROR = -4;
//     int WITHDRAW_BROADCAST_ERROR = -5;
//     int WITHDRAW_CANCELED = -1;
}
