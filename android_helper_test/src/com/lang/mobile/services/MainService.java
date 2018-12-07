package com.lang.mobile.services;

import com.lang.mobile.models.VersionInfo;

/**
 * Created by Zailong shi on 2018/12/7
 */
public interface MainService extends BaseService {

    @Get("/services/version/{code}")
    Observable<HttpResponse<VersionInfo>> getVersionInfo(@Query("code") int versionCode);
}
