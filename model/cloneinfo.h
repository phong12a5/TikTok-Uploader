#ifndef COMMONINFORCLONE_H
#define COMMONINFORCLONE_H
#include <QObject>
#include <QJsonObject>
#include <QJsonDocument>

#define CLONE_INFO_FIELD_ID "id"
#define CLONE_INFO_FIELD_UID "uid"
#define CLONE_INFO_FIELD_PASSWORD "password"
#define CLONE_INFO_FIELD_COUNTRY "country"
#define CLONE_INFO_FIELD_APPNAME "appname"
#define CLONE_INFO_FIELD_NAME "name"
#define CLONE_INFO_FIELD_EMAIL "email"
#define CLONE_INFO_FIELD_COOKIES "cookie"
#define CLONE_INFO_FIELD_EMAIL_PASSWORD "emailPassword"
#define CLONE_INFO_FIELD_RECOVERY_EMAIL "recovery_email"
#define CLONE_INFO_FIELD_SECRETKEY "secretkey"
#define CLONE_INFO_FIELD_SETTING_SECRETKEY "setting_secretkey"
#define CLONE_INFO_FIELD_SETTING_LANGUAGE "setting_lang"
#define CLONE_INFO_FIELD_SETTING_AVATAR "setting_avatar"
#define CLONE_INFO_FIELD_SETTING_COVER "setting_cover"
#define CLONE_INFO_FIELD_ALIVE_STATUS "clone_page_status"
#define CLONE_INFO_FIELD_RESTRICTION_STATUS "restriction_status"
#define CLONE_INFO_FIELD_USER_AGENT "user_agent"

#define CLONE_INFO_FIELD_MZZ "mzz"
#define CLONE_INFO_FIELD_CZZ "czz"
#define CLONE_INFO_FIELD_ZZ_S "s"
#define CLONE_INFO_FIELD_ZZ_P "p"

#define CLONE_ALIVE_STATUS_LIVE        "live"
#define CLONE_ALIVE_STATUS_GETTING     "getting"
#define CLONE_ALIVE_STATUS_CHECKPOINT  "checkpoint"
#define CLONE_ALIVE_STATUS_STORE       "stored"
#define CLONE_ALIVE_STATUS_CHECKING    "checking"

#define CLONE_RESTRICTED_NONE        "restricted_none"
#define CLONE_RESTRICTED_FOLLOW        "restricted_follow"

#define CLONE_INFO_ACTION_UPDATE_ALIVE_STATUS          "UpdateAliveStatus"
#define CLONE_INFO_ACTION_UPDATE_PASSWORD              "UpdatePassword"
#define CLONE_INFO_ACTION_UPDATE_SECRETKEY             "UpdateSecretkey"
#define CLONE_INFO_ACTION_UPDATE_SETTING_SECRETKEY     "UpdateSettingSecretkey"
#define CLONE_INFO_ACTION_UPDATE_SETTING_LANGUAGE      "UpdateSettingLang"
#define CLONE_INFO_ACTION_UPDATE_SETTING_AVATAR        "UpdateSettingAvatar"
#define CLONE_INFO_ACTION_UPDATE_SETTING_COVER         "UpdateSettingCover"
#define CLONE_INFO_ACTION_UPDATE_RESTRICTION_STATUS    "UpdateRestrictionStatus"

class CloneInfo : public QObject
{
    Q_OBJECT
public:
    ~CloneInfo(){}
    explicit CloneInfo(QJsonObject cloneInfo);

    QString cloneId();

    QString uid();

    QString appname();

    void setPassWord(QString passWord);
    QString password();

    void setSecretkey(QString secretkey);
    QString secretkey();

//    void setToken(QString tocken);
//    QString token();

//    void setCookies(QString cookies);
    QString cookies();

    void setAliveStatus(QString status, bool forceSync = false);
    QString aliveStatus();

    QString userAgent();
    void setUserAgent(QString userAgent);

//    void setProxy(QString proxy);
//    QString getProxy();

    QStringList pageList();
    void setPageList(QStringList page);

    QJsonObject toJson();
    QString toString();

signals:
    void cloneInfoChanged(QString action = "");

private:
    QJsonObject m_cloneInfo;
    QStringList m_pageList;
};

#endif // COMMONINFORCLONE_H
