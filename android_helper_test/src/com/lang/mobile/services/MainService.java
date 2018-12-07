package com.lang.mobile.services;

import com.lang.mobile.models.VersionInfo;

public interface MainService extends BaseService {

    @Get("/services/version/{code}")
    Observable<HttpResponse<VersionInfo>> getVersionInfo(@Query("code") int versionCode);
}
