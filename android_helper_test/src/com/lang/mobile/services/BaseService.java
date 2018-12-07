package com.lang.mobile.services;

import com.lang.mobile.models.UserInfo;

public interface BaseService {

    Observable<HttpResponse<UserInfo>> login(String account, String password);

    Observable<HttpResponse<UserInfo>> getUserInfo(String userId);
}
