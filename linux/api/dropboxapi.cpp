#include "dropboxapi.h"
#include <CkRest.h>
#include <log.h>
#include <QJsonObject>
#include <QJsonDocument>
#include <CkStream.h>

#define token1 "Bearer xlXhh1QUv5QAAAAAAAAAAUz3kQ"
#define token2 "xJXSjn2Kgj92FZgYQAcM2H2hlPGMYiRWFTEYlK"

DropboxAPI* DropboxAPI::sInstance = nullptr;

DropboxAPI::DropboxAPI(QObject *parent) : QObject(parent)
{

}

DropboxAPI *DropboxAPI::instance()
{
    if(!sInstance) {
        sInstance = new DropboxAPI();
    }

    return sInstance;
}

bool DropboxAPI::downloadFile(const char *path, const char *save_path)
{
    CkRest rest;
    rest.put_IdleTimeoutMs(120000);

    //  Connect to Dropbox
    if (!rest.Connect("content.dropboxapi.com", 443, true, true))
    {
        LOGD << "Connect error: " << rest.lastErrorText();
        return false;
    }

    //  Add request headers.

    rest.AddHeader("Authorization", token1 "" token2);

    QJsonObject json;
    json["path"] = path;
    rest.AddHeader("Dropbox-API-Arg", QJsonDocument(json).toJson().data());

    CkStream fileStream;
    fileStream.put_SinkFile(save_path);

    int expectedStatus = 200;
    rest.SetResponseBodyStream(expectedStatus, true, fileStream);

    const char *responseStr = rest.fullRequestNoBody("POST", "/2/files/download");
    if (!rest.get_LastMethodSuccess())
    {
        LOGD << "responseStr error: " << rest.lastErrorText();
        return false;
    }
    else
    {
        LOGD << "responseStr: " << responseStr;
    }

    //  When successful, Dropbox responds with a 200 response code.
    if (rest.get_ResponseStatusCode() != 200)
    {
        //  Examine the request/response to see what happened.
        LOGD << "response status code = "  << rest.get_ResponseStatusCode();
        LOGD << "response status text = "  << rest.responseStatusText();
        LOGD << "response header: "  << rest.responseHeader();
        LOGD << "response body (if any): "  << responseStr;
        LOGD << "LastRequestStartLine: "  << rest.lastRequestStartLine();
        LOGD << "LastRequestHeader: "  << rest.lastRequestHeader();
        LOGD << "lastErrorText: " << rest.lastErrorText();
        return false;
    }
    LOGD << "Download " << path << " succeed --> saved to " << save_path;
    return true;
}
