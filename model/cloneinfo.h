#ifndef COMMONINFORCLONE_H
#define COMMONINFORCLONE_H
#include <QObject>
#include <QJsonObject>
#include <QJsonDocument>

#define CLONE_INFO_FIELD_USERNAME "username"
#define CLONE_INFO_FIELD_PASSWORD "password"
#define CLONE_INFO_FIELD_EMAIL "email"
#define CLONE_INFO_FIELD_STATUS "status"
#define CLONE_INFO_FIELD_LAST_UPLOAD_TIME "last_upload_time"
#define CLONE_INFO_FIELD_CLONED_FROM "cloned_from"
#define CLONE_INFO_FIELD_VIDEO_FOLDER_PATH "video_path"
#define CLONE_INFO_FIELD_USERAGENT "useragent"


#define CLONE_ALIVE_STATUS_LIVE        "live"
#define CLONE_ALIVE_STATUS_GETTING     "getting"
#define CLONE_ALIVE_STATUS_STORE       "stored"



class CloneInfo : public QObject
{
    Q_OBJECT
public:
    ~CloneInfo(){}
    explicit CloneInfo(QJsonObject cloneInfo);

    QString username();

    void setPassWord(QString passWord);
    QString password();

    QString email();
    void setEmail(QString email);


    QString status();
    void setStatus(QString status);

    qint64 lastUploadTime();
    void setLastUploadTime(qint64);

    QString clonedFrome();

    QString videoFolderPath();

    QString userAgent();

    QJsonObject toJson();
    QString toString();

signals:
    void cloneInfoChanged();

private:
    QJsonObject m_cloneInfo;
    QStringList m_pageList;
};

#endif // COMMONINFORCLONE_H
