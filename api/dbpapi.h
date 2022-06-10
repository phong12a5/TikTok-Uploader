#ifndef DBPAPI_H
#define DBPAPI_H

#include <QObject>
#include <QJsonObject>
#include <QJsonDocument>

class DBPApi : public QObject
{
    Q_OBJECT
    explicit DBPApi(QObject *parent = nullptr);

public:

    static DBPApi* instance();

    QJsonObject getClone();
    QJsonObject getCloneInfo(QString username);
    QJsonObject updateClone(QString cloneInfoPath);
    QJsonObject getVideoPath(QString author);
    QJsonObject updateVideoStatus(QString video_id, QString status);

private:
    bool sendRequest(QString url, QJsonObject& body, QJsonObject& response);
private:
    static DBPApi* sInstance;

};

#endif // DBPAPI_H
