#include "dbpapi.h"
#include <CkHttp.h>
#include <CkHttpResponse.h>

DBPApi* DBPApi::sInstance = nullptr;

DBPApi::DBPApi(QObject *parent) : QObject(parent)
{

}

DBPApi *DBPApi::instance()
{
    if(!sInstance) {
        sInstance = new DBPApi();
    }
    return sInstance;
}

QJsonObject DBPApi::getClone()
{
    QJsonObject body, response;
    body["api"] = "get_clone";
    sendRequest ("https://dangbaphong.com/api/tiktok/mm-tiktok-api.php", body, response);
    return response;
}

QJsonObject DBPApi::getCloneInfo(QString username)
{
    QJsonObject body, response;
    body["api"] = "get_clone_info";
    body["username"] = username;
    sendRequest ("https://dangbaphong.com/api/tiktok/mm-tiktok-api.php", body, response);
    return response;
}

QJsonObject DBPApi::updateClone(QString username, QString cloneInfoPath)
{
    QJsonObject body, response;
    body["api"] = "update_clone_info";
    body["clone_info"] = cloneInfoPath;
    sendRequest ("https://dangbaphong.com/api/tiktok/mm-tiktok-api.php", body, response);
    return response;
}

QJsonObject DBPApi::getVideoPath(QString username)
{
    QJsonObject body, response;
    body["api"] = "get_video_apth";
    body["username"] = username;
    sendRequest ("https://dangbaphong.com/api/tiktok/mm-tiktok-api.php", body, response);
    return response;
}

bool DBPApi::sendRequest(QString url, QJsonObject& body, QJsonObject& response)
{
    CkHttp http;
    http.SetRequestHeader("Content-Type", "application/json");

    CkHttpResponse *resp = http.PostJson(url.toUtf8().data(), QJsonDocument(body).toJson().data());
    if (http.get_LastMethodSuccess()) {
        response = QJsonDocument::fromJson(resp->bodyStr()).object();
        return true;
    } else {
        http.lastErrorText();
        response["success"] = false;
        response["message"] = http.lastErrorText();
        return false;
    }
}
